/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ibm.fhir.model.resource.Location;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.persistence.jdbc.dao.api.ParameterDAO;
import com.ibm.fhir.persistence.jdbc.dao.api.ResourceDAO;
import com.ibm.fhir.persistence.util.AbstractQueryBuilder;
import com.ibm.fhir.search.SearchConstants.Modifier;
import com.ibm.fhir.search.parameters.Parameter;

/**
 * This class assists the JDBCQueryBuilder. Its purpose is to aggregate SQL query segments together to produce a well-formed FHIR Resource query or 
 * FHIR Resource count query. 
 */
class QuerySegmentAggregator {
    
    private static final String CLASSNAME = QuerySegmentAggregator.class.getName();
    private static final Logger log = java.util.logging.Logger.getLogger(CLASSNAME);
    
    protected static final String SELECT_ROOT = "SELECT R.RESOURCE_ID, R.LOGICAL_RESOURCE_ID, R.VERSION_ID, R.LAST_UPDATED, R.IS_DELETED, R.DATA, LR.LOGICAL_ID ";
    protected static final String SYSTEM_LEVEL_SELECT_ROOT = "SELECT RESOURCE_ID, LOGICAL_RESOURCE_ID, VERSION_ID, LAST_UPDATED, IS_DELETED, DATA, LOGICAL_ID ";
    protected static final String SYSTEM_LEVEL_SUBSELECT_ROOT = SELECT_ROOT;
    private static final String SELECT_COUNT_ROOT = "SELECT COUNT(R.RESOURCE_ID) ";
    private static final String SYSTEM_LEVEL_SELECT_COUNT_ROOT = "SELECT COUNT(RESOURCE_ID) ";
    private static final String SYSTEM_LEVEL_SUBSELECT_COUNT_ROOT = " SELECT R.RESOURCE_ID ";
    protected static final String FROM_CLAUSE_ROOT = "FROM {0}_RESOURCES R JOIN {0}_LOGICAL_RESOURCES LR ON R.LOGICAL_RESOURCE_ID=LR.LOGICAL_RESOURCE_ID AND R.RESOURCE_ID = LR.CURRENT_RESOURCE_ID ";
    protected static final String WHERE_CLAUSE_ROOT = "WHERE R.IS_DELETED <> 'Y'";
    protected static final String PARAMETER_TABLE_ALIAS = "pX";
    private static final String FROM = " FROM ";
    private static final String UNION = " UNION ALL ";
    protected static final String ON = " ON ";
    private static final String AND = " AND ";
    protected static final String COMBINED_RESULTS = " COMBINED_RESULTS";
    private static final String DEFAULT_ORDERING = " ORDER BY R.RESOURCE_ID ASC ";
        
    protected Class<?> resourceType;

    /**
     * querySegments and searchQueryParameters are used as parallel arrays
     * and should be added to/removed together. 
     */
    protected List<SqlQueryData> querySegments;
    protected List<Parameter> searchQueryParameters;
    
    private int offset;
    private int pageSize;
    protected ParameterDAO parameterDao;
    protected ResourceDAO resourceDao;
    

    /**
     * Constructs a new QueryBuilderHelper
     * @param resourceType - The type of FHIR Resource to be searched for.
     * @param offset - The beginning index of the first search result.
     * @param pageSize - The max number of requested search results.
     */
    protected QuerySegmentAggregator(Class<?> resourceType, int offset, int pageSize, 
                                    ParameterDAO parameterDao, ResourceDAO resourceDao) {
        super();
        this.resourceType = resourceType;
        this.offset = offset;
        this.pageSize = pageSize;
        this.parameterDao = parameterDao;
        this.resourceDao = resourceDao;
        this.querySegments = new ArrayList<>();
        this.searchQueryParameters = new ArrayList<>();
         
    }
    
    /**
     * Adds a query segment, which is a where clause segment corresponding to the passed query Parameter and its encapsulated search values.
     * @param querySegment A piece of a SQL WHERE clause 
     * @param queryParm - The corresponding query parameter
     */
    protected void addQueryData(SqlQueryData querySegment,Parameter queryParm) {
        final String METHODNAME = "addQueryData";
        log.entering(CLASSNAME, METHODNAME);
        
        //parallel arrays
        this.querySegments.add(querySegment);
        this.searchQueryParameters.add(queryParm);
        
        log.exiting(CLASSNAME, METHODNAME);
         
    }
    
