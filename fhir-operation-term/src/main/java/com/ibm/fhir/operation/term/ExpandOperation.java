/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.operation.term;

import static com.ibm.fhir.operation.util.FHIROperationUtil.getOutputParameters;
import static com.ibm.fhir.term.util.ValueSetSupport.isExpanded;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.operation.context.FHIROperationContext;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.rest.FHIRResourceHelpers;
import com.ibm.fhir.term.service.FHIRTermService;
import com.ibm.fhir.term.spi.ExpansionParameters;

public class ExpandOperation extends AbstractTermOperation {
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/ValueSet-expand", OperationDefinition.class);
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
            ValueSet valueSet = getResource(operationContext, logicalId, parameters, resourceHelper, ValueSet.class);
            FHIRTermService service = FHIRTermService.getInstance();

            if (!isExpanded(valueSet) && !service.isExpandable(valueSet)) {
                String url = (valueSet.getUrl() != null) ? valueSet.getUrl().getValue() : null;
                throw new FHIROperationException("ValueSet with url '" + url + "' is not expandable");
            }

            ValueSet expanded = service.expand(valueSet, ExpansionParameters.from(parameters));

            return getOutputParameters(expanded);
        } catch (FHIROperationException e) {
            throw e;
        } catch (Exception e) {
            throw new FHIROperationException("An error occurred during the ValueSet expand operation", e);
        }
    }
}
