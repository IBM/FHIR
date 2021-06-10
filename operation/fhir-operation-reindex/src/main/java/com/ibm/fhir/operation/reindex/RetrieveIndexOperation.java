/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.operation.reindex;

import static com.ibm.fhir.model.type.String.string;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.server.operation.spi.AbstractOperation;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.util.FHIROperationUtil;

/**
 * Custom operation to invoke the persistence layer to retrieve a list of logical resource IDs.
 */
public class RetrieveIndexOperation extends AbstractOperation {
    private static final Logger logger = Logger.getLogger(RetrieveIndexOperation.class.getName());

    private static final String PARAM_COUNT = "_count";
    private static final String PARAM_AFTER_LOGICAL_RESOURCE_ID = "afterLogicalResourceId";
    private static final String PARAM_NOT_MODIFIED_AFTER = "notModifiedAfter";
    private static final String PARAM_LOGICAL_RESOURCE_IDS = "logicalResourceIds";

    // The max number of logical resource IDs we allow to be retrieved by one request
    private static final int MAX_COUNT = 1000;

    static final DateTimeFormatter DAY_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("UTC"));

    public RetrieveIndexOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("retrieve-index.json")) {
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper)
            throws FHIROperationException {

        // Only POST is allowed
        String method = (String) operationContext.getProperty(FHIROperationContext.PROPNAME_METHOD_TYPE);
        if (!"POST".equalsIgnoreCase(method)) {
            throw new FHIROperationException("HTTP method not supported: " + method);
        }

        try {
            String logicalResourceIdsString = "";
            int count = MAX_COUNT;
            Long afterLogicalResourceId = null;
            Instant notModifiedAfter = Instant.now();

            if (parameters != null) {
                for (Parameters.Parameter parameter : parameters.getParameter()) {
                    if (parameter.getValue() != null && logger.isLoggable(Level.FINE)) {
                        logger.fine("retrieve-index param: " + parameter.getName().getValue() + " = " + parameter.getValue().toString());
                    }

                    if (PARAM_COUNT.equals(parameter.getName().getValue())) {
                        Integer val = parameter.getValue().as(com.ibm.fhir.model.type.Integer.class).getValue();
                        if (val != null) {
                            if (val > MAX_COUNT) {
                                logger.info("Clamping resourceCount " + val + " to max allowed: " + MAX_COUNT);
                                val = MAX_COUNT;
                            }
                            count = val;
                        }
                    } else if (PARAM_NOT_MODIFIED_AFTER.equals(parameter.getName().getValue())) {
                        // Only retrieve logical resource IDs for resources not last updated after the specified timestamp
                        String val = parameter.getValue().as(com.ibm.fhir.model.type.String.class).getValue();
                        if (val.length() == 10) {
                            notModifiedAfter = DAY_FORMAT.parse(val, Instant::from);
                        } else {
                            // assume full ISO format
                            notModifiedAfter = Instant.parse(val);
                        }
                    } else if (PARAM_AFTER_LOGICAL_RESOURCE_ID.equals(parameter.getName().getValue())) {
                        // Start retrieving logical resource IDs after this specified logical resource ID
                        afterLogicalResourceId = Long.valueOf(parameter.getValue().as(com.ibm.fhir.model.type.String.class).getValue());
                    }
                }
            }

            // Get logical resource IDs
            List<Long> logicalResourceIds = resourceHelper.doRetrieveIndex(operationContext, count, notModifiedAfter, afterLogicalResourceId);
            if (logicalResourceIds != null) {
                logicalResourceIdsString = logicalResourceIds.stream().map(l -> String.valueOf(l)).collect(Collectors.joining(","));
            }

            // Return output
            return FHIROperationUtil.getOutputParameters(PARAM_LOGICAL_RESOURCE_IDS, !logicalResourceIdsString.isEmpty() ? string(logicalResourceIdsString) : null);

        } catch (FHIROperationException e) {
            throw e;
        } catch (Throwable t) {
            throw new FHIROperationException("Unexpected error occurred while processing request for operation '"
                    + getName() + "': " + getCausedByMessage(t), t);
        }
    }

    private String getCausedByMessage(Throwable throwable) {
        return throwable.getClass().getName() + ": " + throwable.getMessage();
    }
}
