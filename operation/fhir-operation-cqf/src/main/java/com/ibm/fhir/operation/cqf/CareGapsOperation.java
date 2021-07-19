/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.fhir.cql.helpers.DataProviderFactory;
import com.ibm.fhir.cql.helpers.FHIRBundleCursor;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.ecqm.common.MeasureReportType;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.search.SearchConstants;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.util.FHIROperationUtil;

public class CareGapsOperation extends AbstractMeasureOperation {

    public static final String PARAM_IN_TOPIC = "topic";
    public static final String PARAM_IN_SUBJECT = "subject";
    public static final String PARAM_OUT_RETURN = "return";

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Measure-care-gaps", OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
        Parameters parameters, FHIRResourceHelpers resourceHelper) throws FHIROperationException {
       
        ParameterMap paramMap = new ParameterMap(parameters);

        ZoneOffset zoneOffset = getZoneOffset(paramMap);
        Interval measurementPeriod = getMeasurementPeriod(paramMap,zoneOffset);
        
        Parameter pTopic = paramMap.getSingletonParameter(PARAM_IN_TOPIC);
        String topic = ((com.ibm.fhir.model.type.String)pTopic.getValue()).getValue();
        
        Parameter pSubject = paramMap.getSingletonParameter(PARAM_IN_SUBJECT);
        String subject = ((com.ibm.fhir.model.type.String)pSubject.getValue()).getValue();
        
        int pageSize = 10;
        
        MultivaluedMap<String,String> searchParameters = new MultivaluedHashMap<>();
        searchParameters.putSingle("topic", topic);
        searchParameters.putSingle("_count", String.valueOf(pageSize));
        searchParameters.putSingle("_total", "none");
        
        try {
            Bundle bundle = resourceHelper.doSearch(ResourceType.MEASURE.getValue(), null, null, searchParameters, null, null);

            AtomicInteger pageNumber = new AtomicInteger(1);
            FHIRBundleCursor cursor = new FHIRBundleCursor(url -> {
                try {
                    searchParameters.putSingle(SearchConstants.PAGE, String.valueOf(pageNumber.incrementAndGet()));
                    return resourceHelper.doSearch(ResourceType.MEASURE.getValue(), null, null, searchParameters, null, null);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }, bundle);
            
            TerminologyProvider termProvider = getTerminologyProvider(resourceHelper);
            RetrieveProvider retrieveProvider = getRetrieveProvider(resourceHelper, termProvider);
            Map<String,DataProvider> dataProviders = DataProviderFactory.createDataProviders(retrieveProvider);
            
            Bundle result = processAllMeasures( cursor, subject, zoneOffset, measurementPeriod, resourceHelper, termProvider, dataProviders );
            return FHIROperationUtil.getOutputParameters(PARAM_OUT_RETURN, result);
            
        } catch( FHIROperationException fex ) {
            throw fex;
        } catch( Exception otherEx) {
            throw new FHIROperationException("Failed to load measures for topic " + topic, otherEx);
        }
    }

    protected Bundle processAllMeasures(FHIRBundleCursor cursor, String subject, ZoneOffset zoneOffset, Interval measurementPeriod, FHIRResourceHelpers resourceHelper, TerminologyProvider termProvider, Map<String,DataProvider> dataProviders) {
        Bundle.Builder reports = Bundle.builder().type(BundleType.COLLECTION); 
        
        AtomicInteger count = new AtomicInteger(0);
        cursor.forEach(resource -> {
            Measure measure = (Measure) resource;
            MeasureReport report = doMeasureEvaluation(measure, zoneOffset, measurementPeriod, subject, MeasureReportType.INDIVIDUAL, termProvider, dataProviders).build();
            reports.entry( Bundle.Entry.builder().resource(report).build() );
            count.incrementAndGet();
        });
        
        reports.total(UnsignedInt.of(count.get()));
        return reports.build();
    }

}