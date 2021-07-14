/*
 * (C) Copyright IBM Corp. 2016, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.resources;

import static com.ibm.fhir.config.FHIRConfiguration.PROPERTY_UPDATE_CREATE_ENABLED;
import static com.ibm.fhir.model.type.String.string;
import static com.ibm.fhir.server.util.IssueTypeToHttpStatusMapper.issueListToStatus;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.ibm.fhir.config.FHIRConfigHelper;
import com.ibm.fhir.config.FHIRConfiguration;
import com.ibm.fhir.config.FHIRRequestContext;
import com.ibm.fhir.config.PropertyGroup;
import com.ibm.fhir.core.FHIRConstants;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.model.util.FHIRUtil;
import com.ibm.fhir.model.util.ModelSupport;
import com.ibm.fhir.model.visitor.EncodingVisitor;
import com.ibm.fhir.model.visitor.EncodingVisitor.EncodingContext;
import com.ibm.fhir.persistence.FHIRPersistence;
import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.helper.FHIRPersistenceHelper;
import com.ibm.fhir.persistence.helper.PersistenceHelper;
import com.ibm.fhir.server.exception.FHIRRestBundledRequestException;
import com.ibm.fhir.server.listener.FHIRServletContextListener;

import net.jcip.annotations.NotThreadSafe;

/**
 * The base class for JAX-RS "Resource" classes which implement the FHIR HTTP API
 */
@NotThreadSafe
public class FHIRResource {
    private static final Logger log = java.util.logging.Logger.getLogger(FHIRResource.class.getName());

    public static final DateTimeFormatter HTTP_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("EEE")
            .optionalStart()
            // ANSIC date time format for If-Modified-Since
            .appendPattern(" MMM dd HH:mm:ss yyyy")
            .optionalEnd()
            .optionalStart()
            // Touchstone date time format for If-Modified-Since
            .appendPattern(", dd-MMM-yy HH:mm:ss")
            .optionalEnd().toFormatter();

    protected static final String AUDIT_LOGGING_ERR_MSG = "An error occurred while writing the audit log message.";

    private PersistenceHelper persistenceHelper = null;
    private FHIRPersistence persistence = null;

    @Context
    protected ServletContext context;

    @Context
    protected HttpServletRequest httpServletRequest;

    /**
     * UriInfo injected by the JAXRS framework.
     *
     * <p>Use {@link #getRequestUri()} instead to get the original request URI
     * when constructing URIs that will be sent back to the end user.
     */
    @Context
    protected UriInfo uriInfo;

    @Context
    protected SecurityContext securityContext;

    protected PropertyGroup fhirConfig = null;

    /**
     * This method will do a quick check of the "initCompleted" flag in the servlet context. If the flag is FALSE, then
     * we'll throw an error to short-circuit the current in-progress REST API invocation.
     */
    protected void checkInitComplete() throws FHIROperationException {
        Boolean fhirServerInitComplete =
                (Boolean) context.getAttribute(FHIRServletContextListener.FHIR_SERVER_INIT_COMPLETE);
        if (Boolean.FALSE.equals(fhirServerInitComplete)) {
            String msg =
                    "The FHIR Server web application cannot process requests because it did not initialize correctly";
            throw buildRestException(msg, IssueType.EXCEPTION);
        }
    }

    /**
     * This method will do a quick check of the {type} URL parameter. If the type is not a valid FHIR resource type, then
     * we'll throw an error to short-circuit the current in-progress REST API invocation.
     */
    protected void checkType(String type) throws FHIROperationException {
        if (!ModelSupport.isResourceType(type)) {
            throw buildUnsupportedResourceTypeException(type);
        }
        if (!ModelSupport.isConcreteResourceType(type)) {
            log.warning("Use of abstract resource types like '" + type + "' in FHIR URLs is deprecated and will be removed in a future release");
        }
    }

    public FHIRResource() throws Exception {
        if (log.isLoggable(Level.FINEST)) {
            log.entering(this.getClass().getName(), "FHIRResource ctor");
        }
        try {
            fhirConfig = FHIRConfiguration.getInstance().loadConfiguration();
        } catch (Throwable t) {
            log.severe("Unexpected error during initialization: " + t);
            throw t;
        } finally {
            if (log.isLoggable(Level.FINEST)) {
                log.exiting(this.getClass().getName(), "FHIRResource ctor");
            }
        }
    }

