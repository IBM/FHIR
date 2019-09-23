/*
 * (C) Copyright IBM Corp. 2016,2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.test.common;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.Location;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.search.context.FHIRSearchContext;
import com.ibm.fhir.search.util.SearchUtil;

/**
 *  This class contains a collection of tests that will be run against
 *  each of the various persistence layer implementations.
 *  There will be a subclass in each persistence project.
 */
public abstract class AbstractQueryLocationTest extends AbstractPersistenceTest {
    
    /**
     * Tests the FHIRPersistenceCloudantImpl create API for a Location.
     * 
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" })
    public void testCreateLocation1() throws Exception {
        Location location = readResource(Location.class, "location-example.canonical.json");

        persistence.create(getDefaultPersistenceContext(), location);
        assertNotNull(location);
        assertNotNull(location.getId());
        assertNotNull(location.getId().getValue());
        assertNotNull(location.getMeta());
        assertNotNull(location.getMeta().getVersionId().getValue());
        assertEquals("1", location.getMeta().getVersionId().getValue());
    }
    
    /**
     * Tests the FHIRPersistenceCloudantImpl create API for a Location.
     * 
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" })
    public void testCreateLocation2() throws Exception {
        Location location = readResource(Location.class, "location-example-room.canonical.json");

        persistence.create(getDefaultPersistenceContext(), location);
        assertNotNull(location);
        assertNotNull(location.getId());
        assertNotNull(location.getId().getValue());
        assertNotNull(location.getMeta());
        assertNotNull(location.getMeta().getVersionId().getValue());
        assertEquals("1", location.getMeta().getVersionId().getValue());
    }
    
    /**
     * Tests the FHIRPersistenceCloudantImpl create API for a Location.
     * 
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" })
    public void testCreateLocation3() throws Exception {
        Location location = readResource(Location.class, "Location1.json");

        persistence.create(getDefaultPersistenceContext(), location);
        assertNotNull(location);
        assertNotNull(location.getId());
        assertNotNull(location.getId().getValue());
        assertNotNull(location.getMeta());
        assertNotNull(location.getMeta().getVersionId().getValue());
        assertEquals("1", location.getMeta().getVersionId().getValue());
    }
    
    /**
     * Tests a query for a Location with name = 'South Wing, second floor' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation1" })
    public void testLocationQuery_name() throws Exception {
        List<Resource> resources = runQueryTest(Location.class, persistence, "name", "South Wing, second floor");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
    }    
    
    /**
     * Tests a query for a Location with address-country = 'USA' which should yield no results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation1" })
    public void testLocationQuery_addressCountry() throws Exception {
        List<Resource> resources = runQueryTest(Location.class, persistence, "address-country", "USA");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
    }
    
    /**
     * Tests a query for a Location with address-city = 'Den Burg' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation1" })
    public void testLocationQuery_addressCity() throws Exception {
        List<Resource> resources = runQueryTest(Location.class, persistence, "address-city", "Den Burg");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((Location)resources.get(0)).getAddress().getCity().getValue(),"Den Burg");
    }
    
    /**
     * Tests a query for a Location with address = 'Den Burg' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation1" })
    public void testLocationQuery_address() throws Exception {
        List<Resource> resources = runQueryTest(Location.class, persistence, "address-city", "Den Burg");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((Location)resources.get(0)).getAddress().getCity().getValue(),"Den Burg");
        assertEquals(((Location)resources.get(0)).getAddress().getLine().get(0).getValue(),"Galapagosweg 91, Building A");
        assertEquals(((Location)resources.get(0)).getAddress().getPostalCode().getValue(),"9105 PZ");
        assertEquals(((Location)resources.get(0)).getAddress().getCountry().getValue(),"NLD");
    }
    
    /**
     * Tests a query for a Location with organization = 'Organization/f001' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation1" })
    public void testLocationQuery_organization() throws Exception {
        List<Resource> resources = runQueryTest(Location.class, persistence, "organization", "Organization/f001");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((Location)resources.get(0)).getManagingOrganization().getReference().getValue(),"Organization/f001");
    }
    
    /**
     * Tests a query for a Location with partof = 'Location/1' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation2" })
    public void testLocationQuery_partof() throws Exception {
        List<Resource> resources = runQueryTest(Location.class, persistence, "partof", "Location/1");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((Location)resources.get(0)).getPartOf().getReference().getValue(),"Location/1");
    }
    
    /**
     * Test query for geo location with less distance 
     * @throws Exception
     */
    
