/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.helpers.ModelHelper.bundle;
import static com.ibm.fhir.cql.helpers.ModelHelper.coding;
import static com.ibm.fhir.cql.helpers.ModelHelper.concept;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirboolean;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import javax.ws.rs.core.MultivaluedMap;

import org.testng.annotations.Test;
import org.mockito.MockedStatic;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.cql.helpers.LibraryHelper;
import com.ibm.fhir.cql.helpers.ModelHelper;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Encounter;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.code.EncounterStatus;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class CqlOperationTest extends BaseCqlOperationTest<CqlOperation> {

    @Override
    protected CqlOperation getOperation() {
        return new CqlOperation();
    }

    @Test
    public void testInlineExpression() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter =
                Encounter.builder().reasonCode(concept(reason)).status(EncounterStatus.FINISHED).clazz(reason).period(Period.builder().start(DateTime.now()).end(DateTime.now()).build()).build();

        String expression = "[Encounter] e where e.status = 'finished'";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pDebug = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DEBUG)).value(fhirboolean(true)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary, pDebug).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString(), any())).thenReturn(bundle(encounter));

        // Library fhirHelpers = TestHelper.getTestLibraryResource("FHIRHelpers-4.0.1.json");

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            // when(mockRegistry.getResource(javastring(fhirHelpers.getUrl()), Library.class)).thenReturn(fhirHelpers);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext();
            Parameters result = getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            assertNotNull(resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_RETURN));

        }
    }

    @Test
    public void testInlineExpressionWithIncludes() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);

        Encounter encounter =
                Encounter.builder().reasonCode(concept(reason)).status(EncounterStatus.FINISHED).clazz(reason).period(Period.builder().start(DateTime.now()).end(DateTime.now()).build()).build();

        Library fhirHelpers = TestHelper.getTestLibraryResource("FHIRHelpers-4.0.1.json");

        Library.Builder builder = TestHelper.buildBasicLibrary("FHIR-ModelInfo-4.0.1", "http://hl7.org/fhir/Library/FHIR-ModelInfo", "FHIR-ModelInfo", "4.0.1");
        builder.type(ModelHelper.concept(Constants.HL7_TERMINOLOGY_LIBRARY_TYPE, Constants.LIBRARY_TYPE_MODEL_DEFINITION));
        Library modelInfo = builder.build();

        String expression = "[Encounter] e where e.status = 'finished'";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pExpression = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pIncludeFHIRHelpers =
                Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_LIBRARY_URL)).value(Canonical.of(LibraryHelper.canonicalUrl(fhirHelpers))).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_LIBRARY)).part(pIncludeFHIRHelpers).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pExpression, pLibrary).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString(), any())).thenReturn(bundle(encounter));

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            Canonical canonical;
            canonical = ModelHelper.canonical(fhirHelpers.getUrl(), fhirHelpers.getVersion());
            when(mockRegistry.getResource(canonical.getValue(), Library.class)).thenReturn(fhirHelpers);

            canonical = ModelHelper.canonical(modelInfo.getUrl(), modelInfo.getVersion());
            when(mockRegistry.getResource(canonical.getValue(), Library.class)).thenReturn(modelInfo);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext();
            getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper);
        }
    }

    @Test
    public void testInlineExpressionInvalid() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String expression = "[NotAResource]";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> asResult(patient));

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext();
            getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper);
            fail("Missing expected Exception");
        } catch (FHIROperationException fex) {
            assertNotNull(fex.getIssues());
            assertEquals(fex.getIssues().size(), 1);
            assertEquals(fex.getIssues().get(0).getCode(), IssueType.INVALID);
            System.out.println(fex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInlineExpressionWithDebug() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String expression = "[Condition]";

        Parameter pSubject = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();
        Parameter pLibrary = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_EXPRESSION)).value(fhirstring(expression)).build();
        Parameter pDebug = Parameter.builder().name(fhirstring(CqlOperation.PARAM_IN_DEBUG)).value(fhirboolean(true)).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary, pDebug).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> asResult(patient));
        when(resourceHelper.doSearch(eq("Condition"), anyString(), anyString(), any(MultivaluedMap.class), anyString(), any(Resource.class))).thenReturn(ModelHelper.bundle());

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            FHIROperationContext ctx = FHIROperationContext.createSystemOperationContext();
            Parameters result = getOperation().doInvoke(ctx, null, null, null, parameters, resourceHelper);

            assertNotNull(result);
            System.out.println(result.toString());
            ParameterMap resultMap = new ParameterMap(result);
            resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_RETURN);
            resultMap.getSingletonParameter(CqlOperation.PARAM_OUT_DEBUG_RESULT);
        }
    }
}
