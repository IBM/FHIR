/*
 * (C) Copyright IBM Corp. 2016,2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.audit.logging.api;

import com.ibm.fhir.audit.logging.beans.AuditLogEntry;
import com.ibm.fhir.config.PropertyGroup;

/**
 * Defines the internal FHIR Server APIs for audit logging
 * 
 * @author markd
 *
 */
public interface AuditLogService {

    /**
     * Persists the passed audit log entry in a location determined by the log
     * service.
     * 
     * @param logEntry - The audit log entry to be saved.
     * @throws Exception
     */
    void logEntry(AuditLogEntry logEntry) throws Exception;

    /**
     * @return true if the audit log service is enabled; false if not enabled.
     */
    boolean isEnabled();

    /**
     * Performs any required audit log service initialization using the passed
     * Properties file.
     * 
     * @param auditLogProperties - Contains audit log related properties which are
     *                           configured in fhir-server-config.json.
     * @throws Exception - Any non-recoverable exception thrown during audit log
     *                   service initialization.
     * 
     */
    void initialize(PropertyGroup auditLogProperties) throws Exception;
}
