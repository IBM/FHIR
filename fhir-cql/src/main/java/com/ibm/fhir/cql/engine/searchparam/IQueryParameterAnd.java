/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import java.util.List;

public interface IQueryParameterAnd<T extends IQueryParameterOr<? extends IQueryParameter>> {

    List<T> getParameterValues();
}
