/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import static com.ibm.fhir.cql.helpers.ModelHelper.concept;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.DataRequirement;
import com.ibm.fhir.model.type.ParameterDefinition;
import com.ibm.fhir.model.type.RelatedArtifact;
import com.ibm.fhir.model.type.code.PublicationStatus;
import com.ibm.fhir.model.type.code.RelatedArtifactType;
import com.ibm.fhir.server.operation.spi.AbstractOperation;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.util.FHIROperationUtil;

public abstract class AbstractDataRequirementsOperation extends AbstractOperation {
    public static final String PARAM_OUT_RETURN = "return";

    protected Parameters doDataRequirements(List<Library> fhirLibraries) {
        return doDataRequirements(fhirLibraries, null);
    }
    
    protected Parameters doDataRequirements(List<Library> fhirLibraries, Supplier<List<RelatedArtifact>> additionalRelated) {
        Library.Builder result = Library.builder()
                .status(PublicationStatus.UNKNOWN)
                .type( concept(Constants.LIBRARY_TYPE_MODEL_DEFINITION) );
        
        Collection<RelatedArtifact> related = new ArrayList<>();
        Collection<ParameterDefinition> libParams = new ArrayList<>();
        Collection<DataRequirement> dataReqs = new ArrayList<>();
        
        if( additionalRelated != null ) {
            related.addAll( additionalRelated.get() );
        }
        
        for( Library l : fhirLibraries ) { 
            related.addAll( l.getRelatedArtifact().stream().filter( r -> r.getType().equals(RelatedArtifactType.DEPENDS_ON) ).collect(Collectors.toList()) );
            libParams.addAll( l.getParameter() );
            dataReqs.addAll( l.getDataRequirement() );
        }
        
        if( related.size() > 0 ) {
            // deduplication by resource
            result.relatedArtifact( related.stream()
                .collect( Collectors.toMap( ra -> ra.getResource().getValue(), Function.identity(), (x,y) -> y ) )
                .values() );
        }
        if( libParams.size() > 0 ) {
            // deduplication by parameter name
            result.parameter( libParams.stream()
                .collect( Collectors.toMap( pd -> pd.getName().getValue(), Function.identity(), (x,y) -> y ) )
                .values() );
        }
        if( dataReqs.size() > 0 ) {
            // deduplication by equality
            result.dataRequirement( dataReqs.stream()
                .collect( Collectors.toSet() ) );
        }
                
        return FHIROperationUtil.getOutputParameters(PARAM_OUT_RETURN, result.build());
    }
    
    @Override
    public abstract Parameters doInvoke(
        FHIROperationContext operationContext,
        Class<? extends Resource> resourceType,
        String logicalId, String versionId,
        Parameters parameters,
        FHIRResourceHelpers resourceHelper) throws FHIROperationException;
}
