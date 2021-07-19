/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.server.test.cqf;

import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Library;

public class ServerMeasureDataRequirementsOperationTest extends BaseMeasureOperationTest {
    @Test
    public void testMeasureDataRequirementsInstance() throws Exception {
        Response response =
                getWebTarget().path("/Measure/{id}/$data-requirements")
                    .resolveTemplate("id", TEST_MEASURE_ID)
                    .queryParam("periodStart", TEST_PERIOD_START)
                    .queryParam("periodEnd", TEST_PERIOD_END)
                    .request().get();
        assertResponse(response, 200);

        String responseBody = response.readEntity(String.class);
        System.out.println(responseBody);
        Library module = (Library) FHIRParser.parser(Format.JSON).parse(new StringReader(responseBody));
        assertNotNull(module);
    }
}
