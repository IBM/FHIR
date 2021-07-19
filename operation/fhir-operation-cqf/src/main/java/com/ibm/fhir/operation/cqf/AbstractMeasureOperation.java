/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.retrieve.RetrieveProvider;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.fhir.core.FHIRConstants;
import com.ibm.fhir.cql.engine.model.FHIRModelResolver;
import com.ibm.fhir.cql.engine.searchparam.SearchParameterResolver;
import com.ibm.fhir.cql.engine.server.retrieve.ServerFHIRRetrieveProvider;
import com.ibm.fhir.cql.engine.server.terminology.ServerFHIRTerminologyProvider;
import com.ibm.fhir.cql.helpers.DateHelper;
import com.ibm.fhir.cql.helpers.LibraryHelper;
import com.ibm.fhir.cql.helpers.ModelHelper;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.cql.translator.CqlTranslationProvider;
import com.ibm.fhir.cql.translator.FHIRLibraryLibrarySourceProvider;
import com.ibm.fhir.cql.translator.impl.InJVMCqlTranslationProvider;
import com.ibm.fhir.ecqm.common.MeasureReportType;
import com.ibm.fhir.ecqm.r4.R4MeasureEvaluation;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.AbstractOperation;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public abstract class AbstractMeasureOperation extends AbstractOperation {

    public static final String CQL_PARAM_MEASUREMENT_PERIOD = "Measurement Period";

    public static final String PARAM_IN_PERIOD_END = "periodEnd";
    public static final String PARAM_IN_PERIOD_START = "periodStart";

    /**
     * Create the terminology provider that will be used to access terminology data
     * during operation evaluation.
     * 
     * @return Terminology Provider
     */
    public TerminologyProvider getTerminologyProvider(FHIRResourceHelpers resourceHelpers) {
        TerminologyProvider termProvider = new ServerFHIRTerminologyProvider(resourceHelpers);
        return termProvider;
    }

    /**
     * Create the retrieve provider that will be used to perform data retrieval during
     * the operation evaluation.
     * 
     * @param resourceHelper FHIR Resource Helpers 
     * @param termProvider Terminology Provider
     * @return Retrieve provider configured as appropriate for the environment
     */
    public RetrieveProvider getRetrieveProvider(FHIRResourceHelpers resourceHelper, TerminologyProvider termProvider) {
        SearchParameterResolver resolver = new SearchParameterResolver();
        ServerFHIRRetrieveProvider retrieveProvider = new ServerFHIRRetrieveProvider(resourceHelper, resolver);
        retrieveProvider.setExpandValueSets(false); // TODO - use server config settings
        retrieveProvider.setTerminologyProvider(termProvider);
        retrieveProvider.setPageSize(FHIRConstants.FHIR_PAGE_SIZE_DEFAULT_MAX); // TODO - use server config settings?
        return retrieveProvider;
    }

    public MeasureReport.Builder doMeasureEvaluation(Measure measure, ZoneOffset zoneOffset, Interval measurementPeriod, String subjectOrPractitionerId,
        MeasureReportType reportType,
        TerminologyProvider termProvider, Map<String, DataProvider> dataProviders) {

        int numLibraries = (measure.getLibrary() != null) ? measure.getLibrary().size() : 0;
        if (numLibraries != 1) {
            throw new IllegalArgumentException(String.format("Unexpected number of libraries '%d' referenced by measure '%s'", numLibraries, measure.getId()));
        }

        Library primaryLibrary = FHIRRegistry.getInstance().getResource(measure.getLibrary().get(0).getValue(), Library.class);
        List<Library> fhirLibraries = LibraryHelper.loadLibraries(primaryLibrary);

        List<org.cqframework.cql.elm.execution.Library> cqlLibraries = loadCqlLibraries(fhirLibraries);
        LibraryLoader ll = new InMemoryLibraryLoader(cqlLibraries);

        ZonedDateTime zdt = ZonedDateTime.now(zoneOffset);

        Context context = new Context(cqlLibraries.get(0), zdt);
        dataProviders.entrySet().stream().forEach(e -> context.registerDataProvider(e.getKey(), e.getValue()));
        context.registerTerminologyProvider(termProvider);
        context.registerLibraryLoader(ll);

        // By convention, measure authors such as CMS and HEDIS expect the measurement period to be available in the
        // CQL libraries under this well-known name.
        context.setParameter(null, CQL_PARAM_MEASUREMENT_PERIOD, measurementPeriod);

        R4MeasureEvaluation<Patient> evaluation =
                new R4MeasureEvaluation<>(context, measure, measurementPeriod, FHIRModelResolver.RESOURCE_PACKAGE_NAME, r -> r.getId(), subjectOrPractitionerId);

        MeasureReport.Builder report = null;
        if (reportType != null) {
            report = evaluation.evaluate(reportType);
        } else {
            report = evaluation.evaluatePopulationMeasure();
        }
        return report;
    }

    /**
     * Return the <code>ZoneOffset</code> which will be used
     * to interpret dates that do not have associated time zone information.
     * This includes the periodStart, periodEnd, and those specified in the
     * CQL that is evaluated, . The default implementation uses the zone
     * offset of the server performing the evaluation.
     * 
     * @param paramMap
     *            Operation input parameters
     * @return timezone offset that will be applied to dates without TimeZone specified
     */
    public ZoneOffset getZoneOffset(ParameterMap paramMap) {
        ZoneOffset zoneOffset = null;

        Parameter pStart = paramMap.getSingletonParameter(PARAM_IN_PERIOD_START);
        if (pStart != null) {
            zoneOffset = ModelHelper.getZoneOffset(pStart.getValue());
        }

        if (zoneOffset == null) {
            zoneOffset = OffsetDateTime.now().getOffset();
        }

        return zoneOffset;
    }

    /**
     * Construct the Measurement Period interval that will be
     * passed to the CQL Engine and Measurement Report builder.
     * The input parameters are FHIR date objects that do not
     * contain timezone information. The start and end DateTime
     * objects will be calculated based on the <code>zoneOffset</code>
     * that is provided.
     * 
     * @param paramMap
     *            Operation input parameters
     * @param zoneOffset
     * @return Interval of DateTime values representing the measurement period.
     */
    public Interval getMeasurementPeriod(ParameterMap paramMap, ZoneOffset zoneOffset) {
        Parameter pStart = paramMap.getSingletonParameter(PARAM_IN_PERIOD_START);
        Parameter pEnd = paramMap.getSingletonParameter(PARAM_IN_PERIOD_END);

        TemporalAccessor ta;
        ta = ((Date) pStart.getValue()).getValue();
        LocalDateTime ldStart = DateHelper.dateFloor(ta);

        ta = ((Date) pEnd.getValue()).getValue();
        LocalDateTime ldEnd = DateHelper.dateCeiling(ta);

        OffsetDateTime odtStart = OffsetDateTime.of(ldStart, zoneOffset);
        OffsetDateTime odtEnd = OffsetDateTime.of(ldEnd, zoneOffset);

        DateTime dtStart = new DateTime(odtStart);
        DateTime dtEnd = new DateTime(odtEnd);

        return new Interval(dtStart, true, dtEnd, true);
    }

    /**
     * Create a library loader that will server up the CQL library content of the
     * provided list of FHIR Library resources.
     * 
     * @param libraries
     *            FHIR library resources
     * @return LibraryLoader that will serve the CQL Libraries for the provided FHIR resources
     */
    protected LibraryLoader createLibraryLoader(List<Library> libraries) {
        List<org.cqframework.cql.elm.execution.Library> result = loadCqlLibraries(libraries);
        return new InMemoryLibraryLoader(result);
    }

    /**
     * Load the CQL Library content for each of the provided FHIR Library resources with
     * translation as needed for Libraries with CQL attachments and no corresponding
     * ELM attachment.
     * 
     * @param libraries
     *            FHIR Libraries
     * @return CQL Libraries
     */
    protected List<org.cqframework.cql.elm.execution.Library> loadCqlLibraries(List<Library> libraries) {
        FHIRLibraryLibrarySourceProvider sourceProvider = new FHIRLibraryLibrarySourceProvider(libraries);
        CqlTranslationProvider translator = new InJVMCqlTranslationProvider(sourceProvider);

        List<org.cqframework.cql.elm.execution.Library> result =
                libraries.stream().flatMap(fl -> LibraryHelper.loadLibrary(translator, fl).stream()).filter(Objects::nonNull).collect(Collectors.toList());
        return result;
    }
}
