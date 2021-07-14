/*
 * (C) Copyright IBM Corp. 2017, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.operation.spi;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

/**
 * This class holds context information for custom operation invocations.
 */
public class FHIROperationContext {
    public enum Type { SYSTEM, RESOURCE_TYPE, INSTANCE }

    /**
     * This property is of type String and represents the base URI string (e.g.
     * "https://localhost:9443/fhir-server/api/v4") associated with the request.
     */
    public static final String PROPNAME_REQUEST_BASE_URI = "REQUEST_BASE_URI";

    /**
     * This property is of type String and represents the value of the Location header.
     */
    public static final String PROPNAME_LOCATION_URI = "LOCATION_URI";

    /**
     * This property is of type FHIRPersistence and is the handle to the persistence layer implementation
     * being used by the FHIR Server while processing the current request.
     * It is recommended that custom operation implementors use the RESOURCE_HELPER property instead of
     * this property unless you have been advised otherwise.
     */
    public static final String PROPNAME_PERSISTENCE_IMPL = "PERSISTENCE_IMPL";

    /**
     * This property is of type javax.ws.rs.core.UriInfo and contains Application and Request
     * URI information associated with the REST API request for which the interceptor is being invoked.
     */
    public static final String PROPNAME_URI_INFO = "URI_INFO";

    /**
     * This property is of type javax.ws.rs.core.HttpHeaders and contains the set of HTTP headers
     * associated with the REST API request.
     */
    public static final String PROPNAME_HTTP_HEADERS = "HTTP_HEADERS";

    /**
     * This property is of type javax.ws.rs.core.SecurityContext and contains security-related information
     * associated with the REST API request for which the interceptor is being invoked.
     */
    public static final String PROPNAME_SECURITY_CONTEXT = "SECURITY_CONTEXT";

    /**
     *This property is of type HttpMethod
     */
    public static final String PROPNAME_METHOD_TYPE = "METHOD_TYPE";

    /**
     * The property is of Response.Status.
     */
    public static final String PROPNAME_STATUS_TYPE = "STATUS";

    /**
     * The property is of the Response
     */
    public static final String PROPNAME_RESPONSE = "RESPONSE";

    /**
     * The property is of the Http Request object
     */
    public static final String PROPNAME_HTTP_REQUEST = "HTTP_REQUEST";

    private Type type = null;
    private Map<String, Object> properties = null;

    private FHIROperationContext(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Context type cannot be null");
        }
        this.type = type;
        properties = new HashMap<String, Object>();
    }

    public Type getType() {
        return type;
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Returns the HttpHeaders instance associated with the request that triggered the operation.
     * Note that this HttpHeaders instance is only valid within the scope of a single request.
     */
    public HttpHeaders getHttpHeaders() {
        return (HttpHeaders) getProperty(PROPNAME_HTTP_HEADERS);
    }

    public static FHIROperationContext createSystemOperationContext() {
        return new FHIROperationContext(Type.SYSTEM);
    }

    public static FHIROperationContext createResourceTypeOperationContext() {
        return new FHIROperationContext(Type.RESOURCE_TYPE);
    }

    public static FHIROperationContext createInstanceOperationContext() {
        return new FHIROperationContext(Type.INSTANCE);
    }

    /**
     * Retrieves the specified header from the list of request headers.
     * @param headerName the name of the header to retrieve
     * @return the value of the request header or null if not present
     */
    public String getHeaderString(String headerName) {
        String value = null;
        if (getHttpHeaders() != null) {
            value = getHttpHeaders().getHeaderString(headerName);
        }
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append("type=" + type.name());
        sb.append(", properties={").append((properties != null ? properties.toString() : "<null>")).append("}");
        sb.append("]");

        return sb.toString();
    }
}