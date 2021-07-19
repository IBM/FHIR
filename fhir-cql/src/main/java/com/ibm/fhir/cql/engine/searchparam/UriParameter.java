/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import com.ibm.fhir.search.SearchConstants.Modifier;

public class UriParameter extends BaseQueryParameter<UriParameter> {

    Modifier modifier = null;
    String value = null;

    public UriParameter() {
        super();
    }

    public UriParameter(String value) {
        // setValue(value);
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getParameterValue() {
        return getValue();
    }
}
