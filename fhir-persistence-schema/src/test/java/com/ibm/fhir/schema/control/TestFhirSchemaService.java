/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.schema.control;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.ibm.fhir.database.utils.common.PrintTarget;
import com.ibm.fhir.database.utils.db2.Db2Adapter;
import com.ibm.fhir.database.utils.db2.Db2Translator;
import com.ibm.fhir.database.utils.derby.DerbyAdapter;
import com.ibm.fhir.database.utils.model.PhysicalDataModel;
import com.ibm.fhir.database.utils.model.Table;
import com.ibm.fhir.database.utils.version.CreateVersionHistory;
import com.ibm.fhir.schema.control.FhirSchemaConstants;
import com.ibm.fhir.schema.control.FhirSchemaGenerator;
import com.ibm.fhir.task.api.ITaskCollector;
import com.ibm.fhir.task.core.service.TaskService;

/**
 * @author rarnold
 *
 */
public class TestFhirSchemaService {
    private static final Logger logger = Logger.getLogger(TestFhirSchemaService.class.getName());
    private static final String SCHEMA_NAME = "PTNG";
    private static final String ADMIN_SCHEMA_NAME = "ADMIN_FHIR";

    @Test
    public void testDb2TableCreation() {

        logger.info("Testing DB2 schema creation");

        // Create an instance of the service and use it to test creation
        // of the FHIR schema
        FhirSchemaGenerator gen = new FhirSchemaGenerator(ADMIN_SCHEMA_NAME, SCHEMA_NAME);
        PhysicalDataModel model = new PhysicalDataModel();
        gen.buildSchema(model);

        // Print the statements instead of executing them against a database
        PrintTarget tgt = new PrintTarget(null, logger.isLoggable(Level.FINE));

        // Pretend that our target is a DB2 database
        Db2Adapter adapter = new Db2Adapter(tgt);
        model.apply(adapter);
    }

    @Test
    public void testParallelTableCreation() {
        logger.info("Testing DB2 parallel schema build");

        // Create an instance of the service and use it to test creation
        // of the FHIR schema
        FhirSchemaGenerator gen = new FhirSchemaGenerator(ADMIN_SCHEMA_NAME, SCHEMA_NAME);
        PhysicalDataModel model = new PhysicalDataModel();
        gen.buildSchema(model);

        TestVersionHistoryService vhs = new TestVersionHistoryService();

        TaskService taskService = new TaskService();
        ExecutorService pool = Executors.newFixedThreadPool(40);
        ITaskCollector collector = taskService.makeTaskCollector(pool);
        PrintTarget tgt = new PrintTarget(null, logger.isLoggable(Level.FINE));
        Db2Adapter adapter = new Db2Adapter(tgt);
        model.collect(collector, adapter, new TestTransactionProvider(), vhs);

        // FHIR in the hole!
        collector.startAndWait();

    }

    @Test
    public void testDerbyTableCreation() {

        logger.info("Testing Derby schema creation");

        // Create an instance of the service and use it to test creation
        // of the FHIR schema
        FhirSchemaGenerator gen = new FhirSchemaGenerator(ADMIN_SCHEMA_NAME, SCHEMA_NAME);
        PhysicalDataModel model = new PhysicalDataModel();
        gen.buildSchema(model);

        // Print the statements instead of executing them against a database
        PrintTarget tgt = new PrintTarget(null, logger.isLoggable(Level.FINE));

        // Pretend that our target is a Derby database
        DerbyAdapter adapter = new DerbyAdapter(tgt);
        model.apply(adapter);
    }

    @Test
    public void testTenantPartitioning() {

        // Create an instance of the service and use it to test creation
        // of the FHIR schema
        FhirSchemaGenerator gen = new FhirSchemaGenerator(ADMIN_SCHEMA_NAME, SCHEMA_NAME);
        PhysicalDataModel model = new PhysicalDataModel();
        gen.buildSchema(model);

        // Pretend that our target is a DB2 database. Need to use a connection provider
        // for this particular
        // test
        Db2Translator translator = new Db2Translator();
        TestConnectionProvider cp = new TestConnectionProvider(translator);
        Db2Adapter adapter = new Db2Adapter(cp);

        // Exercise the partitioning
        List<Table> tables = new ArrayList<>();
        adapter.createTenantPartitions(tables, SCHEMA_NAME, 10, FhirSchemaConstants.FHIR_TS_EXTENT_KB);
    }

    @Test
    public void testDrop() {

        logger.info("Testing schema drop");

        // Create an instance of the service and use it to test creation
        // of the FHIR schema
        FhirSchemaGenerator gen = new FhirSchemaGenerator(ADMIN_SCHEMA_NAME, SCHEMA_NAME);
        PhysicalDataModel model = new PhysicalDataModel();
        gen.buildSchema(model);

        // Print the statements instead of executing them against a database
        PrintTarget tgt = new PrintTarget(null, logger.isLoggable(Level.FINE));

        // Pretend that our target is a DB2 database
        Db2Adapter adapter = new Db2Adapter(tgt);
        model.drop(adapter);
    }

    @Test
    public void testVersionHistorySchema() {
        // Create a simple print target so all we see is the DDL
        PrintTarget tgt = new PrintTarget(null, logger.isLoggable(Level.FINE));

        // Pretend that our target is a DB2 database
        Db2Adapter adapter = new Db2Adapter(tgt);
        CreateVersionHistory.createTableIfNeeded(ADMIN_SCHEMA_NAME, adapter);

    }
}
