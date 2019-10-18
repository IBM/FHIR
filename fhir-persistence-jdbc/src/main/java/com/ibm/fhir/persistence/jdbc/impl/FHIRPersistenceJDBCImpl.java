/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.impl;

import static com.ibm.fhir.config.FHIRConfiguration.PROPERTY_UPDATE_CREATE_ENABLED;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import com.ibm.fhir.config.FHIRConfiguration;
import com.ibm.fhir.config.PropertyGroup;
import com.ibm.fhir.core.FHIRUtilities;
import com.ibm.fhir.database.utils.api.IConnectionProvider;
import com.ibm.fhir.exception.FHIRException;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.parser.FHIRJsonParser;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.resource.SearchParameter;
import com.ibm.fhir.model.type.Id;
import com.ibm.fhir.model.type.Instant;
import com.ibm.fhir.model.type.Meta;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.model.util.FHIRUtil;
import com.ibm.fhir.persistence.FHIRPersistence;
import com.ibm.fhir.persistence.FHIRPersistenceTransaction;
import com.ibm.fhir.persistence.MultiResourceResult;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.persistence.context.FHIRHistoryContext;
import com.ibm.fhir.persistence.context.FHIRPersistenceContext;
import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.exception.FHIRPersistenceResourceNotFoundException;
import com.ibm.fhir.persistence.jdbc.dao.api.FHIRDbDAO;
import com.ibm.fhir.persistence.jdbc.dao.api.ParameterDAO;
import com.ibm.fhir.persistence.jdbc.dao.api.ResourceDAO;
import com.ibm.fhir.persistence.jdbc.dao.impl.FHIRDbDAOBasicImpl;
import com.ibm.fhir.persistence.jdbc.dao.impl.ParameterDAOBasicImpl;
import com.ibm.fhir.persistence.jdbc.dao.impl.ResourceDAOBasicImpl;
import com.ibm.fhir.persistence.jdbc.dto.Parameter;
import com.ibm.fhir.persistence.jdbc.exception.FHIRPersistenceDBConnectException;
import com.ibm.fhir.persistence.jdbc.exception.FHIRPersistenceDataAccessException;
import com.ibm.fhir.persistence.jdbc.util.JDBCParameterBuilder;
import com.ibm.fhir.persistence.jdbc.util.JDBCQueryBuilder;
import com.ibm.fhir.persistence.jdbc.util.JDBCSortQueryBuilder;
import com.ibm.fhir.persistence.util.Processor;
import com.ibm.fhir.search.SearchConstants;
import com.ibm.fhir.search.SearchConstants.Type;
import com.ibm.fhir.search.context.FHIRSearchContext;
import com.ibm.fhir.search.util.SearchUtil;

/**
 * This class is the JDBC implementation of the FHIRPersistence interface, providing implementations for CRUD type APIs and search.
 * @author markd
 *
 */
public class FHIRPersistenceJDBCImpl implements FHIRPersistence, FHIRPersistenceTransaction {
    
    private static final String CLASSNAME = FHIRPersistenceJDBCImpl.class.getName();
    private static final Logger log = Logger.getLogger(CLASSNAME);
    private static final ZoneId systemZoneId = ZoneId.systemDefault();
    
    protected static final String TXN_JNDI_NAME = "java:comp/UserTransaction";
    
    private FHIRDbDAO baseDao;
    private ResourceDAO resourceDao;
    private ParameterDAO parameterDao;
    
    // only used outside a web container
    private Connection managedConnection;
    
    private Connection sharedConnection = null;
    protected UserTransaction userTransaction = null;
    protected Boolean updateCreateEnabled = null;

    
    /**
     * Constructor for use when running as web application in WLP. 
     * @throws Exception 
     */
    public FHIRPersistenceJDBCImpl() throws Exception {
        super();
        final String METHODNAME = "FHIRPersistenceJDBCImpl()";
        log.entering(CLASSNAME, METHODNAME);
        
        PropertyGroup fhirConfig = FHIRConfiguration.getInstance().loadConfiguration();
        this.updateCreateEnabled = fhirConfig.getBooleanProperty(PROPERTY_UPDATE_CREATE_ENABLED, Boolean.TRUE);
        this.userTransaction = retrieveUserTransaction(TXN_JNDI_NAME);
        this.resourceDao = new ResourceDAOBasicImpl();
        this.parameterDao = new ParameterDAOBasicImpl();
                                
        log.exiting(CLASSNAME, METHODNAME);
    }
    
