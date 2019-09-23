/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query;

/**
 * An item of the SELECT list which is a simple column reference
 * @author rarnold
 *
 */
public class SelectItemColumn extends SelectItem {
    
    // The object from which we're selecting (e.g. table, or sub-query alias)
    private final String source;

    // The column name or expression
    private final String columnName;
    
    protected SelectItemColumn(String source, String columnName, Alias alias) {
        super(alias);
        this.source = source;
        this.columnName = columnName;
    }
}
