/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.helpers.ModelHelper.bundle;
import static com.ibm.fhir.cql.helpers.ModelHelper.coding;
import static com.ibm.fhir.cql.helpers.ModelHelper.concept;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.valueset;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.testng.annotations.Test;
import org.mockito.MockedStatic;

import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.model.resource.Encounter;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Procedure;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.code.EncounterStatus;
import com.ibm.fhir.model.type.code.ProcedureStatus;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class LibraryEvaluateOperationTest extends BaseCqlOperationTest<LibraryEvaluateOperation> {

    @Override
    protected LibraryEvaluateOperation getOperation() {
        return new LibraryEvaluateOperation();
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

        List<Library> fhirLibraries = TestHelper.getBundleResources("EXM74-10.2.000-request.json", Library.class);
        Library primaryLibrary = fhirLibraries.get(0);

        Parameters.Parameter pSubject = Parameters.Parameter.builder().name(fhirstring("subject")).value(fhirstring("Patient/" + patient.getId())).build();
        Parameters.Parameter pLibrary = Parameters.Parameter.builder().name(fhirstring("library")).value(primaryLibrary.getUrl()).build();
        Parameters.Parameter pExpression = Parameters.Parameter.builder().name(fhirstring("expression")).value(fhirstring("Initial Population")).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pExpression, pLibrary).build();
        ParameterMap paramMap = new ParameterMap(parameters);

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> asResult(patient));

        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString(), any())).thenReturn(bundle(encounter));
        when(resourceHelper.doSearch(eq("Procedure"), anyString(), anyString(), any(), anyString(), any())).thenReturn(bundle(procedure));

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001", ValueSet.class)).thenReturn(valueset(codesystem, encounterCode));
            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.125.12.1002", ValueSet.class)).thenReturn(valueset(codesystem, procedureCode));

            Parameters result = getOperation().doEvaluation(resourceHelper, paramMap, fhirLibraries);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            assertTrue(resultMap.containsKey("return"));

            Parameter returnParam = resultMap.getSingletonParameter("return");
            assertEquals(returnParam.getPart().size(), 1);
        }
    }

    @Test
    public void testDoEvaluationSupplementalDataElements() throws Exception {
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

        List<Library> fhirLibraries = TestHelper.getBundleResources("EXM74-10.2.000-request.json", Library.class);

        // reorder so that the SDE library is at the front and the Measure lib is gone
        Library primaryLibrary = fhirLibraries.stream().filter(l -> l.getName().getValue().equals("SupplementalDataElements")).reduce((x, y) -> {
            throw new IllegalArgumentException("Found more than one matching library");
        }).get();
        fhirLibraries.remove(primaryLibrary);
        fhirLibraries.set(0, primaryLibrary);

        Parameters.Parameter pSubject = Parameters.Parameter.builder().name(fhirstring("subject")).value(fhirstring("Patient/" + patient.getId())).build();
        Parameters.Parameter pLibrary = Parameters.Parameter.builder().name(fhirstring("library")).value(primaryLibrary.getUrl()).build();
        Parameters parameters = Parameters.builder().parameter(pSubject, pLibrary).build();
        ParameterMap paramMap = new ParameterMap(parameters);

        FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
        when(resourceHelper.doRead(eq("Patient"), anyString(), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> asResult(patient));

        when(resourceHelper.doSearch(eq("Encounter"), anyString(), anyString(), any(), anyString(), any())).thenReturn(bundle(encounter));
        when(resourceHelper.doSearch(eq("Procedure"), anyString(), anyString(), any(), anyString(), any())).thenReturn(bundle(procedure));

        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.101.12.1001", ValueSet.class)).thenReturn(valueset(codesystem, encounterCode));
            when(mockRegistry.getResource("http://cts.nlm.nih.gov/fhir/ValueSet/2.16.840.1.113883.3.464.1003.125.12.1002", ValueSet.class)).thenReturn(valueset(codesystem, procedureCode));

            Parameters result = getOperation().doEvaluation(resourceHelper, paramMap, fhirLibraries);
            assertNotNull(result);

            ParameterMap resultMap = new ParameterMap(result);
            assertTrue(resultMap.containsKey("return"));

            Parameter retParams = resultMap.getSingletonParameter("return");
            assertEquals(retParams.getPart().size(), 5);
        }
    }
}
