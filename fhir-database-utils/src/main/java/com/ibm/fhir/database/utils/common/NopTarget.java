/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.common;

import com.ibm.fhir.database.utils.api.IDatabaseStatement;
import com.ibm.fhir.database.utils.api.IDatabaseSupplier;
import com.ibm.fhir.database.utils.api.IDatabaseTarget;
import com.ibm.fhir.database.utils.api.IDatabaseTranslator;

/**
 * An {@link IDatabaseTarget} which just acts as a sink and doesn't actually
 * do anything. Useful for tests, just to exercise the code.
 * @author rarnold
 *
 */
public class NopTarget implements IDatabaseTarget {
    
    /**
     * Public constructor
     * @param decorated
     */
    public NopTarget() {
    }
    
    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatement(java.lang.String)
     */
    @Override
    public void runStatement(IDatabaseTranslator translator, String ddl) {
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatementWithInt(com.ibm.fhir.database.utils.api.IDatabaseTranslator, java.lang.String, int)
     */
    @Override
    public void runStatementWithInt(IDatabaseTranslator translator, String sql, int value) {
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatement(com.ibm.fhir.database.utils.api.IDatabaseStatement)
     */
    @Override
    public void runStatement(IDatabaseTranslator translator, IDatabaseStatement statement) {
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatement(com.ibm.fhir.database.utils.api.IDatabaseTranslator, com.ibm.fhir.database.utils.api.IDatabaseSupplier)
     */
    @Override
    public <T> T runStatement(IDatabaseTranslator translator, IDatabaseSupplier<T> supplier) {
        return null;
    }
}
