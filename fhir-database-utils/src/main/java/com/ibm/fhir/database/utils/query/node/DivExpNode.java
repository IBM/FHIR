/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query.node;


/**
 * Multiplication expression node
 */
public class DivExpNode extends MultiplicativeExpNode {

    @Override
    public <T> T visit(ExpNodeVisitor<T> visitor) {
        T leftValue = getLeft().visit(visitor);
        T rightValue = getRight().visit(visitor);
        return visitor.div(leftValue, rightValue);
    }
}