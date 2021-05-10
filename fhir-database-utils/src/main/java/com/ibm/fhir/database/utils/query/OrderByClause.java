/*
 * (C) Copyright IBM Corp. 2019,2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The OrderByClause SQL definition
 */
public class OrderByClause {
    private final List<String> items = new ArrayList<>();

    /**
     * Add the given expressions to the order by items list
     * @param expressions
     */
    public void add(String... expressions) {
        items.addAll(Arrays.asList(expressions));
    }

    @Override
    public String toString() {
        return new StringBuilder("ORDER BY ")
                .append(items.stream().collect(Collectors.joining(", ")))
                .toString();
    }
}