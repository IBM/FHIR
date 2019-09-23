/*
 * (C) Copyright IBM Corp. 2016,2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.test.common;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.RelatedPerson;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Address;

/**
 *  This class contains a collection of tests that will be run against
 *  each of the various persistence layer implementations.
 *  There will be a subclass in each persistence project.
 */
public abstract class AbstractQueryRelatedPersonTest extends AbstractPersistenceTest {
    
    /**
     * Tests the FHIRPersistenceCloudantImpl create API for a Device.
     * 
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" })
    public void testCreateRelatedPerson() throws Exception {
        RelatedPerson relatedPerson = readResource(RelatedPerson.class, "relatedperson-example.canonical.json");

        persistence.create(getDefaultPersistenceContext(), relatedPerson);
        assertNotNull(relatedPerson);
        assertNotNull(relatedPerson.getId());
        assertNotNull(relatedPerson.getId().getValue());
        assertNotNull(relatedPerson.getMeta());
        assertNotNull(relatedPerson.getMeta().getVersionId().getValue());
        assertEquals("1", relatedPerson.getMeta().getVersionId().getValue());
    }        
    
    /**
     * Tests a query for a RelatedPerson with name = 'Bénédicte' which should yield correct results.
     * This is a "starts-with" case insensitive, accent-insensitive string value search.
     * @throws Exception
     * TODO fix
     */
//    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
//    public void testRelatedPersonQuery_name1() throws Exception {
//        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "name", "Bénédicte");
//        assertNotNull(resources);
//        assertTrue(resources.size() != 0);
//        assertEquals(((RelatedPerson)resources.get(0)).getName().getGiven().get(0).getValue(),"Bénédicte");
//    }
//    
//    /**
//     * This is an exact string value search.
//     * @throws Exception
//     */
//    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
//    public void testRelatedPersonQuery_name2() throws Exception {
//        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "name:exact", "Bénédicte");
//        assertNotNull(resources);
//        assertTrue(resources.size() != 0);
//        assertEquals(((RelatedPerson)resources.get(0)).getName().getGiven().get(0).getValue(),"Bénédicte");
//    }
//    
//    /**
//     * This is a "starts-with" case insensitive, accent-insensitive string value search, with an all lower case search parameter value.
//     * @throws Exception
//     */
//    @Test(groups = { "cloudant", "jpa", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
//    public void testRelatedPersonQuery_name3() throws Exception {
//        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "name", "Bénédicte");
//        assertNotNull(resources);
//        assertTrue(resources.size() != 0);
//        assertEquals(((RelatedPerson)resources.get(0)).getName().getGiven().get(0).getValue(),"Bénédicte");
//    }
    
    /**
     * This is an exact string value search, with non-matching all lower case search parameter value.
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
    public void testRelatedPersonQuery_name4() throws Exception {
        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "name:exact", "bénédicte");
        assertNotNull(resources);
        assertTrue(resources.size() == 0);
    }
    
    /**
     * Tests a query for a RelatedPerson with incorrect name which should yield no results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
    public void testRelatedPersonQuery_name_noResults() throws Exception {
        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "name", "None");
        assertNotNull(resources);
        assertTrue(resources.size() == 0);
    }
    
    /**
     * Tests a query for a RelatedPerson with gender = 'female' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
    public void testRelatedPersonQuery_gender() throws Exception {
        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "gender", "female");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((RelatedPerson)resources.get(0)).getGender().getValue(),"female");
    }
    
    /**
     * Tests a query for a RelatedPerson with relationship = 'Bénédicte' which should yield correct results
     * @throws Exception
     * TODO fix
     */
//    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
//    public void testRelatedPersonQuery_relationship() throws Exception {
//        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "name", "Bénédicte");
//        assertNotNull(resources);
//        assertTrue(resources.size() != 0);
//        assertEquals(((RelatedPerson)resources.get(0)).getName().getGiven().get(0).getValue(),"Bénédicte");
//    }
    
    /**
     * Tests a query for a RelatedPerson with address-city = 'Paris' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
    public void testRelatedPersonQuery_address_city() throws Exception {
        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "address-city", "Paris");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        List<Address> addrList = ((RelatedPerson)resources.get(0)).getAddress();
        assertEquals(addrList.get(0).getCity().getValue(),"Paris");
    }
    
    /**
     * Tests a query for a RelatedPerson with phone = '+33 (237) 998327' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "cloudant", "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
    public void testRelatedPersonQuery_phone() throws Exception {
        List<Resource> resources = runQueryTest(RelatedPerson.class, persistence, "phone", "+33 (237) 998327");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((RelatedPerson)resources.get(0)).getTelecom().get(0).getValue().getValue(),"+33 (237) 998327");
    }
    
    /*
     * 
     * Compartment search testcases
     * 
     */
    
    /**
     * Tests a query for a RelatedPerson with name = 'Bénédicte' which should yield correct results
     * @throws Exception
     * TODO fix
     */
//    @Test(groups = { "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
//    public void testRelatedPersonQuery_name_PatCompmt() throws Exception {
//        List<Resource> resources = runQueryTest("Patient", "example", RelatedPerson.class, persistence, "name", "Bénédicte");
//        assertNotNull(resources);
//        assertTrue(resources.size() != 0);
//        assertEquals(((RelatedPerson)resources.get(0)).getName().getGiven().get(0).getValue(),"Bénédicte");
//    }
    
    /**
     * Tests a query for a RelatedPerson with incorrect name which should yield no results
     * @throws Exception
     */
    @Test(groups = { "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
    public void testRelatedPersonQuery_name_noResults_PatCompmt() throws Exception {
        List<Resource> resources = runQueryTest("Patient", "example", RelatedPerson.class, persistence, "name", "None");
        assertNotNull(resources);
        assertTrue(resources.size() == 0);
    }
    
    /**
     * Tests a query for a RelatedPerson with gender = 'Female' which should yield correct results
     * @throws Exception
     */
    @Test(groups = { "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
    public void testRelatedPersonQuery_gender_PatCompmt() throws Exception {
        List<Resource> resources = runQueryTest("Patient", "example", RelatedPerson.class, persistence, "gender", "female");
        assertNotNull(resources);
        assertTrue(resources.size() != 0);
        assertEquals(((RelatedPerson)resources.get(0)).getGender().getValue(),"female");
    }
    
    /**
     * Tests a query for a RelatedPerson with relationship = 'Bénédicte' which should yield correct results
     * @throws Exception
     * TODO fix
     */
//    @Test(groups = { "jpa", "jdbc", "jdbc-normalized" }, dependsOnMethods = { "testCreateRelatedPerson" })
//    public void testRelatedPersonQuery_relationship_PatCompmt() throws Exception {
//        List<Resource> resources = runQueryTest("Patient", "example", RelatedPerson.class, persistence, "name", "Bénédicte");
//        assertNotNull(resources);
//        assertTrue(resources.size() != 0);
//        assertEquals(((RelatedPerson)resources.get(0)).getName().getGiven().get(0).getValue(),"Bénédicte");
//    }
}
