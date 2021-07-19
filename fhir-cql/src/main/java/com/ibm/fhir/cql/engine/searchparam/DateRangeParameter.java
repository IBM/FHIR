/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import java.util.ArrayList;
import java.util.List;

public class DateRangeParameter implements IQueryParameterAnd<DateParameter> {

    private DateParameter lowBound;
    private DateParameter highBound;

    public DateRangeParameter(DateParameter param) {
        setLowBound(param);
    }

    public DateRangeParameter(DateParameter lowBound, DateParameter highBound) {
        setLowBound(lowBound);
        setHighBound(highBound);
    }

    public DateParameter getLowBound() {
        return lowBound;
    }

    public void setLowBound(DateParameter lowBound) {
        this.lowBound = lowBound;
    }

    public DateParameter getHighBound() {
        return highBound;
    }

    public void setHighBound(DateParameter highBound) {
        this.highBound = highBound;
    }

    @Override
    public List<DateParameter> getParameterValues() {
        List<DateParameter> values = new ArrayList<>();

        if (lowBound != null) {
            values.add(lowBound);
        }
        if (highBound != null) {
            values.add(highBound);
        }
        return values;
    }
}
