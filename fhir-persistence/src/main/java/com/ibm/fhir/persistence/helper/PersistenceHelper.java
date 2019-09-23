/*
 * (C) Copyright IBM Corp. 2016,2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.helper;

import com.ibm.fhir.persistence.FHIRPersistence;
import com.ibm.fhir.persistence.exception.FHIRPersistenceException;

public interface PersistenceHelper {

    /**
     * Returns an appropriate FHIRPersistance implementation according to the current configuration.
     * @throws FHIRPersistenceException 
     */
    FHIRPersistence getFHIRPersistenceImplementation() throws FHIRPersistenceException;

    /**
     * Returns an appropriate FHIRPersistance implementation according to the current configuration.
     * @throws FHIRPersistenceException 
     */
    FHIRPersistence getFHIRPersistenceImplementation(String factoryPropertyName) throws FHIRPersistenceException;

    /**
     * Identifies if a FHIRPersistence implementation can be found by using the given identifier.
     * 
     * @param identifier
     * @return TRUE if the identifier maps to a valid {@link FHIRPersistence} identifier. Default value is FALSE.
     * @throws FHIRPersistenceException
     */
    default boolean isValidFHIRPersistenceImplementation(String identifier) throws FHIRPersistenceException {
        return false;
    }
}