    //enable for cloudant once query is implemented 
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithLessDistance() throws Exception {
        //this test will return no records
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        queryParms.put("near", Collections.singletonList("44.977490|-93.275220"));
        queryParms.put("near-distance", Collections.singletonList("1|km"));
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms,null);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() == 0);


    }

    /**
     *  Test query for geo location without distance - Default is 5KM
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithNoDistance() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        //system:code
        queryParms.put("near", Collections.singletonList("44.977490|-93.275220"));
    
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms,null);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
    }

    /**
     *  Test query for geo location without distance - Default is 5KM
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithNoNear() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        //system:code
        queryParms.put("near-distance", Collections.singletonList("4|km"));
    
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms,null);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() == 0);
    }
    
    
    /**
     *  Test query for geo location without distance - Default is 5KM
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithDistanceCity() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        //system:code
        queryParms.put("near", Collections.singletonList("44.977490|-93.275220"));
        queryParms.put("address-city", Collections.singletonList("Ann Arbor"));
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms,null);
         
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
    }
    
    
    /**
     *  Test query for geo location without distance - Default is 5KM
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithMultipleParameter() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        //system:code
    
        queryParms.put("address-city", Collections.singletonList("Ann Arbor"));
        queryParms.put("partof", Collections.singletonList("Location/1"));
        
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms,null);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        //I know that it will not be match..
        assertTrue(resources.size() == 0);
    }

    /**
     * Test query for geo location with distance in KM
     * @throws Exception
     */
    
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithKMDistance() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        //system:code
        queryParms.put("near", Collections.singletonList("44.977490|-93.275220"));
        //4=system|km=code
        queryParms.put("near-distance", Collections.singletonList("4|km"));
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms, null);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
    }
    
    /**
     * Test query for geo location with distance in kilometers
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithKilometersDistance() throws Exception {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        //system:code
        queryParms.put("near", Collections.singletonList("44.977490|-93.275220"));
        //4=system|km=code
        queryParms.put("near-distance", Collections.singletonList("4|kilometers"));
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms, null);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
    }
    
    /**
     * Test query for geo location with distance in miles
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation3" })
    public void testWithMilesDistance() throws Exception {
        Map<String, List<String>> queryParms= new HashMap<String, List<String>>();
        //system:code
        queryParms.put("near", Collections.singletonList("44.977490|-93.275220"));
        //4=system|km=code
        queryParms.put("near-distance", Collections.singletonList("4|miles"));
        Class<? extends Resource> resourceType = Location.class;
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms, null);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
    }
    
    /*
     * Pagination Testcases
     */
    
    /**
     * Tests a query with a resource type but without any query parameters. This should yield correct results using pagination
     * 
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation1", "testCreateLocation2", "testCreateLocation3" })
    public void testLocationPagination_001() throws Exception {
        
        Class<? extends Resource> resourceType = Location.class;
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms, null);
        context.setPageNumber(1);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        long count = context.getTotalCount();
        int pageSize = context.getPageSize();
        int lastPgNum = context.getLastPageNumber();
        assertEquals(context.getLastPageNumber(), (int) ((count + pageSize - 1) / pageSize));
        assertTrue((count > 10) ? (lastPgNum > 1) : (lastPgNum == 1));
    }
    
    /**
     * Tests a query for a Location with partof = 'Location/1' which should yield correct results using pagination
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation2" })
    public void testLocationPagination_002() throws Exception {
        
        String parmName = "partof";
        String parmValue = "Location/1";
        Class<? extends Resource> resourceType = Location.class;
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        
        queryParms.put(parmName, Collections.singletonList(parmValue));
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms, null);
        context.setPageNumber(1);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((Location)resources.get(0)).getPartOf().getReference().getValue(),"Location/1");
        long count = context.getTotalCount();
        int pageSize = context.getPageSize();
        int lastPgNum = context.getLastPageNumber();
        assertEquals(context.getLastPageNumber(), (int) ((count + pageSize - 1) / pageSize));
        assertTrue((count > 10) ? (lastPgNum > 1) : (lastPgNum == 1));
    }
    
    /**
     * Tests a query for a Location with partof = 'Location/999' which should yield no results using pagination
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateLocation2" })
    public void testLocationPagination_003() throws Exception {
        
        String parmName = "partof";
        String parmValue = "Location/999";
        Class<? extends Resource> resourceType = Location.class;
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        
        queryParms.put(parmName, Collections.singletonList(parmValue));
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms, null);
        context.setPageNumber(1);
        List<Resource> resources = persistence.search(getPersistenceContextForSearch(context), Location.class);
        assertNotNull(resources);
        assertTrue(resources.size() == 0);
        long count = context.getTotalCount();
//        int lastPgNum = context.getLastPageNumber();
        assertTrue((count == 0)/* && (lastPgNum == Integer.MAX_VALUE)*/);
    }    
}
