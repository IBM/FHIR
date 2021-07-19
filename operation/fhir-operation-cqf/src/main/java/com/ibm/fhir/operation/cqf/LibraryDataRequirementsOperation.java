/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import java.util.List;

import com.ibm.fhir.cql.helpers.LibraryHelper;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class LibraryDataRequirementsOperation extends AbstractDataRequirementsOperation {

    public static final String PARAM_IN_TARGET = "target";

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Library-data-requirements", OperationDefinition.class);
    }

    @Override
    public Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
        Parameters parameters, FHIRResourceHelpers resourceHelper) throws FHIROperationException {

        Library library = null;
        if (operationContext.getType().equals(FHIROperationContext.Type.INSTANCE)) {
            try {
                SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.LIBRARY.getValue(), logicalId, true, false, null);
                library = (Library) readResult.getResource();
            } catch (Exception ex) {
                throw new FHIROperationException("Failed to read resource", ex);
            }
        } else if (operationContext.getType().equals(FHIROperationContext.Type.SYSTEM)) {
            ParameterMap paramMap = new ParameterMap(parameters);
            Parameter p = paramMap.getSingletonParameter(PARAM_IN_TARGET);
            
            String target = ((com.ibm.fhir.model.type.String) p.getValue()).getValue();
            
            //The meaning of the target parameter is somewhat ambiguous in the base spec. I talked with the spec
            //author and the intention was for target to be a library ID. The operation should probably have been
            //defined instance level instead of system level. Things have improved in the QM IG, so I'm not going
            //to spend a lot of time trying to finagle this into something less confusing here. It can be improved
            //when we implement the IGs.
            //https://chat.fhir.org/#narrow/stream/179220-cql/topic/Library.20data-requirements.20operation
            
            String [] parts = target.split("/");
            
            logicalId = target;
            String targetType = ResourceType.LIBRARY.getValue();
            if( parts.length > 1 ) {
                targetType = parts[ parts.length - 2 ];
                logicalId = parts[ parts.length - 1 ];
            }
            
            try {
                SingleResourceResult<?> readResult = resourceHelper.doRead(targetType, logicalId, true, false, null);
                if( readResult.getResource() instanceof Library ) {
                    library = (Library) readResult.getResource();
                } else { 
                    throw new IllegalStateException("The target parameter must be a Library resource");
                }
            } catch (Exception ex) {
                throw new FHIROperationException("Failed to read resource", ex);
            }
        } else { 
            throw new IllegalStateException("Unsupported context " + operationContext.getType().toString());
        }

        List<Library> fhirLibraries = LibraryHelper.loadLibraries(library);
        return doDataRequirements(fhirLibraries);
    }

}
