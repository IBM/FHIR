/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.bucket.scanner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.ibm.fhir.bucket.api.BucketLoaderJob;
import com.ibm.fhir.bucket.api.CosItem;
import com.ibm.fhir.bucket.api.ResourceBundleData;
import com.ibm.fhir.bucket.api.ResourceBundleError;
import com.ibm.fhir.bucket.api.ResourceIdValue;
import com.ibm.fhir.bucket.api.ResourceRef;
import com.ibm.fhir.bucket.persistence.AddBucketPath;
import com.ibm.fhir.bucket.persistence.AddResourceBundle;
import com.ibm.fhir.bucket.persistence.AddResourceBundleErrors;
import com.ibm.fhir.bucket.persistence.AllocateJobs;
import com.ibm.fhir.bucket.persistence.ClearStaleAllocations;
import com.ibm.fhir.bucket.persistence.GetLastProcessedLineNumber;
import com.ibm.fhir.bucket.persistence.GetResourceRefsForBundleLine;
import com.ibm.fhir.bucket.persistence.LoaderInstanceHeartbeat;
import com.ibm.fhir.bucket.persistence.MarkBundleDone;
import com.ibm.fhir.bucket.persistence.RecordLogicalId;
import com.ibm.fhir.bucket.persistence.RecordLogicalIdList;
import com.ibm.fhir.bucket.persistence.RegisterLoaderInstance;
import com.ibm.fhir.bucket.persistence.ResourceTypeRec;
import com.ibm.fhir.bucket.persistence.ResourceTypesReader;
import com.ibm.fhir.database.utils.api.IDatabaseAdapter;
import com.ibm.fhir.database.utils.api.ITransaction;
import com.ibm.fhir.database.utils.api.ITransactionProvider;

/**
 * The data access layer encapsulating interactions with the FHIR bucket schema
 */
public class DataAccess {
    private static final Logger logger = Logger.getLogger(DataAccess.class.getName());

    // no heartbeats for 60 seconds means something has gone wrong
    private static final long HEARTBEAT_TIMEOUT_MS = 60000;
    
    // how many errors to insert per JDBC batch
    private int errorBatchSize = 10;
    
    // The adapter we use to execute database statements
    private final IDatabaseAdapter dbAdapter;
    
    // Simple transaction service for use outside of JEE
    private final ITransactionProvider transactionProvider;

    // Internal cache of resource types, which are created as part of schema deployment
    private final Map<String, Integer> resourceTypeMap = new ConcurrentHashMap<>();
    
    // The unique id string representing this instance of the loader
    private final String instanceId;
    
    // The id returned by the database when registering this loader instance
    private long loaderInstanceId;
    
    // the name of the schema holding all the tables
    private final String schemaName;
    
    /**
     * Public constructor
     * @param connectionPool
     * @param txProvider
     * @param schemaName
     */
    public DataAccess(IDatabaseAdapter dbAdapter, ITransactionProvider txProvider, String schemaName) {
        this.dbAdapter = dbAdapter;
        this.transactionProvider = txProvider;
        this.schemaName = schemaName;
        
        // Generate a unique id string to represent this instance of the loader while it's running
        UUID uuid = UUID.randomUUID();
        this.instanceId = uuid.toString();
    }