    protected FHIROperationException buildRestException(String msg, IssueType issueType) {
        return buildRestException(msg, issueType, IssueSeverity.FATAL);
    }

    protected FHIROperationException buildRestException(String msg, IssueType issueType, IssueSeverity severity) {
        return new FHIROperationException(msg).withIssue(buildOperationOutcomeIssue(severity, issueType, msg));
    }

    protected long parseIfModifiedSince() {
        // Modified since date time in EpochMilli
        long modifiedSince = -1;
        try {
            // Handle RFC_1123 and RFC_850 formats first.
            // e.g "Sun, 06 Nov 1994 08:49:37 GMT", "Sunday, 06-Nov-94 08:49:37 GMT", "Sunday, 06-Nov-1994 08:49:37 GMT"
            // If 2 digits year is used, then means 1940 to 2039.
            modifiedSince = httpServletRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        } catch (Exception e) {
            if (log.isLoggable(Level.FINE)) {
                String headerVal = httpServletRequest.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
                log.log(Level.FINE, HttpHeaders.IF_MODIFIED_SINCE + " header value '" + headerVal + "' is not valid per rfc1123 or rfc850; "
                        + "continuing with alternate formats.", e);
            }
            try {
                // Then handle ANSIC format, e.g, "Sun Nov  6 08:49:37 1994"
                // and touchStone specific format, e.g, "Sat, 28-Sep-19 16:11:14"
                // assuming the time zone is GMT.
                modifiedSince = HTTP_DATETIME_FORMATTER.parse(httpServletRequest.getHeader(HttpHeaders.IF_MODIFIED_SINCE), LocalDateTime::from)
                        .atZone(ZoneId.of("GMT")).toInstant().toEpochMilli();
            } catch (DateTimeParseException e1) {
                if (log.isLoggable(Level.FINE)) {
                    String headerVal = httpServletRequest.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
                    log.log(Level.FINE, HttpHeaders.IF_MODIFIED_SINCE + " header value '" + headerVal + "' is not valid per rfc1123 or rfc850; "
                            + "continuing without.", e);
                }
                modifiedSince = -1;
            }
        }
        return modifiedSince;
    }

    /**
     * Builds an OperationOutcomeIssue with the respective values for some of the fields.
     */
    protected OperationOutcome.Issue buildOperationOutcomeIssue(IssueSeverity severity, IssueType type, String msg) {
        return OperationOutcome.Issue.builder()
                .severity(severity)
                .code(type)
                .details(CodeableConcept.builder().text(string(msg)).build())
                .build();
    }

    /**
     * This function will build an absolute URI from the specified base URI and relative URI.
     *
     * @param baseUri
     *            the base URI to be used; this will be of the form <scheme>://<host>:<port>/<context-root>
     * @param relativeUri
     *            the path and query parts
     * @return the full URI value as a String
     */
    protected String getAbsoluteUri(String baseUri, String relativeUri) {
        StringBuilder fullUri = new StringBuilder();
        fullUri.append(baseUri);
        if (!baseUri.endsWith("/")) {
            fullUri.append("/");
        }
        fullUri.append((relativeUri.startsWith("/") ? relativeUri.substring(1) : relativeUri));
        return fullUri.toString();
    }

    /**
     * Adds the Etag and Last-Modified headers to the specified response object.
     */
    protected ResponseBuilder addHeaders(ResponseBuilder rb, Resource resource) {
        return rb.header(HttpHeaders.ETAG, getEtagValue(resource))
                // According to 3.3.1 of RTC2616(HTTP/1.1), we MUST only generate the RFC 1123 format for representing HTTP-date values
                // in header fields, e.g Sat, 28 Sep 2019 16:11:14 GMT
                .lastModified(Date.from(resource.getMeta().getLastUpdated().getValue().toInstant()));
    }

    private String getEtagValue(Resource resource) {
        return "W/\"" + resource.getMeta().getVersionId().getValue() + "\"";
    }