    /**
     * Constructor for use when running standalone, outside of any web container.
     * @throws Exception 
     */
    @SuppressWarnings("rawtypes")
    public FHIRPersistenceJDBCImpl(Properties configProps) throws Exception {
        super();
        final String METHODNAME = "FHIRPersistenceJDBCImpl(Properties)";
        log.entering(CLASSNAME, METHODNAME);
        
        this.updateCreateEnabled = Boolean.parseBoolean(configProps.getProperty("updateCreateEnabled"));
        this.setBaseDao(new FHIRDbDAOBasicImpl(configProps));
        this.setManagedConnection(this.getBaseDao().getConnection());
        this.resourceDao = new ResourceDAOBasicImpl(this.getManagedConnection());
        this.parameterDao = new ParameterDAOBasicImpl(this.getManagedConnection());
        
        log.exiting(CLASSNAME, METHODNAME);
    }
    
    /**
     * Constructor when using the new database-utils {@link IConnectionProvider} pattern
     */
    @SuppressWarnings("rawtypes")
    public FHIRPersistenceJDBCImpl(Properties configProps, IConnectionProvider cp) throws Exception {
        super();
        final String METHODNAME = "FHIRPersistenceJDBCImpl(Properties)";
        log.entering(CLASSNAME, METHODNAME);
        
        this.updateCreateEnabled = Boolean.parseBoolean(configProps.getProperty("updateCreateEnabled"));
        this.setBaseDao(new FHIRDbDAOBasicImpl(cp, configProps.getProperty("adminSchemaName")));
        this.setManagedConnection(this.getBaseDao().getConnection());
        this.resourceDao = new ResourceDAOBasicImpl(this.getManagedConnection());
        this.parameterDao = new ParameterDAOBasicImpl(this.getManagedConnection());
        
        log.exiting(CLASSNAME, METHODNAME);
    }
    
    
    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistenceTransaction#isActive()
     */
    @Override
    public boolean isActive() throws FHIRPersistenceException {
        
        boolean isActive = false;
        try {
            if (userTransaction != null) {
                isActive = (userTransaction.getStatus() == Status.STATUS_ACTIVE);
            }
        } 
        catch (Throwable e) {
            String msg = "An unexpected error occurred while examining transactional status.";
            FHIRPersistenceException fx = new FHIRPersistenceException(msg);
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;
        }
        return isActive;
    }
    
    protected Instant instant(long millis) {
        java.time.Instant time = java.time.Instant.ofEpochMilli(millis);
        return Instant.builder().value(time.atZone(systemZoneId)).build();
    }
    
