/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query;

/**
 * An abstract representation of a select statement which can be translated
 * into an executable select statement. Keeps track of bind variables (parameter
 * markers) and hopefully is a bit easier to use (and is more reliable) than 
 * constructing SQL with StringBuilder.
 * 
 * The goal is to support two main use-cases (which drive the API design):
 *   1. Simplify construction of hand-written SQL statements
 *   2. Support code-generated SQL where a statement is constructed
 *      from another model (e.g. FHIR search queries).
 *      
 * The goal isn't to enforce building a syntactically perfect SQL statement - checking
 * that is the role of the RDBMS SQL parser. But hopefully this makes things
 * a bit easier, less error-prone and therefore quicker. It also helps to
 * standardize the SQL statement building process across the project.
 * @author rarnold
 *
 */
public class SelectAdapter {

    // the select statement under construction
    private final Select select;

    /**
     * Adapter this select statement
     * @param select
     */
    public SelectAdapter(Select select) {
        this.select = select;
    }
    
    /**
     * Public constructor taking a collection of string column names
     * @param columns
     */
    public SelectAdapter(String... columns) {
        this.select = new Select();

        this.select.addColumns(columns);
    }

    /**
     * Create a from clause for this select statement
     * @return
     */
    public FromAdapter from(String tableName, Alias alias) {
        select.addTable(null, tableName, alias);
        return new FromAdapter(select);
    }

    /**
     * Add the sub-query select to the FROM clause
     * @param sub the sub-query select statement
     * @param alias
     * @return
     */
    public FromAdapter from(Select sub, Alias alias) {
        select.addFrom(sub, alias);
        return new FromAdapter(select);
    }

    /**
     * Create a from clause for this select statement
     * @return
     */
    public FromAdapter from(String table) {
        FromAdapter result = new FromAdapter(select);
        return result;
    }

    /**
     * Getter for the select statement we are managing
     * @return
     */
    public Select getSelect() {
        return this.select;
    }

    /**
     * Get the statement we've been constructing
     * @return
     */
    public Select build() {
        return this.select;
    }
}
