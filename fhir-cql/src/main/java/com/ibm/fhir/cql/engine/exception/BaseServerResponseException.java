/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.exception;

import com.ibm.fhir.model.resource.OperationOutcome;

/**
 * Provides an extension point for server exceptions based on HTTP
 * interactions.
 */
public class BaseServerResponseException extends RuntimeException {

    private static final long serialVersionUID = -8334964870841899079L;

    private int statusCode;
    private OperationOutcome operationOutcome;

    public BaseServerResponseException(int statusCode, String message) {
        super(message);
        setStatusCode(statusCode);
    }

    public BaseServerResponseException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        setStatusCode(statusCode);
    }

    public BaseServerResponseException(int statusCode, String message, Throwable cause, OperationOutcome outcome) {
        super(message, cause);
        setStatusCode(statusCode);
        setOperationOutcome(outcome);
    }

    public BaseServerResponseException(int statusCode, Throwable cause) {
        super(cause);
        setStatusCode(statusCode);
    }

    public BaseServerResponseException(int statusCode, Throwable cause, OperationOutcome outcome) {
        super(cause);
        setStatusCode(statusCode);
        setOperationOutcome(outcome);
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public OperationOutcome getOperationOutcome() {
        return operationOutcome;
    }

    public void setOperationOutcome(OperationOutcome operationOutcome) {
        this.operationOutcome = operationOutcome;
    }
}
