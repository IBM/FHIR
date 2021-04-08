/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query.expression;

/**
 * Implements SQL "!="
 */
public class NotEqualsPredicate extends BinaryPredicate {

    public NotEqualsPredicate(Predicate left, Predicate right) {
        super(left, right);
    }

    @Override
    public <T> T render(StatementRenderer<T> renderer) {
        T left = getLeft().render(renderer);
        T right = getRight().render(renderer);
        return renderer.notEquals(left, right);
    }
}