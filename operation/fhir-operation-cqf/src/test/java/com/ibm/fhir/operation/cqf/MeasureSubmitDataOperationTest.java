/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import static com.ibm.fhir.cql.helpers.ModelHelper.*;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.reference;
import static org.testng.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.MockedStatic;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Encounter;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.HumanName;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.EncounterStatus;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class MeasureSubmitDataOperationTest {
    MeasureSubmitDataOperation op;
    
    @BeforeMethod
    public void setup() {
        op = new MeasureSubmitDataOperation();
    }
    
    @Test
    public void testInstanceExecutionWithResources() throws Exception {
        
        MeasureReport.Builder builder = MeasureReport.builder()
                .id("measure-report-1");
        builder.setValidating(false);
        
        Patient p = Patient.builder()
                .id(UUID.randomUUID().toString())
                .name(HumanName.builder().family(fhirstring("Doe")).given(fhirstring("John")).build())
                .build();
        
        Encounter e = Encounter.builder()
                //.id(UUID.randomUUID().toString())
                .status(EncounterStatus.FINISHED)
                .subject( reference( p ) )
                .clazz( coding("wellness") )
                .build();
        
        MeasureReport report = builder.build();
        List<Resource> resources = Arrays.asList(p,e);
        
        runTest(report, resources);
    }
    
    @Test
    public void testInstanceExecutionWithoutResources() throws Exception {
        
        MeasureReport.Builder builder = MeasureReport.builder()
                .id("measure-report-1");
        builder.setValidating(false);
        
        MeasureReport report = builder.build();
        List<Resource> resources = Collections.emptyList();
        
        runTest(report, resources);
    }    

    protected Parameters runTest(MeasureReport report, List<Resource> resources) throws Exception, FHIROperationException {
        Parameters inParams = createInParameters(report, resources);
        Bundle responseBundle = Bundle.builder()
                .type(BundleType.COLLECTION)
                .total(UnsignedInt.of(0))
                .build();
        
        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);
            
            FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
            when( resourceHelper.doBundle(any(Bundle.class), anyBoolean())).thenReturn(responseBundle);
            
            Parameters outParams = op.doInvoke(FHIROperationContext.createInstanceOperationContext(), null, null, null, inParams, resourceHelper);
            assertNotNull(outParams);
            return outParams;
        }
    }

    public Parameters createInParameters(MeasureReport report, List<Resource> resources) {
        Parameters.Builder inParams = Parameters.builder()
                .parameter(Parameter.builder()
                    .name(fhirstring(MeasureSubmitDataOperation.PARAM_IN_MEASURE_REPORT))
                    .resource(report)
                    .build());
        if( resources != null ) {
            resources.forEach( r -> {
                inParams.parameter(Parameter.builder()
                        .name(fhirstring(MeasureSubmitDataOperation.PARAM_IN_RESOURCE))
                        .resource(r)
                        .build());
            });
        }
        return inParams.build();
    }
}
