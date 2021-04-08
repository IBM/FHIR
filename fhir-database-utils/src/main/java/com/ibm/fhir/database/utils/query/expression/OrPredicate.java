/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query.expression;

/**
 * Implements the SQL "OR" predicate
 */
public class OrPredicate extends BinaryPredicate {

    /**
     * Public constructor
     * @param left
     * @param right
     */
    public OrPredicate(Predicate left, Predicate right) {
        super(left, right);
    }

    @Override
    public <T> T render(StatementRenderer<T> renderer) {
        T left = getLeft().render(renderer);
        T right = getRight().render(renderer);
        return renderer.or(left, right);
    }

}
