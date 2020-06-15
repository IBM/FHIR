/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.path.function;

import static com.ibm.fhir.model.util.ModelSupport.FHIR_STRING;
import static com.ibm.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_FALSE;
import static com.ibm.fhir.path.evaluator.FHIRPathEvaluator.SINGLETON_TRUE;
import static com.ibm.fhir.path.util.FHIRPathUtil.empty;
import static com.ibm.fhir.path.util.FHIRPathUtil.getElementNode;
import static com.ibm.fhir.path.util.FHIRPathUtil.getString;
import static com.ibm.fhir.path.util.FHIRPathUtil.isCodedElementNode;
import static com.ibm.fhir.path.util.FHIRPathUtil.isStringElementNode;
import static com.ibm.fhir.path.util.FHIRPathUtil.isStringValue;
import static com.ibm.fhir.path.util.FHIRPathUtil.isUriElementNode;
import static com.ibm.fhir.term.util.ValueSetSupport.getValueSet;
import static com.ibm.fhir.term.util.ValueSetSupport.isExpanded;

import java.util.Collection;
import java.util.List;

import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.type.Boolean;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.path.FHIRPathElementNode;
import com.ibm.fhir.path.FHIRPathNode;
import com.ibm.fhir.path.FHIRPathType;
import com.ibm.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import com.ibm.fhir.term.service.FHIRTermService;
import com.ibm.fhir.term.spi.ValidationOutcome;

/**
 * Implementation of the 'memberOf' FHIRPath function per: <a href="http://hl7.org/fhir/fhirpath.html#functions">http://hl7.org/fhir/fhirpath.html#functions</a>
 *
 * <p>This implementation supports an optional second argument (binding strength). The binding strength
 * is used to determine whether or not to add a warning to the evaluation context if the membership check fails.
 */
public class MemberOfFunction extends FHIRPathAbstractFunction {
    @Override
    public String getName() {
        return "memberOf";
    }

    @Override
    public int getMinArity() {
        return 1;
    }

    @Override
    public int getMaxArity() {
        return 2;
    }

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        if (context.isEmpty()) {
            return empty();
        }

        if (!isCodedElementNode(context) && !isStringElementNode(context) && !isUriElementNode(context)) {
            throw new IllegalArgumentException("The 'memberOf' function must be invoked on a coded element node, string element node, or uri element node");
        }

        if (!isStringValue(arguments.get(0))) {
            throw new IllegalArgumentException("The argument to the 'memberOf' function must be a string value");
        }

        if (arguments.size() == 2 && !isStringValue(arguments.get(1))) {
            throw new IllegalArgumentException("The optional second argument to the 'memberOf' function must be a string value");
        }

        FHIRPathElementNode elementNode = getElementNode(context);
        Element element = elementNode.element();
        String url = getString(arguments.get(0));
        String strength = (arguments.size() == 2) ? getString(arguments.get(1)) : null;

        ValueSet valueSet = getValueSet(url);
        if (valueSet != null) {
            FHIRTermService service = FHIRTermService.getInstance();
            if (isExpanded(valueSet) || service.isExpandable(valueSet)) {
                if (element.is(Code.class)) {
                    Uri system = getSystem(evaluationContext.getTree().getParent(elementNode));
                    Code code = element.as(Code.class);
                    if (validateCode(service, valueSet, system, null, code, null, evaluationContext, elementNode, strength)) {
                        return SINGLETON_TRUE;
                    }
                } else if (element.is(Coding.class)) {
                    Coding coding = element.as(Coding.class);
                    if (validateCode(service, valueSet, coding, evaluationContext, elementNode, strength)) {
                        return SINGLETON_TRUE;
                    }
                } else if (element.is(CodeableConcept.class)) {
                    CodeableConcept codeableConcept = element.as(CodeableConcept.class);
                    if (validateCode(service, valueSet, codeableConcept, evaluationContext, elementNode, strength)) {
                        return SINGLETON_TRUE;
                    }
                } else {
                    // element.is(FHIR_STRING) || element.is(Uri.class)
                    Code code = element.is(FHIR_STRING) ? Code.of(element.as(FHIR_STRING).getValue()) : Code.of(element.as(Uri.class).getValue());
                    if (validateCode(service, valueSet, null, null, code, null, evaluationContext, elementNode, strength)) {
                        return SINGLETON_TRUE;
                    }
                }
                return membershipCheckFailed(evaluationContext, elementNode, url, strength);
            } else {
                generateIssue(evaluationContext, IssueSeverity.WARNING, IssueType.INCOMPLETE, "Membership check was not performed: value set '" + url + "' is empty or could not be expanded", elementNode.path());
            }
        } else {
            generateIssue(evaluationContext, IssueSeverity.WARNING, IssueType.NOT_SUPPORTED, "Membership check was not performed: value set '" + url + "' is not supported", elementNode.path());
        }

