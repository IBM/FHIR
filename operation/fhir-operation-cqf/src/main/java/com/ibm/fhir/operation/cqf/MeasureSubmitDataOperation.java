/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import static com.ibm.fhir.cql.helpers.ModelHelper.*;

import java.util.List;

import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.HTTPVerb;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.AbstractOperation;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.util.FHIROperationUtil;

public class MeasureSubmitDataOperation extends AbstractOperation {

    public static final String PARAM_IN_MEASURE_REPORT = "measureReport";
    public static final String PARAM_IN_RESOURCE = "resource";
    public static final String PARAM_OUT_RETURN ="return";
    
    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Measure-submit-data", OperationDefinition.class);
    }

    @Override
    public Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
        Parameters parameters, FHIRResourceHelpers resourceHelper) throws FHIROperationException {

        ParameterMap paramMap = new ParameterMap(parameters);
        
        Parameter param = paramMap.getSingletonParameter(PARAM_IN_MEASURE_REPORT);
        MeasureReport measureReport = (MeasureReport) param.getResource();
        
        List<Parameter> resources = paramMap.getParameter(PARAM_IN_RESOURCE);

        Bundle.Builder builder = Bundle.builder()
                .type(BundleType.TRANSACTION)
                .entry( createEntry(measureReport) );
        
        if( resources != null ) {
            resources.stream()
                .map(p -> p.getResource())
                .forEach(r -> builder.entry(createEntry(r)));
        }
        
        try {
            Bundle response = resourceHelper.doBundle(builder.build(), false);
            
            return FHIROperationUtil.getOutputParameters(PARAM_OUT_RETURN, response);

        } catch( Exception ex ) {
            throw new FHIROperationException("Operation failed", ex);
        }
    }
    
    protected Bundle.Entry createEntry( Resource resource ) {
        // the cqf-ruler implementation uses POST, but the operation definition says that 
        // this operation needs to be idempotent. In order to be idempotent, I believe this
        // needs to be a PUT. Otherwise, we are going to end up with multiple copies of the
        // submitted resource.
        
        Bundle.Entry.Request request = null;
        if( resource.getId() != null ) {
            request = Bundle.Entry.Request.builder()
                .method( HTTPVerb.PUT )
                .url( Uri.of( reference(resource).getReference().getValue() ) )
                .build();
        } else { 
            request = Bundle.Entry.Request.builder()
                .method(HTTPVerb.POST)
                .url( Uri.of("/" + resource.getClass().getSimpleName()) )
                .build();
        }
        // TODO - how to handle resources that do not already specify an ID?
        return Bundle.Entry.builder()
            .resource(resource)
            .request(request)
            .build();
    }
}