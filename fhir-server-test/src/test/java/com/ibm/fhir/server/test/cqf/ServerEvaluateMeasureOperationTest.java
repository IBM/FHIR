/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.server.test.cqf;

import static org.testng.Assert.assertEquals;

import java.io.StringReader;

import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.type.Period;

public class ServerEvaluateMeasureOperationTest extends BaseMeasureOperationTest {

    @Test
    public void testEvaluatePatientMeasureResourceType() throws Exception {
        Response response =
                getWebTarget().path("/Measure/$evaluate-measure")
                    .queryParam("periodStart", TEST_PERIOD_START)
                    .queryParam("periodEnd", TEST_PERIOD_END)
                    .queryParam("measure", TEST_MEASURE_URL)
                    .queryParam("subject", TEST_PATIENT_ID)
                    .request().get();
        assertResponse(response, 200);

        String responseBody = response.readEntity(String.class);
        //System.out.println(responseBody);
        MeasureReport report = FHIRParser.parser(Format.JSON).parse(new StringReader(responseBody));
        assertEquals(report.getSubject().getReference().getValue(), TEST_PATIENT_ID);
    }
    
    @Test
    public void testEvaluatePatientMeasureInstance() throws Exception {
        Response response =
                getWebTarget().path("/Measure/{measureId}/$evaluate-measure")
                    .resolveTemplate("measureId", TEST_MEASURE_ID)
                    .queryParam("periodStart", TEST_PERIOD_START)
                    .queryParam("periodEnd", TEST_PERIOD_END)
                    .queryParam("subject", TEST_PATIENT_ID)
                    .request().get();
        assertResponse(response, 200);

        String responseBody = response.readEntity(String.class);
        //System.out.println(responseBody);
        MeasureReport report = FHIRParser.parser(Format.JSON).parse(new StringReader(responseBody));
        assertEquals(report.getSubject().getReference().getValue(), TEST_PATIENT_ID);
        
        Period expectedPeriod = getPeriod(TEST_PERIOD_START, TEST_PERIOD_END);
        assertEquals( report.getPeriod(), expectedPeriod );
    }
}