    /**
     * Builds a complete SQL Query based upon the encapsulated query segments and bind variables.
     * @return SqlQueryData - contains the complete SQL query string and any associated bind variables.
     * @throws Exception 
     */
    protected SqlQueryData buildQuery() throws Exception {
        final String METHODNAME = "buildQuery";
        log.entering(CLASSNAME, METHODNAME);
        
        StringBuilder queryString = new StringBuilder();
        SqlQueryData queryData;
        List<Object> allBindVariables = new ArrayList<>();
        
        if (this.isSystemLevelSearch()) {
            queryData = this.buildSystemLevelQuery(SYSTEM_LEVEL_SELECT_ROOT, SYSTEM_LEVEL_SUBSELECT_ROOT, true);
        }
        else {
            queryString.append(SELECT_ROOT);
                    
            queryString.append(this.buildFromClause());
            
            queryString.append(this.buildWhereClause());
            
            for (SqlQueryData querySegment : this.querySegments) {
                allBindVariables.addAll(querySegment.getBindVariables());
            }
            // Add default ordering
            queryString.append(DEFAULT_ORDERING);
            this.addPaginationClauses(queryString);        
            queryData = new SqlQueryData(queryString.toString(), allBindVariables);
        }
        
        log.exiting(CLASSNAME, METHODNAME, queryData);
        return queryData;
    }
    
    /**
     *   Builds a complete SQL count query based upon the encapsulated query segments and bind variables.
     * @return SqlQueryData - contains the complete SQL count query string and any associated bind variables.
     * @throws Exception 
     */
    protected SqlQueryData buildCountQuery() throws Exception {
        final String METHODNAME = "buildCountQuery";
        log.entering(CLASSNAME, METHODNAME);
        
        StringBuilder queryString = new StringBuilder();
        SqlQueryData queryData;
        List<Object> allBindVariables = new ArrayList<>();
        
        if (this.isSystemLevelSearch()) {
            queryData = this.buildSystemLevelQuery(SYSTEM_LEVEL_SELECT_COUNT_ROOT, SYSTEM_LEVEL_SUBSELECT_COUNT_ROOT, false);
        }
        else {
            queryString.append(SELECT_COUNT_ROOT);
                    
            queryString.append(this.buildFromClause());
            
            queryString.append(this.buildWhereClause());
            
            for (SqlQueryData querySegment : this.querySegments) {
                allBindVariables.addAll(querySegment.getBindVariables());
            }
            queryData = new SqlQueryData(queryString.toString(), allBindVariables);
        }
        
        log.exiting(CLASSNAME, METHODNAME, queryData);
        return queryData;
        
    }
    
    /**
     * Build a system level query or count query, based upon the encapsulated query segments and bind variables and
     * the passed select-root strings.
     * A FHIR system level query spans multiple resource types, and therefore spans multiple tables in the database. 
     * @param selectRoot - The text of the outer SELECT ('SELECT' to 'FROM')
     * @param subSelectRoot - The text of the inner SELECT root to use in each sub-select
     * @param addFinalClauses - Indicates whether or not ordering and pagination clauses should be generated.
     * @return SqlQueryData - contains the complete SQL query string and any associated bind variables.
     * @throws Exception
     */
    protected SqlQueryData buildSystemLevelQuery(String selectRoot, String subSelectRoot, boolean addFinalClauses) 
                                                    throws Exception {
        final String METHODNAME = "buildSystemLevelQuery";
        log.entering(CLASSNAME, METHODNAME);
        
        StringBuilder queryString = new StringBuilder();
        SqlQueryData queryData;
        List<Object> allBindVariables = new ArrayList<>();
        Collection<Integer> resourceTypeIds;
        String tempFromClause;
        String resourceTypeName;
        boolean resourceTypeProcessed = false;
        Map<String, Integer> resourceNameIdMap = null;
        Map<Integer, String> resourceIdNameMap = null;
        
        queryString.append(selectRoot).append(FROM).append("(");
         
        resourceNameIdMap = this.resourceDao.readAllResourceTypeNames();
        resourceTypeIds = resourceNameIdMap.values();
        resourceIdNameMap = resourceNameIdMap.entrySet().stream()
                           .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
         
        for(Integer resourceTypeId : resourceTypeIds) {
             
            resourceTypeName =  resourceIdNameMap.get(resourceTypeId) + "_";
            
            tempFromClause = this.buildFromClause();
            tempFromClause = tempFromClause.replaceAll("Resource_", resourceTypeName);
            if (resourceTypeProcessed) {
                queryString.append(UNION);
            }
            queryString.append(subSelectRoot).append(tempFromClause);
            resourceTypeProcessed = true;

            tempFromClause = this.buildWhereClause();
            tempFromClause = tempFromClause.replaceAll("Resource_", resourceTypeName);
            queryString.append(tempFromClause);

            for (SqlQueryData querySegment : this.querySegments) {
                allBindVariables.addAll(querySegment.getBindVariables());
            }
        }
        queryString.append(")").append(COMBINED_RESULTS);
        if (addFinalClauses) {
            queryString.append(" ORDER BY RESOURCE_ID ASC ");
            this.addPaginationClauses(queryString);
        }
        
        queryData = new SqlQueryData(queryString.toString(), allBindVariables);
        
        log.exiting(CLASSNAME, METHODNAME, queryData);
        return queryData;
    }
    
