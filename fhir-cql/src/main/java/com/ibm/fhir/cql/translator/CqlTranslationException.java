/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.translator;

import java.util.List;

/**
 * Encapsulates a CQL Translator Exception as produced by the CQL
 * to ELM translator.
 */
public class CqlTranslationException extends RuntimeException {

    private static final long serialVersionUID = 16518219151102500L;
    private List<?> errors;

    public CqlTranslationException(String message) {
        super(message);
    }
    
    public CqlTranslationException(String message, Exception cause) {
        super(message, cause);
    }
    
    public CqlTranslationException(Exception cause) {
        super(cause);
    }
    
    public CqlTranslationException(String message, List<?> errors) {
        super(message);
        this.errors = errors;
    }
    
    public CqlTranslationException(String message, Exception cause, List<?> errors) {
        super(message, cause);
        this.errors = errors;
    }
    
    public CqlTranslationException(Exception cause, List<?> errors) {
        super(cause);
        this.errors = errors;
    }
    
    public List<?> getErrors() {
        return errors;
    }
}
