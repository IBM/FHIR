/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.schema.control;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ibm.fhir.database.utils.api.IDatabaseSupplier;
import com.ibm.fhir.database.utils.api.IDatabaseTranslator;
import com.ibm.fhir.schema.model.ResourceType;

/**
 * @author rarnold
 *
 */
public class Db2GetResourceTypeList implements IDatabaseSupplier<List<ResourceType>> {
    private final String schemaName;

    public Db2GetResourceTypeList(String schemaName) {
        this.schemaName = schemaName;
    }

    /* (non-Javadoc)
     * @see com.ibm.fhir.database.utils.api.IDatabaseSupplier#run(com.ibm.fhir.database.utils.api.IDatabaseTranslator, java.sql.Connection)
     */
    @Override
    public List<ResourceType> run(IDatabaseTranslator translator, Connection c) {
        List<ResourceType> result = new ArrayList<>();

        final String SQL = ""
                + "SELECT resource_type_id, resource_type "
                + "  FROM " + schemaName + ".RESOURCE_TYPES";

        try (Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery(SQL);
            while (rs.next()) {
                ResourceType rt = new ResourceType();
                rt.setId(rs.getLong(1));
                rt.setName(rs.getString(2));
                result.add(rt);
            }
        }
        catch (SQLException x) {
            throw translator.translate(x);
        }

        return result;
    }

}
