/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.schema.control;

import static com.ibm.fhir.schema.control.FhirSchemaConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.ibm.fhir.database.utils.model.GroupPrivilege;
import com.ibm.fhir.database.utils.model.IDatabaseObject;
import com.ibm.fhir.database.utils.model.ObjectGroup;
import com.ibm.fhir.database.utils.model.PhysicalDataModel;
import com.ibm.fhir.database.utils.model.SessionVariableDef;
import com.ibm.fhir.database.utils.model.Table;
import com.ibm.fhir.database.utils.model.Tablespace;

/**
 * Utility to create all the tables associated with a particular resource type
 */
public class FhirResourceGroup {
    // The model containing all the tables for the entire schema
    private final PhysicalDataModel model;

    // The schema we place all of our tables into
    private final String schemaName;

    // The session variable we depend on for access control
    private final SessionVariableDef sessionVariable;

    // All the tables created by this component
    @SuppressWarnings("unused")
    private final Set<IDatabaseObject> procedureDependencies;

    private final Tablespace fhirTablespace;

    // Privileges to be granted to each of the resource tables created by this class
    private final Collection<GroupPrivilege> resourceTablePrivileges;

    private static final String _LOGICAL_RESOURCES = "_LOGICAL_RESOURCES";
    private static final String _RESOURCES = "_RESOURCES";

    /**
     * Public constructor
     * @param model
     */
    public FhirResourceGroup(PhysicalDataModel model, String schemaName, SessionVariableDef sessionVariable,
            Set<IDatabaseObject> procedureDependencies, Tablespace fhirTablespace, Collection<GroupPrivilege> privileges) {
        this.model = model;
        this.schemaName = schemaName;
        this.sessionVariable = sessionVariable;
        this.procedureDependencies = procedureDependencies;
        this.fhirTablespace = fhirTablespace;
        this.resourceTablePrivileges = privileges;
    }

    /**
     * Add all the tables required for the given resource type. For example, if the
     * resourceTypeName is Patient, the following tables will be added:
     * - patient_logical_resources
     * - patient_resources
     * - patient_str_values
     * - patient_date_values
     * - patient_token_values
     * - patient_number_values
     * - patient_latlng_values
     * - patient_quantity_values
     * @param resourceTypeName
     */
    public ObjectGroup addResourceType(String resourceTypeName) {
        final String tablePrefix = resourceTypeName.toUpperCase();

        // Stick all the objects we want to create under one group which is executed
        // in the order in which they are defined (not parallelized)
        List<IDatabaseObject> group = new ArrayList<>();

        addLogicalResources(group, tablePrefix);
        addResources(group, tablePrefix);
        addStrValues(group, tablePrefix);
        addTokenValues(group, tablePrefix);
        addDateValues(group, tablePrefix);
        addNumberValues(group, tablePrefix);
        addLatLngValues(group, tablePrefix);
        addQuantityValues(group, tablePrefix);

        // group all the tables under one object so that we can perform everything within one
        // transaction. This helps to eliminate deadlocks when adding the FK constraints due to
        // issues with DB2 managing its catalog
        return new ObjectGroup(schemaName, tablePrefix + "_RESOURCE_TABLE_GROUP", group);
    }

    /**
     * Add the logical_resources table definition for the given resource prefix
     * @param prefix
     */
    public void addLogicalResources(List<IDatabaseObject> group, String prefix) {
        final String tableName = prefix + "_LOGICAL_RESOURCES";

        // This is the resource-specific instance of the logical resources table, and
        // shares a common primary key (logical_resource_id) with the system-wide table
        // We also have a FK constraint pointing back to that table to try and keep
        // things sensible.
        Table tbl = Table.builder(schemaName, tableName)
                .setTenantColumnName(MT_ID)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .addBigIntColumn(LOGICAL_RESOURCE_ID, false)
                .addVarcharColumn(LOGICAL_ID, LOGICAL_ID_BYTES, false)
                .addBigIntColumn(CURRENT_RESOURCE_ID, true)
                .addPrimaryKey(tableName + "_PK", LOGICAL_RESOURCE_ID)
                .addForeignKeyConstraint("FK_" + tableName + "_LRID", schemaName, LOGICAL_RESOURCES, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model);

        group.add(tbl);
        model.addTable(tbl);
        
        
        // Special case for LIST resource...we need a table to store the list items
        if ("LIST".equalsIgnoreCase(prefix)) {
            addListLogicalResourceItems(group, prefix);
        }
        
        // Extension table for patient to support references to current lists 
        // such as $current-allergies
        // https://www.hl7.org/fhir/lifecycle.html#current
        if ("PATIENT".equalsIgnoreCase(prefix)) {
            addPatientCurrentRefs(group, prefix);
        }
    }

