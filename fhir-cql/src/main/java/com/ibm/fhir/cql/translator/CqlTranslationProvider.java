/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.translator;

import java.io.InputStream;
import java.util.List;

import org.cqframework.cql.elm.execution.Library;

/**
 * Define an interface that applications can use for CQL to ELM 
 * translation.
 */
public interface CqlTranslationProvider {
    public enum Option {
        EnableDateRangeOptimization,
        EnableAnnotations,
        EnableLocators,
        EnableResultTypes,
        EnableDetailedErrors,
        DisableListTraversal,
        DisableListDemotion,
        DisableListPromotion,
        EnableIntervalDemotion,
        EnableIntervalPromotion,
        DisableMethodInvocation,
        RequireFromKeyword
    }
    
    public static enum Format { XML, JSON, JXSON, COFFEE }
    
    List<Library> translate(InputStream cql) throws CqlTranslationException;

    List<Library> translate(InputStream cql, List<Option> options) throws CqlTranslationException;

    List<Library> translate(InputStream cql, List<Option> options, Format targetFormat) throws CqlTranslationException;
    
}
