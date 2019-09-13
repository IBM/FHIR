/**
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.watson.health.fhir.operation.bullkdata.model;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.ibm.watson.health.fhir.model.type.Instant;
import com.ibm.watson.health.fhir.operation.bullkdata.model.PollingLocationResponse.Output;

/**
 * Simple Test for the Rough Response defined in the BulkData Export 
 * 
 * @author pbastide
 *
 */
public class ResponseMetadataTest {

    @Test
    public void testResponseMetadataJsonEmpty() {
        PollingLocationResponse metadata = new PollingLocationResponse();
        assertEquals(metadata.toJsonString(), "{\n" +
                "\"output\" : []\n" +
                "}");
    }

    @Test
    public void testResponseMetadataJsonFull() {
        PollingLocationResponse metadata = new PollingLocationResponse();
        metadata.setRequest("request");
        metadata.setRequiresAccessToken(Boolean.FALSE);
        Instant now = Instant.now();
        metadata.setTransactionTime(now);
        assertEquals(metadata.toJsonString().replaceFirst(now.getValue().format(Instant.PARSER_FORMATTER), ""), "{\n"
                +
                "\"transactionTime\": \"{\n" +
                "    \"instant\": \"\"\n" +
                "}\",\"request\": \"request\",\"requiresAccessToken\": false,\"output\" : []\n" +
                "}");
    }

    @Test
    public void testResponseMetadataJsonFullWithOutput() {
        List<Output> outputs = new ArrayList<>();
        outputs.add(new Output("type", "url"));

        PollingLocationResponse metadata = new PollingLocationResponse();
        metadata.setRequest("request");
        metadata.setRequiresAccessToken(Boolean.FALSE);
        Instant now = Instant.now();
        metadata.setTransactionTime(now);
        metadata.setOutput(outputs);
        assertEquals(metadata.toJsonString().replaceFirst(now.getValue().format(Instant.PARSER_FORMATTER), ""), "{\n"
                +
                "\"transactionTime\": \"{\n" +
                "    \"instant\": \"\"\n" +
                "}\",\"request\": \"request\",\"requiresAccessToken\": false,\"output\" : [{ \"type\" : \"type\", \"url\": \"url\"}]\n"
                +
                "}");
    }
}