    /**
     * Add the resources table definition
     * resource_id, 
     * logical_resource_id, 
     * version_id, 
     * data, 
     * last_updated, 
     * is_deleted, 
  tenant_id                 INT             NOT NULL,
  resource_id            BIGINT             NOT NULL,
  logical_resource_id    BIGINT             NOT NULL,
  version_id                INT             NOT NULL,
  last_updated        TIMESTAMP             NOT NULL,
  is_deleted               CHAR(1)          NOT NULL,
  data                     BLOB(2147483647) INLINE LENGTH 10240;

  CREATE UNIQUE INDEX device_resource_prf_in1    ON device_resources (resource_id) INCLUDE (logical_resource_id, version_id, is_deleted);

     * @param prefix
     */
    public void addResources(List<IDatabaseObject> group, String prefix) {

        // The index which also used by the database to support the primary key constraint
        final List<String> prfIndexCols = Arrays.asList(RESOURCE_ID);
        final List<String> prfIncludeCols = Arrays.asList(LOGICAL_RESOURCE_ID, VERSION_ID, IS_DELETED);
        final String tableName = prefix + _RESOURCES;

        // Issue #364: The values identified here are unused, and for backwards compatibility, 
        // These values are maintained so the stored procedure agrees with the table definition. 
        final String TX_CORRELATION_ID = "TX_CORRELATION_ID";  
        final String CHANGED_BY = "CHANGED_BY"; 
        final String CORRELATION_TOKEN = "CORRELATION_TOKEN";
        final String REASON = "REASON";
        final String SERVICE_ID = "SERVICE_ID";

        Table tbl = Table.builder(schemaName, tableName)
                .setTenantColumnName(MT_ID)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .addBigIntColumn(        RESOURCE_ID,              false)
                .addBigIntColumn(LOGICAL_RESOURCE_ID,              false)
                .addIntColumn(            VERSION_ID,              false)
                .addTimestampColumn(    LAST_UPDATED,              false)
                .addCharColumn(           IS_DELETED,           1, false)
                .addBlobColumn(                 DATA,  2147483647,  10240,   true)
                // Start Backwards Compatibility 
                .addVarcharColumn(TX_CORRELATION_ID, 36, true)
                .addVarcharColumn(CHANGED_BY, 64, true)
                .addVarcharColumn(CORRELATION_TOKEN, 36, true) 
                .addVarcharColumn(REASON, 255, true)
                .addVarcharColumn(SERVICE_ID, 32, true)
                // End Backwards Compatibility
                .addUniqueIndex(tableName + "_PRF_IN1", prfIndexCols, prfIncludeCols)
                .addPrimaryKey(tableName + "_PK", RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model);

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * Add the STR_VALUES table for the given resource name prefix

  tenant_id                INT             NOT NULL,
  parameter_name_id        INT             NOT NULL,
  str_value            VARCHAR(511 OCTETS),
  str_value_lcase      VARCHAR(511 OCTETS),
  resource_id           BIGINT             NOT NULL


CREATE INDEX idx_device_str_values_psr ON device_str_values(parameter_name_id, str_value, resource_id);
CREATE INDEX idx_device_str_values_plr ON device_str_values(parameter_name_id, str_value_lcase, resource_id);
CREATE INDEX idx_device_str_values_rps ON device_str_values(resource_id, parameter_name_id, str_value);
CREATE INDEX idx_device_str_values_rpl ON device_str_values(resource_id, parameter_name_id, str_value_lcase);
ALTER TABLE device_str_values ADD CONSTRAINT fk_device_str_values_pnid FOREIGN KEY (parameter_name_id) REFERENCES parameter_names;
ALTER TABLE device_str_values ADD CONSTRAINT fk_device_str_values_rid  FOREIGN KEY (resource_id) REFERENCES device_resources;

     * 
     * @param prefix
     */
    public void addStrValues(List<IDatabaseObject> group, String prefix) {

        final int msb = MAX_SEARCH_STRING_BYTES;
        final String tableName = prefix + "_STR_VALUES";
        final String logicalResourcesTable = prefix + "_LOGICAL_RESOURCES";

        // Parameters are tied to the logical resource
        Table tbl = Table.builder(schemaName, tableName)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addIntColumn(     PARAMETER_NAME_ID,      false)
                .addVarcharColumn(         STR_VALUE, msb,  true)
                .addVarcharColumn(   STR_VALUE_LCASE, msb,  true)
                .addBigIntColumn(LOGICAL_RESOURCE_ID,      false)
                .addIndex(IDX + tableName + "_PSR", PARAMETER_NAME_ID, STR_VALUE, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_PLR", PARAMETER_NAME_ID, STR_VALUE_LCASE, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_RPS", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, STR_VALUE)
                .addIndex(IDX + tableName + "_RPL", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, STR_VALUE_LCASE)
                .addForeignKeyConstraint(FK + tableName + "_PNID", schemaName, PARAMETER_NAMES, PARAMETER_NAME_ID)
                .addForeignKeyConstraint(FK + tableName + "_RID", schemaName, logicalResourcesTable, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * 
  parameter_name_id        INT NOT NULL,
  code_system_id           INT NOT NULL,
  token_value          VARCHAR(255 OCTETS),
  resource_id           BIGINT NOT NULL
)
;

CREATE INDEX idx_device_token_values_pncscv ON device_token_values(parameter_name_id, code_system_id, token_value, resource_id);
CREATE INDEX idx_device_token_values_rps ON device_token_values(resource_id, parameter_name_id, code_system_id, token_value);
ALTER TABLE device_token_values ADD CONSTRAINT fk_device_token_values_pn FOREIGN KEY (parameter_name_id) REFERENCES parameter_names;
ALTER TABLE device_token_values ADD CONSTRAINT fk_device_token_values_cs FOREIGN KEY (code_system_id)    REFERENCES code_systems;
ALTER TABLE device_token_values ADD CONSTRAINT fk_device_token_values_r  FOREIGN KEY (resource_id)       REFERENCES device_resources;
     * @param prefix
     */
    public void addTokenValues(List<IDatabaseObject> group, String prefix) {
        final String tableName = prefix + "_TOKEN_VALUES";
        final String logicalResourcesTable = prefix + _LOGICAL_RESOURCES;

        Table tbl = Table.builder(schemaName, tableName)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addIntColumn(     PARAMETER_NAME_ID,      false)
                .addIntColumn(        CODE_SYSTEM_ID,      false)
                .addVarcharColumn(       TOKEN_VALUE, 511,  true)
                .addBigIntColumn(LOGICAL_RESOURCE_ID,      false)
                .addIndex(IDX + tableName + "_PNCSCV", PARAMETER_NAME_ID, CODE_SYSTEM_ID, TOKEN_VALUE, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_RPS", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, CODE_SYSTEM_ID, TOKEN_VALUE)
                .addForeignKeyConstraint(FK + tableName + "_PN", schemaName, PARAMETER_NAMES, PARAMETER_NAME_ID)
                .addForeignKeyConstraint(FK + tableName + "_CS", schemaName, CODE_SYSTEMS, CODE_SYSTEM_ID)
                .addForeignKeyConstraint(FK + tableName + "_R", schemaName, logicalResourcesTable, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * CREATE TABLE device_date_values  (
  parameter_name_id         INT NOT NULL,
  date_value          TIMESTAMP,
  date_start          TIMESTAMP,
  date_end            TIMESTAMP,
  resource_id            BIGINT NOT NULL
)
;

CREATE INDEX idx_device_date_values_pvr ON device_date_values(parameter_name_id, date_value, resource_id);
CREATE INDEX idx_device_date_values_rpv  ON device_date_values(resource_id, parameter_name_id, date_value);
CREATE INDEX idx_device_date_values_pser ON device_date_values(parameter_name_id, date_start, date_end, resource_id);
CREATE INDEX idx_device_date_values_pesr ON device_date_values(parameter_name_id, date_end, date_start, resource_id);
CREATE INDEX idx_device_date_values_rpse   ON device_date_values(resource_id, parameter_name_id, date_start, date_end);
ALTER TABLE device_date_values ADD CONSTRAINT fk_device_date_values_pn FOREIGN KEY (parameter_name_id) REFERENCES parameter_names;
ALTER TABLE device_date_values ADD CONSTRAINT fk_device_date_values_r  FOREIGN KEY (resource_id)       REFERENCES device_resources;

     * @param prefix
     */
    public void addDateValues(List<IDatabaseObject> group, String prefix) {
        final String tableName = prefix + "_DATE_VALUES";
        final String logicalResourcesTable = prefix + _LOGICAL_RESOURCES;
        
        Table tbl = Table.builder(schemaName, tableName)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addIntColumn(     PARAMETER_NAME_ID,      false)
                .addTimestampColumn(      DATE_VALUE,      true)
                .addTimestampColumn(      DATE_START,      true)
                .addTimestampColumn(        DATE_END,      true)
                .addBigIntColumn(LOGICAL_RESOURCE_ID,      false)
                .addIndex(IDX + tableName + "_PVR", PARAMETER_NAME_ID, DATE_VALUE, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_RPV", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, DATE_VALUE)
                .addIndex(IDX + tableName + "_PSER", PARAMETER_NAME_ID, DATE_START, DATE_END, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_PESR", PARAMETER_NAME_ID, DATE_END, DATE_START, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_RPSE", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, DATE_START, DATE_END)
                .addForeignKeyConstraint(FK + tableName + "_PN", schemaName, PARAMETER_NAMES, PARAMETER_NAME_ID)
                .addForeignKeyConstraint(FK + tableName + "_R", schemaName, logicalResourcesTable, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * -- ----------------------------------------------------------------------------
--
-- ----------------------------------------------------------------------------
CREATE TABLE device_number_values  (
  parameter_name_id        INT NOT NULL,
  number_value        DOUBLE,
  resource_id         BIGINT NOT NULL
)
;
CREATE INDEX idx_device_number_values_pnnv ON device_number_values(parameter_name_id, number_value, resource_id);
CREATE INDEX idx_device_number_values_rps ON device_number_values(resource_id, parameter_name_id, number_value);
ALTER TABLE device_number_values ADD CONSTRAINT fk_device_number_values_pn FOREIGN KEY (parameter_name_id) REFERENCES parameter_names ON DELETE CASCADE;
ALTER TABLE device_number_values ADD CONSTRAINT fk_device_number_values_r  FOREIGN KEY (resource_id)       REFERENCES device_resources ON DELETE CASCADE;
     * @param prefix
     */
    public void addNumberValues(List<IDatabaseObject> group, String prefix) {
        final String tableName = prefix + "_NUMBER_VALUES";
        final String logicalResourcesTable = prefix + _LOGICAL_RESOURCES;

        Table tbl = Table.builder(schemaName, tableName)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addIntColumn(     PARAMETER_NAME_ID,      false)
                .addDoubleColumn(       NUMBER_VALUE,       true)
                .addBigIntColumn(LOGICAL_RESOURCE_ID,      false)
                .addIndex(IDX + tableName + "_PNNV", PARAMETER_NAME_ID, NUMBER_VALUE, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_RPS", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, NUMBER_VALUE)
                .addForeignKeyConstraint(FK + tableName + "_PN", schemaName, PARAMETER_NAMES, PARAMETER_NAME_ID)
                .addForeignKeyConstraint(FK + tableName + "_RID", schemaName, logicalResourcesTable, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * 
     * CREATE TABLE device_latlng_values  (
  parameter_name_id   INT NOT NULL,
  latitude_value      DOUBLE,
  longitude_value     DOUBLE,
  resource_id         BIGINT NOT NULL
)
CREATE INDEX idx_device_latlng_values_pnnlv ON device_latlng_values(parameter_name_id, latitude_value, resource_id);
CREATE INDEX idx_device_latlng_values_pnnhv ON device_latlng_values(parameter_name_id, longitude_value, resource_id);
CREATE INDEX idx_device_latlng_values_rplat ON device_latlng_values(resource_id, parameter_name_id, latitude_value);
CREATE INDEX idx_device_latlng_values_rplng ON device_latlng_values(resource_id, parameter_name_id, longitude_value);
ALTER TABLE device_latlng_values ADD CONSTRAINT fk_device_latlng_values_pn FOREIGN KEY (parameter_name_id) REFERENCES parameter_names;
ALTER TABLE device_latlng_values ADD CONSTRAINT fk_device_latlng_values_r  FOREIGN KEY (resource_id)       REFERENCES device_resources;


     * @param prefix
     */
    public void addLatLngValues(List<IDatabaseObject> group, String prefix) {
        final String tableName = prefix + "_LATLNG_VALUES";
        final String logicalResourcesTable = prefix + _LOGICAL_RESOURCES;

        Table tbl = Table.builder(schemaName, tableName)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addIntColumn(     PARAMETER_NAME_ID,      false)
                .addDoubleColumn(     LATITUDE_VALUE,       true)
                .addDoubleColumn(    LONGITUDE_VALUE,       true)
                .addBigIntColumn(LOGICAL_RESOURCE_ID,      false)
                .addIndex(IDX + tableName + "_PNNLV", PARAMETER_NAME_ID, LATITUDE_VALUE, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_PNNHV", PARAMETER_NAME_ID, LONGITUDE_VALUE, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_RPLAT", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, LATITUDE_VALUE)
                .addIndex(IDX + tableName + "_RPLNG", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, LONGITUDE_VALUE)
                .addForeignKeyConstraint(FK + tableName + "_PN", schemaName, PARAMETER_NAMES, PARAMETER_NAME_ID)
                .addForeignKeyConstraint(FK + tableName + "_RID", schemaName, logicalResourcesTable, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * 
     * CREATE TABLE device_quantity_values  (
  parameter_name_id        INT NOT NULL,
  code                 VARCHAR(255 OCTETS) NOT NULL,
  quantity_value        DOUBLE,
  quantity_value_low    DOUBLE,
  quantity_value_high   DOUBLE,
  code_system_id           INT,
  resource_id           BIGINT NOT NULL
)
;

CREATE INDEX idx_device_quantity_values_pnnv   ON device_quantity_values(parameter_name_id, code, quantity_value, resource_id, code_system_id);
CREATE INDEX idx_device_quantity_values_rps    ON device_quantity_values(resource_id, parameter_name_id, code, quantity_value, code_system_id);

CREATE INDEX idx_device_quantity_values_pclhsr  ON device_quantity_values(parameter_name_id, code, quantity_value_low, quantity_value_high, code_system_id, resource_id);
CREATE INDEX idx_device_quantity_values_pchlsr  ON device_quantity_values(parameter_name_id, code, quantity_value_high, quantity_value_low, code_system_id, resource_id);
CREATE INDEX idx_device_quantity_values_rpclhs  ON device_quantity_values(resource_id, parameter_name_id, code, quantity_value_low, quantity_value_high, code_system_id);
CREATE INDEX idx_device_quantity_values_rpchls  ON device_quantity_values(resource_id, parameter_name_id, code, quantity_value_high, quantity_value_low, code_system_id);

ALTER TABLE device_quantity_values ADD CONSTRAINT fk_device_quantity_values_pn FOREIGN KEY (parameter_name_id) REFERENCES parameter_names ON DELETE CASCADE;
ALTER TABLE device_quantity_values ADD CONSTRAINT fk_device_quantity_values_r  FOREIGN KEY (resource_id)       REFERENCES device_resources ON DELETE CASCADE;

     * @param prefix
     */
    public void addQuantityValues(List<IDatabaseObject> group, String prefix) {
        final String tableName = prefix + "_QUANTITY_VALUES";
        final String logicalResourcesTable = prefix + _LOGICAL_RESOURCES;

        Table tbl = Table.builder(schemaName, tableName)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addIntColumn(     PARAMETER_NAME_ID,      false)
                .addVarcharColumn(              CODE, 255, false)
                .addDoubleColumn(     QUANTITY_VALUE,      true)
                .addDoubleColumn( QUANTITY_VALUE_LOW,      true)
                .addDoubleColumn(QUANTITY_VALUE_HIGH,      true)
                .addIntColumn(        CODE_SYSTEM_ID,      true)
                .addBigIntColumn(LOGICAL_RESOURCE_ID,      false)
                .addIndex(IDX + tableName + "_PNNV", PARAMETER_NAME_ID, CODE, QUANTITY_VALUE, LOGICAL_RESOURCE_ID, CODE_SYSTEM_ID)
                .addIndex(IDX + tableName + "_RPS", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, CODE, QUANTITY_VALUE, CODE_SYSTEM_ID)
                .addIndex(IDX + tableName + "_PCLHSR", PARAMETER_NAME_ID, CODE, QUANTITY_VALUE_LOW, QUANTITY_VALUE_HIGH, CODE_SYSTEM_ID, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_PCHLSR", PARAMETER_NAME_ID, CODE, QUANTITY_VALUE_HIGH, QUANTITY_VALUE_LOW, CODE_SYSTEM_ID, LOGICAL_RESOURCE_ID)
                .addIndex(IDX + tableName + "_RPCLHS", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, CODE, QUANTITY_VALUE_LOW, QUANTITY_VALUE_HIGH, CODE_SYSTEM_ID)
                .addIndex(IDX + tableName + "_RPCHLS", LOGICAL_RESOURCE_ID, PARAMETER_NAME_ID, CODE, QUANTITY_VALUE_HIGH, QUANTITY_VALUE_LOW, CODE_SYSTEM_ID)
                .addForeignKeyConstraint(FK + tableName + "_PN", schemaName, PARAMETER_NAMES, PARAMETER_NAME_ID)
                .addForeignKeyConstraint(FK + tableName + "_R", schemaName, logicalResourcesTable, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * Special case for LIST resources where we attach a child table to its LIST_LOGICAL_RESOURCES
     * to support usage of the list items in search queries. The FK to LIST_LOGICAL_RESOURCES is
     * its parent. We then point to the resource being referenced via a resourceType/logicalId
     * tuple. This means that the list item record can be created before the referenced resource
     * is created.
     * @param group
     * @param prefix
     */
    public void addListLogicalResourceItems(List<IDatabaseObject> group, String prefix) {
        final int lib = LOGICAL_ID_BYTES;

        Table tbl = Table.builder(schemaName, LIST_LOGICAL_RESOURCE_ITEMS)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addBigIntColumn( LOGICAL_RESOURCE_ID,      false)
                .addIntColumn(       RESOURCE_TYPE_ID,      false)
                .addVarcharColumn(    ITEM_LOGICAL_ID, lib,  true)
                .addForeignKeyConstraint(FK + LIST_LOGICAL_RESOURCE_ITEMS + "_LRID", schemaName, LIST_LOGICAL_RESOURCES, LOGICAL_RESOURCE_ID)
                .addForeignKeyConstraint(FK + LIST_LOGICAL_RESOURCE_ITEMS + "_RTID", schemaName, RESOURCE_TYPES, RESOURCE_TYPE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

    /**
     * Add the extension table used to support references to the current
     * resources lists defined by the spec: https://www.hl7.org/fhir/lifecycle.html#current
     * @param group - the group of tables for this resource (Patient in this case)
     * @param prefix - the resource name - PATIENT
     */
    public void addPatientCurrentRefs(List<IDatabaseObject> group, String prefix) {
        final int lib = LOGICAL_ID_BYTES;
        
        // The CURRENT_*_LIST columns are the logical_id values of the
        // LIST resources used to host these special lists. We don't
        // model with a foreign key to avoid order of insertion issues

        Table tbl = Table.builder(schemaName, PATIENT_CURRENT_REFS)
                .addTag(FhirSchemaTags.RESOURCE_TYPE, prefix)
                .setTenantColumnName(MT_ID)
                .addBigIntColumn(         LOGICAL_RESOURCE_ID,      false)
                .addVarcharColumn(      CURRENT_PROBLEMS_LIST, lib,  true)
                .addVarcharColumn(   CURRENT_MEDICATIONS_LIST, lib,  true)
                .addVarcharColumn(     CURRENT_ALLERGIES_LIST, lib,  true)
                .addVarcharColumn(CURRENT_DRUG_ALLERGIES_LIST, lib,  true)
                .addPrimaryKey("PK_" + PATIENT_CURRENT_REFS, LOGICAL_RESOURCE_ID)
                .addForeignKeyConstraint(FK + PATIENT_CURRENT_REFS + "_LRID", schemaName, PATIENT_LOGICAL_RESOURCES, LOGICAL_RESOURCE_ID)
                .setTablespace(fhirTablespace)
                .addPrivileges(resourceTablePrivileges)
                .enableAccessControl(this.sessionVariable)
                .build(model)
                ;

        group.add(tbl);
        model.addTable(tbl);
    }

}
