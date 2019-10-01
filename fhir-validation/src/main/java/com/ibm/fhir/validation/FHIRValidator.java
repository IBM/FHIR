/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.validation;

import static com.ibm.fhir.model.path.util.FHIRPathUtil.isFalse;
import static com.ibm.fhir.model.path.util.FHIRPathUtil.singleton;
import static com.ibm.fhir.model.type.String.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.ibm.fhir.model.annotation.Constraint;
import com.ibm.fhir.model.path.FHIRPathElementNode;
import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.FHIRPathQuantityNode;
import com.ibm.fhir.model.path.FHIRPathResourceNode;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import com.ibm.fhir.model.path.visitor.FHIRPathVoidParameterNodeVisitorAdapter;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.IssueSeverity;
import com.ibm.fhir.model.type.IssueType;
import com.ibm.fhir.model.util.ModelSupport;
import com.ibm.fhir.validation.exception.FHIRValidationException;
import com.ibm.fhir.validation.util.ProfileSupport;

public class FHIRValidator {
    public static boolean DEBUG = false;

    private ValidatingNodeVisitor visitor = new ValidatingNodeVisitor();

    private FHIRValidator() { }

    /**
     * Validate a {@link Resource} against constraints in the base specification and 
     * resource-asserted profile references or specific profile references but not both. 
     * 
     * <p>Profile references that are passed into this method are only applicable to the outermost 
     * resource (not contained resources).
     * 
     * @param resource
     *     a {@link Resource} instance (the target of validation)
     * @param profiles
     *     specific profile references to validate the resource against
     * @return
     *     list of issues generated during validation
     * @throws FHIRValidationException
     *     for errors that occur during validation
     */
    public List<Issue> validate(Resource resource, String... profiles) throws FHIRValidationException {
        return validate(resource, (profiles.length == 0), profiles);
    }
    
    /**
     * Validate a {@link Resource} against constraints in the base specification and 
     * resource-asserted profile references and/or specific profile references. 
     * 
     * <p>Profile references that are passed into this method are only applicable to the outermost 
     * resource (not contained resources).
     * 
     * @param resource
     *     a {@link Resource} instance (the target of validation)
     * @param includeResourceAssertedProfiles
     *     whether or not to consider resource-asserted profiles during validation
     * @param profiles
     *     specific profile references to validate the resource against
     * @return
     *     list of issues generated during validation
     * @throws FHIRValidationException
     *     for errors that occur during validation
     */
    public List<Issue> validate(Resource resource, boolean includeResourceAssertedProfiles, String... profiles) throws FHIRValidationException {
        return validate(new EvaluationContext(resource), includeResourceAssertedProfiles, profiles);
    }
    
    /**
     * Validate a resource, using an {@link EvaluationContext}, against constraints in the base specification and 
     * resource-asserted profile references or specific profile references but not both.
     * 
     * <p>Profile references that are passed into this method are only applicable to the outermost 
     * resource (not contained resources).
     * 
     * @param evaluationContext
     *     the {@link EvaluationContext} for this validation which includes a {@link FHIRPathTree} 
     *     built from a {@link Resource} instance (the target of validation)
     * @param profiles
     *     specific profile references to validate the evaluation context against
     * @return
     *     list of issues generated during validation
     * @throws FHIRValidationException
     *     for errors that occur during validation
     * @see {@link FHIRPathEvaluator.EvaluationContext}
     */
    public List<Issue> validate(EvaluationContext evaluationContext, String... profiles) throws FHIRValidationException {
        return validate(evaluationContext, (profiles.length == 0), profiles);
    }

    /**
     * Validate a resource, using an {@link EvaluationContext}, against constraints in the base specification and 
     * resource-asserted profile references and/or specific profile references.
     * 
     * <p>Profile references that are passed into this method are only applicable to the outermost 
     * resource (not contained resources).
     * 
     * @param evaluationContext
     *     the {@link EvaluationContext} for this validation which includes a {@link FHIRPathTree} 
     *     built from a {@link Resource} instance (the target of validation)
     * @param includeResourceAssertedProfiles
     *     whether or not to consider resource-asserted profiles during validation
     * @param profiles
     *     specific profile references to validate the evaluation context against
     * @return
     *     list of issues generated during validation
     * @throws FHIRValidationException
     *     for errors that occur during validation
     * @see {@link FHIRPathEvaluator.EvaluationContext}
     */
    public List<Issue> validate(EvaluationContext evaluationContext, boolean includeResourceAssertedProfiles, String... profiles) throws FHIRValidationException {
        Objects.requireNonNull(evaluationContext);
        Objects.requireNonNull(evaluationContext.getTree());
        if (!evaluationContext.getTree().getRoot().isResourceNode()) {
            throw new IllegalArgumentException("Root must be resource node");
        }
        try {
            return visitor.validate(evaluationContext, includeResourceAssertedProfiles, profiles);
        } catch (Exception e) {
            throw new FHIRValidationException("An error occurred during validation", e);
        }
    }

    public static FHIRValidator validator() {
        return new FHIRValidator();
    }

