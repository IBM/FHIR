/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.api;

/**
 * Translated exception for a SQLException representing a lock timeout or deadlock
 * exception
 * @author rarnold
 *
 */
public class LockException extends DataAccessException {

    // Generated serial number
    private static final long serialVersionUID = 6925970249773549237L;

    // Was this triggered by a database deadlock?
    private final boolean deadlock;

    /**
     * Public constructor
     * @param t
     */
    public LockException(Throwable t, boolean deadlock) {
        super(t);
        this.deadlock = deadlock;
    }

    public boolean isDeadlock() {
        return deadlock;
    }

}
