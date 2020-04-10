/*
 * (C) Copyright IBM Corp. 2016, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.resources;

import static com.ibm.fhir.server.util.IssueTypeToHttpStatusMapper.issueListToStatus;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.ibm.fhir.config.FHIRRequestContext;
import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.core.HTTPReturnPreference;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.persistence.exception.FHIRPersistenceNotSupportedException;
import com.ibm.fhir.persistence.exception.FHIRPersistenceResourceNotFoundException;
import com.ibm.fhir.rest.FHIRRestOperationResponse;
import com.ibm.fhir.server.util.FHIRRestHelper;
import com.ibm.fhir.server.util.RestAuditLogger;

@Path("/")
@Consumes({ FHIRMediaType.APPLICATION_FHIR_JSON, MediaType.APPLICATION_JSON,
        FHIRMediaType.APPLICATION_FHIR_XML, MediaType.APPLICATION_XML })
@Produces({ FHIRMediaType.APPLICATION_FHIR_JSON, MediaType.APPLICATION_JSON,
        FHIRMediaType.APPLICATION_FHIR_XML, MediaType.APPLICATION_XML })
public class Delete extends FHIRResource {
    private static final Logger log = java.util.logging.Logger.getLogger(Delete.class.getName());

    public Delete() throws Exception {
        super();
    }

    @DELETE
    @Path("{type}/{id}")
    public Response delete(@PathParam("type") String type, @PathParam("id") String id) throws Exception {
        log.entering(this.getClass().getName(), "delete(String,String)");
        Date startTime = new Date();
        Response.Status status = null;
        FHIRRestOperationResponse ior = null;

        try {
            checkInitComplete();

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl());
            ior = helper.doDelete(type, id, null, null);

            ResponseBuilder response;

            // The server should return either a 200 OK if the response contains a payload, or a 204 No Content with no response payload
            if (ior.getOperationOutcome() != null && HTTPReturnPreference.OPERATION_OUTCOME == FHIRRequestContext.get().getReturnPreference()) {
                response = Response.ok(ior.getOperationOutcome());
            } else {
                response = Response.noContent();
            }

            if (ior.getResource() != null) {
                response = addHeaders(response, ior.getResource());
            }
            return response.build();
        } catch (FHIRPersistenceResourceNotFoundException e) {
            // Overwrite the exception response status because we want NOT_FOUND to be success for delete
            status = Status.OK;
            return exceptionResponse(e, status);
        } catch (FHIRPersistenceNotSupportedException e) {
            status = Status.METHOD_NOT_ALLOWED;
            return exceptionResponse(e, status);
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logDelete(httpServletRequest,
                        ior != null ? ior.getResource() : null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "delete(String,String)");
        }
    }

    @DELETE
    @Path("{type}")
    public Response conditionalDelete(@PathParam("type") String type) throws Exception {
        log.entering(this.getClass().getName(), "conditionalDelete(String)");
        Date startTime = new Date();
        Response.Status status = null;
        FHIRRestOperationResponse ior = null;

        try {
            checkInitComplete();

            String searchQueryString = httpServletRequest.getQueryString();
            if (searchQueryString == null || searchQueryString.isEmpty()) {
                String msg =
                        "A search query string is required for a conditional delete operation.";
                throw buildRestException(msg, IssueType.INVALID);
            }

            FHIRRestHelper helper = new FHIRRestHelper(getPersistenceImpl());
            ior = helper.doDelete(type, null, searchQueryString, null);
            status = ior.getStatus();
            ResponseBuilder response = Response.status(status);
            if (ior.getOperationOutcome() != null) {
                response.entity(ior.getOperationOutcome());
            }
            if (ior.getResource() != null) {
                response = addHeaders(response, ior.getResource());
            }
            return response.build();
        } catch (FHIRPersistenceResourceNotFoundException e) {
            // Return 200 instead of 404 to pass TouchStone test
            status = Status.OK;
            return exceptionResponse(e, status);
        } catch (FHIRPersistenceNotSupportedException e) {
            status = Status.METHOD_NOT_ALLOWED;
            return exceptionResponse(e, status);
        } catch (FHIROperationException e) {
            status = issueListToStatus(e.getIssues());
            return exceptionResponse(e, status);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            return exceptionResponse(e, status);
        } finally {
            try {
                RestAuditLogger.logDelete(httpServletRequest,
                        ior != null ? ior.getResource() : null,
                        startTime, new Date(), status);
            } catch (Exception e) {
                log.log(Level.SEVERE, AUDIT_LOGGING_ERR_MSG, e);
            }

            log.exiting(this.getClass().getName(), "conditionalDelete(String)");
        }
    }
}
