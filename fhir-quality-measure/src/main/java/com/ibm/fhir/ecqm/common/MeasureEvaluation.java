/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.ecqm.common;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
/**
 * Implement the logic necessary to evaluate a FHIR Clinical Quality Measure
 * in a model-agnostic fashion.
 * 
 * @param <BaseT> Model's base class
 * @param <MeasureT> Model's Measure resource type class
 * @param <MeasureGroupComponentT> Model's Measure.Group class
 * @param <MeasureGroupPopulationComponentT> Model's Measure.Group.Population class
 * @param <MeasureSupplementalDataComponentT> Model's Measure.SupplementalData class
 * @param <MeasureReportT> Model's mutable MeasureReport class
 * @param <MeasureReportGroupComponentT> Model's MeasureReport.Group class
 * @param <MeasureReportGroupPopulationComponentT> Model's MeasureReport.Group.Population class
 * @param <CodingT> Model's Coding class
 * @param <ExtensionT> Model's Extension class
 * @param <ReferenceT> Model's Reference class
 * @param <ListResourceT> Model's List resource class
 * @param <ListEntryT> Model's List.Entry class
 * @param <ResourceT> Model's Resource class
 * @param <SubjectT> Model class for the subject type being evaluated (e.g. Patient)
 */
public abstract class MeasureEvaluation<BaseT, MeasureT extends BaseT, MeasureGroupComponentT extends BaseT, MeasureGroupPopulationComponentT extends BaseT,
    MeasureSupplementalDataComponentT extends BaseT, MeasureReportT extends BaseT, MeasureReportGroupComponentT extends BaseT,
    MeasureReportGroupPopulationComponentT extends BaseT, CodingT extends BaseT, ExtensionT extends BaseT,
    ReferenceT extends BaseT, ListResourceT extends ResourceT, ListEntryT extends BaseT, ResourceT, SubjectT extends ResourceT> {

    public static final String URL_CODESYSTEM_MEASURE_POPULATION = "http://teminology.hl7.org/CodeSystem/measure-population";

    public static final String EXT_DAVINCI_POPULATION_REFERENCE = "http://hl7.org/fhir/us/davinci-deqm/StructureDefinition/extension-populationReference";

    private static final Logger logger = LoggerFactory.getLogger(MeasureEvaluation.class);

    protected MeasureT measure;
    protected Context context;
    protected String subjectOrPractitionerId;
    protected Interval measurementPeriod;

    // TODO: Figure this out dynamically based on the ResourceType
    protected String packageName;

    protected Function<ResourceT, String> getId;

    protected abstract MeasureScoring getMeasureScoring();

    protected abstract String getCriteriaExpression(MeasureGroupPopulationComponentT mgpc);

    protected abstract void setGroupScore(MeasureReportGroupComponentT mrgc, Double score);

    protected abstract MeasurePopulationType getPopulationType(MeasureGroupPopulationComponentT mgpc);

    protected abstract Iterable<MeasureGroupComponentT> getGroup();

    protected abstract Iterable<MeasureGroupPopulationComponentT> getPopulation(MeasureGroupComponentT mgc);

    protected abstract void addPopulationReport(MeasureReportT report, MeasureReportGroupComponentT reportGroup,
            MeasureGroupPopulationComponentT populationCriteria, int populationCount,
            Iterable<SubjectT> subjectPopulation);

    protected abstract MeasureReportT createMeasureReport(String status, MeasureReportType type,
            Interval measurementPeriod, List<SubjectT> subjects);

    protected abstract MeasureReportGroupComponentT createReportGroup(String id);

    protected abstract String getGroupId(MeasureGroupComponentT group);

    protected abstract void addReportGroup(MeasureReportT report, MeasureReportGroupComponentT group);
    
    protected abstract List<MeasureSupplementalDataComponentT> getSupplementalData(MeasureT measure);
    
    protected abstract String getSDEExpression(MeasureSupplementalDataComponentT sdeItem);
    
    protected abstract CodingT getSDECoding(MeasureSupplementalDataComponentT sdeItem);
        
    protected abstract boolean isCoding(Object obj);
    
    protected abstract CodingT createCoding(String text);
    
    protected abstract String getCodingCode(CodingT coding);
    
    protected abstract ResourceT createPatientObservation(MeasureT measure, String populationId, CodingT coding);
    
    protected abstract ResourceT createPopulationObservation(MeasureT measure, String populationId, CodingT coding, Integer value);
    
    protected abstract void addEvaluatedResource(MeasureReportT report, ResourceT resource);
    
    protected abstract void addContained(MeasureReportT report, ResourceT resource);
    
    /**
     * Retrieve the coding from an extension that that looks like the following...
     * 
     * {
     *   "url": "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race",
     *   "extension": [ {
     *     "url": "ombCategory",
     *     "valueCoding": {
     *       "system": "urn:oid:2.16.840.1.113883.6.238",
     *       "code": "2054-5",
     *       "display": "Black or African American"
     *     }
     *   } ]
     * }
     */
    protected abstract CodingT getExtensionCoding(SubjectT patient, String category, String code);
    
    protected abstract ExtensionT createCodingExtension(String url, String codeSystem, String code);
    
    protected abstract ReferenceT createReference(String resourceId);
    
    protected abstract ListResourceT createListResource(Collection<ListEntryT> entries);
    
    protected abstract ListEntryT createListEntry(ReferenceT reference);
    
    protected abstract void addExtension(ReferenceT resource, ExtensionT extension);
    
    protected abstract void setEvaluatedResources(MeasureReportT report, Collection<ReferenceT> evaluatedResources);

    public MeasureEvaluation(Context context, MeasureT measure, Interval measurementPeriod, String packageName,
            Function<ResourceT, String> getId) {
        this(context, measure, measurementPeriod, packageName, getId, null);
    }

    public MeasureEvaluation(Context context, MeasureT measure, Interval measurementPeriod, String packageName,
            Function<ResourceT, String> getId, String patientOrPractitionerId) {
        this.measure = measure;
        this.context = context;
        this.subjectOrPractitionerId = patientOrPractitionerId;
        this.measurementPeriod = measurementPeriod;
        this.getId = getId;
        this.packageName = packageName;        
    }

    public MeasureReportT evaluate(MeasureReportType type) {
        switch (type) {
            case INDIVIDUAL:
                return this.evaluatePatientMeasure();
            case SUBJECTLIST:
                return this.evaluateSubjectListMeasure();
            case PATIENTLIST:
                return this.evaluatePatientListMeasure();
            default:
                return this.evaluatePatientListMeasure();
        }
    }

    protected MeasureReportT evaluatePatientMeasure() {
        logger.info("Generating individual report");

        if (this.subjectOrPractitionerId == null) {
            return evaluatePopulationMeasure();
        }

        String id = this.subjectOrPractitionerId;
        if (id.startsWith("Patient/") ) {
            id = id.substring("Patient/".length());
        }
        
        Iterable<Object> subjectRetrieve = this.getDataProvider().retrieve("Patient", "id",
                id, "Patient", null, null, null, null, null, null, null, null);
        SubjectT patient = null;
        if (subjectRetrieve.iterator().hasNext()) {
            patient = (SubjectT) subjectRetrieve.iterator().next();
        }

        return evaluate(patient == null ? Collections.emptyList() : Collections.singletonList(patient),
                MeasureReportType.INDIVIDUAL, true);
    }

    protected MeasureReportT evaluateSubjectListMeasure() {
        logger.info("Generating subject-list report");
        List<SubjectT> subjects = this.subjectOrPractitionerId == null ? getAllSubjects()
                : getPractitionerSubjects(this.subjectOrPractitionerId);
        return evaluate(subjects, MeasureReportType.SUBJECTLIST, false);
    }

    protected MeasureReportT evaluatePatientListMeasure() {
        logger.info("Generating patient-list report");
        List<SubjectT> subjects = this.subjectOrPractitionerId == null ? getAllSubjects()
                : getPractitionerSubjects(this.subjectOrPractitionerId);
        return evaluate(subjects, MeasureReportType.PATIENTLIST, false);
    }

    private List<SubjectT> getPractitionerSubjects(String practitionerRef) {
        List<SubjectT> subjects = new ArrayList<>();
        Iterable<Object> subjectRetrieve = this.getDataProvider().retrieve("Practitioner", "generalPractitioner",
                practitionerRef, "Patient", null, null, null, null, null, null, null, null);
        subjectRetrieve.forEach(x -> subjects.add((SubjectT) x));
        return subjects;
    }

    private DataProvider getDataProvider() {
        return this.context.resolveDataProvider(this.packageName);
    }

    private List<SubjectT> getAllSubjects() {
        List<SubjectT> patients = new ArrayList<>();
        Iterable<Object> patientRetrieve = this.getDataProvider().retrieve(null, null, null, "Patient", null, null,
                null, null, null, null, null, null);
        patientRetrieve.forEach(x -> patients.add((SubjectT) x));
        return patients;
    }

    public MeasureReportT evaluatePopulationMeasure() {
        logger.info("Generating summary report");

        return evaluate(getAllSubjects(), MeasureReportType.SUMMARY, false);
    }

    private Iterable<ResourceT> evaluateCriteria(SubjectT subject, MeasureGroupPopulationComponentT pop) {
        String criteriaExpression = this.getCriteriaExpression(pop);
        if (criteriaExpression == null || criteriaExpression.isEmpty()) {
            return Collections.emptyList();
        }

        context.setContextValue("Patient", this.getId.apply(subject));
        Object result = context.resolveExpressionRef(criteriaExpression).evaluate(context);
        if (result == null) {
            Collections.emptyList();
        }

        if (result instanceof Boolean) {
            if (((Boolean) result)) {
                return Collections.singletonList(subject);
            } else {
                return Collections.emptyList();
            }
        }

        return (Iterable<ResourceT>)result;
    }

    private boolean evaluatePopulationCriteria(SubjectT subject, MeasureGroupPopulationComponentT criteria,
            HashMap<String, ResourceT> population, HashMap<String, SubjectT> populationSubjects,
            MeasureGroupPopulationComponentT exclusionCriteria, HashMap<String, ResourceT> exclusionPopulation,
            HashMap<String, SubjectT> exclusionSubjects) {
        boolean inPopulation = false;
        if (criteria != null) {
            for (ResourceT resource : evaluateCriteria(subject, criteria)) {
                inPopulation = true;
                population.put(this.getId.apply(resource), resource);
            }
        }

        if (inPopulation) {
            // Are they in the exclusion?
            if (exclusionCriteria != null) {
                for (ResourceT resource : evaluateCriteria(subject, exclusionCriteria)) {
                    inPopulation = false;
                    exclusionPopulation.put(this.getId.apply(resource), resource);
                    population.remove(this.getId.apply(resource));
                }
            }
        }

        if (inPopulation && populationSubjects != null) {
            populationSubjects.put(this.getId.apply(subject), subject);
        }
        if (!inPopulation && exclusionSubjects != null) {
            exclusionSubjects.put(this.getId.apply(subject), subject);
        }

        return inPopulation;
    }

    private void addPopulationCriteriaReport(MeasureReportT report, MeasureReportGroupComponentT reportGroup,
            MeasureGroupPopulationComponentT populationCriteria, int populationCount,
            Iterable<SubjectT> patientPopulation) {
        if (populationCriteria != null) {
            this.addPopulationReport(report, reportGroup, populationCriteria, populationCount, patientPopulation);
        }
    }

    private MeasureReportT evaluate(List<SubjectT> patients, MeasureReportType type, boolean isSingle) {
        MeasureReportT report = this.createMeasureReport("complete", type, this.measurementPeriod, patients);
        HashMap<String, ResourceT> resources = new HashMap<>();
        HashMap<String, Set<String>> codeToResourceMap = new HashMap<>();

        MeasureScoring measureScoring = this.getMeasureScoring();
        if (measureScoring == null) {
            throw new RuntimeException("MeasureType scoring is required in order to calculate.");
        }
        
        List<MeasureSupplementalDataComponentT> sde = new ArrayList<>();
        HashMap<String, HashMap<String, Integer>> sdeAccumulators = null;

        for (MeasureGroupComponentT group : this.getGroup()) {
            MeasureReportGroupComponentT reportGroup = this.createReportGroup(this.getGroupId(group));
            this.addReportGroup(report, reportGroup);

            // Declare variables to avoid a hash lookup on every patient
            // TODO: Isn't quite right, there may be multiple initial populations for a
            // ratio MeasureType...
            MeasureGroupPopulationComponentT initialPopulationCriteria = null;
            MeasureGroupPopulationComponentT numeratorCriteria = null;
            MeasureGroupPopulationComponentT numeratorExclusionCriteria = null;
            MeasureGroupPopulationComponentT denominatorCriteria = null;
            MeasureGroupPopulationComponentT denominatorExclusionCriteria = null;
            MeasureGroupPopulationComponentT denominatorExceptionCriteria = null;
            MeasureGroupPopulationComponentT measurePopulationCriteria = null;
            MeasureGroupPopulationComponentT measurePopulationExclusionCriteria = null;
            // TODO: Isn't quite right, there may be multiple MeasureType observations...
            MeasureGroupPopulationComponentT measureObservationCriteria = null;

            HashMap<String, ResourceT> initialPopulation = null;
            HashMap<String, ResourceT> numerator = null;
            HashMap<String, ResourceT> numeratorExclusion = null;
            HashMap<String, ResourceT> denominator = null;
            HashMap<String, ResourceT> denominatorExclusion = null;
            HashMap<String, ResourceT> denominatorException = null;
            HashMap<String, ResourceT> measurePopulation = null;
            HashMap<String, ResourceT> measurePopulationExclusion = null;
            HashMap<String, ResourceT> measureObservation = null;

            HashMap<String, SubjectT> initialPopulationPatients = null;
            HashMap<String, SubjectT> numeratorPatients = null;
            HashMap<String, SubjectT> numeratorExclusionPatients = null;
            HashMap<String, SubjectT> denominatorPatients = null;
            HashMap<String, SubjectT> denominatorExclusionPatients = null;
            HashMap<String, SubjectT> denominatorExceptionPatients = null;
            HashMap<String, SubjectT> measurePopulationPatients = null;
            HashMap<String, SubjectT> measurePopulationExclusionPatients = null;

            sdeAccumulators = new HashMap<>();
            sde = getSupplementalData(measure);
            for (MeasureGroupPopulationComponentT pop : this.getPopulation(group)) {
                MeasurePopulationType populationType = this.getPopulationType(pop);
                if (populationType != null) {
                    switch (populationType) {
                        case INITIALPOPULATION:
                            initialPopulationCriteria = pop;
                            initialPopulation = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                initialPopulationPatients = new HashMap<>();
                            }
                            break;
                        case NUMERATOR:
                            numeratorCriteria = pop;
                            numerator = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                numeratorPatients = new HashMap<>();
                            }
                            break;
                        case NUMERATOREXCLUSION:
                            numeratorExclusionCriteria = pop;
                            numeratorExclusion = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                numeratorExclusionPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOR:
                            denominatorCriteria = pop;
                            denominator = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                denominatorPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOREXCLUSION:
                            denominatorExclusionCriteria = pop;
                            denominatorExclusion = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                denominatorExclusionPatients = new HashMap<>();
                            }
                            break;
                        case DENOMINATOREXCEPTION:
                            denominatorExceptionCriteria = pop;
                            denominatorException = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                denominatorExceptionPatients = new HashMap<>();
                            }
                            break;
                        case MEASUREPOPULATION:
                            measurePopulationCriteria = pop;
                            measurePopulation = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                measurePopulationPatients = new HashMap<>();
                            }
                            break;
                        case MEASUREPOPULATIONEXCLUSION:
                            measurePopulationExclusionCriteria = pop;
                            measurePopulationExclusion = new HashMap<>();
                            if (type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) {
                                measurePopulationExclusionPatients = new HashMap<>();
                            }
                            break;
                        case MEASUREOBSERVATION:
                            measureObservation = new HashMap<>();
                            break;
                    }
                }
            }

            switch (measureScoring) {
                case PROPORTION:
                case RATIO: {

                    // For each patient in the initial population
                    for (SubjectT patient : patients) {

                        // Are they in the initial population?
                        boolean inInitialPopulation = evaluatePopulationCriteria(patient, initialPopulationCriteria,
                                initialPopulation, initialPopulationPatients, null, null, null);
                        populateResourceMap(MeasurePopulationType.INITIALPOPULATION, resources, codeToResourceMap);

                        if (inInitialPopulation) {
                            // Are they in the denominator?
                            boolean inDenominator = evaluatePopulationCriteria(patient, denominatorCriteria,
                                    denominator, denominatorPatients, denominatorExclusionCriteria,
                                    denominatorExclusion, denominatorExclusionPatients);
                            populateResourceMap(MeasurePopulationType.DENOMINATOR, resources, codeToResourceMap);

                            if (inDenominator) {
                                // Are they in the numerator?
                                boolean inNumerator = evaluatePopulationCriteria(patient, numeratorCriteria, numerator,
                                        numeratorPatients, numeratorExclusionCriteria, numeratorExclusion,
                                        numeratorExclusionPatients);
                                populateResourceMap(MeasurePopulationType.NUMERATOR, resources, codeToResourceMap);

                                if (!inNumerator && inDenominator && (denominatorExceptionCriteria != null)) {
                                    // Are they in the denominator exception?
                                    boolean inException = false;
                                    for (ResourceT resource : evaluateCriteria(patient, denominatorExceptionCriteria)) {
                                        inException = true;
                                        denominatorException.put(this.getId.apply(resource), resource);
                                        denominator.remove(this.getId.apply(resource));
                                        populateResourceMap(MeasurePopulationType.DENOMINATOREXCEPTION, resources,
                                                codeToResourceMap);
                                    }
                                    if (inException) {
                                        if (denominatorExceptionPatients != null) {
                                            denominatorExceptionPatients.put(this.getId.apply(patient), patient);
                                        }
                                        if (denominatorPatients != null) {
                                            denominatorPatients.remove(this.getId.apply(patient));
                                        }
                                    }
                                }
                            }
                        }
                        
                        populateSDEAccumulators(measure, context, patient, sdeAccumulators, sde);
                    }

                    // Calculate actual MeasureType score, Count(numerator) / Count(denominator)
                    if (denominator != null && numerator != null && denominator.size() > 0) {
                        this.setGroupScore(reportGroup, numerator.size() / (double) denominator.size());
                    }

                    break;
                }
                case CONTINUOUSVARIABLE: {

                    // For each patient in the PatientType list
                    for (SubjectT patient : patients) {

                        // Are they in the initial population?
                        boolean inInitialPopulation = evaluatePopulationCriteria(patient, initialPopulationCriteria,
                                initialPopulation, initialPopulationPatients, null, null, null);
                        populateResourceMap(MeasurePopulationType.INITIALPOPULATION, resources, codeToResourceMap);

                        if (inInitialPopulation) {
                            // Are they in the MeasureType population?
                            boolean inMeasurePopulation = evaluatePopulationCriteria(patient, measurePopulationCriteria,
                                    measurePopulation, measurePopulationPatients, measurePopulationExclusionCriteria,
                                    measurePopulationExclusion, measurePopulationExclusionPatients);

                            if (inMeasurePopulation) {
                                // TODO: Evaluate MeasureType observations
                                for (ResourceT resource : evaluateCriteria(patient, measureObservationCriteria)) {
                                    measureObservation.put(this.getId.apply(resource), resource);
                                }
                            }
                        }
                        
                        populateSDEAccumulators(measure, context, patient, sdeAccumulators,sde);
                    }

                    break;
                }

                case COHORT: {

                    // For each patient in the PatientType list
                    for (SubjectT patient : patients) {
                        // Are they in the initial population?
                        evaluatePopulationCriteria(patient, initialPopulationCriteria,
                                initialPopulation, initialPopulationPatients, null, null, null);
                        populateResourceMap(MeasurePopulationType.INITIALPOPULATION, resources, codeToResourceMap);
                        populateSDEAccumulators(measure, context, patient, sdeAccumulators,sde);
                    }

                    break;
                }
            }

            // Add population reports for each group
            addPopulationCriteriaReport(report, reportGroup, initialPopulationCriteria,
                    initialPopulation != null ? initialPopulation.size() : 0,
                    initialPopulationPatients != null ? initialPopulationPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, numeratorCriteria,
                    numerator != null ? numerator.size() : 0,
                    numeratorPatients != null ? numeratorPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, numeratorExclusionCriteria,
                    numeratorExclusion != null ? numeratorExclusion.size() : 0,
                    numeratorExclusionPatients != null ? numeratorExclusionPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, denominatorCriteria,
                    denominator != null ? denominator.size() : 0,
                    denominatorPatients != null ? denominatorPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, denominatorExclusionCriteria,
                    denominatorExclusion != null ? denominatorExclusion.size() : 0,
                    denominatorExclusionPatients != null ? denominatorExclusionPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, denominatorExceptionCriteria,
                    denominatorException != null ? denominatorException.size() : 0,
                    denominatorExceptionPatients != null ? denominatorExceptionPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, measurePopulationCriteria,
                    measurePopulation != null ? measurePopulation.size() : 0,
                    measurePopulationPatients != null ? measurePopulationPatients.values() : null);
            addPopulationCriteriaReport(report, reportGroup, measurePopulationExclusionCriteria,
                    measurePopulationExclusion != null ? measurePopulationExclusion.size() : 0,
                    measurePopulationExclusionPatients != null ? measurePopulationExclusionPatients.values() : null);
            // TODO: MeasureType Observations...
        }
        
        List<ReferenceT> evaluatedResourceIds = new ArrayList<>();
        Map<String, ReferenceT> referenceMap = new HashMap<String, ReferenceT>();
        for (String code: codeToResourceMap.keySet()) {
            Set<String> resourceIds = codeToResourceMap.get(code);
            if( resourceIds.size() > 0 ) {
                
                List<ListEntryT> entries = new ArrayList<>(resourceIds.size());
                for (String resourceId : codeToResourceMap.get(code)) {
                    if (referenceMap.containsKey(resourceId)) {
                        ExtensionT ext = createCodingExtension( EXT_DAVINCI_POPULATION_REFERENCE, URL_CODESYSTEM_MEASURE_POPULATION, code);
                        
                        ReferenceT ref = referenceMap.get(resourceId);
                        addExtension( ref, ext );
                        evaluatedResourceIds.add(ref);
                    } else {
                        ExtensionT ext = createCodingExtension( EXT_DAVINCI_POPULATION_REFERENCE, URL_CODESYSTEM_MEASURE_POPULATION, code);
                        
                        ReferenceT reference = createReference(resourceId);
                        addExtension(reference, ext);
                        
                        ListEntryT comp = createListEntry(reference);
                        entries.add(comp);
                        // Do not want to add extension to ListEntryReference
    
                        evaluatedResourceIds.add(reference);
                        referenceMap.put(resourceId, reference);
                    }
                }
    
                ListResourceT list = createListResource(entries);
                resources.put(this.getId.apply(list), list);
            }
        }
        setEvaluatedResources(report, evaluatedResourceIds);

        if (sdeAccumulators.size() > 0) {
            report = processAccumulators(measure, report, sdeAccumulators, sde, isSingle, patients);
        }

        return report;
    }
    
    private void populateSDEAccumulators(MeasureT measure, Context context, SubjectT patient,
            HashMap<String, HashMap<String, Integer>> sdeAccumulators,
            List<MeasureSupplementalDataComponentT> sde) {
        context.setContextValue("Patient", this.getId.apply(patient));
        List<Object> sdeList = sde.stream()
                .map(sdeItem -> context.resolveExpressionRef(getSDEExpression(sdeItem)).evaluate(context))
                .collect(Collectors.toList());
        if (!sdeList.isEmpty()) {
            for (int i = 0; i < sdeList.size(); i++) {
                Object sdeListItem = sdeList.get(i);
                if (null != sdeListItem) {
                    String sdeAccumulatorKey = getCodingCode( getSDECoding(sde.get(i)) );
                    if (null == sdeAccumulatorKey || sdeAccumulatorKey.isEmpty() ) {
                        String expression = getSDEExpression(sde.get(i));
                        if( expression != null ) {
                            sdeAccumulatorKey = expression.toLowerCase(Locale.ROOT).replace(" ", "-");
                        }
                    }

                    HashMap<String, Integer> sdeItemMap = sdeAccumulators.get(sdeAccumulatorKey);
                    String code = "";

                    if( sdeListItem instanceof Code ) {
                        code = ((Code) sdeListItem).getCode();
                    } else if( sdeListItem instanceof List ) {
                        List<?> list = (List<?>) sdeListItem;
                        if( ! list.isEmpty() ) {
                            Object first = list.get(0);
                            if( first instanceof Code ) {
                                code = ((Code) first).getCode();
                            } else if( isCoding(first) ) {
                                code = getCodingCode( (CodingT) first );
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    
                    if (null == code || code.isEmpty()) {
                        continue;
                    }
                    if (null != sdeItemMap && null != sdeItemMap.get(code)) {
                        Integer sdeItemValue = sdeItemMap.get(code);
                        sdeItemValue++;
                        sdeItemMap.put(code, sdeItemValue);
                        sdeAccumulators.get(sdeAccumulatorKey).put(code, sdeItemValue);
                    } else {
                        if (null == sdeAccumulators.get(sdeAccumulatorKey)) {
                            HashMap<String, Integer> newSDEItem = new HashMap<>();
                            newSDEItem.put(code, 1);
                            sdeAccumulators.put(sdeAccumulatorKey, newSDEItem);
                        } else {
                            sdeAccumulators.get(sdeAccumulatorKey).put(code, 1);
                        }
                    }
                }
            }
        }
    }

    private MeasureReportT processAccumulators(MeasureT measure, MeasureReportT report,
            HashMap<String, HashMap<String, Integer>> sdeAccumulators,
            List<MeasureSupplementalDataComponentT> sde, boolean isSingle, List<SubjectT> patients) {
        sdeAccumulators.forEach((sdeKey, sdeAccumulator) -> {
            sdeAccumulator.forEach((sdeAccumulatorKey, sdeAccumulatorValue) -> {

                CodingT valueCoding = createCoding(sdeAccumulatorKey);
                if (! sdeKey.equalsIgnoreCase("sde-sex")) {
                    
                    /** 
                     * Match up the category part of our SDE key (e.g. sde-race has a category of race) with a 
                     * patient extension of the same category (e.g. http://hl7.org/fhir/us/core/StructureDefinition/us-core-race)
                     * and the same code as sdeAccumulatorKey (aka the value extracted from the CQL expression
                     * named in the Measure SDE metadata) and then record the coding details.
                     * 
                     * We know that at least one patient matches the sdeAccumulatorKey or else it wouldn't show up
                     * in the map.
                     */
                    
                    String coreCategory = sdeKey.substring(sdeKey.lastIndexOf('-') >= 0 ? sdeKey.lastIndexOf('-') : 0);
                    for( SubjectT pt : patients ) {
                        valueCoding = getExtensionCoding(pt, coreCategory, sdeAccumulatorKey);
                        
                        //TODO - Is there any reason to continue looking at additional patients? The original 
                        // cqf-ruler implementation would use the last matching patient's data vs. the first.
                        if( valueCoding != null ) {
                            break;
                        }
                    }
                }
                
                ResourceT obs;
                if (isSingle) {
                    obs = createPatientObservation(measure, sdeKey, valueCoding);
                } else {
                    obs = createPopulationObservation(measure, sdeKey, valueCoding, sdeAccumulatorValue);
                }
                
                addEvaluatedResource( report, obs );
                addContained(report, obs);
                //newRefList.add(new Reference("#" + this.getId.apply(obs)));
                //report.addContained(obs);
            });
        });
        //newRefList.addAll(report.getEvaluatedResource());
        //report.setEvaluatedResource(newRefList);
        return report;
    }

    private void populateResourceMap(MeasurePopulationType type, Map<String, ResourceT> resources,
            Map<String, Set<String>> codeToResourceMap) {
        if (this.context.getEvaluatedResources().isEmpty()) {
            return;
        }

        Set<String> codeSet = codeToResourceMap.computeIfAbsent(type.toCode(), key -> new HashSet<>());

        for (Object o : this.context.getEvaluatedResources()) {
            try {
                ResourceT r = (ResourceT) o;
                String id = this.getId.apply(r);
                
                codeSet.add(id);
                resources.computeIfAbsent(id, key -> r);

            } catch (Exception e) {
            }
        }

        this.context.clearEvaluatedResources();
    }
}