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
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.fhir.bucket.api.FileType;
import com.ibm.fhir.database.utils.api.IDatabaseStatement;
import com.ibm.fhir.database.utils.api.IDatabaseSupplier;
import com.ibm.fhir.database.utils.api.IDatabaseTranslator;
import com.ibm.fhir.database.utils.model.DbType;

/**
 * DAO to encapsulate all the SQL/DML used to retrieve and persist data
 * in the schema
 */
public class RecordLogicalId implements IDatabaseStatement {
    private static final Logger logger = Logger.getLogger(RegisterLoaderInstance.class.getName());

    // FK describing the type of the resource
    private final int resourceTypeId;

    // The newly assigned logical id of the resource
    private final String logicalId;

    // The bundle file from which the resource originated
    private final long resourceBundleLoadId;

    // the line number of the resource (in an NDJSON file)
    private final int lineNumber;
    
    // Response time if this was an individual resource create (not part of a bundle)
    private final Integer responseTimeMs;

    /**
     * Public constructor
     * @param bucketId
     * @param objectName
     */
    public RecordLogicalId(int resourceTypeId, String logicalId, long resourceBundleLoadId, int lineNumber, Integer responseTimeMs) {
        this.resourceTypeId = resourceTypeId;
        this.logicalId = logicalId;
        this.resourceBundleLoadId = resourceBundleLoadId;
        this.lineNumber = lineNumber;
        this.responseTimeMs = responseTimeMs;
    }

    @Override
    public void run(IDatabaseTranslator translator, Connection c) {
        final String currentTimestamp = translator.currentTimestampString();

        String dml;
        if (translator.getType() == DbType.POSTGRESQL) {
            // Use UPSERT syntax for Postgres to avoid breaking the transaction when
            // a statement fails
            dml = 
                    "INSERT INTO logical_resources ("
                    + "          resource_type_id, logical_id, resource_bundle_load_id, line_number, response_time_ms, created_tstamp) "
                    + "   VALUES (?, ?, ?, ?, ?, " + currentTimestamp + ") ON CONFLICT (resource_type_id, logical_id) DO NOTHING";
        } else {
            dml = 
                "INSERT INTO logical_resources ("
                + "          resource_type_id, logical_id, resource_bundle_load_id, line_number, response_time_ms, created_tstamp) "
                + "   VALUES (?, ?, ?, ?, ?, " + currentTimestamp + ")";
        }
        
        try (PreparedStatement ps = c.prepareStatement(dml)) {
            ps.setLong(1, resourceTypeId);
            ps.setString(2, logicalId);
            ps.setLong(3, resourceBundleLoadId);
            ps.setInt(4, lineNumber);
            if (this.responseTimeMs != null) {
                ps.setInt(5, this.responseTimeMs);
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException x) {
            if (translator.isDuplicate(x)) {
                // This resource has already been recorded, so we'll just warn in case something
                // is going wrong
                logger.warning("Duplicate resource logical id: " + resourceTypeId + "/" + logicalId 
                    + " from " + resourceBundleLoadId + "#" + lineNumber);
            } else {
                // log this, but don't propagate values in the exception
                logger.log(Level.SEVERE, "Error registering logical resource: " + dml + "; "
                    + resourceTypeId + ", " + logicalId + ", " + resourceBundleLoadId + ", " + lineNumber);
                throw translator.translate(x);
            }
        }
    }
}