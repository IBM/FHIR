/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.ibm.fhir.search.SearchConstants.Prefix;

public class QuantityParameter extends NumberParameter {

    private String system;
    private String units;

    public QuantityParameter() {
        super();
    }

    public QuantityParameter(long value) {
        setValue(value);
    }

    public QuantityParameter(BigDecimal value) {
        setValue(value);
    }

    public QuantityParameter(long value, String system, String units) {
        setValue(value);
        setSystem(system);
        setUnits(units);
    }

    public QuantityParameter(BigDecimal value, String system, String units) {
        setValue(value);
        setSystem(system);
        setUnits(units);
    }

    public QuantityParameter(Prefix valuePrefix, long value, String system, String units) {
        setValuePrefix(valuePrefix);
        setValue(value);
        setSystem(system);
        setUnits(units);
    }

    public QuantityParameter(Prefix valuePrefix, BigDecimal value, String system, String units) {
        setValuePrefix(valuePrefix);
        setValue(value);
        setSystem(system);
        setUnits(units);
    }

    public String getSystem() {
        return system;
    }

    public QuantityParameter setSystem(String system) {
        this.system = system;
        return this;
    }

    public String getUnits() {
        return units;
    }

    public QuantityParameter setUnits(String units) {
        this.units = units;
        return this;
    }

    @Override
    public String getParameterValue() {
        StringBuilder paramValue = new StringBuilder();
        paramValue.append(getValue().toString());
        if (getSystem() != null) {
            paramValue.append("|");
            paramValue.append(StringUtils.defaultString(getSystem()));
            paramValue.append("|");
            paramValue.append(StringUtils.defaultString(getUnits()));
        }
        return paramValue.toString();
    }
}