        return SINGLETON_TRUE;
    }

    private boolean validateCode(FHIRTermService service, ValueSet valueSet, Uri system, com.ibm.fhir.model.type.String version, Code code, com.ibm.fhir.model.type.String display, EvaluationContext evaluationContext, FHIRPathElementNode elementNode, String strength) {
        ValidationOutcome outcome = service.validateCode(valueSet, system, version, code, display);
        if (Boolean.FALSE.equals(outcome.getResult())) {
            generateIssue(outcome, evaluationContext, elementNode, strength);
            return false;
        }
        return true;
    }

    private boolean validateCode(FHIRTermService service, ValueSet valueSet, Coding coding, EvaluationContext evaluationContext, FHIRPathElementNode elementNode, String strength) {
        ValidationOutcome outcome = service.validateCode(valueSet, coding);
        if (Boolean.FALSE.equals(outcome.getResult())) {
            generateIssue(outcome, evaluationContext, elementNode, strength);
            return false;
        }
        return true;
    }

    private boolean validateCode(FHIRTermService service, ValueSet valueSet, CodeableConcept codeableConcept, EvaluationContext evaluationContext, FHIRPathElementNode elementNode, String strength) {
        ValidationOutcome outcome = service.validateCode(valueSet, codeableConcept);
        if (Boolean.FALSE.equals(outcome.getResult())) {
            generateIssue(outcome, evaluationContext, elementNode, strength);
            return false;
        }
        return true;
    }

    private void generateIssue(ValidationOutcome outcome, EvaluationContext evaluationContext, FHIRPathElementNode elementNode, String strength) {
        if (outcome.getMessage() != null) {
            IssueSeverity severity = ("extensible".equals(strength) || "preferred".equals(strength)) ? IssueSeverity.WARNING : IssueSeverity.ERROR;
            generateIssue(evaluationContext, severity, IssueType.CODE_INVALID, outcome.getMessage().getValue(), elementNode.path());
        }
    }

    private Collection<FHIRPathNode> membershipCheckFailed(EvaluationContext evaluationContext, FHIRPathElementNode elementNode, String url, String strength) {
        if ("extensible".equals(strength) || "preferred".equals(strength)) {
            String prefix = evaluationContext.hasConstraint() ? evaluationContext.getConstraint().id() + ": " : "";
            String description = prefix + "The concept in this element " + ("extensible".equals(strength) ? "must" : "should") + " be from the specified value set '" + url + "' if possible";
            generateIssue(evaluationContext, IssueSeverity.WARNING, IssueType.CODE_INVALID, description, elementNode.path());
            return SINGLETON_TRUE;
        }
        return SINGLETON_FALSE;
    }

    /**
     * Get a URI-typed child node of the input parameter with name "system".
     *
     * @param node
     *     the parent node
     * @return
     *     the URI-typed child node with name "system", or null if no such child node exists
     */
    private Uri getSystem(FHIRPathNode node) {
        if (node == null || !node.isElementNode()) {
            return null;
        }
        for (FHIRPathNode child : node.children()) {
            if ("system".equals(child.name()) && FHIRPathType.FHIR_URI.equals(node.type())) {
                return child.asElementNode().element().as(Uri.class);
            }
        }
        return null;
    }
}