    protected Response exceptionResponse(FHIRRestBundledRequestException e) {
        Response response;
        if (e.getResponseBundle() != null) {
            if (e.getIssues().size() > 0) {
                // R4 says we should return a single OperationOutcome with the issues:
                // http://www.hl7.org/fhir/r4/http.html#transaction-response
                String msg =
                        "FHIRRestBundledRequestException contains both a response bundle and a list of issues. "
                                + "Only the response bundle will be returned.";
                log.log(Level.WARNING, msg, e);
            }

            List<Bundle.Entry> toAdd = new ArrayList<Bundle.Entry>();
            // replace bundle entries that have an empty response
            for (Bundle.Entry entry : e.getResponseBundle().getEntry()) {
                if (entry.getResponse() != null && entry.getResponse().getStatus() == null) {
                    entry = entry.toBuilder()
                            .response(entry.getResponse().toBuilder()
                                .status(string(Integer.toString(Status.BAD_REQUEST.getStatusCode())))
                                .build())
                            .build();
                }
                toAdd.add(entry);
            }

            Bundle responseBundle = e.getResponseBundle().toBuilder().entry(toAdd).build();

            response = Response.status(Status.OK).entity(responseBundle).build();
        } else {
            // Override the status code with a generic client (400) or server (500) error code
            Status status = issueListToStatus(e.getIssues());
            if (status.getFamily() == Status.Family.CLIENT_ERROR) {
                status = Status.BAD_REQUEST;
            } else {
                status = Status.INTERNAL_SERVER_ERROR;
            }
            response = exceptionResponse(e, status);
        }
        return response;
    }