    /**
     * Builds the FROM clause for the SQL query being generated. The appropriate Resource and Parameter table names are included 
     * along with an alias for each table.
     * @return A String containing the FROM clause
     * @throws Exception 
     */
    protected String buildFromClause() throws Exception {
        final String METHODNAME = "buildFromClause";
        log.entering(CLASSNAME, METHODNAME);
        
        StringBuilder fromClause = new StringBuilder();
        fromClause.append(MessageFormat.format(FROM_CLAUSE_ROOT, this.resourceType.getSimpleName()));
        fromClause.append(" ");
            
        log.exiting(CLASSNAME, METHODNAME);
        return fromClause.toString();
        
    }
    
    /**
     * Builds the WHERE clause for the query being generated. This method aggregates the contained query segments, and ties those segments back
     * to the appropriate parameter table alias.
     * @return
     */
    protected String buildWhereClause() {
        final String METHODNAME = "buildWhereClause";
        log.entering(CLASSNAME, METHODNAME);
        boolean isLocationQuery;
        
        StringBuilder whereClause = new StringBuilder();
        String whereClauseSegment;
                         
        whereClause.append(WHERE_CLAUSE_ROOT);
        if (!this.querySegments.isEmpty()) {
            for(int i = 0; i < this.querySegments.size(); i++) {
                SqlQueryData querySegment = this.querySegments.get(i);
                Parameter param = this.searchQueryParameters.get(i);

                whereClauseSegment = querySegment.getQueryString();
                if (Modifier.MISSING.equals(param.getModifier())) {
                    whereClause.append(AND).append(whereClauseSegment);
                } else {
                    
                    whereClause.append(AND).append("R.LOGICAL_RESOURCE_ID IN (SELECT LOGICAL_RESOURCE_ID FROM ");
                    whereClause.append(this.resourceType.getSimpleName());
                    isLocationQuery = Location.class.equals(this.resourceType) && param.getName().equals(AbstractQueryBuilder.NEAR);
                    switch(param.getType()) {
                        case URI :
                        case REFERENCE : 
                        case STRING :   whereClause.append("_STR_VALUES ");
                             break;
                        case NUMBER :   whereClause.append("_NUMBER_VALUES "); 
                             break;
                        case QUANTITY : whereClause.append("_QUANTITY_VALUES ");
                             break;
                        case DATE :     whereClause.append("_DATE_VALUES ");
                             break;
                        case TOKEN :    if (isLocationQuery) {
                                            whereClause.append("_LATLNG_VALUES ");
                                        }
                                        else {
                                            whereClause.append("_TOKEN_VALUES ");
                                        }
                             break;
                    }
                    whereClauseSegment = whereClauseSegment.replaceAll(PARAMETER_TABLE_ALIAS + ".", "");
                    whereClause.append(" WHERE ").append(whereClauseSegment).append(")");
                }
            }
        }
        
        log.exiting(CLASSNAME, METHODNAME);
        return whereClause.toString();
    }
    
    /**
     * 
     * @return true if this instance represents a FHIR system level search
     */
    protected boolean isSystemLevelSearch() {
        return Resource.class.equals(this.resourceType);
    }
    
    /**
     * Adds the appropriate pagination clauses to the passed query string buffer, based on the type
     * of database we're running against.
     * @param queryString A query string buffer.
     * @throws Exception
     */
    protected void addPaginationClauses(StringBuilder queryString) throws Exception {
        
        if(this.parameterDao.isDb2Database()) {
            queryString.append(" LIMIT ").append(this.pageSize).append(" OFFSET ").append(this.offset);
        }
        else {
            queryString.append(" OFFSET ").append(this.offset).append(" ROWS")
                       .append(" FETCH NEXT ").append(this.pageSize).append(" ROWS ONLY");
        }
    }
}
