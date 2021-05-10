/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query.node;

import java.util.Stack;

/**
 * An expression contained within parens
 */
public class LeftParenExpNode implements ExpNode {

    @Override
    public <T> T visit(ExpNodeVisitor<T> visitor) {
        // NOP. Only used during parse.
        return null;
    }

    @Override
    public int precedence() {
        return 1;
    }

    @Override
    public void popOperands(Stack<ExpNode> stack) {
        // NOP
    }

    @Override
    public boolean isLeftParen() {
        return true;
    }
}