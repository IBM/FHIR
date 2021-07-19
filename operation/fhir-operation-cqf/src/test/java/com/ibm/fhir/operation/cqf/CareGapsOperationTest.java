/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import static com.ibm.fhir.cql.helpers.ModelHelper.bundle;
import static com.ibm.fhir.cql.helpers.ModelHelper.canonical;
import static com.ibm.fhir.cql.helpers.ModelHelper.coding;
import static com.ibm.fhir.cql.helpers.ModelHelper.concept;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.valueset;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.MockedStatic;

import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Encounter;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Procedure;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.code.EncounterStatus;
import com.ibm.fhir.model.type.code.ProcedureStatus;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class CareGapsOperationTest {

    private CareGapsOperation operation;

    @BeforeMethod
    public void setup() {
        operation = new CareGapsOperation();
    }

    @Test
    public void testDoEvaluationEXM74() throws Exception {
        Patient patient = (Patient) TestHelper.getTestResource("Patient.json");

        String codesystem = "http://snomed.ct/info";
        String encounterCode = "office-visit";
        Coding reason = coding(codesystem, encounterCode);
        Encounter encounter =
                Encounter.builder().reasonCode(concept(reason)).status(EncounterStatus.FINISHED).clazz(reason).period(Period.builder().start(DateTime.now()).end(DateTime.now()).build()).build();

        String procedureCode = "fluoride-application";
        Coding type = coding(codesystem, procedureCode);
        Procedure procedure = Procedure.builder().subject(Reference.builder().reference(fhirstring("Patient/"
                + patient.getId())).build()).code(concept(type)).status(ProcedureStatus.COMPLETED).performed(DateTime.of("2019-03-14")).build();

        List<Measure> measures = TestHelper.getBundleResources("EXM74-10.2.000-request.json", Measure.class);
        assertEquals(measures.size(), 1);
        Measure measure = measures.get(0);
        String measureURL = canonical(measure.getUrl(), measure.getVersion()).getValue();

        List<Library> fhirLibraries = TestHelper.getBundleResources("EXM74-10.2.000-request.json", Library.class);

        LocalDate periodStart = LocalDate.of(2000, 1, 1);
        LocalDate periodEnd = periodStart.plus(1, ChronoUnit.YEARS);

        Parameters.Parameter pPeriodStart =
                Parameters.Parameter.builder().name(fhirstring(CareGapsOperation.PARAM_IN_PERIOD_START)).value(Date.of(periodStart)).build();
        Parameters.Parameter pPeriodEnd =
                Parameters.Parameter.builder().name(fhirstring(CareGapsOperation.PARAM_IN_PERIOD_END)).value(Date.of(periodEnd)).build();
        Parameters.Parameter pTopic = Parameters.Parameter.builder().name(fhirstring(CareGapsOperation.PARAM_IN_TOPIC)).value(fhirstring(measureURL)).build();
        Parameters.Parameter pSubject =
                Parameters.Parameter.builder().name(fhirstring(CareGapsOperation.PARAM_IN_SUBJECT)).value(fhirstring("Patient/" + patient.getId())).build();

        Parameters parameters = Parameters.builder().parameter(pPeriodStart, pPeriodEnd, pTopic, pSubject).build();

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> asResult(patient));

        when(resourceHelper.doSearch(eq("Encounter"), nullable(String.class), nullable(String.class), any(), nullable(String.class), nullable(Resource.class))).thenReturn(bundle(encounter));
        when(resourceHelper.doSearch(eq("Procedure"), nullable(String.class), nullable(String.class), any(), nullable(String.class), nullable(Resource.class))).thenReturn(bundle(procedure));
        when(resourceHelper.doSearch(eq("Measure"), nullable(String.class), nullable(String.class), any(), nullable(String.class), nullable(Resource.class))).thenReturn(bundle(measure));

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001", ValueSet.class)).thenReturn(valueset(codesystem, encounterCode));
            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.125.12.1002", ValueSet.class)).thenReturn(valueset(codesystem, procedureCode));

            when(mockRegistry.getResource(measureURL, Measure.class)).thenReturn(measure);
            fhirLibraries.stream().forEach(l -> when(mockRegistry.getResource(canonical(l.getUrl(), l.getVersion()).getValue(), Library.class)).thenReturn(l));

            Parameters result =
                    operation.doInvoke(FHIROperationContext.createResourceTypeOperationContext(), Measure.class, null, null, parameters, resourceHelper);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            Bundle bundle = (Bundle) resultMap.getSingletonParameter(CareGapsOperation.PARAM_OUT_RETURN).getResource();
            assertSame(bundle.getTotal().getValue(), 1);
        }
    }

    @SuppressWarnings("unchecked")
    protected SingleResourceResult<? extends Resource> asResult(Resource patient) {
        SingleResourceResult<Resource> result = (SingleResourceResult<Resource>) mock(SingleResourceResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.getResource()).thenReturn(patient);
        return result;
    }
}
