/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import com.ibm.fhir.search.SearchConstants.Modifier;

public class TokenParameter extends BaseQueryParameter<TokenParameter> {

    private String system;
    private String value;

    public TokenParameter() {
        super();
    }

    public TokenParameter(Modifier modifier, String url) {
        setModifier(modifier);
        setValue(url);
    }

    public TokenParameter(String system, String value) {
        setSystem(system);
        setValue(value);
    }

    public TokenParameter(String value) {
        setValue(value);
    }

    public String getSystem() {
        return system;
    }

    public TokenParameter setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getValue() {
        return value;
    }

    public TokenParameter setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String getParameterValue() {
        StringBuilder paramValue = new StringBuilder();
        if (getSystem() != null) {
            // users should set the empty string if they want any system value
            paramValue.append(getSystem());
            paramValue.append("|");
        }

        if (getValue() != null) {
            paramValue.append(getValue());
        }
        return paramValue.toString();
    }
}
