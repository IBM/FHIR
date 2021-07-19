/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import com.ibm.fhir.search.SearchConstants.Modifier;

public interface IQueryParameter {

    Boolean getMissing();

    Modifier getModifier();

    String getParameterValue();
}