    private static class ValidatingNodeVisitor extends FHIRPathVoidParameterNodeVisitorAdapter {
        private FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        private EvaluationContext evaluationContext;
        private boolean includeResourceAssertedProfiles;
        private List<String> profiles;
        private List<Issue> issues = new ArrayList<>();

        private ValidatingNodeVisitor() { }

        private List<Issue> validate(EvaluationContext evaluationContext, boolean includeResourceAssertedProfiles, String... profiles) {
            reset();
            this.evaluationContext = evaluationContext;
            this.includeResourceAssertedProfiles = includeResourceAssertedProfiles;
            this.profiles = Arrays.asList(profiles);
            this.evaluationContext.getTree().getRoot().accept(this);
            return issues;
        }

        private void reset() {
            issues.clear();
        }
        
        @Override
        protected void doVisit(FHIRPathElementNode node) {
            validate(node);
        }
        
        @Override
        protected void doVisit(FHIRPathResourceNode node) {
            validate(node);
        }
        
        @Override
        protected void doVisit(FHIRPathQuantityNode node) {
            validate(node);
        }

        private void validate(FHIRPathElementNode elementNode) {
            Class<?> elementType = elementNode.element().getClass();
            validate(elementType, elementNode, ModelSupport.getConstraints(elementType));
        }
        
        private void validate(FHIRPathResourceNode resourceNode) {
            Class<?> resourceType = resourceNode.resource().getClass();
            validate(resourceType, resourceNode, ModelSupport.getConstraints(resourceType));
            if (includeResourceAssertedProfiles) {
                validate(resourceType, resourceNode, ProfileSupport.getConstraints(resourceNode.resource()));
            }
            if (!profiles.isEmpty() && !resourceNode.path().contains(".")) {
                validate(resourceType, resourceNode, ProfileSupport.getConstraints(profiles, resourceType));
            }
        }
        
        private void validate(Class<?> type, FHIRPathNode node, Collection<Constraint> constraints) {
            for (Constraint constraint : constraints) {
                if (constraint.modelChecked()) {
                    if (DEBUG) {
                        System.out.println("    Constraint: " + constraint.id() + " is model-checked");
                    }
                    continue;
                }
                validate(type, node, constraint);
            }
        }

        private void validate(Class<?> type, FHIRPathNode node, Constraint constraint) {
            String path = node.path();
            try {
                if (DEBUG) {
                    System.out.println("    Constraint: " + constraint);
                }

                Collection<FHIRPathNode> initialContext = singleton(node);
                if (!Constraint.LOCATION_BASE.equals(constraint.location())) {
                    initialContext = evaluator.evaluate(evaluationContext, constraint.location(), initialContext);
                }

                IssueSeverity severity = Constraint.LEVEL_WARNING.equals(constraint.level()) ? IssueSeverity.WARNING : IssueSeverity.ERROR;

                for (FHIRPathNode contextNode : initialContext) {
                    evaluationContext.setExternalConstant("resource", getResource(type, contextNode));
                    Collection<FHIRPathNode> result = evaluator.evaluate(evaluationContext, constraint.expression(), singleton(contextNode));

                    if (!result.isEmpty() && isFalse(result)) {
                        // constraint validation failed
                        Issue issue = Issue.builder()
                            .severity(severity)
                            .code(IssueType.INVARIANT)
                            .details(CodeableConcept.builder()
                                .text(string(constraint.id() + ": " + constraint.description()))
                                .build())
                            .expression(string(contextNode.path()))
                            .build();

                        issues.add(issue);
                    }

                    if (DEBUG) {
                        System.out.println("    Evaluation result: " + result + ", Path: " + contextNode.path());
                    }                    
                }
            } catch (Exception e) {
                throw new Error("An error occurred while validating constraint: " + constraint.id() +
                    " with location: " + constraint.location() + " and expression: " + constraint.expression() +
                    " at path: " + path, e);
            }
        }

        private FHIRPathResourceNode getResource(Class<?> type, FHIRPathNode node) {
            if (node.isResourceNode()) {
                // the current context node is a resource node
                return (FHIRPathResourceNode) node;
            }

            boolean isDefinedOnResource = Resource.class.isAssignableFrom(type);

            // move up in the tree to find the first ancestor that is a resource node
            String path = node.path();
            int index = path.lastIndexOf(".");
            while (index != -1) {
                path = path.substring(0, index);
                node = evaluationContext.getTree().getNode(path);
                if (node instanceof FHIRPathResourceNode) {
                    // if the constraint isn't defined on the resource and we're dealing with a contained resource
                    // then return the parent resource node which contains this resource
                    int lastSegmentStart = path.lastIndexOf(".");
                    if (!isDefinedOnResource && lastSegmentStart != -1 
                            && path.substring(lastSegmentStart + 1).matches("contained\\[\\d+\\]")) {
                        return (FHIRPathResourceNode) evaluationContext.getTree().getNode(path.substring(0, lastSegmentStart));
                    }
                    // otherwise return the resource node
                    return (FHIRPathResourceNode) node;
                }
                index = path.lastIndexOf(".");
            }

            return null;
        }
    }
}