    /**
     * Initialize the object
     */
    public void init() {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                List<ResourceTypeRec> resourceTypes = dbAdapter.runStatement(new ResourceTypesReader());
                resourceTypes.stream().forEach(rt -> resourceTypeMap.put(rt.getResourceType(), rt.getResourceTypeId()));
                
                // Register this loader instance
                InetAddress addr = InetAddress.getLocalHost();
                RegisterLoaderInstance c1 = new RegisterLoaderInstance(instanceId, addr.getHostName(), -1);
                this.loaderInstanceId = dbAdapter.runStatement(c1);
            } catch (UnknownHostException x) {
                logger.severe("FATAL ERROR. Failed to register instance");
                tx.setRollbackOnly();
                throw new IllegalStateException(x);
            }
        }
    }

    /**
     * Create a record in the database to track this item if it doesn't
     * currently exist
     * @param item
     */
    public void registerBucketItem(CosItem item) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                String name;
                String path = item.getItemName();
                int idx = path.lastIndexOf('/');
                if (idx > 0) {
                    name = path.substring(idx+1); // everything after the last /
                    path = path.substring(0, idx+1); // up to and including the last /
                } else if (idx == 0) {
                    // item name is just '/' which we don't think is valid
                    name = null;
                } else {
                    // In the root "folder"
                    name = path;
                    path = "/";
                }
                
                if (name != null) {
                    AddBucketPath c1 = new AddBucketPath(item.getBucketName(), path);
                    Long bucketPathId = dbAdapter.runStatement(c1);
        
                    // Now register the bundle using the bucket record we created/retrieved
                    AddResourceBundle c2 = new AddResourceBundle(bucketPathId, name, item.getSize(), item.getFileType(),
                        item.geteTag(), item.getLastModified());
                    ResourceBundleData old = dbAdapter.runStatement(c2);
                    if (old != null && !old.matches(item.getSize(), item.geteTag(), item.getLastModified())) {
                        // log the fact that the item has been changed in COS and so we've updated our
                        // record of it in the bucket database -> it will be processed again.
                        logger.info("COS item changed, " + item.toString() 
                        + ", old={size=" + old.getObjectSize() + ", etag=" + old.geteTag() + ", lastModified=" + old.getLastModified() + "}"
                        + ", new={size=" + item.getSize() + ", etag=" + item.geteTag() + ", lastModified=" + item.getLastModified() + "}"
                        );
                    }
                } else {
                    logger.warning("Bad item name: '" + item.toString());
                }
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * Allocate up to free jobs to this loader instance
     * @param jobList
     * @param free
     */
    public void allocateJobs(List<BucketLoaderJob> jobList, int free, int recycleSeconds) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                // First business of the day is to check for liveness and clear
                // any allocations for instances we think are no longer active
                ClearStaleAllocations liveness = new ClearStaleAllocations(loaderInstanceId, HEARTBEAT_TIMEOUT_MS, recycleSeconds);
                dbAdapter.runStatement(liveness);
                
                AllocateJobs cmd = new AllocateJobs(schemaName, jobList, loaderInstanceId, free);
                dbAdapter.runStatement(cmd);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * Save the logical id
     * @param simpleName
     * @param logicalId
     */
    public void recordLogicalId(String resourceType, String logicalId, long resourceBundleLoadId, int lineNumber, Integer responseTimeMs) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                Integer resourceTypeId = resourceTypeMap.get(resourceType);
                if (resourceTypeId == null) {
                    // unlikely, unless the map hasn't been initialized properly
                    throw new IllegalStateException("resourceType not found: " + resourceType);
                }
                
                RecordLogicalId cmd = new RecordLogicalId(resourceTypeId, logicalId, resourceBundleLoadId, lineNumber, responseTimeMs);
                dbAdapter.runStatement(cmd);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * Update the heartbeat tstamp of the record representing this loader instance
     * to tell everyone that we're still alive.
     */
    public void heartbeat() {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                LoaderInstanceHeartbeat heartbeat = new LoaderInstanceHeartbeat(this.loaderInstanceId);
                dbAdapter.runStatement(heartbeat);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * @param job
     */
    public void markJobDone(BucketLoaderJob job) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                // The file itself counts as one completion, so we have to subtract 1 to get the
                // actual row count of the file contents
                MarkBundleDone c1 = new MarkBundleDone(job.getResourceBundleLoadId(), job.getFailureCount(), job.getCompletedCount()-1);
                dbAdapter.runStatement(c1);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * Load the list of resourceType/logicalId DTO objects as a batch in one transaction
     * @param resourceBundleId
     * @param lineNumber
     * @param idValues
     */
    public void recordLogicalIds(long resourceBundleLoadId, int lineNumber, List<ResourceIdValue> idValues, int batchSize) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                RecordLogicalIdList cmd = new RecordLogicalIdList(resourceBundleLoadId, lineNumber, idValues, resourceTypeMap, batchSize);
                dbAdapter.runStatement(cmd);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
        
    }

    /**
     * Save the errors generated when loading the given resource bundle. Because a given
     * bundle may be loaded multiple times with different outcomes, the error records are
     * each associated with the current loaderInstanceId. This can occur when a loader
     * dies before the bundle completes.
     * @param resourceBundleId
     * @param lineNumber
     * @param errors
     * @param batchSize
     */
    public void recordErrors(long resourceBundleLoadId, int lineNumber, List<ResourceBundleError> errors) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                AddResourceBundleErrors cmd = new AddResourceBundleErrors(resourceBundleLoadId, errors, errorBatchSize);
                dbAdapter.runStatement(cmd);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * Get the last processed line number for the given resource bundle identified by its id.
     * This is calculated by looking for the max line_number value recorded for the bundle
     * in the logical_resources table.
     * @param resourceBundleId
     * @param version
     */
    public Integer getLastProcessedLineNumber(long resourceBundleId, int version) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                GetLastProcessedLineNumber cmd = new GetLastProcessedLineNumber(resourceBundleId, version);
                return dbAdapter.runStatement(cmd);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }

    /**
     * Get the list of resourceType/logicalId resource references generated when processing
     * the given lineNumber of the identified resource bundle and its version
     * @param resourceBundleId
     * @param version
     * @param lineNumber
     * @return
     */
    public List<ResourceRef> getResourceRefsForLine(long resourceBundleId, int version, int lineNumber) {
        try (ITransaction tx = transactionProvider.getTransaction()) {
            try {
                GetResourceRefsForBundleLine cmd = new GetResourceRefsForBundleLine(resourceBundleId, version, lineNumber);
                return dbAdapter.runStatement(cmd);
            } catch (Exception x) {
                tx.setRollbackOnly();
                throw x;
            }
        }
    }
}