/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import java.util.Arrays;
import java.util.List;

import com.ibm.fhir.cql.helpers.LibraryHelper;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.RelatedArtifact;
import com.ibm.fhir.model.type.code.RelatedArtifactType;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class MeasureDataRequirementsOperation extends AbstractDataRequirementsOperation {

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Measure-data-requirements", OperationDefinition.class);
    }

    @Override
    public Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
        Parameters parameters, FHIRResourceHelpers resourceHelper) throws FHIROperationException {
        
        Measure measure = null;
        try {
            SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.MEASURE.getValue(), logicalId, true, false, null);
            measure = (Measure) readResult.getResource();
        } catch (Exception ex) {
            throw new FHIROperationException("Failed to read resource", ex);
        }
        
        int numLibraries = (measure.getLibrary() != null) ? measure.getLibrary().size() : 0;
        if (numLibraries != 1) {
            throw new IllegalArgumentException(String.format("Unexpected number of libraries '%d' referenced by measure '%s'", numLibraries, measure.getId()));
        }

        Library primaryLibrary = FHIRRegistry.getInstance().getResource(measure.getLibrary().get(0).getValue(), Library.class);
        List<Library> fhirLibraries = LibraryHelper.loadLibraries(primaryLibrary);

        RelatedArtifact related = RelatedArtifact.builder().type(RelatedArtifactType.DEPENDS_ON).resource(measure.getLibrary().get(0)).build();
        return doDataRequirements(fhirLibraries, () -> Arrays.asList(related) );
    }
}
