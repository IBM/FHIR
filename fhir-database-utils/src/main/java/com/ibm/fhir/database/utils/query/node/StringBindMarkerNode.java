/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query.node;

import com.ibm.fhir.database.utils.query.expression.BindMarkerNodeVisitor;

/**
 * A bind marker representing a String value
 */
public class StringBindMarkerNode extends BindMarkerNode {
    // The string value (can be null)
    private final String value;

    public StringBindMarkerNode(String value) {
        this.value = value;
    }

    @Override
    public <T> T visit(ExpNodeVisitor<T> visitor) {
        return visitor.bindMarker(value);
    }

    @Override
    public void visit(BindMarkerNodeVisitor visitor) {
        visitor.bindString(value);
    }
}