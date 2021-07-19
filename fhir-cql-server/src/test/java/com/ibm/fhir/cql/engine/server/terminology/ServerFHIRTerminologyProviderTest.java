/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.server.terminology;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhirboolean;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhircode;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.resource.ValueSet.Builder;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.PublicationStatus;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.term.service.FHIRTermService;
import com.ibm.fhir.term.service.LookupOutcome;
import com.ibm.fhir.term.service.ValidationOutcome;
import com.ibm.fhir.term.util.ValueSetSupport;

public class ServerFHIRTerminologyProviderTest {

    private static final String TEST_SYSTEM_ID = "http://snomed.info/sct";

    private static final String TEST_VALUE_SET_ID = "7.8.9";
    private static final String TEST_VALUE_SET_URL = "http://localhost/fhir/ValueSet/7.8.9";

    private MockedStatic<FHIRTermService> staticTermService;
    private MockedStatic<ValueSetSupport> staticValueSetSupport;
    private FHIRTermService termService;

    private ServerFHIRTerminologyProvider termProvider;
    private FHIRResourceHelpers resourceHelper;
    
    @BeforeMethod
    public void setup() {
        termService = Mockito.mock(FHIRTermService.class);

        staticTermService = Mockito.mockStatic(FHIRTermService.class);
        staticTermService.when(FHIRTermService::getInstance).thenReturn(termService);

        staticValueSetSupport = Mockito.mockStatic(ValueSetSupport.class);

        resourceHelper = mock(FHIRResourceHelpers.class);
        termProvider = new ServerFHIRTerminologyProvider(resourceHelper);
    }

    @AfterMethod
    public void cleanup() {
        staticTermService.close();
        staticValueSetSupport.close();
    }

    @Test
    public void testInSuccess() {
        ValueSetInfo info = new ValueSetInfo();
        info.withId(TEST_VALUE_SET_URL);

        ValueSet valueSet = getBaseValueSet(info).build();
        staticValueSetSupport.when(() -> ValueSetSupport.getValueSet(info.getId())).thenReturn(valueSet);

        ValidationOutcome outcome = mock(ValidationOutcome.class);
        when(outcome.getResult()).thenReturn(fhirboolean(true));

        when(termService.validateCode(any(ValueSet.class), any(com.ibm.fhir.model.type.Code.class))).thenReturn(outcome);

        assertTrue(termProvider.in(new Code().withCode("123"), info));
    }

    @Test
    public void testExpandSuccess() {
        ValueSetInfo info = new ValueSetInfo();
        info.withId(TEST_VALUE_SET_URL);

        Set<String> expected = Arrays.asList("123", "456", "789").stream().collect(Collectors.toSet());

        ValueSet.Builder valueSetBuilder = getBaseValueSet(info);
        addExpansion(valueSetBuilder, TEST_SYSTEM_ID, expected);

        ValueSet valueSet = valueSetBuilder.build();

        staticValueSetSupport.when(() -> ValueSetSupport.getValueSet(info.getId())).thenReturn(valueSet);
        when(termService.expand(any(ValueSet.class))).thenReturn(valueSet);

        Iterable<Code> codes = termProvider.expand(info);
        assertNotNull(codes);

        Set<String> actual = new HashSet<String>();
        codes.forEach(code -> actual.add(code.getCode()));
        assertEquals(actual, expected);
    }

    @Test
    public void testLookupWithoutVersionSuccess() {

        CodeSystemInfo info = new CodeSystemInfo().withId(TEST_SYSTEM_ID);
        Code code = new Code().withCode("123");

        LookupOutcome outcome = mock(LookupOutcome.class);
        when(outcome.getDisplay()).thenReturn(fhirstring("test"));

        when(termService.lookup(eq(Uri.of(TEST_SYSTEM_ID)), any(), any(com.ibm.fhir.model.type.Code.class))).thenReturn(outcome);

        Code actual = termProvider.lookup(code, info);
        assertNotNull(actual);
        assertEquals(actual.getDisplay(), "test");
    }
    
    @Test
    public void testResolveByUrlURNOID() throws Exception {
        runResolveByIdTest(Constants.URN_OID + TEST_VALUE_SET_ID, true, true);
    }
    
    @Test
    public void testResolveByUrlURNUUID() throws Exception {
        runResolveByIdTest(Constants.URN_UUID  + TEST_VALUE_SET_ID, true, true);
    }
    
    @Test
    public void testResolveByUrlURNValidFHIRID() throws Exception {
        runResolveByIdTest(TEST_VALUE_SET_ID, true, true);
    }
    
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testResolveByUrlURNInvalidFHIRID() throws Exception {
        runResolveByIdTest("***", false, false);
    }
    
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testResolveByUrlURNMissingFHIRID() throws Exception {
        runResolveByIdTest("9999", false, false);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void runResolveByIdTest(String idInCQL, boolean matchByURL, boolean matchByID) throws Exception {
        ValueSetInfo info = new ValueSetInfo().withId(idInCQL);
        
        Set<String> expected = Arrays.asList("123", "456", "789").stream().collect(Collectors.toSet());

        ValueSet.Builder valueSetBuilder = getBaseValueSet(info);
        addExpansion(valueSetBuilder, TEST_SYSTEM_ID, expected);

        ValueSet valueSet = valueSetBuilder.build();

        if( matchByURL ) {
            staticValueSetSupport.when(() -> ValueSetSupport.getValueSet(info.getId())).thenReturn(valueSet);
        }
        
        SingleResourceResult result = mock(SingleResourceResult.class);
        if( matchByID ) {
            when(result.isSuccess()).thenReturn(true);
            when(result.getResource()).thenReturn(valueSet);
            when(resourceHelper.doRead("ValueSet", TEST_SYSTEM_ID, false, false, null)).thenReturn(result);
        } else { 
            when(result.isSuccess()).thenReturn(false);
            when(resourceHelper.doRead("ValueSet", idInCQL, false, false, null)).thenReturn(result);
        }
        
        termProvider.resolveByUrl(info);
    }

    private Builder getBaseValueSet(ValueSetInfo info) {
        return ValueSet.builder().id(TEST_VALUE_SET_ID).status(PublicationStatus.ACTIVE).url(Uri.of(info.getId()));
    }

    private void addExpansion(ValueSet.Builder valueSet, String system, Set<String> codes) {
        ValueSet.Expansion.Builder expansion = ValueSet.Expansion.builder();
        expansion.timestamp(DateTime.now());
        for (String code : codes) {
            ValueSet.Expansion.Contains.Builder contains = ValueSet.Expansion.Contains.builder();
            contains.system(Uri.of(system));
            contains.code(fhircode(code));
            expansion.contains(contains.build());
        }
        valueSet.expansion(expansion.build());
    }
}
