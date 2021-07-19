/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.fhir.cql.engine.model.FHIRModelResolver;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.util.ModelSupport;
import com.ibm.fhir.model.util.ModelSupport.ElementInfo;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class MeasureCollectDataOperation extends EvaluateMeasureOperation {

    public static final String PARAM_OUT_MEASURE_REPORT = "measureReport";
    public static final String PARAM_OUT_RESOURCE = "resource";

    private FHIRModelResolver resolver = new FHIRModelResolver();
    
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Measure-collect-data", OperationDefinition.class);

    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
        Parameters parameters, FHIRResourceHelpers resourceHelper) throws FHIROperationException {

        // This approach is modeled directly on the cqf-ruler implementation. It seems to me that
        // running the measure in order to get the data requirements is a cop out and duplication
        // of effort.
        Parameters intermediateOutParams = super.doInvoke(operationContext, resourceType, logicalId, versionId, parameters, resourceHelper);
        Parameter returnParam = intermediateOutParams.getParameter().get(0);
        MeasureReport measureReport = (MeasureReport) returnParam.getResource();
        
        Parameters.Builder outParams = Parameters.builder();
        outParams.parameter( Parameter.builder().name(fhirstring(PARAM_OUT_MEASURE_REPORT)).resource(measureReport).build());
        
        if( measureReport.getContained() != null ) {
            for( Resource containedResource : measureReport.getContained() ) {
                // This is exactly how the cqf-ruler implementation is coded. 
                // Right now, this is never going to fire based on both the 
                // cqf-ruler and cql-evaluator implementations. This needs to
                // be discussed with the spec team and revisited as appropriate.
                if( containedResource instanceof Bundle ) {
                    addEvaluatedResourcesToParameters((Bundle) containedResource, outParams, resourceHelper);
                }
            }
        }
        
        return outParams.build();
    }
    
    protected void addEvaluatedResourcesToParameters( Bundle contained, Parameters.Builder parameters, FHIRResourceHelpers resourceHelper ) throws FHIROperationException {
        Map<String,Resource> resourceMap = new HashMap<>();
        if( contained.getEntry() != null ) {
            for( Bundle.Entry entry : contained.getEntry() ) {
                if( entry.getResource() != null && !(entry.getResource() instanceof com.ibm.fhir.model.resource.List) ) {
                    if( ! resourceMap.containsKey(entry.getResource().getId()) ) {
                        parameters.parameter(Parameter.builder().name(fhirstring(PARAM_OUT_RESOURCE)).resource(entry.getResource()).build());
                        resourceMap.put(entry.getResource().getId(), entry.getResource());
                        
                        resolveReferences(entry.getResource(), parameters, resourceMap, resourceHelper);
                    }
                }
            }
        }
    }
    
    /**
     * Loop through all the data elements of the structure looking for things that are references. Pull
     * in each referenced resource and add it to the output.
     * 
     * @throws FHIROperationException 
     */
    protected void resolveReferences(Resource resource, Parameters.Builder parameters, Map<String, Resource> resourceMap, FHIRResourceHelpers resourceHelper) throws FHIROperationException {
        
        Collection<ElementInfo> elementInfos = ModelSupport.getElementInfo(resource.getClass());
        for( ElementInfo elementInfo : elementInfos ) {
            Set<String> referenceTypes = elementInfo.getReferenceTypes();
            if( referenceTypes.size() > 0 ) {
                Reference reference = (Reference) resolver.resolvePath(resource,  elementInfo.getName());
                if( reference != null ) {
                    String[] parts = reference.getReference().getValue().split("/");
                    if( parts.length == 2 ) {
                        String type = parts[0];
                        String id = parts[1];
                        
                        if( ! resourceMap.containsKey(id) ) {
                            try {
                                SingleResourceResult<? extends Resource> readResult = resourceHelper.doRead(type, id, /*throwExOnNull=*/true, /*includeDeleted=*/false, /*contextResource=*/null);
                                if( readResult.isSuccess() ) {
                                    Resource fetchedResource = readResult.getResource();
                                    parameters.parameter(Parameter.builder().name(fhirstring(PARAM_OUT_RESOURCE)).resource(fetchedResource).build());
                                    resourceMap.put(id, resource);
                                }
                            } catch( Exception ex ) {
                                throw new FHIROperationException("Failed to read referenced resource", ex);
                            }
                        }
                    }
                }
            }
        }
    }

}
