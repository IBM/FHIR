/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.ecqm.r4;

import static com.ibm.fhir.cql.helpers.ModelHelper.canonical;
import static com.ibm.fhir.cql.helpers.ModelHelper.coding;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirinteger;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.javastring;
import static com.ibm.fhir.cql.helpers.ModelHelper.reference;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opencds.cqf.cql.engine.execution.Context;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.fhir.ecqm.common.MeasureEvaluation;
import com.ibm.fhir.ecqm.common.MeasureInfo;
import com.ibm.fhir.ecqm.common.MeasurePopulationType;
import com.ibm.fhir.ecqm.common.MeasureReportType;
import com.ibm.fhir.ecqm.common.MeasureScoring;
import com.ibm.fhir.model.resource.DomainResource;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.Measure.SupplementalData;
import com.ibm.fhir.model.resource.MeasureReport;
import com.ibm.fhir.model.resource.Observation;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Decimal;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.Quantity;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.code.ListMode;
import com.ibm.fhir.model.type.code.ListStatus;
import com.ibm.fhir.model.type.code.MeasureReportStatus;
import com.ibm.fhir.model.type.code.ObservationStatus;

/**
 * Implementation of FHIR Quality Measure Evaluation logic on top of the IBM FHIR Server
 * model.
 * 
 * @param <ST> Model class for the subject type that will be evaluated (e.g. Patient)
 */
