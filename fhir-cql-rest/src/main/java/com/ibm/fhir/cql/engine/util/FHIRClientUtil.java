/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.Response;

import com.ibm.fhir.client.FHIRResponse;
import com.ibm.fhir.cql.engine.exception.BaseServerResponseException;

/**
 * Helper methods for working with the IBM FHIR Server Client and 
 * produced FHIRResponse objects.
 */
public class FHIRClientUtil {

    public static void handleErrorResponse(FHIRResponse response) {
        Response r = null;
        try {
            r = response.getResponse();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        handleErrorResponse(r);
    }

    public static void handleErrorResponse(Response response) {
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new BaseServerResponseException(response.getStatus(), response.readEntity(String.class));
        }
    }

    public static String urlencode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
