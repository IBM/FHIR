/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ibm.fhir.search.SearchConstants.Prefix;

public class DateParameter extends BaseQueryParameter<DateParameter> implements IQueryParameterOr<DateParameter> {

    private Prefix valuePrefix;
    private Date value;

    public DateParameter() {
        super();
    }

    public DateParameter(Date value) {
        setValue(value);
    }

    public DateParameter(Prefix valuePrefix, Date value) {
        setValuePrefix(valuePrefix);
        setValue(value);
    }

    public Prefix getValuePrefix() {
        return valuePrefix;
    }

    public void setValuePrefix(Prefix valuePrefix) {
        this.valuePrefix = valuePrefix;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    @Override
    public String getParameterValue() {
        StringBuilder sb = new StringBuilder();
        if (valuePrefix != null) {
            sb.append(valuePrefix.value());
        }
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        sb.append(fmt.format(getValue()));
        return sb.toString();
    }

    @Override
    public List<DateParameter> getParameterValues() {
        return Collections.singletonList(this);
    }
}
