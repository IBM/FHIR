/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import java.math.BigDecimal;

import com.ibm.fhir.search.SearchConstants;
import com.ibm.fhir.search.SearchConstants.Prefix;

public class NumberParameter extends BaseQueryParameter<NumberParameter> {

    private Prefix valuePrefix;
    private BigDecimal value;

    public NumberParameter() {
        super();
    }

    public NumberParameter(boolean isMissing) {
        setMissing(isMissing);
    }

    public NumberParameter(long value) {
        setValue(value);
    }

    public NumberParameter(BigDecimal value) {
        setValue(value);
    }

    public NumberParameter(SearchConstants.Prefix valuePrefix, long value) {
        setValue(value);
        setValuePrefix(valuePrefix);
    }

    public NumberParameter(SearchConstants.Prefix valuePrefix, BigDecimal value) {
        setValue(value);
        setValuePrefix(valuePrefix);
    }

    public SearchConstants.Prefix getValuePrefix() {
        return valuePrefix;
    }

    public NumberParameter setValuePrefix(SearchConstants.Prefix valuePrefix) {
        this.valuePrefix = valuePrefix;
        return this;
    }

    public BigDecimal getValue() {
        return value;
    }

    public NumberParameter setValue(long value) {
        this.value = BigDecimal.valueOf(value);
        return this;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String getParameterValue() {
        return getValue().toString();
    }
}