    protected Response exceptionResponse(FHIROperationException e, Status status) {
        if (status == null) {
            status = issueListToStatus(e.getIssues());
        }

        // Only log full stack trace if server (5xx) error, or if logging level is at least FINE
        if (status.getFamily() == Status.Family.SERVER_ERROR) {
            log.log(Level.SEVERE, e.getMessage(), e);
        } else if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, e.getMessage(), e);
        } else if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, e.getMessage());
        }

        OperationOutcome operationOutcome = FHIRUtil.buildOperationOutcome(e, false);
        return exceptionResponse(operationOutcome, status);
    }

    protected Response exceptionResponse(Exception e, Status status) {
        log.log(Level.SEVERE, "An unexpected exception occurred while processing the request", e);
        OperationOutcome oo = FHIRUtil.buildOperationOutcome(e, false);
        return this.exceptionResponse(oo, status);
    }

    protected Response exceptionResponse(OperationOutcome oo, Status status) {
        if (log.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("\nOperationOutcome:\n").append(serializeOperationOutcome(oo));
            log.log(Level.FINE, sb.toString());
        }

        // Single Location to ensure the Operation Outcomes are Encode.forHtml and avoids any injections.
        EncodingVisitor<OperationOutcome> v = new EncodingVisitor<>(EncodingContext.HTML);
        oo.accept(v);
        return Response.status(status)
                .entity(v.getResult())
                .build();
    }

    private String serializeOperationOutcome(OperationOutcome oo) {
        try {
            StringWriter sw = new StringWriter();
            FHIRGenerator.generator(Format.JSON, false).generate(oo, sw);
            return sw.toString();
        } catch (Throwable t) {
            return "Error encountered while serializing OperationOutcome resource: "
                    + t.getMessage();
        }
    }

    /**
     * Retrieves the shared persistence helper object from the servlet context.
     */
    private PersistenceHelper getPersistenceHelper() {
        if (persistenceHelper == null) {
            persistenceHelper =
                    (PersistenceHelper) context.getAttribute(FHIRPersistenceHelper.class.getName());
            if (log.isLoggable(Level.FINE)) {
                log.fine("Retrieved FHIRPersistenceHelper instance from servlet context: "
                        + persistenceHelper);
            }
        }
        return persistenceHelper;
    }

    /**
     * Retrieves the persistence implementation to use for the current request.
     * @see {@link PersistenceHelper#getFHIRPersistenceImplementation()}
     */
    protected FHIRPersistence getPersistenceImpl() throws FHIRPersistenceException {
        if (persistence == null) {
            persistence = getPersistenceHelper().getFHIRPersistenceImplementation();
            if (log.isLoggable(Level.FINE)) {
                log.fine("Obtained new FHIRPersistence instance: " + persistence);
            }
        }
        return persistence;
    }

    protected boolean isDeleteSupported() throws FHIRPersistenceException {
        return getPersistenceImpl().isDeleteSupported();
    }

    protected Boolean isUpdateCreateEnabled() {
        return FHIRConfigHelper.getBooleanProperty(PROPERTY_UPDATE_CREATE_ENABLED, Boolean.TRUE);
    }

    /**
     * Get the original request URI from either the HttpServletRequest or a configured Header (in case of re-writing proxies).
     *
     * <p>When the 'fhirServer/core/originalRequestUriHeaderName' property is empty, this method returns the equivalent of
     * uriInfo.getRequestUri().toString(), except that uriInfo.getRequestUri() will throw an IllegalArgumentException
     * when the query string portion contains a vertical bar | character. The vertical bar is one known case of a special character
     * causing the exception. There could be others.
     *
     * @return String The complete request URI
     * @throws Exception if an error occurs while reading the config
     */
    protected String getRequestUri() throws Exception {
        return FHIRRequestContext.get().getOriginalRequestUri();
    }

    /**
     * This method returns the "base URI" associated with the current request. For example, if a client invoked POST
     * https://myhost:9443/fhir-server/api/v4/Patient to create a Patient resource, this method would return
     * "https://myhost:9443/fhir-server/api/v4".
     *
     * @return The base endpoint URI associated with the current request.
     * @throws Exception if an error occurs while reading the config
     * @implNote This method uses {@link #getRequestUri()} to get the original request URI and then strips it to the
     *           <a href="https://www.hl7.org/fhir/http.html#general">Service Base URL</a>
     */
    protected String getRequestBaseUri(String type) throws Exception {
        String baseUri = null;

        String requestUri = getRequestUri();

        // Strip off everything after the path
        int queryPathSeparatorLoc = requestUri.indexOf("?");
        if (queryPathSeparatorLoc != -1) {
            baseUri = requestUri.substring(0, queryPathSeparatorLoc);
        } else {
            baseUri = requestUri;
        }

        // Strip off any path elements after the base
        if (type != null && !type.isEmpty()) {
            int resourceNamePathLocation = baseUri.lastIndexOf("/" + type);
            if (resourceNamePathLocation != -1) {
                baseUri = requestUri.substring(0, resourceNamePathLocation);
            } else {
                // Assume the request was a batch/transaction and just use the requestUri as the base
                baseUri = requestUri;
            }
        } else {
            if (baseUri.endsWith("/_history")) {
                baseUri = baseUri.substring(0, baseUri.length() - "/_history".length());
            } else if (baseUri.endsWith("/_search")) {
                baseUri = baseUri.substring(0, baseUri.length() - "/_search".length());
            } else if (baseUri.contains("/$")) {
                baseUri = baseUri.substring(0, baseUri.lastIndexOf("/$"));
            }
        }

        return baseUri;
    }

    /**
     * This method simply returns a URI object containing the specified URI string.
     *
     * @param uriString
     *            the URI string for which the URI object will be created
     * @throws URISyntaxException
     */
    protected URI toUri(String uriString) throws URISyntaxException {
        return new URI(uriString);
    }

    protected FHIROperationException buildUnsupportedResourceTypeException(String resourceTypeName) {
        String msg = "'" + resourceTypeName + "' is not a valid resource type.";
        Issue issue = OperationOutcome.Issue.builder()
                .severity(IssueSeverity.FATAL)
                .code(IssueType.NOT_SUPPORTED.toBuilder()
                        .extension(Extension.builder()
                            .url(FHIRConstants.EXT_BASE + "not-supported-detail")
                            .value(Code.of("resource"))
                            .build())
                        .build())
                .details(CodeableConcept.builder().text(string(msg)).build())
                .build();
        return new FHIROperationException(msg).withIssue(issue);
    }
}
