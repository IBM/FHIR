/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.SearchParameter;
import com.ibm.fhir.model.type.code.SearchParamType;

public class SearchParameterResolverTest {
    
    SearchParameterResolver resolver;
    
    @BeforeMethod
    public void setUp() {
        resolver = new SearchParameterResolver();
    }
    
    @Test
    public void testResolveConditionCode() throws Exception {
        SearchParameter param = resolver.getSearchParameterDefinition("Condition", "code", SearchParamType.TOKEN);
        assertNotNull(param, "Could not resolve search parameter for Condition.code");
    }
    
    @Test
    public void testResolveConditionSubject() throws Exception {
        SearchParameter param = resolver.getSearchParameterDefinition("Condition", "subject");
        assertNotNull(param, "Could not resolve search parameter for Condition.subject");
    }
    
    @Test
    public void testResolvePatientID() throws Exception {
        SearchParameter param = resolver.getSearchParameterDefinition("Patient", "id");
        assertNotNull(param, "Could not resolve search parameter for Patient.id");
    }
    
    @Test
    public void testResolveMedicationRequestMedication() throws Exception {
        SearchParameter param = resolver.getSearchParameterDefinition("MedicationRequest", "medication");
        assertNotNull(param, "Could not resolve search parameter for MedicationRequest.medication");
    }
    
    @Test
    public void testResolveAllergyIntoleranceCode() throws Exception {
        SearchParameter param = resolver.getSearchParameterDefinition("AllergyIntolerance", "code");
        assertNotNull(param, "Could not resolve search parameter for MedicationRequest.medication");
    }
}
