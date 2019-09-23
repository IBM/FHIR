/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.ibm.fhir.database.utils.api.IDatabaseStatement;
import com.ibm.fhir.database.utils.api.IDatabaseSupplier;
import com.ibm.fhir.database.utils.api.IDatabaseTarget;
import com.ibm.fhir.database.utils.api.IDatabaseTranslator;

/**
 * @author rarnold
 *
 */
public class JdbcTarget implements IDatabaseTarget {
    private final Connection connection;
    
    /**
     * Public constructor
     * @param c
     */
    public JdbcTarget(Connection c) {
        this.connection = c;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatement(java.lang.String)
     */
    @Override
    public void runStatement(IDatabaseTranslator translator, String ddl) {
        // Execute the DDL (no parameters)
        try (Statement s = connection.createStatement()) {
            s.executeUpdate(ddl);
        }
        catch (SQLException x) {
            throw translator.translate(x);
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatementWithInt(java.lang.String, int)
     */
    @Override
    public void runStatementWithInt(IDatabaseTranslator translator, String sql, int value) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, value);
            ps.executeUpdate(sql);
        }
        catch (SQLException x) {
            throw translator.translate(x);
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatement(com.ibm.fhir.database.utils.api.IDatabaseStatement)
     */
    @Override
    public void runStatement(IDatabaseTranslator translator, IDatabaseStatement statement) {
        statement.run(translator, connection);
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseTarget#runStatement(com.ibm.fhir.database.utils.api.IDatabaseTranslator, com.ibm.fhir.database.utils.api.IDatabaseSupplier)
     */
    @Override
    public <T> T runStatement(IDatabaseTranslator translator, IDatabaseSupplier<T> supplier) {
        // execute the statement using the given translator and the connection held by this
        return supplier.run(translator, connection);
    }

}
