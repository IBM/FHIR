/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.domain;

import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.search.parameters.QueryParameter;

/**
 * Used by the {@link SearchQuery} domain model to render the model
 * into another form (such as a Select statement.
 */
public interface SearchQueryVisitor<T> {

    /**
     * The root query (select statement) for a count query
     * @param rootResourceType
     * @param columns
     * @return
     */
    T countRoot(String rootResourceType);

    /**
     * The root query (select statement) for the data query
     * @param rootResourceType
     * @param columns
     * @return
     */
    T dataRoot(String rootResourceType);

    /**
     * Filter the query using the given parameter id and token value
     * @param query
     * @param parameterNameId
     * @param parameterValue
     * @return
     */
    T addTokenParam(T query, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException;

    /**
     * Filter the query using the given string parameter
     * @param query
     * @param parameterNameId
     * @param strValue
     * @return
     */
    T addStringParam(T query, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException;

    /**
     * Filter the query using the given number parameter
     * @param queryData
     * @param resourceType
     * @param queryParm
     * @return
     * @throws FHIRPersistenceException
     */
    public T addNumberParam(T queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException;

    /**
     * Filter the query using the given quantity parameter
     * @param queryData
     * @param resourceType
     * @param queryParm
     * @return
     * @throws FHIRPersistenceException
     */
    public T addQuantityParam(T queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException;

    /**
     * Filter the query using the given date parameter
     * @param queryData
     * @param resourceType
     * @param queryParm
     * @return
     * @throws FHIRPersistenceException
     */
    public T addDateParam(T queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException;

    /**
     * Filter the query using the given location (lat/lng) param
     * @param queryData
     * @param resourceType
     * @param queryParm
     * @return
     * @throws FHIRPersistenceException
     */
    public T addLocationParam(T queryData, String resourceType, QueryParameter queryParm) throws FHIRPersistenceException;

    /**
     * Add a missing (NOT EXISTS) parameter clause to the query
     * @param query
     * @param rootResourceType
     * @param code
     * @param isMissing true if the condition should be that the parameter does not exist
     * @return
     */
    T addMissingParam(T query, String rootResourceType, QueryParameter queryParm, boolean isMissing) throws FHIRPersistenceException;

    /**
     * Add sorting (order by) to the query
     * @param query
     * @return
     */
    T addSorting(T query);

    /**
     * Add pagination (LIMIT/OFFSET) to the query
     * @param query
     * @return
     */
    T addPagination(T query);

    /**
     * Add a chain subquery element as part of a chained parameter search
     * @param currentSubQuery
     * @param currentParm
     * @param aliasIndex
     * @param sourceResourceType
     * @return
     */
    T addChained(T currentSubQuery, QueryParameter currentParm, String sourceResourceType) throws FHIRPersistenceException;

    /**
     * Add a reverse chain subquery element as part of a chained parameter search
     * @param currentSubQuery
     * @param currentParm
     * @param aliasIndex
     * @oaram refResourceType the resource type of the target reference
     * @return
     */
    T addReverseChained(T currentSubQuery, QueryParameter currentParm, String refResourceType) throws FHIRPersistenceException;

    /**
     * Add a filter predicate to the given chained sub-query element. This must be
     * the last element of the chain.
     * @param currentSubQuery
     * @param currentParm
     * @param aliasIndex
     */
    void addFilter(T currentSubQuery, QueryParameter currentParm) throws FHIRPersistenceException;
}