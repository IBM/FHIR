/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import java.time.ZoneOffset;
import java.util.Map;

import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.fhir.cql.helpers.DataProviderFactory;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.ecqm.common.MeasureReportType;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.util.FHIROperationUtil;

public class EvaluateMeasureOperation extends AbstractMeasureOperation {

    public static final String PARAM_IN_MEASURE = "measure";
    public static final String PARAM_IN_PRACTITIONER = "practitioner";
    public static final String PARAM_IN_SUBJECT = "subject";
    public static final String PARAM_IN_REPORT_TYPE = "reportType";
    public static final String PARAM_OUT_RETURN = "return";

    @Override
    protected OperationDefinition buildOperationDefinition() {
        return FHIRRegistry.getInstance().getResource("http://hl7.org/fhir/OperationDefinition/Measure-evaluate-measure", OperationDefinition.class);
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType, String logicalId, String versionId,
        Parameters parameters, FHIRResourceHelpers resourceHelper) throws FHIROperationException {

        ParameterMap paramMap = new ParameterMap(parameters);

        Measure measure = null;
        if (operationContext.getType().equals(FHIROperationContext.Type.INSTANCE)) {
            try {
                SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.MEASURE.getValue(), logicalId, true, false, null);
                measure = (Measure) readResult.getResource();
            } catch (Exception ex) {
                throw new FHIROperationException("Failed to read resource", ex);
            }
        } else if (operationContext.getType().equals(FHIROperationContext.Type.RESOURCE_TYPE)) {
            Parameter param = paramMap.getSingletonParameter(PARAM_IN_MEASURE);
            String canonicalURL = ((com.ibm.fhir.model.type.String) param.getValue()).getValue();
            measure = FHIRRegistry.getInstance().getResource(canonicalURL, Measure.class);
        } else {
            assert false;
        }

        String subject = getSubject(paramMap);

        String practitioner = getPractitioner(paramMap);

        String subjectOrPractitionerId = null;
        if (subject != null) {
            subjectOrPractitionerId = subject;
        } else if (practitioner != null) {
            subjectOrPractitionerId = practitioner;
        }
        
        MeasureReportType reportType = getReportType(paramMap, subject);

        ZoneOffset zoneOffset = getZoneOffset(paramMap);
        Interval measurementPeriod = getMeasurementPeriod(paramMap,zoneOffset);

        TerminologyProvider termProvider = getTerminologyProvider(resourceHelper);

        RetrieveProvider retrieveProvider = getRetrieveProvider(resourceHelper, termProvider);

        Map<String, DataProvider> dataProviders = DataProviderFactory.createDataProviders(retrieveProvider);

        MeasureReport.Builder report = doMeasureEvaluation(measure, zoneOffset, measurementPeriod, subjectOrPractitionerId, reportType, termProvider, dataProviders);

        return FHIROperationUtil.getOutputParameters(PARAM_OUT_RETURN, report.build());
    }

    public MeasureReportType getReportType(ParameterMap paramMap, String subject) {
        MeasureReportType reportType = null;
        
        Parameter pReportType = paramMap.getOptionalSingletonParameter(PARAM_IN_REPORT_TYPE);
        if (pReportType != null) {
            Code code = (Code) pReportType.getValue();
            reportType = MeasureReportType.fromCode(code.getValue());
        } else {
            if (subject != null) {
                reportType = MeasureReportType.INDIVIDUAL;
            } else {
                reportType = MeasureReportType.SUMMARY;
            }
        }
        return reportType;
    }

    public String getSubject(ParameterMap paramMap) {
        String subject = null;
        Parameter pSubject = paramMap.getOptionalSingletonParameter(PARAM_IN_SUBJECT);
        if (pSubject != null) {
            subject = ((com.ibm.fhir.model.type.String) pSubject.getValue()).getValue();
        }
        return subject;
    }

    public String getPractitioner(ParameterMap paramMap) {
        String practitioner = null;
        Parameter pPractitioner = paramMap.getOptionalSingletonParameter(PARAM_IN_PRACTITIONER);
        if (pPractitioner != null) {
            practitioner = ((com.ibm.fhir.model.type.String) pPractitioner.getValue()).getValue();
        }
        return practitioner;
    }

}
