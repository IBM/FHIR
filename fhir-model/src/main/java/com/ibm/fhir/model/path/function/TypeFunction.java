/*
 * (C) Copyright IBM Corp. 2019
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.path.function;

import static com.ibm.fhir.model.path.util.FHIRPathUtil.buildClassInfo;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.buildSimpleTypeInfo;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.buildTupleTypeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.FHIRPathType;
import com.ibm.fhir.model.path.FHIRPathTypeInfoNode;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class TypeFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "type";
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
        List<FHIRPathNode> result = new ArrayList<>();
        for (FHIRPathNode node : context) {
            FHIRPathType type = node.type();
            if ("System".equals(type.namespace()) && !FHIRPathType.SYSTEM_TYPE_INFO.isAssignableFrom(type)) {
                result.add(FHIRPathTypeInfoNode.typeInfoNode(buildSimpleTypeInfo(type)));
            } else if (FHIRPathType.FHIR_BACKBONE_ELEMENT.equals(type)){
                result.add(FHIRPathTypeInfoNode.typeInfoNode(buildTupleTypeInfo(node.asElementNode().element().getClass())));
            } else {
                result.add(FHIRPathTypeInfoNode.typeInfoNode(buildClassInfo(type)));
            }
        }
        return result;
    }
}