public class R4MeasureEvaluation<ST extends DomainResource> extends
        MeasureEvaluation<Object, Measure, Measure.Group, Measure.Group.Population, Measure.SupplementalData, 
            MeasureReport.Builder, MeasureReport.Group.Builder, MeasureReport.Group.Population.Builder, 
            Coding, Extension, Reference.Builder, com.ibm.fhir.model.resource.List, com.ibm.fhir.model.resource.List.Entry,
            DomainResource, ST> {

    public R4MeasureEvaluation(Context context, Measure measure, Interval measurementPeriod, String packageName,
            Function<DomainResource, String> getId, String patientOrPractitionerId) {
        super(context, measure, measurementPeriod, packageName, getId, patientOrPractitionerId);
    }

    public R4MeasureEvaluation(Context context, Measure measure, Interval measurementPeriod, String packageName,
            Function<DomainResource, String> getId) {
        super(context, measure, measurementPeriod, packageName, getId);
    }

    @Override
    protected void addPopulationReport(MeasureReport.Builder report, MeasureReport.Group.Builder group,
        Measure.Group.Population populationCriteria, int populationCount, Iterable<ST> subjectPopulation) {
        MeasureReport.Group.Population.Builder populationReport = MeasureReport.Group.Population.builder();
        populationReport.count(com.ibm.fhir.model.type.Integer.of(populationCount));
        populationReport.code(populationCriteria.getCode());

        MeasureReportType type = MeasureReportType.fromCode(report.build().getType().getValue());
        if ((type == MeasureReportType.SUBJECTLIST || type == MeasureReportType.PATIENTLIST) && subjectPopulation != null) {
            com.ibm.fhir.model.resource.List.Builder SUBJECTLIST = com.ibm.fhir.model.resource.List.builder();
            String listId = UUID.randomUUID().toString();
            SUBJECTLIST.id( listId );
            populationReport.subjectResults(Reference.builder().reference(fhirstring("#" + listId)).build());
            for( ST patient : subjectPopulation ) {
                com.ibm.fhir.model.resource.List.Entry.Builder entry = com.ibm.fhir.model.resource.List.Entry.builder();
                
                String patientId = this.getId.apply( patient );
                
                entry.item(Reference.builder().reference(fhirstring(patientId.startsWith("Patient/") ? patientId : String.format("Patient/%s", patientId))).build());
                SUBJECTLIST.entry(entry.build());
            }
            report.contained(SUBJECTLIST.build());
        }

        group.population(populationReport.build());
    }

    @Override
    protected void addReportGroup(MeasureReport.Builder report, MeasureReport.Group.Builder group) {
        report.group(group.build());
    }

    @Override
    protected MeasureReport.Builder createMeasureReport(String status, MeasureReportType type, Interval measurementPeriod,
        List<ST> subjects) {
        MeasureReport.Builder report = MeasureReport.builder();
        report.status(MeasureReportStatus.of(status));
        report.type(com.ibm.fhir.model.type.code.MeasureReportType.of(type.toCode()));
        report.measure(canonical(measure.getUrl(), measure.getVersion()));

        if (type == MeasureReportType.INDIVIDUAL && !subjects.isEmpty()) {
            ST subject = subjects.get(0);
            report.subject(Reference.builder().reference(fhirstring(subject.getClass().getSimpleName() + "/" + this.getId.apply(subject))).build());
        }
        
        OffsetDateTime start =  ((org.opencds.cqf.cql.engine.runtime.DateTime) measurementPeriod.getStart()).getDateTime();
        OffsetDateTime end = ((org.opencds.cqf.cql.engine.runtime.DateTime) measurementPeriod.getEnd()).getDateTime();
        
        report.period(Period.builder().start(DateTime.of(start.toZonedDateTime())).end(DateTime.of(end.toZonedDateTime())).build());

        return report;
    }

    @Override
    protected MeasureReport.Group.Builder createReportGroup(String id) {
        MeasureReport.Group.Builder builder = MeasureReport.Group.builder().id(id);
        builder.setValidating(false);
        return builder;
    }

    @Override
    protected String getCriteriaExpression(Measure.Group.Population population) {
        return population.getCriteria().getExpression().getValue();
    }

    @Override
    protected Iterable<Measure.Group> getGroup() {
        return this.measure.getGroup();
    }

    @Override
    protected String getGroupId(Measure.Group group) {
        return group.getId();
    }

    @Override
    protected MeasureScoring getMeasureScoring() {
        return MeasureScoring.fromCode(this.measure.getScoring().getCoding().get(0).getCode().getValue());
    }

    @Override
    protected Iterable<Measure.Group.Population> getPopulation(Measure.Group mgc) {
        return mgc.getPopulation();
    }

    @Override
    protected MeasurePopulationType getPopulationType(Measure.Group.Population population) {
        return MeasurePopulationType.fromCode(population.getCode().getCoding().get(0).getCode().getValue());
    }

    @Override
    protected void setGroupScore(MeasureReport.Group.Builder group, Double score) {
        group.measureScore(Quantity.builder().value(Decimal.of(score)).build());
    }

    @Override
    protected String getSDEExpression(SupplementalData sdeItem) {
        String result = null;
        if( sdeItem.getCriteria() != null ) {
            result = javastring( sdeItem.getCriteria().getExpression() );
        }
        return result;
    }
    
    @Override
    protected List<Measure.SupplementalData> getSupplementalData(Measure measure) {
        return measure.getSupplementalData();
    }

    @Override
    protected Coding getSDECoding(SupplementalData sdeItem) {
        Coding result = null;
        if( sdeItem.getCode() != null && sdeItem.getCode().getCoding() != null && sdeItem.getCode().getCoding().size() > 0 ) {
            result = sdeItem.getCode().getCoding().get(0);
        }
        return result;
    }

    @Override
    protected boolean isCoding(Object obj) {
        return obj instanceof Coding;
    }

    @Override
    protected Coding createCoding(String text) {
        return Coding.builder().code(Code.of(text)).build();
    }

    @Override
    protected String getCodingCode(Coding coding) {
        String result = null;
        if( coding != null ) {
            result = javastring( coding.getCode() );
        }
        return result;
    }

    @Override
    protected DomainResource createPatientObservation(Measure measure, String populationId, Coding coding) {
        return createObservation(measure, populationId)
                .code( CodeableConcept.builder().text(fhirstring(populationId)).build() )
                .value( CodeableConcept.builder().coding( coding ).build() )
                .build();
    }

    @Override
    protected DomainResource createPopulationObservation(Measure measure, String populationId, Coding coding, Integer value) {
        return createObservation(measure, populationId)
                .code( CodeableConcept.builder().coding(coding).build() )
                .value( fhirinteger(value) )
                .build();
    }
    
    private Observation.Builder createObservation(Measure measure, String populationId) {
        MeasureInfo measureInfo = new MeasureInfo()
                .withMeasure(MeasureInfo.MEASURE_PREFIX + measure.getId())
                .withPopulationId(populationId);
        
        Observation.Builder obs = Observation.builder()
                .status(ObservationStatus.FINAL)
                .extension(createMeasureInfoExtension(measureInfo));
        
        return obs;
    }
    
    protected Extension createMeasureInfoExtension(MeasureInfo measureInfo) {
        
        Extension extExtMeasure = Extension.builder().url(MeasureInfo.MEASURE)
                .value(Canonical.of(measureInfo.getMeasure()))
                .build();
        
        Extension extExtPop = Extension.builder().url(MeasureInfo.POPULATION_ID)
                .value(fhirstring(measureInfo.getPopulationId()))
                .build();
        
        return Extension.builder().url(MeasureInfo.EXT_URL)
                .extension(extExtMeasure, extExtPop)
                .build();
    }

    @Override
    protected void addEvaluatedResource(MeasureReport.Builder report, DomainResource resource) {
        report.evaluatedResource(reference(resource));
    }

    @Override
    protected void addContained(MeasureReport.Builder report, DomainResource resource) {
        report.contained(resource);
    }

    @Override
    protected Coding getExtensionCoding(ST patient, String coreCategory, String sdeCode) {
        Coding.Builder valueCoding = Coding.builder();
        
        patient.getExtension().forEach((ptExt) -> {
            if (ptExt.getUrl().contains(coreCategory)) {
                Coding extCoding = (Coding) ptExt.getExtension().get(0).getValue();
                String extCode = getCodingCode(extCoding);
                if (extCode.equalsIgnoreCase(sdeCode)) {
                    valueCoding.system(extCoding.getSystem());
                    valueCoding.code(extCoding.getCode());
                    valueCoding.display(extCoding.getDisplay());
                }
            }
        });
        
        return valueCoding.build();
    }

    @Override
    protected Extension createCodingExtension(String url, String codeSystem, String code) {
        return Extension.builder().url(url).value(coding(codeSystem, code)).build();
    }

    @Override
    protected Reference.Builder createReference(String resourceId) {
        return Reference.builder().reference(fhirstring(resourceId));
    }

    @Override
    protected com.ibm.fhir.model.resource.List createListResource(Collection<com.ibm.fhir.model.resource.List.Entry> entries) {
        return com.ibm.fhir.model.resource.List.builder()
                .status(ListStatus.CURRENT)
                .mode(ListMode.WORKING)
                .entry(entries).build(); 
    }

    @Override
    protected com.ibm.fhir.model.resource.List.Entry createListEntry(Reference.Builder reference) {
        return com.ibm.fhir.model.resource.List.Entry.builder().item(reference.build()).build();
    }

    @Override
    protected void addExtension(Reference.Builder reference, Extension extension) {
        reference.extension(extension);
    }

    @Override
    protected void setEvaluatedResources(MeasureReport.Builder report, Collection<Reference.Builder> evaluatedResources) {
        report.evaluatedResource( evaluatedResources.stream().map( r -> r.build() ).collect(Collectors.toList() ) );
    }
}
