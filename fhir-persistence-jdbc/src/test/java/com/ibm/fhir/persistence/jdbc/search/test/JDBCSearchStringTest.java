/*
 * (C) Copyright IBM Corp. 2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.search.test;

import java.util.Properties;

import com.ibm.fhir.model.test.TestUtil;
import com.ibm.fhir.persistence.FHIRPersistence;
import com.ibm.fhir.persistence.jdbc.impl.FHIRPersistenceJDBCImpl;
import com.ibm.fhir.persistence.jdbc.test.util.DerbyInitializer;
import com.ibm.fhir.persistence.search.test.AbstractSearchStringTest;


public class JDBCSearchStringTest extends AbstractSearchStringTest {

    private Properties testProps;

    public JDBCSearchStringTest() throws Exception {
        this.testProps = TestUtil.readTestProperties("test.jdbc.properties");
    }

    @Override
    public void bootstrapDatabase() throws Exception {
        String dbDriverName = this.testProps.getProperty("dbDriverName");
        if (dbDriverName != null && dbDriverName.contains("derby")) {
            DerbyInitializer derbyInit = new DerbyInitializer(this.testProps);
            derbyInit.bootstrapDb();
        }
    }

    @Override
    public FHIRPersistence getPersistenceImpl() throws Exception {
        return new FHIRPersistenceJDBCImpl(this.testProps);
    }
}
