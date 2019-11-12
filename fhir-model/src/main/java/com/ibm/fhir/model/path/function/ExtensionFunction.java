/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.path.function;

import static com.ibm.fhir.model.path.util.FHIRPathUtil.getStringValue;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.hasStringValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import com.ibm.fhir.model.type.Extension;

public class ExtensionFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "extension";
    }

    @Override
    public int getMinArity() {
        return 1;
    }

    @Override
    public int getMaxArity() {
        return 1;
    }
    
    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        List<FHIRPathNode> result = new ArrayList<>();
        if (hasStringValue(arguments.get(0))) {
            String url = getStringValue(arguments.get(0)).string();
            for (FHIRPathNode node : context) {
                if (node.isElementNode() && node.asElementNode().element().is(Extension.class)) {
                    Extension extension = node.asElementNode().element().as(Extension.class);
                    if (extension.getUrl().equals(url)) {
                        result.add(node);
                    }
                } 
            }      
        }
        return result;
    }
}
