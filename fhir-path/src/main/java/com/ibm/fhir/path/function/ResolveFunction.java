/*
 * (C) Copyright IBM Corp. 2019, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.path.function;

import static com.ibm.fhir.model.util.FHIRUtil.REFERENCE_PATTERN;
import static com.ibm.fhir.model.util.ModelSupport.isResourceType;
import static com.ibm.fhir.path.util.FHIRPathUtil.convertsToBoolean;
import static com.ibm.fhir.path.util.FHIRPathUtil.getRootResourceNode;
import static com.ibm.fhir.path.util.FHIRPathUtil.isTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import com.ibm.fhir.model.resource.DomainResource;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.path.FHIRPathNode;
import com.ibm.fhir.path.FHIRPathResourceNode;
import com.ibm.fhir.path.FHIRPathTree;
import com.ibm.fhir.path.FHIRPathType;
import com.ibm.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;

public class ResolveFunction extends FHIRPathAbstractFunction {
    public static final String RESOLVE_RELATIVE_REFERENCES = "resolveRelativeReferences";
    public static final boolean DEFAULT_RESOLVE_RELATIVE_REFERENCES = true;

    private static final int BASE_URL_GROUP = 1;
    private static final int RESOURCE_TYPE_GROUP = 4;
    private static final int LOGICAL_ID_GROUP = 5;
    private static final int VERSION_ID_GROUP = 7;

    @Override
    public String getName() {
        return "resolve";
    }

    @Override
    public int getMinArity() {
        return 0;
    }

    @Override
    public int getMaxArity() {
        return 0;
    }

    /**
     * For each item in the collection, if it is a string that is a uri (or canonical or url), locate the target of the
     * reference, and add it to the resulting collection. If the item does not resolve to a resource, the item is ignored
     * and nothing is added to the output collection. The items in the collection may also represent a Reference, in which
     * case the Reference.reference is resolved.
     *
     * <p>This method creates a resource node that is a placeholder for the actual resource, thus allowing for the FHIRPath
     * evaluator to perform type checking on the result of the resolve function. For example:
     *
     * <pre>Observation.subject.where(resolve() is Patient)</pre>
     *
     * <p>If the resource type cannot be inferred from the reference URL or type, then {@code FHIR_UNKNOWN_RESOURCE_TYPE} is used
     * so that we index these references by default.
     *
     * @param evaluationContext
     *     the evaluation environment
     * @param context
     *     the current evaluation context
     * @param arguments
     *     the arguments for this function
     * @return
     *     the result of the function applied to the context and arguments
     */
    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        Collection<FHIRPathNode> result = new ArrayList<>();
        for (FHIRPathNode node : context) {
            if (node.isElementNode() && node.asElementNode().element().is(Reference.class)) {
                Reference reference = node.asElementNode().element().as(Reference.class);

                String referenceReference = getReferenceReference(reference);
                String referenceType = getReferenceType(reference);

                String resourceType = null;
                Resource resource = null;

                if (referenceReference != null) {
                    if (referenceReference.startsWith("#")) {
                        // internal fragment reference
                        resource = resolveInternalFragmentReference(evaluationContext.getTree(), node, referenceReference);
                        if (resource != null) {
                            resourceType = resource.getClass().getSimpleName();
                        }
                    } else {
                        Matcher matcher = REFERENCE_PATTERN.matcher(referenceReference);
                        if (matcher.matches()) {
                            resourceType = matcher.group(RESOURCE_TYPE_GROUP);
                            if (referenceType != null && !resourceType.equals(referenceType)) {
                                throw new IllegalArgumentException("Resource type found in reference URL does not match reference type");
                            }
                            if (matcher.group(BASE_URL_GROUP) == null && resolveRelativeReferences(evaluationContext)) {
                                // relative reference
                                resource = resolve(resourceType, matcher.group(LOGICAL_ID_GROUP), matcher.group(VERSION_ID_GROUP));
                            }
                        }
                    }
                }

                if (resourceType == null) {
                    resourceType = referenceType;
                }

                FHIRPathType type = isResourceType(resourceType) ? FHIRPathType.from(resourceType) : FHIRPathType.FHIR_UNKNOWN_RESOURCE_TYPE;

                if (referenceReference != null && FHIRPathType.FHIR_UNKNOWN_RESOURCE_TYPE.equals(type)) {
                    generateIssue(evaluationContext, IssueSeverity.INFORMATION, IssueType.INFORMATIONAL, "Resource type could not be inferred from reference: " + referenceReference, node.path());
                }

                result.add((resource != null) ? FHIRPathResourceNode.resourceNode(resource) : FHIRPathResourceNode.resourceNode(type));
            }
        }
        return result;
    }

    protected Resource resolve(String resourceType, String logicalId, String versionId) {
        return null;
    }

    private Resource resolveInternalFragmentReference(FHIRPathTree tree, FHIRPathNode node, String referenceReference) {
        if (tree != null) {
            FHIRPathResourceNode rootResource = getRootResourceNode(tree, node);
            if (rootResource != null) {
                Resource resource = rootResource.resource();
                if ("#".equals(referenceReference)) {
                    return resource;
                }
                String id = referenceReference.substring(1);
                if (resource instanceof DomainResource) {
                    DomainResource domainResource = (DomainResource) resource;
                    for (Resource contained : domainResource.getContained()) {
                        if (contained.getId() != null && contained.getId().equals(id)) {
                            return contained;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String getReferenceReference(Reference reference) {
        if (reference.getReference() != null && reference.getReference().getValue() != null) {
            return reference.getReference().getValue();
        }
        return null;
    }

    private String getReferenceType(Reference reference) {
        if (reference.getType() != null && reference.getType().getValue() != null) {
            return reference.getType().getValue();
        }
        return null;
    }

    private boolean resolveRelativeReferences(EvaluationContext evaluationContext) {
        Collection<FHIRPathNode> resolveRelativeReferences = evaluationContext.getExternalConstant(RESOLVE_RELATIVE_REFERENCES);
        if (convertsToBoolean(resolveRelativeReferences)) {
            return isTrue(resolveRelativeReferences);
        }
        return DEFAULT_RESOLVE_RELATIVE_REFERENCES;
    }
}
