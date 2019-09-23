/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ibm.fhir.database.utils.api.IDatabaseAdapter;

/**
 * Definition of an index on a table
 * @author rarnold
 *
 */
public class IndexDef {
        
    // The name of the index
    private final String indexName;
    
    // The list of columns comprising the index
    private final List<String> indexColumns = new ArrayList<>();
    
    // Is this a unique index?
    private final boolean unique;
    
    // The list of include columns associated with the index
    private final List<String> includeColumns = new ArrayList<>();
    
    public IndexDef(String indexName, Collection<String> indexColumns, boolean unique) {
        this.indexName = indexName;
        this.unique = unique;
        this.indexColumns.addAll(indexColumns);
    }
    
    /**
     * Construct an index definition for a unique index with include columns. Note that it only
     * makes sense for an index with include columns to be unique, so the unique flag is set true
     * @param indexName
     * @param indexColumns
     * @param includeColumns
     */
    public IndexDef(String indexName, Collection<String> indexColumns, Collection<String> includeColumns) {
        this.indexName = indexName;
        this.unique = true;
        this.indexColumns.addAll(indexColumns);
        this.includeColumns.addAll(includeColumns);
    }

    /**
     * Getter for the unique flag
     * @return
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Apply this object to the given database target
     * @param tableName
     * @param target
     */
    public void apply(String schemaName, String tableName, String tenantColumnName, IDatabaseAdapter target) {
        if (includeColumns != null && includeColumns.size() > 0) {
            target.createUniqueIndex(schemaName, tableName, indexName, tenantColumnName, indexColumns, includeColumns);
        }
        else if (unique) {
            target.createUniqueIndex(schemaName, tableName, indexName, tenantColumnName, indexColumns);            
        }
        else {
            target.createIndex(schemaName, tableName, indexName, tenantColumnName, indexColumns);            
        }
    }
}
