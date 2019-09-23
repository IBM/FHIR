/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.path.function;

import static com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.getSingleton;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.isSingleton;

import java.util.Collection;
import java.util.List;

import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class HasValueFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "hasValue";
    }

    @Override
    public int getMinArity() {
        return 0;
    }

    @Override
    public int getMaxArity() {
        return 0;
    }

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        if (isSingleton(context)) {
            FHIRPathNode node = getSingleton(context);
            if (node.isElementNode() && node.asElementNode().hasValue()) {
                return SINGLETON_TRUE;
            }
        }
        return SINGLETON_FALSE;        
    }
}
