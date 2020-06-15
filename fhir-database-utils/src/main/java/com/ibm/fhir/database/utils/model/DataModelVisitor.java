/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.database.utils.model;


/**
 * Visitor interface used to traverse the PhysicalDataModel
 */
public interface DataModelVisitor {

    /**
     * Process a foreign key constraint from the model
     * @param fk
     */
    public void visited(Table fromChildTable, ForeignKeyConstraint fk);
    
    /**
     * Process a {@link Table} from the model
     * @param tbl
     */
    public void visited(Table tbl);
    
    /**
     * A NOP (no operation). Should do nothing, but we let the visitor implementation decide.
     */
    public void nop();

    /**
     * Process a ProcedureDef from the model.
     * @param procedureDef
     */
    public void visited(ProcedureDef procedureDef);

    /**
     * @param rowArrayType
     */
    public void visited(RowArrayType rowArrayType);

    /**
     * @param rowType
     */
    public void visited(RowType rowType);

    /**
     * @param sequence
     */
    public void visited(Sequence sequence);

    /**
     * @param sessionVariableDef
     */
    public void visited(SessionVariableDef sessionVariableDef);

    /**
     * @param tablespace
     */
    public void visited(Tablespace tablespace);

    /**
     * @param functionDef
     */
    public void visited(FunctionDef functionDef);
}
