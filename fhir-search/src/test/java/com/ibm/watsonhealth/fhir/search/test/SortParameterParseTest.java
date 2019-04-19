/**
 * (C) Copyright IBM Corp. 2016,2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.search.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ibm.watsonhealth.fhir.model.Observation;
import com.ibm.watsonhealth.fhir.model.Patient;
import com.ibm.watsonhealth.fhir.search.Parameter;
import com.ibm.watsonhealth.fhir.search.ParameterValue;
import com.ibm.watsonhealth.fhir.search.SortParameter;
import com.ibm.watsonhealth.fhir.search.SortParameter.SortDirection;
import com.ibm.watsonhealth.fhir.search.context.FHIRSearchContext;
import com.ibm.watsonhealth.fhir.search.exception.FHIRSearchException;
import com.ibm.watsonhealth.fhir.search.util.SearchUtil;

/**
 * This JUNIT test class contains methods that test the parsing of sort parameters in the SearchUtil class. 
 * @author markd
 *
 */
public class SortParameterParseTest {
    
    /**
     *  Tests an invalid direction modifier on the _sort query parameter.
     * @throws Exception
     */
    @Test(expected = FHIRSearchException.class) 
    public void testInvalidDirection() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;
        String queryString = "&_sort:xxx=birthdate";
        
        queryParameters.put("_sort:xxx", Collections.singletonList("birthdate"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString);
    }
    
    /**
     *  Tests an invalid sort parameter value.
     * @throws Exception
     */
    @Test 
    public void testUnknownSortParm() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Patient> resourceType = Patient.class;
        String queryString = "&_sort=bogusSortParm";
        
