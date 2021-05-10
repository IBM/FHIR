/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query;


/**
 * A table, values or subselect statement which can be included
 * in the FROM clause of a select statement
 */
public interface RowSource {

    /**
     * Get an implied alias for this row source if one is appropriate (e.g. the table name)
     * @return
     */
    default Alias getImpliedAlias() {
        return null;
    }

    /**
     * Render the object as a string
     * @param pretty pretty-print the return value if true
     * @return
     */
    String toPrettyString(boolean pretty);
}
