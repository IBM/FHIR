/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.ecqm.r4;

import static com.ibm.fhir.cql.helpers.ModelHelper.canonical;
import static com.ibm.fhir.cql.helpers.ModelHelper.coding;
import static com.ibm.fhir.cql.helpers.ModelHelper.concept;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhircode;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhiruri;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.testng.annotations.Test;

import com.ibm.fhir.cql.engine.model.FHIRModelResolver;
import com.ibm.fhir.cql.helpers.DataProviderFactory;
import com.ibm.fhir.ecqm.BaseMeasureEvaluationTest;
import com.ibm.fhir.ecqm.common.MeasurePopulationType;
import com.ibm.fhir.ecqm.common.MeasureReportType;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.Observation;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Attachment;
import com.ibm.fhir.model.type.Base64Binary;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.Expression;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.HumanName;
import com.ibm.fhir.model.type.code.PublicationStatus;

public class R4MeasureEvaluationTest extends BaseMeasureEvaluationTest {
    @Test
    public void testCohortMeasureEvaluation() throws Exception {
        Patient patient = john_doe();
        
        RetrieveProvider retrieveProvider = mock(RetrieveProvider.class);
        when(retrieveProvider.retrieve(eq("Patient"), anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Arrays.asList(patient));
        
        String cql = skeleton_cql() + sde_race() + "define InitialPopulation: 'Doe' in Patient.name.family\n";
        
        Measure.Builder measure = cohort_measure();
        
        MeasureReport report = runTest(cql, patient, measure, retrieveProvider);
        checkEvidence(patient, report);
    }
    
    @Test
    public void testProportionMeasureEvaluation() throws Exception {
        Patient patient = john_doe();
        
        RetrieveProvider retrieveProvider = mock(RetrieveProvider.class);
        when(retrieveProvider.retrieve(eq("Patient"), anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Arrays.asList(patient));
        
        String cql = skeleton_cql() + sde_race() + 
                "define InitialPopulation: 'Doe' in Patient.name.family\n" +
                "define Denominator: 'John' in Patient.name.given\n" + 
                "define Numerator: Patient.birthDate > @1970-01-01\n";
        
        Measure.Builder measure = proportion_measure();
        
        MeasureReport report = runTest(cql, patient, measure, retrieveProvider);
        checkEvidence(patient, report);
    }
    
    @Test
    public void testContinousVariableMeasureEvaluation() throws Exception {
        Patient patient = john_doe();
        
        RetrieveProvider retrieveProvider = mock(RetrieveProvider.class);
        when(retrieveProvider.retrieve(eq("Patient"), anyString(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Arrays.asList(patient));
        
        String cql = skeleton_cql() + sde_race() + 
                "define InitialPopulation: 'Doe' in Patient.name.family\n" + 
                "define MeasurePopulation: Patient.birthDate > @1970-01-01\n";
        
        Measure.Builder measure = continuous_variable_measure();
        
        MeasureReport report = runTest(cql, patient, measure, retrieveProvider);
        checkEvidence(patient, report);
    }

    private MeasureReport runTest(String cql, Patient patient, Measure.Builder measure, RetrieveProvider retrieveProvider)
            throws Exception {
        Interval measurementPeriod = measurementPeriod("2000-01-01", "2001-01-01");
        
        Library primaryLibrary = library(cql);
        measure.library(canonical("Library/" + primaryLibrary.getId()));
        
        List<org.cqframework.cql.elm.execution.Library> cqlLibraries = translate(cql);
        LibraryLoader ll = new InMemoryLibraryLoader(cqlLibraries);
        
        
        Context context = new Context(cqlLibraries.get(0));
        Map<String,DataProvider> dataProvider = DataProviderFactory.createDataProviders(retrieveProvider);
        dataProvider.entrySet().forEach( dp -> {
            context.registerDataProvider( dp.getKey(), dp.getValue() );
        });
        context.registerLibraryLoader(ll);
        
        R4MeasureEvaluation<Patient> evaluation = new R4MeasureEvaluation<>(context, measure.build(), measurementPeriod, FHIRModelResolver.RESOURCE_PACKAGE_NAME, r -> r.getId() , patient.getId());
        MeasureReport.Builder report = evaluation.evaluate(MeasureReportType.INDIVIDUAL);
        assertNotNull(report);
        
        System.out.println(report.build().toString());
        
        // Simulate sending it across the wire
        MeasureReport inflated = (MeasureReport) FHIRParser
                .parser(Format.JSON)
                .parse(new ByteArrayInputStream(report.build().toString().getBytes()));
        return inflated;
    }
    
    private void checkEvidence(Patient patient, MeasureReport report) {
        Map<String,Resource> contained = report.getContained().stream().collect(Collectors.toMap(r -> r.getClass().getSimpleName(), Function.identity()));
        
        assertEquals( contained.size(), 1);
        
        Observation obs = (Observation) contained.get("Observation");
        assertNotNull(obs);
        assertEquals( ((CodeableConcept)obs.getValue()).getCoding().get(0).getCode().getValue(), OMB_CATEGORY_RACE_BLACK );
    }

    private Measure.Builder cohort_measure() {
        
        Measure.Builder measure = measure("cohort")
                .group(Measure.Group.builder()
                    .population(
                        population( MeasurePopulationType.INITIALPOPULATION, "InitialPopulation")
                        )
                    .build())
                .supplementalData(supplementalData("sde-race", "SDE Race"));
        
        return measure;
    }
    
    private Measure.Builder proportion_measure() {
        
        Measure.Builder measure = measure("proportion")
                .group(Measure.Group.builder()
                    .population(
                        population( MeasurePopulationType.INITIALPOPULATION, "InitialPopulation"),
                        population( MeasurePopulationType.DENOMINATOR, "Denominator"),
                        population( MeasurePopulationType.NUMERATOR, "Numerator")
                        )
                    .build())
                .supplementalData(supplementalData("sde-race", "SDE Race"));
        
        return measure;
    }
    
    private Measure.Builder continuous_variable_measure() {
        
        Measure.Builder measure = measure("continuous-variable")
                .group(Measure.Group.builder()
                    .population(
                        population(MeasurePopulationType.INITIALPOPULATION, "InitialPopulation"),
                        population(MeasurePopulationType.MEASUREPOPULATION, "MeasurePopulation")                
                            ).build())
                .supplementalData(supplementalData("sde-race", "SDE Race"));
        
        return measure;
    }

    private Measure.Group.Population population(MeasurePopulationType measurePopulationType, String expression) {
        return Measure.Group.Population.builder()
            .code(concept(measurePopulationType.toCode()))
            .criteria(Expression.builder()
                .language(fhircode("text/cql"))
                .expression(fhirstring(expression))
                .build())
            .build();
    }
    
    private Measure.SupplementalData supplementalData(String text, String expression) {
        return Measure.SupplementalData.builder()
            .code(CodeableConcept.builder()
                .text(fhirstring(text))
                .build())
            .criteria(Expression.builder()
                .language(Code.of("text/cql"))
                .expression(fhirstring(expression))
                .build())
            .build();
    }

    private Measure.Builder measure(String scoring) {
        Measure.Builder measure = Measure.builder();
        measure.status(PublicationStatus.ACTIVE);
        measure.name(fhirstring("Test"));
        measure.version(fhirstring("1.0.0"));
        measure.url(fhiruri("http://test.com/fhir/Measure/Test"));
        measure.scoring(concept(scoring));
        return measure;
    }

    private Library library(String cql) {
        Library.Builder library = Library.builder();
        library.id("library-Test");
        library.status(PublicationStatus.ACTIVE);
        library.name(fhirstring("Test"));
        library.version(fhirstring("1.0.0"));
        library.url(fhiruri("http://test.com/fhir/Library/Test"));
        library.type(concept("logic-library"));
        library.content(Attachment.builder()
            .contentType(Code.of("text/cql"))
            .data(Base64Binary.of(cql.getBytes()))
            .build());
        return library.build();
    }
    
    private Patient john_doe() {
        Patient.Builder patient = Patient.builder();
        patient.id("test-patient");
        patient.name(HumanName.builder().family(fhirstring("Doe")).given(fhirstring("John")).build());
        patient.birthDate(Date.of("1969-08-09"));
        
        /**
         * Retrieve the coding from an extension that that looks like the following...
         * 
         * {
         *   "url": "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race",
         *   "extension": [ {
         *     "url": "ombCategory",
         *     "valueCoding": {
         *       "system": "urn:oid:2.16.840.1.113883.6.238",
         *       "code": "2054-5",
         *       "display": "Black or African American"
         *     }
         *   } ]
         * }
         */
        
        Extension usCoreRace = Extension.builder()
            .url(EXT_URL_US_CORE_RACE)
            .extension(Extension.builder()
                .url(OMB_CATEGORY)
                .value( coding(URL_SYSTEM_RACE, OMB_CATEGORY_RACE_BLACK, BLACK_OR_AFRICAN_AMERICAN) )
                .build()
                )
            .build();
        patient.extension(usCoreRace);
        
        return patient.build();
    }
}
