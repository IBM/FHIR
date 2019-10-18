/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.dao.api;

import java.util.List;
import java.util.Map;

import com.ibm.fhir.persistence.context.FHIRPersistenceContext;
import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.exception.FHIRPersistenceVersionIdMismatchException;
import com.ibm.fhir.persistence.jdbc.dto.Parameter;
import com.ibm.fhir.persistence.jdbc.dto.Resource;
import com.ibm.fhir.persistence.jdbc.exception.FHIRPersistenceDBConnectException;
import com.ibm.fhir.persistence.jdbc.exception.FHIRPersistenceDataAccessException;
import com.ibm.fhir.persistence.jdbc.util.SqlQueryData;


/**
 * This Data Access Object interface defines APIs specific to the Resource DAO implementation 
 * for the "normalized" relational schema.
 * 
 * @author markd
 *
 */
public interface ResourceNormalizedDAO extends ResourceDAO {
    
    /**
     * Executes the search contained in the passed SqlQueryData, using it's encapsulated search string and bind variables.
     * @param queryData - Contains a search string and (optionally) bind variables.
     * @return List<Resource> A list of FHIR Resources satisfying the passed search.
     * @throws FHIRPersistenceDataAccessException
     * @throws FHIRPersistenceDBConnectException
     */
    List<Resource> search(SqlQueryData queryData) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException;
    
    
    /**
     * Executes a count query based on the data contained in the passed SqlQueryData, using it's encapsulated search string and bind variables.
     * @param queryData - Contains a search string and (optionally) bind variables.
     * @return int A count of FHIR Resources satisfying the passed search.
     * @throws FHIRPersistenceDataAccessException
     * @throws FHIRPersistenceDBConnectException
     */
    int searchCount(SqlQueryData queryData) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException;
    
    /**
     * Sets thc current persistence context
     * @param context
     */
    void setPersistenceContext(FHIRPersistenceContext context);
    
    /**
     * Reads all rows in the resource_types table and returns the data as a Map
     * @return Map<String, Long> - A map containing key=parameter-name, value=parameter-name-id
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    Map<String,Integer> readAllResourceTypeNames() throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;
    
    /**
     * Reads the id associated with the name of the passed Resource type from the Resource_Types table. If the id for the passed name is not present
     * in the database, an id is generated, persisted, and returned.
     * @param String A valid FHIR resource type.
     * @return Integer - the id associated with the name of the passed Resource type.
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceDataAccessException
     */
    Integer readResourceTypeId(String parameterName) throws FHIRPersistenceDBConnectException, FHIRPersistenceDataAccessException;
    
    /**
     * Sets a flag indicating whether or not ReplicationInfo is required on Resource insertion.
     * @param isRepInfoRequired
     */
    void setRepInfoRequired(boolean isRepInfoRequired);


    /**
     * Reteives the flag indicating if ReplicationInfo is required on Resource insertion.
     * @return boolean
     */
    boolean isRepInfoRequired();
    
    /**
     * This method supports the execution of a specialized query designed to return Resource ids, based on the contents
     * of the passed select statement.
     * Note that the first column to be selected MUST be the Resource.id column.
     * @param sqlSelect - A select for Resource ids.
     * @return - A List of resource ids that satisfy the passed SQL query.
     * @throws FHIRPersistenceDataAccessException
     * @throws FHIRPersistenceDBConnectException
     */
    List<Long> searchForIds(SqlQueryData  queryData) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException;
    
    /**
     * Searches for Resources that contain one of the passed ids.
     * @param resourceType - The type of the FHIR Resource.
     * @param resourceIds - A List of resource ids.
     * @return List<Resource> - A List of resources matching the the passed list of ids.
     * @throws FHIRPersistenceDataAccessException
     * @throws FHIRPersistenceDBConnectException
     */
    List<Resource> searchByIds(String resourceType, List<Long> resourceIds) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException;

    
    /**
     * Adds a resource type/ resource id pair to a candidate collection for population into the ResourceTypesCache. 
     * This pair must be present as a row in the FHIR DB RESOURCE_TYPES table.
     * @param resourceType A valid FHIR resource type.
     * @param resourceTypeId The corresponding id for the resource type.
     * @throws FHIRPersistenceException
     */
    void addResourceTypeCacheCandidate(String resourceType, Integer resourceTypeId) throws FHIRPersistenceException;
    
    /**@deprecated This method is replaced by method: insert(Resource resource, List<Parameter> parameters, ParameterNormalizedDAO parameterDao)
     * Inserts the passed Resource DTO to the FHIR Resource table. 
     * After insert, the generated primary key is acquired and set in the Resource object.
     * @param resource
     * @return Resource
     * @throws FHIRPersistenceDataAccessException
     * @throws FHIRPersistenceDBConnectException
     */
    @Deprecated
    default Resource insert(Resource resource) throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException
            {return null;};
    
     /**
     * Inserts the passed Resource DTO and its associated search parameters to the appropriate FHIR resource tables. 
     * After insert, the generated primary key is acquired and set in the Resource object.
     * @param resource A Resource Data Transfer Object
     * @param parameters A collection of search parameters to be persisted along with the passed Resource
     * @param parameterDao The Parameter DAO
     * @return Resource The Resource DTO
     * @throws FHIRPersistenceDataAccessException
     * @throws FHIRPersistenceDBConnectException
     * @throws FHIRPersistenceVersionIdMismatchException 
     */
    Resource insert(Resource resource, List<Parameter> parameters, ParameterNormalizedDAO parameterDao) 
                    throws FHIRPersistenceDataAccessException, FHIRPersistenceDBConnectException, FHIRPersistenceVersionIdMismatchException;

    

}
