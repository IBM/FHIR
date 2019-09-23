/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.api;

/**
 * @author rarnold
 *
 */
public class UndefinedNameException extends DataAccessException {

    // Generated id
    private static final long serialVersionUID = -4113574235913420649L;

    /**
     * @param t
     */
    public UndefinedNameException(Throwable t) {
        super(t);
    }

}
