/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.path.function;

import java.util.Collection;
import java.util.List;

import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import com.ibm.fhir.model.path.function.registry.FHIRPathFunctionRegistry;

public interface FHIRPathFunction {
    String getName();
    int getMinArity();
    int getMaxArity();
    
    Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments);
    
    static FHIRPathFunctionRegistry registry() {
        return FHIRPathFunctionRegistry.getInstance();
    }
}