    protected Id id(String idString) {
        return Id.of(idString);
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#create(com.ibm.fhir.persistence.context.FHIRPersistenceContext, com.ibm.fhir.model.Resource)
     */
    @Override
    public <T extends Resource> SingleResourceResult<T> create(FHIRPersistenceContext context, T resource) throws FHIRPersistenceException  {
        final String METHODNAME = "create";
        log.entering(CLASSNAME, METHODNAME);
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String logicalId;

        // We need to update the meta in the resource, so we need a modifiable version
        Resource.Builder resultBuilder = resource.toBuilder();
                
        try {
            // Default version is 1 for a brand new FHIR Resource.
            int newVersionNumber = 1;
            logicalId = (resource.getId() != null ? resource.getId().getValue() : UUID.randomUUID().toString());
            Instant lastUpdated = instant(System.currentTimeMillis());
            if (log.isLoggable(Level.FINE)) {
                log.fine("Creating new FHIR Resource of type '" + resource.getClass().getSimpleName() + "'");
            }
            
            // Set the resource id and meta fields.
            resultBuilder.id(Id.of(logicalId));
            Meta meta = resource.getMeta();
            Meta.Builder metaBuilder = meta == null ? Meta.builder() : meta.toBuilder();
            metaBuilder.versionId(Id.of(Integer.toString(newVersionNumber)));
            metaBuilder.lastUpdated(lastUpdated);
            resultBuilder.meta(metaBuilder.build());
            
            @SuppressWarnings("unchecked")
            T updatedResource = (T) resultBuilder.build();
            
            // Create the new Resource DTO instance.
            com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO = new com.ibm.fhir.persistence.jdbc.dto.Resource();
            resourceDTO.setLogicalId(logicalId);
            resourceDTO.setVersionId(newVersionNumber);
            Timestamp timestamp = FHIRUtilities.convertToTimestamp(lastUpdated.getValue());
            resourceDTO.setLastUpdated(timestamp);
            resourceDTO.setResourceType(updatedResource.getClass().getSimpleName());
             
            // Serialize and compress the Resource
            GZIPOutputStream zipStream = new GZIPOutputStream(stream);
            
            FHIRGenerator.generator( Format.JSON, false).generate(updatedResource, zipStream);
            
            zipStream.finish();
            resourceDTO.setData(stream.toByteArray());
            zipStream.close();
            
            // Persist the Resource DTO.
            this.getResourceDao().insert(resourceDTO);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Persisted FHIR Resource '" + resourceDTO.getResourceType() + "/" + resourceDTO.getLogicalId() + "' id=" + resourceDTO.getId()
                            + ", version=" + resourceDTO.getVersionId());
            }
            
            // Store search parameters
            this.storeSearchParameters(updatedResource, resourceDTO);
            
            SingleResourceResult<T> result = new SingleResourceResult.Builder<T>()
                    .success(true)
                    .resource(updatedResource)
                    .build();
            
            return result;
        }
        catch(FHIRPersistenceException e) {
            throw e;
        }
        catch(Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while performing a create operation.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#read(com.ibm.fhir.persistence.context.FHIRPersistenceContext, java.lang.Class, java.lang.String)
     */
    @Override
    public <T extends Resource> SingleResourceResult<T> read(FHIRPersistenceContext context, Class<T> resourceType, String logicalId)
                            throws FHIRPersistenceException {
        final String METHODNAME = "read";
        log.entering(CLASSNAME, METHODNAME);
        
        T resource = null;
        com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO = null;
                
        try {
            resourceDTO = this.getResourceDao().read(logicalId, resourceType.getSimpleName());
            resource = this.convertResourceDTO(resourceDTO, resourceType, null);
        }
        catch(FHIRPersistenceException e) {
            throw e;
        }
        catch(Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while performing a read operation.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        
        SingleResourceResult<T> result = new SingleResourceResult.Builder<T>()
                .success(true)
                .resource(resource)
                .build();
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#vread(com.ibm.fhir.persistence.context.FHIRPersistenceContext, java.lang.Class, java.lang.String, java.lang.String)
     */
    @Override
    public <T extends Resource> SingleResourceResult<T> vread(FHIRPersistenceContext context, Class<T> resourceType, String logicalId, String versionId) 
                        throws FHIRPersistenceException {
        final String METHODNAME = "vread";
        log.entering(CLASSNAME, METHODNAME);
        
        T resource = null;
        com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO = null;
        int version;
                        
        try {
            version = Integer.parseInt(versionId);
            resourceDTO = this.getResourceDao().versionRead(logicalId, resourceType.getSimpleName(), version);
            resource = this.convertResourceDTO(resourceDTO, resourceType, null);
        }
        catch(FHIRPersistenceException e) {
            throw e;
        }
        catch (NumberFormatException e) {
            throw new FHIRPersistenceException("Invalid version id specified for vread operation: " + versionId);
        }
        catch(Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while performing a version read operation.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        
        SingleResourceResult<T> result = new SingleResourceResult.Builder<T>()
                .success(true)
                .resource(resource)
                .build();
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#update(com.ibm.fhir.persistence.context.FHIRPersistenceContext, java.lang.String, com.ibm.fhir.model.Resource)
     */
    @Override
    public <T extends Resource> SingleResourceResult<T> update(FHIRPersistenceContext context, String logicalId, T resource) throws FHIRPersistenceException {
        final String METHODNAME = "update";
        log.entering(CLASSNAME, METHODNAME);

        // Resources are immutable, so we need a new builder to update it (since R4)
        Resource.Builder resultBuilder = resource.toBuilder();
        
        Class<? extends Resource> resourceType = resource.getClass();
        com.ibm.fhir.persistence.jdbc.dto.Resource existingResourceDTO;
        int newVersionNumber = 1;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                
        try {
            
            // Get the current version of the Resource.
            existingResourceDTO = this.getResourceDao().read(logicalId, resourceType.getSimpleName());
            
            // If this FHIR Resource doesn't exist and updateCreateEnabled is turned off, throw an exception
            if (existingResourceDTO == null && !updateCreateEnabled) {
                String msg = "Resource '" + resourceType.getSimpleName() + "/" + logicalId + "' not found.";
                log.log(Level.SEVERE, msg);
                throw new FHIRPersistenceResourceNotFoundException(msg);
            }
            
            stream = new ByteArrayOutputStream();
                
            // If the FHIR Resource already exists, then we'll simply bump up the version #, use its logical id,
            // and remove its Parameter entries.
            if (existingResourceDTO != null) {
                newVersionNumber = existingResourceDTO.getVersionId() + 1;
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Updating FHIR Resource '" + resource.getClass().getSimpleName() + "/" + existingResourceDTO.getLogicalId() + "', version="
                                + existingResourceDTO.getVersionId());
                }
                 
                // Retrieve the Parameters associated with the current version of the Resource and remove them.
                this.getParameterDao().deleteByResource(existingResourceDTO.getId());
            } 
            
            Instant lastUpdated = instant(System.currentTimeMillis());
            
            // Set the resource id and meta fields.
            resultBuilder.id(Id.of(logicalId));
            Meta meta = resource.getMeta();
            Meta.Builder metaBuilder = meta == null ? Meta.builder() : meta.toBuilder();
            metaBuilder.versionId(Id.of(Integer.toString(newVersionNumber)));
            metaBuilder.lastUpdated(lastUpdated);
            resultBuilder.meta(metaBuilder.build());
            
            
            @SuppressWarnings("unchecked")
            T updatedResource = (T) resultBuilder.build();
            
            // Create the new Resource DTO instance.
            com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO = new com.ibm.fhir.persistence.jdbc.dto.Resource();
            resourceDTO.setLogicalId(logicalId);
            resourceDTO.setVersionId(newVersionNumber);
            Timestamp timestamp = FHIRUtilities.convertToTimestamp(lastUpdated.getValue());
            resourceDTO.setLastUpdated(timestamp);
            resourceDTO.setResourceType(updatedResource.getClass().getSimpleName());
             
            // Serialize and compress the Resource
            GZIPOutputStream zipStream = new GZIPOutputStream(stream);
            
            FHIRGenerator.generator( Format.JSON, false).generate(updatedResource, zipStream);
            zipStream.finish();
            resourceDTO.setData(stream.toByteArray());
            zipStream.close();
            
            // Persist the Resource DTO.
            this.getResourceDao().insert(resourceDTO);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Persisted FHIR Resource '" + resourceDTO.getResourceType() + "/" + resourceDTO.getLogicalId() + "' id=" + resourceDTO.getId()
                            + ", version=" + resourceDTO.getVersionId());
            }
            
            // Store search parameters
            this.storeSearchParameters(updatedResource, resourceDTO);
            
            SingleResourceResult<T> result = new SingleResourceResult.Builder<T>()
                    .success(true)
                    .resource(updatedResource)
                    .build();
            
            return result;
        }
        catch(FHIRPersistenceException e) {
            throw e;
        }
        catch(Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while performing an update operation.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;                        
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
    }

//    /* (non-Javadoc)
//     * @see com.ibm.fhir.persistence.FHIRPersistence#delete(com.ibm.fhir.persistence.context.FHIRPersistenceContext, java.lang.String)
//     */
// The interface contains a default implementation of this, so leave commented out until we add support here.
//    @Override
//    public Resource delete(FHIRPersistenceContext context, Class<? extends Resource> resourceType, String logicalId) throws FHIRPersistenceException {
//        throw new FHIRPersistenceNotSupportedException("delete is not yet implemented");
//    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#history(com.ibm.fhir.persistence.context.FHIRPersistenceContext, java.lang.Class, java.lang.String)
     */
    @Override
    public <T extends Resource> MultiResourceResult<T> history(FHIRPersistenceContext context, Class<T> resourceType,
            String logicalId) throws FHIRPersistenceException {
        final String METHODNAME = "history";
        log.entering(CLASSNAME, METHODNAME);
        
        List<T> resources = new ArrayList<>();
        List<com.ibm.fhir.persistence.jdbc.dto.Resource> resourceDTOList;
        FHIRHistoryContext historyContext;
        int resourceCount;
        Instant since;
        Timestamp fromDateTime = null;
        int pageSize;
        int lastPageNumber;
        int offset;
                
        try {
            historyContext = context.getHistoryContext();
            since = historyContext.getSince();
            if (since != null) {
                fromDateTime = FHIRUtilities.convertToTimestamp(since.getValue());
            }
            
            resourceCount = this.getResourceDao().historyCount(resourceType.getSimpleName(), logicalId, fromDateTime);
            historyContext.setTotalCount(resourceCount);
            pageSize = historyContext.getPageSize();
            lastPageNumber = (int) ((resourceCount + pageSize - 1) / pageSize);
            historyContext.setLastPageNumber(lastPageNumber);            
            
            if (resourceCount > 0) {
                offset = (historyContext.getPageNumber() - 1) * pageSize;
                resourceDTOList = this.getResourceDao().history(resourceType.getSimpleName(), logicalId, fromDateTime, offset, pageSize);
                resources = this.convertResourceDTOList(resourceDTOList, resourceType);
            } 
        }
        catch(FHIRPersistenceException e) {
            throw e;
        }
        catch(Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while performing a history operation.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        
        MultiResourceResult<T> result = new MultiResourceResult.Builder<T>()
                .success(true)
                .resource(resources)
                .build();
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#search(com.ibm.fhir.persistence.context.FHIRPersistenceContext, java.lang.Class)
     */
    @Override
    public MultiResourceResult<Resource> search(FHIRPersistenceContext context, Class<? extends Resource> resourceType)
            throws FHIRPersistenceException {
        final String METHODNAME = "search";
        log.entering(CLASSNAME, METHODNAME);
        
        List<Resource> resources = new ArrayList<Resource>();
        FHIRSearchContext searchContext = context.getSearchContext();
        JDBCQueryBuilder queryBuilder;
        List<Long> sortedIdList;
        List<com.ibm.fhir.persistence.jdbc.dto.Resource> unsortedResultsList;
        int searchResultCount = 0;
        int pageSize;
        int lastPageNumber;
        String queryString;
                
        try {
            if (searchContext.hasSortParameters()) {
                queryBuilder = new JDBCSortQueryBuilder(this.getResourceDao());
            }
            else {
                queryBuilder = new JDBCQueryBuilder(this.getResourceDao());
            }
            
            String countQueryString = queryBuilder.buildCountQuery(resourceType, searchContext);
            if (countQueryString != null) {
                searchResultCount = this.getResourceDao().searchCount(countQueryString);
                if (log.isLoggable(Level.FINE)) {
                    log.fine("searchResultCount = " + searchResultCount);
                }
                searchContext.setTotalCount(searchResultCount);
                pageSize = searchContext.getPageSize();
                lastPageNumber = (int) ((searchResultCount + pageSize - 1) / pageSize);
                searchContext.setLastPageNumber(lastPageNumber);
                
                 
                if (searchResultCount > 0) {
                    queryString = queryBuilder.buildQuery(resourceType, searchContext);
                    
                    if (searchContext.hasSortParameters()) {
                        sortedIdList = this.getResourceDao().searchForIds(queryString);
                        resources = this.buildSortedFhirResources(context, resourceType, sortedIdList, null);
                    }
                    else {
                        unsortedResultsList = this.getResourceDao().search(queryString);
                        resources = this.convertResourceDTOListOld(unsortedResultsList, resourceType);
                    }  
                }
            }
        }
        catch(FHIRPersistenceException e) {
            throw e;
        }
        catch(Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while performing a search operation.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        
        MultiResourceResult<Resource> result = new MultiResourceResult.Builder<>()
                .success(true)
                .resource(resources)
                .build();
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#isTransactional()
     */
    @Override
    public boolean isTransactional() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#getTransaction()
     */
    @Override
    public FHIRPersistenceTransaction getTransaction() {
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistence#isDeleteSupported()
     */
    @Override
    public boolean isDeleteSupported() {
        return false;
    }
    
    /**
     * Retrieves (via a JNDI lookup) a reference to the UserTransaction. If the JNDI lookup fails, we'll assume that
     * we're not running inside the container.
     */
    protected UserTransaction retrieveUserTransaction(String jndiName) {
        UserTransaction txn = null;
        try {
            InitialContext ctx = new InitialContext();
            txn = (UserTransaction) ctx.lookup(jndiName);
        } catch (Throwable t) {
            // ignore any exceptions here.
        }

        return txn;
    }
    
    /**
     * Extracts and stores search parameters for the passed FHIR Resource to the FHIR DB Parameter table.
     * @param fhirResource - Some FHIR Resource
     * @param resourceDTO - A Resource DTO representation of the passed FHIR Resource.
     * @throws Exception 
     */
    protected void storeSearchParameters(Resource fhirResource, com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO) 
                 throws Exception {
        final String METHODNAME = "storeSearchParameters";
        log.entering(CLASSNAME, METHODNAME);
        
        Map<SearchParameter, List<FHIRPathNode>> map;
        String code, type, expression;
        List<Parameter> allParameters = new ArrayList<>();
        Processor<List<Parameter>> processor = new JDBCParameterBuilder();
        
        try {
            map = SearchUtil.extractParameterValues(fhirResource);
            
            for (Entry<SearchParameter, List<FHIRPathNode>> entry : map.entrySet()) {
                code = entry.getKey().getCode().getValue();
                type = entry.getKey().getType().getValue();
                expression = entry.getKey().getExpression().getValue();
                
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Processing SearchParameter code: " + code + ", type: " + type + ", expression: " + expression);
                }
                
                List<FHIRPathNode> values = entry.getValue();
                for (FHIRPathNode value : values) {
                    List<Parameter> parameters = processor.process(entry.getKey(), value);
                    for (Parameter p : parameters) {
                        p.setType(Type.fromValue(type));
                        p.setResourceId(resourceDTO.getId());
                        p.setResourceType(fhirResource.getClass().getSimpleName());
                        allParameters.add(p);
                        if (log.isLoggable(Level.FINE)) {
                            log.fine("Added Parameter '" + p.getName() + "' to Resource.");
                        }
                    }
                }
            }
            this.getParameterDao().insert(allParameters);
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
    }
    
    /**
     * Converts the passed Resource Data Transfer Object collection to a collection of FHIR Resource objects.
     * @param resourceDTOList
     * @param resourceType
     * @return
     * @throws FHIRException
     * @throws IOException 
     */
    // This variant uses generics and is used in history.
    // TODO: this method needs to either get merged or better differentiated with the old one used for search
    protected <T extends Resource> List<T> convertResourceDTOList(List<com.ibm.fhir.persistence.jdbc.dto.Resource> resourceDTOList, Class<T> resourceType) 
                                throws FHIRException, IOException {
        final String METHODNAME = "convertResourceDTO List";
        log.entering(CLASSNAME, METHODNAME);
        
        List<T> resources = new ArrayList<>();
        try {
            for (com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO : resourceDTOList) {
                resources.add(this.convertResourceDTO(resourceDTO, resourceType, null));
            }
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return resources;
    }
    
    /**
     * Converts the passed Resource Data Transfer Object collection to a collection of FHIR Resource objects.
     * @param resourceDTOList
     * @param resourceType
     * @return
     * @throws FHIRException
     * @throws IOException 
     */
    // This variant doesn't use generics and is used in search.
    // TODO: this method needs to either get merged or better differentiated with the new one that supports history operation via generics.
    // Start by better understanding what happens for `_include` and `_revinclude` search results that contain multiple different types
    protected List<Resource> convertResourceDTOListOld(List<com.ibm.fhir.persistence.jdbc.dto.Resource> resourceDTOList, Class<? extends Resource> resourceType) 
                                throws FHIRException, IOException {
        final String METHODNAME = "convertResourceDTO List";
        log.entering(CLASSNAME, METHODNAME);
        
        List<Resource> resources = new ArrayList<>();
        try {
            for (com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO : resourceDTOList) {
                resources.add(this.convertResourceDTO(resourceDTO, resourceType, null));
            }
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return resources;
    }
    
    /**
     * Converts the passed Resource Data Transfer Object to a FHIR Resource object.
     * @param resourceDTO - A valid Resource DTO
     * @param resourceType - The FHIR type of resource to be converted.
     * @param elements - An optional filter for including only specified elements inside a Resource.
     * @return Resource - A FHIR Resource object representation of the data portion of the passed Resource DTO.
     * @throws FHIRException
     * @throws IOException 
     */
    protected <T extends Resource> T convertResourceDTO(com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO, 
            Class<T> resourceType, 
            List<String> elements) throws FHIRException, IOException {
        final String METHODNAME = "convertResourceDTO";
        log.entering(CLASSNAME, METHODNAME);
        T resource = null;
        try {
            if (resourceDTO != null) {
                InputStream in = new GZIPInputStream(new ByteArrayInputStream(resourceDTO.getData()));
                if (elements != null) {
                    // parse/filter the resource using elements
                    resource = FHIRParser.parser(Format.JSON).as(FHIRJsonParser.class).parseAndFilter(in, elements);
                    if (resourceType.equals(resource.getClass()) && !FHIRUtil.hasTag(resource, SearchConstants.SUBSETTED_TAG)) {
                        // add a SUBSETTED tag to this resource to indicate that its elements have been filtered
                        resource = FHIRUtil.addTag(resource, SearchConstants.SUBSETTED_TAG);
                    }
                } else {
                    resource = FHIRParser.parser(Format.JSON).parse(in);  
                }
                in.close();
            }
        } finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
        return resource;
    }

    /**
     * This method takes the passed list of sorted Resource ids, acquires the Resource corresponding to each id, and returns those Resources in a List,
     * sorted according to the input sorted ids.
     * @param context - The FHIR persistence context for the current request.
     * @param resourceType - The type of Resource that each id in the passed list represents.
     * @param sortedIdList - A list of Resource ids representing the proper sort order for the list of Resources to be returned.
     * @param elements - An optional list of element names to include in the resources. If null, filtering will be skipped.
     * @return List<Resource> - A list of Resources of the passed resourceType, sorted according the order of ids in the passed sortedIdList.
     * @throws FHIRPersistenceException
     * @throws IOException 
     */
    protected List<Resource> buildSortedFhirResources(FHIRPersistenceContext context, Class<? extends Resource> resourceType, List<Long> sortedIdList, 
                                                      List<String> elements) 
                            throws FHIRException, FHIRPersistenceException, IOException {
        final String METHOD_NAME = "buildFhirResource";
        log.entering(this.getClass().getName(), METHOD_NAME);
        
        long resourceId;
        Resource[] sortedFhirResources = new Resource[sortedIdList.size()];
        Resource fhirResource;
        int sortIndex;
        List<Resource> sortedResourceList = new ArrayList<>();
        List<com.ibm.fhir.persistence.jdbc.dto.Resource> resourceDTOList;
        Map<Long,Integer> idPositionMap = new HashMap<>();
                
        // This loop builds a Map where key=resourceId, and value=its proper position in the returned sorted collection.
        for(int i = 0; i < sortedIdList.size(); i++) {
            resourceId = sortedIdList.get(i);
            idPositionMap.put(new Long(resourceId), new Integer(i));
        }
        
        resourceDTOList = this.getResourceDTOs(resourceType, sortedIdList);
        
        
        // Convert the returned JPA Resources to FHIR Resources, and store each FHIRResource in its proper position
        // in the returned sorted resource list.
        for (com.ibm.fhir.persistence.jdbc.dto.Resource resourceDTO : resourceDTOList) {
            fhirResource = this.convertResourceDTO(resourceDTO, resourceType, elements);
            if (fhirResource != null) {
                sortIndex = idPositionMap.get(resourceDTO.getId());
                sortedFhirResources[sortIndex] = fhirResource;
            }
        }
        
        for (int i = 0; i <sortedFhirResources.length; i++) {
            if (sortedFhirResources[i] != null) {
                sortedResourceList.add(sortedFhirResources[i]);
            }
        }
        log.exiting(this.getClass().getName(), METHOD_NAME);
        return sortedResourceList;
    }
    
    /**
     * Returns a List of Resource DTOs corresponding to the passed list of Resource IDs.
     * @param resourceType The type of resource being queried.
     * @param sortedIdList A sorted list of Resource IDs.
     * @return List - A list of ResourceDTOs
     * @throws FHIRPersistenceDataAccessException
     * @throws FHIRPersistenceDBConnectException
     */
    protected List<com.ibm.fhir.persistence.jdbc.dto.Resource> getResourceDTOs(
            Class<? extends Resource> resourceType, List<Long> sortedIdList) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException {
         
        return this.getResourceDao().searchByIds(sortedIdList);
    }

    protected ParameterDAO getParameterDao() {
        return this.parameterDao;
    }

    protected ResourceDAO getResourceDao() {
        return resourceDao;
    }

    protected FHIRDbDAO getBaseDao() {
        return baseDao;
    }

    protected void setBaseDao(FHIRDbDAO baseDao) {
        this.baseDao = baseDao;
    }

    protected Connection getManagedConnection() {
        return managedConnection;
    }

    protected void setManagedConnection(Connection managedConnection) {
        this.managedConnection = managedConnection;
    }

    @SuppressWarnings("rawtypes")
    protected Connection createConnection() throws FHIRPersistenceDBConnectException {
        
        FHIRDbDAOBasicImpl dao = new FHIRDbDAOBasicImpl();
        return dao.getConnection();
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistenceTransaction#begin()
     */
    @Override
    public void begin() throws FHIRPersistenceException {
        final String METHODNAME = "begin";
        log.entering(CLASSNAME, METHODNAME);
            
        try {
            if (userTransaction != null) {
                sharedConnection = createConnection();
                resourceDao.setExternalConnection(sharedConnection);
                parameterDao.setExternalConnection(sharedConnection);
                userTransaction.begin();
            }
            else if (this.getManagedConnection() != null) {
                this.getManagedConnection().setAutoCommit(false);
            }
        }
        catch (Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while starting a transaction.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        }
        finally {
            log.exiting(CLASSNAME, METHODNAME);
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistenceTransaction#commit()
     */
    @Override
    public void commit() throws FHIRPersistenceException {
        final String METHODNAME = "commit";
        log.entering(CLASSNAME, METHODNAME);
        
        try {
            if (userTransaction != null) {
                userTransaction.commit();
            } else if (this.getManagedConnection() != null) {
                this.getManagedConnection().commit();
            }
        } 
        catch (Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while committing a transaction.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        }
        finally {
            if (sharedConnection != null) {
                try {
                    sharedConnection.close();
                } catch (SQLException e) {
                    throw new FHIRPersistenceException("Failure closing DB Conection", e);
                }
                sharedConnection = null;
            }
            log.exiting(CLASSNAME, METHODNAME);
        }
    
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.persistence.FHIRPersistenceTransaction#rollback()
     */
    @Override
    public void rollback() throws FHIRPersistenceException {
        final String METHODNAME = "rollback";
        log.entering(CLASSNAME, METHODNAME);
        
        try {
            if (userTransaction != null) {
                userTransaction.rollback();
            } 
            else if (this.getManagedConnection() != null) {
                this.getManagedConnection().rollback();
            }
        } 
        catch (Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException("Unexpected error while rolling back a transaction.");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        } 
        finally {
            if (sharedConnection != null) {
                try {
                    sharedConnection.close();
                } 
                catch (SQLException e) {
                    throw new FHIRPersistenceException("Failure closing DB Conection", e);
                }
                sharedConnection = null;
            }
        }
    }
    
    @Override
    public void setRollbackOnly() throws FHIRPersistenceException {
        final String METHODNAME = "setRollbackOnly";
        log.entering(CLASSNAME, METHODNAME);
        String errorMessage = "Unexpected error while rolling a transaction.";
        
        try {
            if (userTransaction != null) {
                errorMessage = "Unexpected error while marking a transaction for rollback.";
                userTransaction.setRollbackOnly();
            } 
            else if (this.getManagedConnection() != null) {
                this.getManagedConnection().rollback();
            }
        } 
        catch (Throwable e) {
            FHIRPersistenceException fx = new FHIRPersistenceException(errorMessage);
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;            
        } 
        finally {
            if (sharedConnection != null) {
                try {
                    sharedConnection.close();
                } 
                catch (SQLException e) {
                    FHIRPersistenceException fx = new FHIRPersistenceException("Failure closing DB Conection");
                    log.log(Level.SEVERE, fx.getMessage(), e);
                    throw fx;            
                }
                sharedConnection = null;
            }
        }
    }

    @Override
    public OperationOutcome getHealth() throws FHIRPersistenceException {
        try (Connection connection = createConnection()){
            if (connection.isValid(2)) {
                return buildOKOperationOutcome();
            } else {
                return buildErrorOperationOutcome();
            }
        } catch (SQLException e) {
            FHIRPersistenceDataAccessException fx = new FHIRPersistenceDataAccessException("Error while validating the database connection");
            log.log(Level.SEVERE, fx.getMessage(), e);
            throw fx;
        }
    }

    private OperationOutcome buildOKOperationOutcome() {
        return FHIRUtil.buildOperationOutcome("All OK", IssueType.ValueSet.INFORMATIONAL, IssueSeverity.ValueSet.INFORMATION);
    }

    private OperationOutcome buildErrorOperationOutcome() {
        return FHIRUtil.buildOperationOutcome("The database connection was not valid", IssueType.ValueSet.NO_STORE, IssueSeverity.ValueSet.ERROR);
    }
}
