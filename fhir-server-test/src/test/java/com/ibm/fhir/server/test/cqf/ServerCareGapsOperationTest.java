/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.server.test.cqf;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.code.BundleType;

public class ServerCareGapsOperationTest extends BaseMeasureOperationTest {

    private static final String TEST_TOPIC = "test";

    @Test
    public void testEvaluatePatientCareGaps() {
        
        Response response =
                getWebTarget().path("/Measure/$care-gaps")
                    .queryParam("periodStart", TEST_PERIOD_START)
                    .queryParam("periodEnd", TEST_PERIOD_END)
                    .queryParam("topic", TEST_TOPIC)
                    .queryParam("subject", TEST_PATIENT_ID)
                    .request().get();
        assertResponse(response, 200);

        Bundle result = response.readEntity(Bundle.class);
        assertNotNull(result, "Null result");
        assertEquals(result.getType(), BundleType.COLLECTION);
        assertEquals(result.getEntry().size(), 1);
        
        MeasureReport report = (MeasureReport) result.getEntry().get(0).getResource();
        assertEquals(report.getSubject().getReference().getValue(), TEST_PATIENT_ID);
        
        Period expectedPeriod = getPeriod(TEST_PERIOD_START, TEST_PERIOD_END);
        
        assertEquals(report.getPeriod(), expectedPeriod);
    }
    
    @Test
    public void testEvaluatePatientCareGapsNoMeasuresFound() {
        
        Response response =
                getWebTarget().path("/Measure/$care-gaps")
                    .queryParam("periodStart", TEST_PERIOD_START)
                    .queryParam("periodEnd", TEST_PERIOD_END)
                    .queryParam("topic", "invalid")
                    .queryParam("subject", TEST_PATIENT_ID)
                    .request().get();
        assertResponse(response, 200);

        Bundle result = response.readEntity(Bundle.class);
        assertNotNull(result, "Null result");
        assertEquals(result.getType(), BundleType.COLLECTION);
        assertEquals(result.getEntry().size(), 0);
    }
}
