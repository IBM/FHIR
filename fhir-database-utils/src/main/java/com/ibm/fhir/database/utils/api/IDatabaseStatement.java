/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.api;

import java.sql.Connection;

/**
 * @author rarnold
 *
 */
public interface IDatabaseStatement {

    /**
     * Execute the statement using the connection.
     * @param translator to translate any exceptions
     * @param c
     */
    public void run(IDatabaseTranslator translator, Connection c);
}
