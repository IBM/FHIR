/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * This package provides model-independent classes for evaluating FHIR clinical
 * quality measures. This code was ported from the cql-evaluator/evaluator.measure
 * module and adapted to remove an unnecessary dependency on the HAPI FHIR model
 * and to support supplemental data elements as seen in the cqf-ruler project.
 * 
 * @see https://github.com/DBCG/cql-evaluator
 * @see https://github.com/DBCG/cqf-ruler
 */
package com.ibm.fhir.ecqm.common;