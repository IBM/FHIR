/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.operation.term;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.CodeSystem;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.operation.context.FHIROperationContext;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.rest.FHIRResourceHelpers;
import com.ibm.fhir.term.spi.ValidationOutcome;
import com.ibm.fhir.term.spi.ValidationParameters;

public class CodeSystemValidateCodeOperation extends AbstractTermOperation {
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/CodeSystem-validate-code", OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(
            FHIROperationContext operationContext,
            Class<? extends Resource> resourceType,
            String logicalId,
            String versionId,
            Parameters parameters,
            FHIRResourceHelpers resourceHelper) throws FHIROperationException {
        try {
            CodeSystem codeSystem = getResource(operationContext, logicalId, parameters, resourceHelper, CodeSystem.class);
            Element codedElement = getCodedElement(parameters, "codeableConcept", "coding", "code", false);
            validate(codeSystem, codedElement);
            ValidationOutcome outcome = codedElement.is(CodeableConcept.class) ?
                    service.validateCode(codeSystem, codedElement.as(CodeableConcept.class), ValidationParameters.from(parameters)) :
                    service.validateCode(codeSystem, codedElement.as(Coding.class), ValidationParameters.from(parameters));
            return outcome.toParameters();
        } catch (FHIROperationException e) {
            throw e;
        } catch (Exception e) {
            throw new FHIROperationException("An error occurred during the CodeSystem validate code operation", e);
        }
    }

    private void validate(CodeSystem codeSystem, Element codedElement) throws FHIROperationException {
        if (codedElement.is(CodeableConcept.class)) {
            CodeableConcept codeableConcept = codedElement.as(CodeableConcept.class);
            for (Coding coding : codeableConcept.getCoding()) {
                if (coding.getSystem() != null && codeSystem.getUrl().equals(coding.getSystem()) && (coding.getVersion() == null ||
                        codeSystem.getVersion() == null ||
                        coding.getVersion().equals(codeSystem.getVersion()))) {
                    return;
                }
            }
            throw new FHIROperationException("CodeableConcept does not contain a coding element that matches the specified CodeSystem url and/or version");
        }
        // codedElement.is(Coding.class)
        Coding coding = codedElement.as(Coding.class);
        if (coding.getSystem() != null && codeSystem.getUrl() != null && !coding.getSystem().equals(codeSystem.getUrl())) {
            throw new FHIROperationException("Coding system does not match the specified CodeSystem url");
        }
        if (coding.getVersion() != null && codeSystem.getVersion() != null && !coding.getVersion().equals(codeSystem.getVersion())) {
            throw new FHIROperationException("Coding version does not match the specified CodeSystem version");
        }
    }
}