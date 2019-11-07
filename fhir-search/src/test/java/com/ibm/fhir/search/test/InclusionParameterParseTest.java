/*
 * (C) Copyright IBM Corp. 2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.search.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.Medication;
import com.ibm.fhir.model.resource.Organization;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.search.context.FHIRSearchContext;
import com.ibm.fhir.search.exception.FHIRSearchException;
import com.ibm.fhir.search.parameters.InclusionParameter;
import com.ibm.fhir.search.util.SearchUtil;

/**
 * This TestNG test class contains methods that test the parsing of search result inclusion parameters (_include and
 * _revinclude) in the SearchUtil class.
 * 
 * @author markd
 * @author pbastide
 *
 */
public class InclusionParameterParseTest extends BaseSearchTest {

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testIncludeInvalidSyntax() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;

        queryParameters.put("_include", Collections.singletonList("xxx"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters);
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testIncludeInvalidWithSort() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;

        queryParameters.put("_include", Collections.singletonList("xxx"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters);
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testIncludeInvalidJoinResourceTypeLenient() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;

        queryParameters.put("_include", Collections.singletonList("MedicationOrder:patient"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters, false);
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testIncludeInvalidJoinResourceTypeNonLenient() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;

        queryParameters.put("_include", Collections.singletonList("MedicationOrder:patient"));
        // inherently applies true
        FHIRSearchContext searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, false);
        System.out.println(searchContext);
    }

    @Test
    public void testIncludeUnknownParameterName_lenient() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;
        String queryString = "&_include=Patient:bogus";

        // In lenient mode, the unknown parameter should be ignored
        queryParameters.put("_include", Collections.singletonList("Patient:bogus"));
        FHIRSearchContext searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, true);
        assertNotNull(searchContext);
        assertFalse(searchContext.hasIncludeParameters());

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertFalse(selfUri.contains(queryString));
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testIncludeUnknownParameterName_strict() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;

