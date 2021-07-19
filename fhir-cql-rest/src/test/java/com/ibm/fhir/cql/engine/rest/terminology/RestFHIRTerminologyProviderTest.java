/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.rest.terminology;

import static com.ibm.fhir.cql.engine.util.FHIRClientUtil.urlencode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import com.ibm.fhir.cql.engine.rest.R4RestFHIRTest;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.resource.ValueSet.Expansion;
import com.ibm.fhir.model.resource.ValueSet.Expansion.Contains;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Identifier;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.PublicationStatus;

public class RestFHIRTerminologyProviderTest extends R4RestFHIRTest {

    private static final String TEST_DISPLAY = "Display";
    private static final String TEST_CODE = "425178004";
    private static final String TEST_SYSTEM = "http://snomed.info/sct";
    private static final String TEST_SYSTEM_VERSION = "2013-09";
    RestFHIRTerminologyProvider provider;

    @BeforeMethod
    public void initializeProvider() throws Exception {
        provider = new RestFHIRTerminologyProvider(newClient());
    }

    @Test
    public void resolveByUrlUsingUrlSucceeds() throws Exception {
        ValueSetInfo info = new ValueSetInfo().withId("https://cts.nlm.nih.gov/fhir/ValueSet/1.2.3.4");

        ValueSet response = ValueSet.builder().id("1.2.3.4").status(PublicationStatus.ACTIVE).url(Uri.of(info.getId())).build();

        mockResolveSearchPath(info, response);

        provider.resolveByUrl(info);
    }

    @Test
    public void resolveByUrlUsingIdentifierSucceeds() throws Exception {
        ValueSetInfo info = new ValueSetInfo().withId("urn:oid:1.2.3.4");

        ValueSet response =
                ValueSet.builder().id("1.2.3.4").status(PublicationStatus.ACTIVE).identifier(Identifier.builder().value(com.ibm.fhir.model.type.String.of(info.getId())).build()).build();

        mockResolveSearchPath(info, response);

        provider.resolveByUrl(info);
    }

