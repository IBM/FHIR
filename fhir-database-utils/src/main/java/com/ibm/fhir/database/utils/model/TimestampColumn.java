/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.model;

import com.ibm.fhir.database.utils.api.IDatabaseTypeAdapter;

/**
 * @author rarnold
 *
 */
public class TimestampColumn extends ColumnBase {

    /**
     * @param name
     */
    public TimestampColumn(String name, boolean nullable) {
        super(name, nullable);
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.schema.model.ColumnBase#getTypeInfo()
     */
    @Override
    public String getTypeInfo(IDatabaseTypeAdapter translator) {
        return "TIMESTAMP";
    }

}