        // In strict mode, the query should throw a FHIRSearchException
        queryParameters.put("_include", Collections.singletonList("Patient:bogus"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters, false);
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testIncludeInvalidParameterType() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;

        queryParameters.put("_include", Collections.singletonList("Patient:active"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters);
    }

    @Test
    public void testIncludeValidSingleTargetType() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Patient> resourceType = Patient.class;
        String queryString = "&_include=Patient:organization";

        queryParameters.put("_include", Collections.singletonList("Patient:organization"));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters);
        assertNotNull(searchContext);
        assertTrue(searchContext.hasIncludeParameters());
        assertEquals(1, searchContext.getIncludeParameters().size());
        InclusionParameter incParm = searchContext.getIncludeParameters().get(0);
        assertEquals("Patient", incParm.getJoinResourceType());
        assertEquals("organization", incParm.getSearchParameter());
        assertEquals("Organization", incParm.getSearchParameterTargetType());
        assertFalse(searchContext.hasRevIncludeParameters());

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertTrue(selfUri.contains(queryString));
    }

    @Test
    public void testIncludeMissingTargetType() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;
        FHIRSearchContext searchContext;
        String queryString = "&_include=Patient:general-practitioner";

        List<InclusionParameter> expectedIncludeParms = new ArrayList<>();
        expectedIncludeParms.add(new InclusionParameter("Patient", "general-practitioner", "Organization"));
        expectedIncludeParms.add(new InclusionParameter("Patient", "general-practitioner", "Practitioner"));
        expectedIncludeParms.add(new InclusionParameter("Patient", "general-practitioner", "PractitionerRole"));

        queryParameters.put("_include", Collections.singletonList("Patient:general-practitioner"));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters);

        assertNotNull(searchContext);
        assertTrue(searchContext.hasIncludeParameters());

        assertEquals(expectedIncludeParms.size(), searchContext.getIncludeParameters().size());
        for (InclusionParameter includeParm : expectedIncludeParms) {
            assertTrue(expectedIncludeParms.contains(includeParm));
        }

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertTrue(selfUri.contains(queryString));
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testIncludeInvalidTargetType() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Patient> resourceType = Patient.class;

        queryParameters.put("_include", Collections.singletonList("Patient:careprovider:Contract"));
        System.out.println(SearchUtil.parseQueryParameters(resourceType, queryParameters, false));
    }

    @Test
    public void testIncludeValidTargetType() throws Exception {
        // Changed to general-practitioner

        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Patient> resourceType = Patient.class;
        String queryString = "&_include=Patient:general-practitioner:Practitioner";

        queryParameters.put("_include", Collections.singletonList("Patient:general-practitioner:Practitioner"));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters);
        assertNotNull(searchContext);

        assertTrue(searchContext.hasIncludeParameters());
        assertEquals(1, searchContext.getIncludeParameters().size());
        InclusionParameter incParm = searchContext.getIncludeParameters().get(0);
        assertEquals("Patient", incParm.getJoinResourceType());
        assertEquals("general-practitioner", incParm.getSearchParameter());
        assertEquals("Practitioner", incParm.getSearchParameterTargetType());
        assertFalse(searchContext.hasRevIncludeParameters());

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertTrue(selfUri.contains(queryString));
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testRevIncludeInvalidTargetType() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Organization> resourceType = Organization.class;

        queryParameters.put("_revinclude", Collections.singletonList("Patient:general-practitioner:Practitioner"));
        System.out.println(SearchUtil.parseQueryParameters(resourceType, queryParameters));
    }

    @Test
    public void testRevIncludeUnknownParameterName_lenient() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Organization> resourceType = Organization.class;
        String queryString = "&_revinclude=Patient:bogus";

        // In lenient mode, the unknown parameter should be ignored
        queryParameters.put("_revinclude", Collections.singletonList("Patient:bogus"));
        FHIRSearchContext searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters, true);
        assertNotNull(searchContext);
        assertFalse(searchContext.hasIncludeParameters());

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertFalse(selfUri.contains(queryString));
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testRevIncludeUnknownParameterName_strict() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Organization> resourceType = Organization.class;

        // In strict mode, the query should throw a FHIRSearchException
        queryParameters.put("_revinclude", Collections.singletonList("Patient:bogus"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters, false);
    }

    @Test
    public void testRevIncludeValidTargetType() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Organization> resourceType = Organization.class;

        String queryString = "&_revinclude=Patient:general-practitioner:Organization";

        queryParameters.put("_revinclude", Collections.singletonList("Patient:general-practitioner:Organization"));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters);
        assertNotNull(searchContext);
        assertTrue(searchContext.hasRevIncludeParameters());
        assertEquals(1, searchContext.getRevIncludeParameters().size());
        InclusionParameter revIncParm = searchContext.getRevIncludeParameters().get(0);
        assertEquals("Patient", revIncParm.getJoinResourceType());
        assertEquals("general-practitioner", revIncParm.getSearchParameter());
        assertEquals("Organization", revIncParm.getSearchParameterTargetType());
        assertFalse(searchContext.hasIncludeParameters());

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertTrue(selfUri.contains(queryString));
    }

    @Test
    public void testRevIncludeUnpsecifiedTargetType() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Organization> resourceType = Organization.class;
        String queryString = "&_revinclude=Patient:general-practitioner";

        queryParameters.put("_revinclude", Collections.singletonList("Patient:general-practitioner"));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters);
        assertNotNull(searchContext);
        assertTrue(searchContext.hasRevIncludeParameters());
        assertEquals(1, searchContext.getRevIncludeParameters().size());
        InclusionParameter revIncParm = searchContext.getRevIncludeParameters().get(0);
        assertEquals("Patient", revIncParm.getJoinResourceType());
        assertEquals("general-practitioner", revIncParm.getSearchParameter());
        assertEquals("Organization", revIncParm.getSearchParameterTargetType());
        assertFalse(searchContext.hasIncludeParameters());

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertTrue(selfUri.contains(queryString));
    }

    @Test(expectedExceptions = FHIRSearchException.class)
    public void testRevIncludeInvalidRevIncludeSpecification() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        Class<Organization> resourceType = Organization.class;

        queryParameters.put("_revinclude", Collections.singletonList("Patient:link"));
        SearchUtil.parseQueryParameters(resourceType, queryParameters);
    }

    @Test
    public void testMultiIncludeRevinclude() throws Exception {
        Map<String, List<String>> queryParameters = new HashMap<>();
        FHIRSearchContext searchContext;
        Class<Medication> resourceType = Medication.class;
        String include1 = "&_include=Medication:manufacturer";
        String include2 = "&_include=Medication:ingredient";
        String include3 = "&_revinclude=MedicationDispense:medication";
        String include4 = "&_revinclude=MedicationAdministration:medication";

        List<InclusionParameter> expectedIncludeParms = new ArrayList<>();
        expectedIncludeParms.add(new InclusionParameter("Medication", "manufacturer", "Organization"));
        expectedIncludeParms.add(new InclusionParameter("Medication", "ingredient", "Substance"));
        expectedIncludeParms.add(new InclusionParameter("Medication", "ingredient", "Medication"));

        List<InclusionParameter> expectedRevIncludeParms = new ArrayList<>();
        expectedRevIncludeParms.add(new InclusionParameter("MedicationDispense", "medication", "Medication"));
        expectedRevIncludeParms.add(new InclusionParameter("MedicationAdministration", "medication", "Medication"));

        queryParameters.put("_include", Arrays.asList(new String[] { "Medication:manufacturer", "Medication:ingredient" }));
        queryParameters.put("_revinclude", Arrays.asList(new String[] { "MedicationDispense:medication", "MedicationAdministration:medication" }));
        searchContext = SearchUtil.parseQueryParameters(resourceType, queryParameters);

        assertNotNull(searchContext);
        assertTrue(searchContext.hasIncludeParameters());
        assertEquals(expectedIncludeParms.size(), searchContext.getIncludeParameters().size());
        for (InclusionParameter includeParm : expectedIncludeParms) {
            assertTrue(expectedIncludeParms.contains(includeParm));
        }

        assertTrue(searchContext.hasRevIncludeParameters());

        for (InclusionParameter revIncludeParm : expectedRevIncludeParms) {
            assertTrue(expectedRevIncludeParms.contains(revIncludeParm));
        }

        String selfUri = SearchUtil.buildSearchSelfUri("http://example.com/Patient", searchContext);
        assertTrue(selfUri.contains(include1));
        assertTrue(selfUri.contains(include2));
        
        assertTrue(selfUri.contains(include3));
        assertTrue(selfUri.contains(include4));
    }

}
