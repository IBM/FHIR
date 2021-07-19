/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.translator.impl;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.elm.execution.Library;

import com.ibm.fhir.cql.translator.CqlTranslationException;
import com.ibm.fhir.cql.translator.CqlTranslationProvider;

/**
 * Provide basic support for implementations of the CqlTranslationProvider 
 * interface.
 */
public abstract class BaseCqlTranslationProvider implements CqlTranslationProvider {

    public static final Format DEFAULT_TARGET_FORMAT = Format.XML;

    public List<Option> getDefaultOptions() {
        List<Option> defaults = CqlTranslatorOptions.defaultOptions().getOptions().stream().map( o -> Option.valueOf( o.name() ) ).collect(Collectors.toList());
        return defaults;
    }

    @Override
    public List<Library> translate(InputStream cql) throws CqlTranslationException {
        return translate(cql, getDefaultOptions());
    }

    @Override
    public List<Library> translate(InputStream cql, List<Option> options) throws CqlTranslationException {
        return translate(cql, options, DEFAULT_TARGET_FORMAT);
    }
}
