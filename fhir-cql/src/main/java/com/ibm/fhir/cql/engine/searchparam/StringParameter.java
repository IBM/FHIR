/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import com.ibm.fhir.search.SearchConstants.Modifier;

public class StringParameter extends BaseQueryParameter<StringParameter> {

    private String value;

    public StringParameter() {
        super();
    }

    public StringParameter(String value) {
        setValue(value);
    }

    public StringParameter(String value, Modifier modifier) {
        setValue(value);
        setModifier(modifier);
    }

    public String getValue() {
        return value;
    }

    public StringParameter setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String getParameterValue() {
        return getValue();
    }
}
