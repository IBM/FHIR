/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.server.test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;

import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.examples.ExamplesUtil;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Patient;

public class ConvertOperationTest extends FHIRServerTestBase {
    private static final String CONVERT_OPERATION_NAME = "$convert";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-type";
    private static final String ACCEPT_HEADER_NAME = "Accept";

    @Test
    public void testConvertOperation1() throws Exception {
        try (BufferedReader reader = new BufferedReader(ExamplesUtil.resourceReader("json/ibm/complete-mock/Patient-1.json"))) {
            String input = reader.lines().collect(Collectors.joining(System.lineSeparator()));

            // input is JSON
            assertTrue(input.startsWith("{"));

            // parse and deserialize JSON input
            Patient patientFromJson = FHIRParser.parser(Format.JSON).parse(new StringReader(input));
            assertNotNull(patientFromJson);

            WebTarget endpoint = getWebTarget();

            Response response = endpoint.path(CONVERT_OPERATION_NAME).request()
                    .header(CONTENT_TYPE_HEADER_NAME, FHIRMediaType.APPLICATION_FHIR_JSON)
                    .header(ACCEPT_HEADER_NAME,  FHIRMediaType.APPLICATION_FHIR_XML)
                    .post(Entity.entity(input, FHIRMediaType.APPLICATION_FHIR_JSON));

            String output = response.readEntity(String.class);

            // output is XML
            assertTrue(output.startsWith("<"));

            // parse and deserialize XML output
            Patient patientFromXML = FHIRParser.parser(Format.XML).parse(new StringReader(output));
            assertNotNull(patientFromXML);

            assertTrue(patientFromXML.equals(patientFromJson));
        }
    }

    @Test
    public void testConvertOperation2() throws Exception {
        try (BufferedReader reader = new BufferedReader(ExamplesUtil.resourceReader("xml/ibm/complete-mock/Patient-1.xml"))) {
            String input = reader.lines().collect(Collectors.joining(System.lineSeparator()));

            // input is XML
            assertTrue(input.startsWith("<"));

            // parse and deserialize XML input
            Patient patientFromXML = FHIRParser.parser(Format.XML).parse(new StringReader(input));
            assertNotNull(patientFromXML);

            WebTarget endpoint = getWebTarget();

            Response response = endpoint.path(CONVERT_OPERATION_NAME).request()
                    .header(CONTENT_TYPE_HEADER_NAME, FHIRMediaType.APPLICATION_FHIR_XML)
                    .header(ACCEPT_HEADER_NAME,  FHIRMediaType.APPLICATION_FHIR_JSON)
                    .post(Entity.entity(input, FHIRMediaType.APPLICATION_FHIR_XML));

            String output = response.readEntity(String.class);

            // output is JSON
            assertTrue(output.startsWith("{"));

            // parse and deserialize JSON output
            Patient patientFromJson = FHIRParser.parser(Format.JSON).parse(new StringReader(output));
            assertNotNull(patientFromJson);

            assertTrue(patientFromJson.equals(patientFromXML));
        }
    }
}
