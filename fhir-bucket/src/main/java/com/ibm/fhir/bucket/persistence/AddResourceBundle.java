/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.bucket.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.fhir.bucket.api.FileType;
import com.ibm.fhir.bucket.api.ResourceBundleData;
import com.ibm.fhir.database.utils.api.IDatabaseSupplier;
import com.ibm.fhir.database.utils.api.IDatabaseTranslator;
import com.ibm.fhir.database.utils.model.DbType;

/**
 * DAO to encapsulate all the SQL/DML used to retrieve and persist data
 * in the schema
 */
public class AddResourceBundle implements IDatabaseSupplier<ResourceBundleData> {
    private static final Logger logger = Logger.getLogger(RegisterLoaderInstance.class.getName());
    
    // The database id of the bucket_path
    private final long bucketPathId;

    // The name of the object (e.g. bundle file) within the bucket
    private final String objectName;
    
    private final long objectSize;
    
    // The type of file represented by this object
    private final FileType fileType;

    // The hash of the object according to COS
    private final String eTag;

    // The last time the object was modified according to COS
    private final Date lastModified;

    /**
     * Public constructor
     * @param bucketId
     * @param objectName
     */
    public AddResourceBundle(long bucketPathId, String objectName, long objectSize, FileType fileType, String eTag, Date lastModified) {
        this.bucketPathId = bucketPathId;
        this.objectName = objectName;
        this.objectSize = objectSize;
        this.fileType = fileType;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    @Override
    public ResourceBundleData run(IDatabaseTranslator translator, Connection c) {
        ResourceBundleData result;

        // MERGE and upsert-like tricks don't appear to work with Derby
        // when using autogenerated identity columns. So we have to
        // try the old-fashioned way and handle duplicate key
        final String currentTimestamp = translator.currentTimestampString();
        int version = 1;
        String dml;
        if (translator.getType() == DbType.POSTGRESQL) {
            // For PostgresSQL, make sure we don't break the current transaction
            // if the statement fails...annoying
            dml = "INSERT INTO resource_bundles ("
                + "bucket_path_id, object_name, object_size, file_type, etag, last_modified, scan_tstamp, version) "
                + " VALUES (?, ?, ?, ?, ?, ?, " + currentTimestamp + ", ?) ON CONFLICT (bucket_path_id, object_name) DO NOTHING";
        } else {
            dml = "INSERT INTO resource_bundles ("
                    + "bucket_path_id, object_name, object_size, file_type, etag, last_modified, scan_tstamp, version) "
                    + " VALUES (?, ?, ?, ?, ?, ?, " + currentTimestamp + ", ?)";
        }
        
        try (PreparedStatement ps = c.prepareStatement(dml, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, bucketPathId);
            ps.setString(2, objectName);
            ps.setLong(3, objectSize);
            ps.setString(4, fileType.name());
            ps.setString(5, eTag);
            ps.setTimestamp(6, new java.sql.Timestamp(lastModified.getTime()));
            ps.setInt(7, version);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs != null && rs.next()) {
                result = new ResourceBundleData(
                    rs.getLong(1),
                    objectSize,
                    fileType,
                    eTag,
                    lastModified,
                    null, // value not available unless we select, which just slows things down
                    version
                );
            } else {
                result = null;
            }
        } catch (SQLException x) {
            if (translator.isDuplicate(x)) {
                result = null; // select it later
            } else {
                // log this, but don't propagate values in the exception
                logger.log(Level.SEVERE, "Error registering bucket path: " + dml + "; "
                    + bucketPathId + ", " + objectName + ", " + objectSize + ", " + fileType.name() + ", " + eTag);
                throw translator.translate(x);
            }
        }
        
        // If we didn't create a new record, fetch the old record before we update. It's important
        // to make this a select-for-update to avoid a possible race-condition
        if (result == null) {
            final String SQL = translator.addForUpdate(""
                    + "SELECT resource_bundle_id, object_size, file_type, etag, last_modified, scan_tstamp, version "
                    + "  FROM resource_bundles "
                    + " WHERE bucket_path_id = ? "
                    + "   AND object_name = ?");
            try (PreparedStatement ps = c.prepareStatement(SQL)) {
                ps.setLong(1, bucketPathId);
                ps.setString(2, objectName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    result = new ResourceBundleData(
                        rs.getLong(1),                          // resource_bundle_id
                        rs.getLong(2),                          // object_size
                        FileType.valueOf(rs.getString(3)),      // file_type
                        rs.getString(4),                        // etag
                        new Date(rs.getTimestamp(5).getTime()), // last_modified
                        new Date(rs.getTimestamp(6).getTime()), // scan_tstamp
                        rs.getInt(7)                            // version
                        );
                }
            } catch (SQLException x) {
                // log this, but don't propagate values in the exception
                logger.log(Level.SEVERE, "Error getting resource bundle: " + SQL + "; "
                    + bucketPathId + ", " + objectName);
                throw translator.translate(x);
            }
            
            // If the current database record doesn't match what we've been passed
            // then we want to update it with the latest and bump the version number
            // so that we can see it has changed.
            if (!result.matches(this.objectSize, this.eTag, this.lastModified)) {
                
                final String UPD = ""
                        + "UPDATE resource_bundles "
                        + "   SET object_size = ?, "
                        + "       etag = ?, "
                        + "       last_modified = ?, "
                        + "       scan_tstamp =  " + currentTimestamp + ", "
                        + "       version = version + 1, "
                        + "       allocation_id      = NULL, " // reset state so that this
                        + "       loader_instance_id = NULL "  // file will be picked up
                        + " WHERE resource_bundle_id = ?";
                
                try (PreparedStatement ps = c.prepareStatement(UPD)) {
                    ps.setLong(1, objectSize);
                    ps.setString(2, eTag);
                    ps.setTimestamp(3, new java.sql.Timestamp(lastModified.getTime()));
                    ps.setLong(4, result.getResourceBundleId());
                    ps.executeUpdate();
                } catch (SQLException x) {
                    // log this, but don't propagate values in the exception
                    logger.log(Level.SEVERE, "Error updating resource bundle: " + UPD + "; "
                        + result.getResourceBundleId());
                    throw translator.translate(x);
                }
            }
        }
        
        return result;
    }
}