        // In lenient mode, invalid search parameters should be ignored
        queryParameters.put("_sort", Collections.singletonList("bogusSortParm"));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString);
        assertNotNull(searchContext);
        assertTrue(searchContext.getSortParameters() == null || searchContext.getSortParameters().isEmpty());
        
        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/" + resourceType.getSimpleName(), searchContext);
        assertFalse(selfUri + " contain unexpected " + queryString, selfUri.contains(queryString));
    }
    
    @Test(expected = FHIRSearchException.class) 
    public void testUnknownSortParm_strict() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;
        String queryString = "&_sort=bogusSortParm";
        
        // In strict mode (lenient=false), the search should throw a FHIRSearchException
        queryParameters.put("_sort", Collections.singletonList("bogusSortParm"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString, false);
    }
    
    /**
     *  Tests a valid sort with: asc modifier, and a valid parameter value.
     * @throws Exception
     */
    @Test 
    public void testValidSortParm() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Patient> resourceType = Patient.class;
        SortDirection direction = SortDirection.ASCENDING; 
        String sortParmName = "birthdate"; 
        String queryString = "&_sort:" + direction.value() + "=" + sortParmName;
        
        queryParameters.put("_sort:" + direction.value(), Collections.singletonList(sortParmName));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString);
        assertNotNull(searchContext);
        
        // Do sort parameter validation
        assertNotNull(searchContext.getSortParameters());
        assertEquals(1,searchContext.getSortParameters().size());
        SortParameter sortParm = searchContext.getSortParameters().get(0);
        assertEquals(sortParmName, sortParm.getName());
        assertEquals(direction, sortParm.getDirection());
        assertEquals(1, sortParm.getQueryStringIndex());
        assertEquals(Parameter.Type.DATE, sortParm.getType());
        
        // Do search parameter validation
        assertNotNull(searchContext.getSearchParameters());
        assertTrue(searchContext.getSearchParameters().isEmpty());
        
        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/" + resourceType.getSimpleName(), searchContext);
        assertTrue(selfUri + " does not contain expected " + queryString, selfUri.contains(queryString));
    }
    
    /**
     *  Tests a valid sort with: desc modifier, and a valid parameter value.
     * @throws Exception
     */
    @Test 
    public void testValidSortParm1() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Patient> resourceType = Patient.class;
        SortDirection direction = SortDirection.DESCENDING; 
        String sortParmName = "birthdate"; 
        String queryString = "&_sort:" + direction.value() + "=" + sortParmName;
        
        queryParameters.put("_sort:" + direction.value(), Collections.singletonList(sortParmName));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString);
        
        // Do sort parameter validation
        assertNotNull(searchContext);
        assertNotNull(searchContext.getSortParameters());
        assertEquals(1,searchContext.getSortParameters().size());
        SortParameter sortParm = searchContext.getSortParameters().get(0);
        assertEquals(sortParmName, sortParm.getName());
        assertEquals(direction, sortParm.getDirection());
        assertEquals(1, sortParm.getQueryStringIndex());
        assertEquals(Parameter.Type.DATE, sortParm.getType());
        
        // Do search parameter validation
        assertNotNull(searchContext.getSearchParameters());
        assertTrue(searchContext.getSearchParameters().isEmpty());
        
        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/" + resourceType.getSimpleName(), searchContext);
        assertTrue(selfUri + " does not contain expected " + queryString, selfUri.contains(queryString));
    }
    
    /**
     *  Tests a valid sort with no modifier, and a valid parameter value.
     * @throws Exception
     */
    @Test
    public void testValidSortParm2() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Patient> resourceType = Patient.class;
        String sortParmName = "birthdate"; 
        String queryString = "&_sort" + "=" + sortParmName;
        
        queryParameters.put("_sort", Collections.singletonList(sortParmName));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString);
        
        // Do sort parameter validation
        assertNotNull(searchContext);
        assertNotNull(searchContext.getSortParameters());
        assertEquals(1,searchContext.getSortParameters().size());
        SortParameter sortParm = searchContext.getSortParameters().get(0);
        assertEquals(sortParmName, sortParm.getName());
        assertEquals(SortDirection.ASCENDING, sortParm.getDirection());
        assertEquals(1, sortParm.getQueryStringIndex());
        assertEquals(Parameter.Type.DATE, sortParm.getType());
        
        // Do search parameter validation
        assertNotNull(searchContext.getSearchParameters());
        assertTrue(searchContext.getSearchParameters().isEmpty());
        
        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/" + resourceType.getSimpleName(), searchContext);
        // The server adds the implicit sort direction and so we just look for the parameter instead of the full queryString
        assertTrue(selfUri + " does not contain expected sort parameter 'birthdate'", selfUri.contains(sortParmName));
    }
    
    /**
     * Tests a valid sort with: asc modifier, a valid sort parameter value, and valid search parameters.
     * @throws Exception
     */
    @Test
    public void testValidSortParmWithSearchParms() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Observation> resourceType = Observation.class;
        SortDirection direction = SortDirection.ASCENDING; 
        String sortParmName = "patient"; 
        String searchParmName = "performer";
        String searchParmValue = "Practioner/1";
        String queryStringPart1 = "&" + searchParmName + "=" + searchParmValue;
        String queryStringPart2 = "&_sort:" + direction.value() + "=" + sortParmName;
        String queryString = queryStringPart1 + queryStringPart2;

        queryParameters.put("_sort:" + direction.value(), Collections.singletonList(sortParmName));
        queryParameters.put(searchParmName, Collections.singletonList(searchParmValue));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString.toString());

        // Do sort parameter validation
        assertNotNull(searchContext);
        assertNotNull(searchContext.getSortParameters());
        assertEquals(1,searchContext.getSortParameters().size());
        SortParameter sortParm = searchContext.getSortParameters().get(0);
        assertEquals(sortParmName, sortParm.getName());
        assertTrue(sortParm.getQueryStringIndex() > 0);
        assertEquals(direction, sortParm.getDirection());
        assertEquals(Parameter.Type.REFERENCE, sortParm.getType());

        // Do search parameter validation
        assertNotNull(searchContext.getSearchParameters());
        assertEquals(1, searchContext.getSearchParameters().size());
        Parameter searchParm = searchContext.getSearchParameters().get(0);
        assertEquals(searchParmName, searchParm.getName());
        assertNotNull(searchParm.getValues());
        assertEquals(1, searchParm.getValues().size());
        ParameterValue parmValue = searchParm.getValues().get(0);
        assertEquals(searchParmValue, parmValue.getValueString());

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/" + resourceType.getSimpleName(), searchContext);
        assertTrue(selfUri + " does not contain expected " + queryStringPart1, selfUri.contains(queryStringPart1));
        assertTrue(selfUri + " does not contain expected " + queryStringPart2, selfUri.contains(queryStringPart2));
    }
    
    /**
     * Tests a valid sort with multiple valid sort parameters and valid search parameters.
     * @throws Exception
     */
    @Test
    public void testMultipleValidSortParmsWithSearchParms() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Observation> resourceType = Observation.class;
        SortDirection directionAsc = SortDirection.ASCENDING;
        SortDirection directionDesc = SortDirection.DESCENDING;
        String sortParmName1 = "patient";
        String sortParmName2 = "status";
        String sortParmName3 = "value-string";
        String sortParmName4 = "value-date";
        String sortParmName5 = "value-quantity";
        String searchParmName = "performer";
        String searchParmValue = "Practioner/1";
        String queryStringPart1 = "&_sort:" + directionAsc.value() + "=" + sortParmName1;
        String queryStringPart2 = "&_sort:" + directionAsc.value() + "=" + sortParmName2;
        String queryStringPart3 = "&_sort:" + directionDesc.value() + "=" + sortParmName3;
        String queryStringPart4 = "&_sort:" + directionDesc.value() + "=" + sortParmName4;
        String queryStringPart5 = "&_sort" + "=" + sortParmName5;
        String queryStringPart6 = "&" + searchParmName + "=" + searchParmValue;
        String queryString = queryStringPart1 + queryStringPart2 + queryStringPart3 + queryStringPart4 + queryStringPart5 + queryStringPart6; 

        queryParameters.put("_sort:" + directionAsc.value(), Arrays.asList(new String[] {sortParmName2, sortParmName1}));
        queryParameters.put("_sort:" + directionDesc.value(), Arrays.asList(new String[] {sortParmName4, sortParmName3}));
        queryParameters.put("_sort", Arrays.asList(new String[] {sortParmName5}));
        queryParameters.put(searchParmName, Collections.singletonList(searchParmValue));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, queryString);
         
        // Do sort parameter validation
        assertNotNull(searchContext);
        assertNotNull(searchContext.getSortParameters());
        assertEquals(5,searchContext.getSortParameters().size());
        
        SortParameter sortParm1 = searchContext.getSortParameters().get(0);
        assertEquals(sortParmName1, sortParm1.getName());
        assertEquals(directionAsc, sortParm1.getDirection());
        assertTrue(sortParm1.getQueryStringIndex() > 0);
        assertEquals(Parameter.Type.REFERENCE, sortParm1.getType());
        
        SortParameter sortParm2 = searchContext.getSortParameters().get(1);
        assertEquals(sortParmName2, sortParm2.getName());
        assertEquals(directionAsc, sortParm2.getDirection());
        assertTrue(sortParm2.getQueryStringIndex() > 0);
        assertEquals(Parameter.Type.TOKEN, sortParm2.getType());
        
        SortParameter sortParm3 = searchContext.getSortParameters().get(2);
        assertEquals(sortParmName3, sortParm3.getName());
        assertEquals(directionDesc, sortParm3.getDirection());
        assertTrue(sortParm3.getQueryStringIndex() > 0);
        assertEquals(Parameter.Type.STRING, sortParm3.getType());
        
        SortParameter sortParm4 = searchContext.getSortParameters().get(3);
        assertEquals(sortParmName4, sortParm4.getName());
        assertEquals(directionDesc, sortParm4.getDirection());
        assertTrue(sortParm4.getQueryStringIndex() > 0);
        assertEquals(Parameter.Type.DATE, sortParm4.getType());
        
        SortParameter sortParm5 = searchContext.getSortParameters().get(4);
        assertEquals(sortParmName5, sortParm5.getName());
        assertEquals(directionAsc, sortParm5.getDirection());
        assertTrue(sortParm5.getQueryStringIndex() > 0);
        assertEquals(Parameter.Type.QUANTITY, sortParm5.getType());
        
        
        // Do search parameter validation
        assertNotNull(searchContext.getSearchParameters());
        assertEquals(1, searchContext.getSearchParameters().size());
        Parameter searchParm = searchContext.getSearchParameters().get(0);
        assertEquals(searchParmName, searchParm.getName());
        assertNotNull(searchParm.getValues());
        assertEquals(1, searchParm.getValues().size());
        ParameterValue parmValue = searchParm.getValues().get(0);
        assertEquals(searchParmValue, parmValue.getValueString());
        
        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/" + resourceType.getSimpleName(), searchContext);
        assertTrue(selfUri + " does not contain expected " + queryStringPart1, selfUri.contains(queryStringPart1));
        assertTrue(selfUri + " does not contain expected " + queryStringPart2, selfUri.contains(queryStringPart2));
        assertTrue(selfUri + " does not contain expected " + queryStringPart3, selfUri.contains(queryStringPart3));
        assertTrue(selfUri + " does not contain expected " + queryStringPart4, selfUri.contains(queryStringPart4));
        // The server adds the implicit sort direction and so we just look for the parameter instead of the full queryString
        assertTrue(selfUri + " does not contain expected sort parameter '" + sortParmName5 + "'", selfUri.contains(sortParmName5));
        assertTrue(selfUri + " does not contain expected " + queryStringPart6, selfUri.contains(queryStringPart6));
    }
    
}
