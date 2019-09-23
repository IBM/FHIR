/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.path.function;

import static com.ibm.fhir.model.path.util.FHIRPathUtil.empty;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.getSingleton;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.isSingleton;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.singleton;

import java.util.Collection;
import java.util.List;

import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class GetValueFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "getValue";
    }

    @Override
    public int getMinArity() {
        return -1;
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
                return singleton(node.asElementNode().getValue());
            }
        }
        return empty();
    }
}