    @Test
    public void resolveByUrlUsingResourceIdSucceeds() throws Exception {
        ValueSetInfo info = new ValueSetInfo().withId("urn:oid:1.2.3.4");

        ValueSet response = ValueSet.builder().id("1.2.3.4").status(PublicationStatus.ACTIVE).build();

        mockResolveSearchPath(info, response);

        provider.resolveByUrl(info);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void resolveByUrlNoMatchesThrowsException() throws Exception {
        ValueSetInfo info = new ValueSetInfo().withId("urn:oid:1.2.3.4");

        mockResolveSearchPath(info, null);

        provider.resolveByUrl(info);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void nonNullVersionUnsupported() {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("urn:oid:Test");
        info.setVersion("1.0.0.");

        provider.resolveByUrl(info);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void nonNullCodesystemsUnsupported() {
        CodeSystemInfo codeSystem = new CodeSystemInfo();
        codeSystem.setId("SNOMED-CT");
        codeSystem.setVersion("2013-09");

        ValueSetInfo info = new ValueSetInfo();
        info.setId("urn:oid:Test");
        info.getCodeSystems().add(codeSystem);

        provider.resolveByUrl(info);
    }

    @Test
    public void urnOidPrefixIsStripped() throws Exception {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("urn:oid:Test");

        ValueSet valueSet = getSingleCodeValueSet("Test", TEST_SYSTEM, TEST_CODE);

        mockResolveSearchPath(info, valueSet);

        provider.resolveByUrl(info);
        assertEquals(info.getId(), "Test");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void moreThanOneURLSearchResultIsError() throws Exception {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("http://localhost/fhir/ValueSet/1.2.3.4");

        ValueSet firstSet = ValueSet.builder().id("1").status(PublicationStatus.ACTIVE).url(Uri.of(info.getId())).build();

        ValueSet secondSet = ValueSet.builder().id("1").status(PublicationStatus.ACTIVE).url(Uri.of(info.getId())).build();

        mockFhirSearch("/ValueSet?url=" + urlencode(info.getId()), firstSet, secondSet);

        provider.resolveByUrl(info);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void zeroURLSearchResultIsError() throws Exception {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("http://localhost/fhir/ValueSet/1.2.3.4");

        mockResolveSearchPath(info, null);

        provider.resolveByUrl(info);
    }

    @Test
    public void expandOperationReturnsCorrectCodesMoreThanZero() throws Exception {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("urn:oid:Test");

        ValueSet valueSet = getSingleCodeValueSet("Test", TEST_SYSTEM, TEST_CODE);

        mockResolveSearchPath(info, valueSet);

        mockFhirRead("/ValueSet/Test/$expand", valueSet);

        Iterable<Code> codes = provider.expand(info);

        List<Code> list = StreamSupport.stream(codes.spliterator(), false).collect(Collectors.toList());
        assertEquals(list.size(), 1);
        assertEquals(list.get(0).getSystem(), TEST_SYSTEM);
        assertEquals(list.get(0).getCode(), TEST_CODE);
    }

    @Test
    public void inOperationReturnsTrueWhenFhirReturnsTrue() throws Exception {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("urn:oid:Test");

        ValueSet valueSet = getSingleCodeValueSet("Test", TEST_SYSTEM, TEST_CODE);

        mockResolveSearchPath(info, valueSet);

        Code code = new Code();
        code.setSystem(TEST_SYSTEM);
        code.setCode(TEST_CODE);
        code.setDisplay(TEST_DISPLAY);

        Parameters parameters =
                Parameters.builder().parameter(Parameters.Parameter.builder().name(com.ibm.fhir.model.type.String.of("result")).value(com.ibm.fhir.model.type.Boolean.of(true)).build()).build();

        mockFhirRead("/ValueSet/Test/$validate-code?code=" + urlencode(code.getCode()) + "&system=" + urlencode(code.getSystem()), parameters);

        boolean result = provider.in(code, info);
        assertTrue(result);
    }

    @Test
    public void inOperationReturnsFalseWhenFhirReturnsFalse() throws Exception {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("urn:oid:Test");

        ValueSet valueSet = ValueSet.builder().id("Test").status(PublicationStatus.ACTIVE).build();

        mockResolveSearchPath(info, valueSet);

        Code code = new Code();
        code.setSystem(TEST_SYSTEM);
        code.setCode(TEST_CODE);
        code.setDisplay(TEST_DISPLAY);

        Parameters parameters =
                Parameters.builder().parameter(Parameters.Parameter.builder().name(com.ibm.fhir.model.type.String.of("result")).value(com.ibm.fhir.model.type.Boolean.of(false)).build()).build();

        mockFhirRead("/ValueSet/Test/$validate-code?code=" + urlencode(code.getCode()) + "&system=" + urlencode(code.getSystem()), parameters);

        boolean result = provider.in(code, info);
        assertFalse(result);
    }

    @Test
    public void inOperationHandlesNullSystem() throws Exception {
        ValueSetInfo info = new ValueSetInfo();
        info.setId("urn:oid:Test");

        ValueSet valueSet = ValueSet.builder().id("Test").status(PublicationStatus.ACTIVE).build();

        mockResolveSearchPath(info, valueSet);

        Code code = new Code();
        code.setCode(TEST_CODE);
        code.setDisplay(TEST_DISPLAY);

        Parameters parameters =
                Parameters.builder().parameter(Parameters.Parameter.builder().name(com.ibm.fhir.model.type.String.of("result")).value(com.ibm.fhir.model.type.Boolean.of(true)).build()).build();

        mockFhirRead("/ValueSet/Test/$validate-code?code=" + urlencode(code.getCode()), parameters);

        boolean result = provider.in(code, info);
        assertTrue(result);
    }

    @Test
    public void lookupOperationSuccess() throws Exception {
        CodeSystemInfo info = new CodeSystemInfo();
        info.setId(TEST_SYSTEM);
        info.setVersion(TEST_SYSTEM_VERSION);

        Code code = new Code();
        code.setCode(TEST_CODE);
        code.setSystem(TEST_SYSTEM);
        code.setDisplay(TEST_DISPLAY);

        Parameters parameters =
                Parameters.builder().parameter(Parameter.builder().name(com.ibm.fhir.model.type.Code.of("name")).value(com.ibm.fhir.model.type.String.of(code.getCode())).build()).parameter(Parameter.builder().name(com.ibm.fhir.model.type.Code.of("version")).value(code.getVersion() != null
                        ? com.ibm.fhir.model.type.String.of(code.getVersion())
                        : null).build()).parameter(Parameter.builder().name(com.ibm.fhir.model.type.Code.of("display")).value(code.getDisplay() != null
                                ? com.ibm.fhir.model.type.String.of(code.getDisplay()) : null).build()).build();

        mockFhirInteraction("/CodeSystem/$lookup?code=" + urlencode(code.getCode()) + "&system=" + urlencode(code.getSystem()), parameters);

        Code result = provider.lookup(code, info);
        assertNotNull(result);
        assertEquals(result.getSystem(), code.getSystem());
        assertEquals(result.getCode(), code.getCode());
        assertEquals(result.getDisplay(), code.getDisplay());
    }

    protected void mockResolveSearchPath(ValueSetInfo info, ValueSet valueSet) throws Exception {
        if (valueSet != null && valueSet.getUrl() != null) {
            mockFhirSearch("/ValueSet?url=" + urlencode(info.getId()), valueSet);
        } else {
            mockFhirSearch("/ValueSet?url=" + urlencode(info.getId()));
        }

        if (valueSet != null && valueSet.getIdentifier().size() > 0) {
            mockFhirSearch("/ValueSet?identifier=" + urlencode(info.getId()), valueSet);
        } else {
            mockFhirSearch("/ValueSet?identifier=" + urlencode(info.getId()));
        }

        if (valueSet != null) {
            mockFhirRead("/ValueSet/" + valueSet.getId(), valueSet);
        } else {
            String[] parts = info.getId().split("[:/]");
            String expectedId = parts[parts.length - 1];
            mockNotFound("/ValueSet/" + expectedId);
        }
    }

    private ValueSet getSingleCodeValueSet(String id, String system, String code) {
        ValueSet valueSet =
                ValueSet.builder().id(id).status(PublicationStatus.ACTIVE).expansion(Expansion.builder().timestamp(DateTime.now()).contains(Contains.builder().system(Uri.of(system)).code(com.ibm.fhir.model.type.Code.of(code)).build()).build()).build();
        return valueSet;
    }
}
