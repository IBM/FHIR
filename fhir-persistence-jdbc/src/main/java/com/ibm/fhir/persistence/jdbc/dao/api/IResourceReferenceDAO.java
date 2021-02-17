/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.dao.api;

import java.util.Collection;

import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.jdbc.dao.impl.ResourceTokenValueRec;
import com.ibm.fhir.persistence.jdbc.dto.CommonTokenValueResult;

/**
 * Contract for DAO implementations handling persistence of
 * resource references (and token parameters) with the
 * normalized schema introduced in issue 1366.
 */
public interface IResourceReferenceDAO {

    /**
     * Get the cache used by the DAO
     * @return
     */
    ICommonTokenValuesCache getResourceReferenceCache();

    /**
     * Execute any statements with pending batch entries
     * @throws FHIRPersistenceException
     */
    void flush() throws FHIRPersistenceException;

    /**
     * Add TOKEN_VALUE_MAP records, creating any CODE_SYSTEMS and COMMON_TOKEN_VALUES
     * as necessary
     * @param resourceType
     * @param xrefs
     */
    void addCommonTokenValues(String resourceType, Collection<ResourceTokenValueRec> xrefs);

    /**
     * Persist the records, which may span multiple resource types
     * @param records
     */
    void persist(Collection<ResourceTokenValueRec> records);

    /**
     * Find the database id for the given token value and system
     * @param codeSystem
     * @param tokenValue
     * @return the matching id from common_token_values.common_token_value_id or null if not found
     */
    CommonTokenValueResult readCommonTokenValueId(String codeSystem, String tokenValue);
}