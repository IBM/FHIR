/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Generated;

import com.ibm.fhir.model.annotation.Binding;
import com.ibm.fhir.model.annotation.Constraint;
import com.ibm.fhir.model.annotation.ReferenceTarget;
import com.ibm.fhir.model.annotation.Required;
import com.ibm.fhir.model.annotation.Summary;
import com.ibm.fhir.model.type.Annotation;
import com.ibm.fhir.model.type.BackboneElement;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.ContactDetail;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Decimal;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.Identifier;
import com.ibm.fhir.model.type.Integer;
import com.ibm.fhir.model.type.Markdown;
import com.ibm.fhir.model.type.Meta;
import com.ibm.fhir.model.type.Narrative;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.RelatedArtifact;
import com.ibm.fhir.model.type.String;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.UsageContext;
import com.ibm.fhir.model.type.code.BindingStrength;
import com.ibm.fhir.model.type.code.PublicationStatus;
import com.ibm.fhir.model.util.ValidationSupport;
import com.ibm.fhir.model.visitor.Visitor;

/**
 * The RiskEvidenceSynthesis resource describes the likelihood of an outcome in a population plus exposure state where 
 * the risk estimate is derived from a combination of research studies.
 */
@Constraint(
    id = "rvs-0",
    level = "Warning",
    location = "(base)",
    description = "Name should be usable as an identifier for the module by machine processing applications such as code generation",
    expression = "name.matches('[A-Z]([A-Za-z0-9_]){0,254}')"
)
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class RiskEvidenceSynthesis extends DomainResource {
    @Summary
    private final Uri url;
    @Summary
    private final List<Identifier> identifier;
    @Summary
    private final String version;
    @Summary
    private final String name;
    @Summary
    private final String title;
    @Summary
    @Binding(
        bindingName = "PublicationStatus",
        strength = BindingStrength.ValueSet.REQUIRED,
        description = "The lifecycle status of an artifact.",
        valueSet = "http://hl7.org/fhir/ValueSet/publication-status|4.0.1"
    )
    @Required
    private final PublicationStatus status;
    @Summary
    private final DateTime date;
    @Summary
    private final String publisher;
    @Summary
    private final List<ContactDetail> contact;
    @Summary
    private final Markdown description;
    private final List<Annotation> note;
    @Summary
    private final List<UsageContext> useContext;
    @Summary
    @Binding(
        bindingName = "Jurisdiction",
        strength = BindingStrength.ValueSet.EXTENSIBLE,
        description = "Countries and regions within which this artifact is targeted for use.",
        valueSet = "http://hl7.org/fhir/ValueSet/jurisdiction"
    )
    private final List<CodeableConcept> jurisdiction;
    private final Markdown copyright;
    private final Date approvalDate;
    private final Date lastReviewDate;
    @Summary
    private final Period effectivePeriod;
    @Binding(
        bindingName = "DefinitionTopic",
        strength = BindingStrength.ValueSet.EXAMPLE,
        description = "High-level categorization of the definition, used for searching, sorting, and filtering.",
        valueSet = "http://hl7.org/fhir/ValueSet/definition-topic"
    )
    private final List<CodeableConcept> topic;
    private final List<ContactDetail> author;
    private final List<ContactDetail> editor;
    private final List<ContactDetail> reviewer;
    private final List<ContactDetail> endorser;
    private final List<RelatedArtifact> relatedArtifact;
    @Binding(
        bindingName = "SynthesisType",
        strength = BindingStrength.ValueSet.EXTENSIBLE,
        description = "Types of combining results from a body of evidence (eg. summary data meta-analysis).",
        valueSet = "http://hl7.org/fhir/ValueSet/synthesis-type"
    )
    private final CodeableConcept synthesisType;
    @Binding(
        bindingName = "StudyType",
        strength = BindingStrength.ValueSet.EXTENSIBLE,
        description = "Types of research studies (types of research methods).",
        valueSet = "http://hl7.org/fhir/ValueSet/study-type"
    )
    private final CodeableConcept studyType;
    @Summary
    @ReferenceTarget({ "EvidenceVariable" })
    @Required
    private final Reference population;
    @Summary
    @ReferenceTarget({ "EvidenceVariable" })
    private final Reference exposure;
    @Summary
    @ReferenceTarget({ "EvidenceVariable" })
    @Required
    private final Reference outcome;
    private final SampleSize sampleSize;
    @Summary
    private final RiskEstimate riskEstimate;
    private final List<Certainty> certainty;

    private volatile int hashCode;

    private RiskEvidenceSynthesis(Builder builder) {
        super(builder);
        url = builder.url;
        identifier = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.identifier, "identifier"));
        version = builder.version;
        name = builder.name;
        title = builder.title;
        status = ValidationSupport.requireNonNull(builder.status, "status");
        date = builder.date;
        publisher = builder.publisher;
        contact = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.contact, "contact"));
        description = builder.description;
        note = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.note, "note"));
        useContext = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.useContext, "useContext"));
        jurisdiction = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.jurisdiction, "jurisdiction"));
        copyright = builder.copyright;
        approvalDate = builder.approvalDate;
        lastReviewDate = builder.lastReviewDate;
        effectivePeriod = builder.effectivePeriod;
        topic = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.topic, "topic"));
        author = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.author, "author"));
        editor = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.editor, "editor"));
        reviewer = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.reviewer, "reviewer"));
        endorser = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.endorser, "endorser"));
        relatedArtifact = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.relatedArtifact, "relatedArtifact"));
        synthesisType = builder.synthesisType;
        studyType = builder.studyType;
        population = ValidationSupport.requireNonNull(builder.population, "population");
        exposure = builder.exposure;
        outcome = ValidationSupport.requireNonNull(builder.outcome, "outcome");
        sampleSize = builder.sampleSize;
        riskEstimate = builder.riskEstimate;
        certainty = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.certainty, "certainty"));
        ValidationSupport.checkReferenceType(population, "population", "EvidenceVariable");
        ValidationSupport.checkReferenceType(exposure, "exposure", "EvidenceVariable");
        ValidationSupport.checkReferenceType(outcome, "outcome", "EvidenceVariable");
        ValidationSupport.requireChildren(this);
    }

    /**
     * An absolute URI that is used to identify this risk evidence synthesis when it is referenced in a specification, model, 
     * design or an instance; also called its canonical identifier. This SHOULD be globally unique and SHOULD be a literal 
     * address at which at which an authoritative instance of this risk evidence synthesis is (or will be) published. This 
     * URL can be the target of a canonical reference. It SHALL remain the same when the risk evidence synthesis is stored on 
     * different servers.
     * 
     * @return
     *     An immutable object of type {@link Uri}.
     */
    public Uri getUrl() {
        return url;
    }

    /**
     * A formal identifier that is used to identify this risk evidence synthesis when it is represented in other formats, or 
     * referenced in a specification, model, design or an instance.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Identifier}.
     */
    public List<Identifier> getIdentifier() {
        return identifier;
    }

    /**
     * The identifier that is used to identify this version of the risk evidence synthesis when it is referenced in a 
     * specification, model, design or instance. This is an arbitrary value managed by the risk evidence synthesis author and 
     * is not expected to be globally unique. For example, it might be a timestamp (e.g. yyyymmdd) if a managed version is 
     * not available. There is also no expectation that versions can be placed in a lexicographical sequence.
     * 
     * @return
     *     An immutable object of type {@link String}.
     */
    public String getVersion() {
        return version;
    }

    /**
     * A natural language name identifying the risk evidence synthesis. This name should be usable as an identifier for the 
     * module by machine processing applications such as code generation.
     * 
     * @return
     *     An immutable object of type {@link String}.
     */
    public String getName() {
        return name;
    }

    /**
     * A short, descriptive, user-friendly title for the risk evidence synthesis.
     * 
     * @return
     *     An immutable object of type {@link String}.
     */
    public String getTitle() {
        return title;
    }

    /**
     * The status of this risk evidence synthesis. Enables tracking the life-cycle of the content.
     * 
     * @return
     *     An immutable object of type {@link PublicationStatus}.
     */
    public PublicationStatus getStatus() {
        return status;
    }

    /**
     * The date (and optionally time) when the risk evidence synthesis was published. The date must change when the business 
     * version changes and it must change if the status code changes. In addition, it should change when the substantive 
     * content of the risk evidence synthesis changes.
     * 
     * @return
     *     An immutable object of type {@link DateTime}.
     */
    public DateTime getDate() {
        return date;
    }

    /**
     * The name of the organization or individual that published the risk evidence synthesis.
     * 
     * @return
     *     An immutable object of type {@link String}.
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Contact details to assist a user in finding and communicating with the publisher.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link ContactDetail}.
     */
    public List<ContactDetail> getContact() {
        return contact;
    }

    /**
     * A free text natural language description of the risk evidence synthesis from a consumer's perspective.
     * 
     * @return
     *     An immutable object of type {@link Markdown}.
     */
    public Markdown getDescription() {
        return description;
    }

    /**
     * A human-readable string to clarify or explain concepts about the resource.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Annotation}.
     */
    public List<Annotation> getNote() {
        return note;
    }

    /**
     * The content was developed with a focus and intent of supporting the contexts that are listed. These contexts may be 
     * general categories (gender, age, ...) or may be references to specific programs (insurance plans, studies, ...) and 
     * may be used to assist with indexing and searching for appropriate risk evidence synthesis instances.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link UsageContext}.
     */
    public List<UsageContext> getUseContext() {
        return useContext;
    }

    /**
     * A legal or geographic region in which the risk evidence synthesis is intended to be used.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link CodeableConcept}.
     */
    public List<CodeableConcept> getJurisdiction() {
        return jurisdiction;
    }

    /**
     * A copyright statement relating to the risk evidence synthesis and/or its contents. Copyright statements are generally 
     * legal restrictions on the use and publishing of the risk evidence synthesis.
     * 
     * @return
     *     An immutable object of type {@link Markdown}.
     */
    public Markdown getCopyright() {
        return copyright;
    }

    /**
     * The date on which the resource content was approved by the publisher. Approval happens once when the content is 
     * officially approved for usage.
     * 
     * @return
     *     An immutable object of type {@link Date}.
     */
    public Date getApprovalDate() {
        return approvalDate;
    }

    /**
     * The date on which the resource content was last reviewed. Review happens periodically after approval but does not 
     * change the original approval date.
     * 
     * @return
     *     An immutable object of type {@link Date}.
     */
    public Date getLastReviewDate() {
        return lastReviewDate;
    }

    /**
     * The period during which the risk evidence synthesis content was or is planned to be in active use.
     * 
     * @return
     *     An immutable object of type {@link Period}.
     */
    public Period getEffectivePeriod() {
        return effectivePeriod;
    }

    /**
     * Descriptive topics related to the content of the RiskEvidenceSynthesis. Topics provide a high-level categorization 
     * grouping types of EffectEvidenceSynthesiss that can be useful for filtering and searching.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link CodeableConcept}.
     */
    public List<CodeableConcept> getTopic() {
        return topic;
    }

    /**
     * An individiual or organization primarily involved in the creation and maintenance of the content.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link ContactDetail}.
     */
    public List<ContactDetail> getAuthor() {
        return author;
    }

    /**
     * An individual or organization primarily responsible for internal coherence of the content.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link ContactDetail}.
     */
    public List<ContactDetail> getEditor() {
        return editor;
    }

    /**
     * An individual or organization primarily responsible for review of some aspect of the content.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link ContactDetail}.
     */
    public List<ContactDetail> getReviewer() {
        return reviewer;
    }

    /**
     * An individual or organization responsible for officially endorsing the content for use in some setting.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link ContactDetail}.
     */
    public List<ContactDetail> getEndorser() {
        return endorser;
    }

    /**
     * Related artifacts such as additional documentation, justification, or bibliographic references.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link RelatedArtifact}.
     */
    public List<RelatedArtifact> getRelatedArtifact() {
        return relatedArtifact;
    }

    /**
     * Type of synthesis eg meta-analysis.
     * 
     * @return
     *     An immutable object of type {@link CodeableConcept}.
     */
    public CodeableConcept getSynthesisType() {
        return synthesisType;
    }

    /**
     * Type of study eg randomized trial.
     * 
     * @return
     *     An immutable object of type {@link CodeableConcept}.
     */
    public CodeableConcept getStudyType() {
        return studyType;
    }

    /**
     * A reference to a EvidenceVariable resource that defines the population for the research.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getPopulation() {
        return population;
    }

    /**
     * A reference to a EvidenceVariable resource that defines the exposure for the research.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getExposure() {
        return exposure;
    }

    /**
     * A reference to a EvidenceVariable resomece that defines the outcome for the research.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getOutcome() {
        return outcome;
    }

    /**
     * A description of the size of the sample involved in the synthesis.
     * 
     * @return
     *     An immutable object of type {@link SampleSize}.
     */
    public SampleSize getSampleSize() {
        return sampleSize;
    }

    /**
     * The estimated risk of the outcome.
     * 
     * @return
     *     An immutable object of type {@link RiskEstimate}.
     */
    public RiskEstimate getRiskEstimate() {
        return riskEstimate;
    }

    /**
     * A description of the certainty of the risk estimate.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Certainty}.
     */
    public List<Certainty> getCertainty() {
        return certainty;
    }

    @Override
    public boolean hasChildren() {
        return super.hasChildren() || 
            (url != null) || 
            !identifier.isEmpty() || 
            (version != null) || 
            (name != null) || 
            (title != null) || 
            (status != null) || 
            (date != null) || 
            (publisher != null) || 
            !contact.isEmpty() || 
            (description != null) || 
            !note.isEmpty() || 
            !useContext.isEmpty() || 
            !jurisdiction.isEmpty() || 
            (copyright != null) || 
            (approvalDate != null) || 
            (lastReviewDate != null) || 
            (effectivePeriod != null) || 
            !topic.isEmpty() || 
            !author.isEmpty() || 
            !editor.isEmpty() || 
            !reviewer.isEmpty() || 
            !endorser.isEmpty() || 
            !relatedArtifact.isEmpty() || 
            (synthesisType != null) || 
            (studyType != null) || 
            (population != null) || 
            (exposure != null) || 
            (outcome != null) || 
            (sampleSize != null) || 
            (riskEstimate != null) || 
            !certainty.isEmpty();
    }

    @Override
    public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
        if (visitor.preVisit(this)) {
            visitor.visitStart(elementName, elementIndex, this);
            if (visitor.visit(elementName, elementIndex, this)) {
                // visit children
                accept(id, "id", visitor);
                accept(meta, "meta", visitor);
                accept(implicitRules, "implicitRules", visitor);
                accept(language, "language", visitor);
                accept(text, "text", visitor);
                accept(contained, "contained", visitor, Resource.class);
                accept(extension, "extension", visitor, Extension.class);
                accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                accept(url, "url", visitor);
                accept(identifier, "identifier", visitor, Identifier.class);
                accept(version, "version", visitor);
                accept(name, "name", visitor);
                accept(title, "title", visitor);
                accept(status, "status", visitor);
                accept(date, "date", visitor);
                accept(publisher, "publisher", visitor);
                accept(contact, "contact", visitor, ContactDetail.class);
                accept(description, "description", visitor);
                accept(note, "note", visitor, Annotation.class);
                accept(useContext, "useContext", visitor, UsageContext.class);
                accept(jurisdiction, "jurisdiction", visitor, CodeableConcept.class);
                accept(copyright, "copyright", visitor);
                accept(approvalDate, "approvalDate", visitor);
                accept(lastReviewDate, "lastReviewDate", visitor);
                accept(effectivePeriod, "effectivePeriod", visitor);
                accept(topic, "topic", visitor, CodeableConcept.class);
                accept(author, "author", visitor, ContactDetail.class);
                accept(editor, "editor", visitor, ContactDetail.class);
                accept(reviewer, "reviewer", visitor, ContactDetail.class);
                accept(endorser, "endorser", visitor, ContactDetail.class);
                accept(relatedArtifact, "relatedArtifact", visitor, RelatedArtifact.class);
                accept(synthesisType, "synthesisType", visitor);
                accept(studyType, "studyType", visitor);
                accept(population, "population", visitor);
                accept(exposure, "exposure", visitor);
                accept(outcome, "outcome", visitor);
                accept(sampleSize, "sampleSize", visitor);
                accept(riskEstimate, "riskEstimate", visitor);
                accept(certainty, "certainty", visitor, Certainty.class);
            }
            visitor.visitEnd(elementName, elementIndex, this);
            visitor.postVisit(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RiskEvidenceSynthesis other = (RiskEvidenceSynthesis) obj;
        return Objects.equals(id, other.id) && 
            Objects.equals(meta, other.meta) && 
            Objects.equals(implicitRules, other.implicitRules) && 
            Objects.equals(language, other.language) && 
            Objects.equals(text, other.text) && 
            Objects.equals(contained, other.contained) && 
            Objects.equals(extension, other.extension) && 
            Objects.equals(modifierExtension, other.modifierExtension) && 
            Objects.equals(url, other.url) && 
            Objects.equals(identifier, other.identifier) && 
            Objects.equals(version, other.version) && 
            Objects.equals(name, other.name) && 
            Objects.equals(title, other.title) && 
            Objects.equals(status, other.status) && 
            Objects.equals(date, other.date) && 
            Objects.equals(publisher, other.publisher) && 
            Objects.equals(contact, other.contact) && 
            Objects.equals(description, other.description) && 
            Objects.equals(note, other.note) && 
            Objects.equals(useContext, other.useContext) && 
            Objects.equals(jurisdiction, other.jurisdiction) && 
            Objects.equals(copyright, other.copyright) && 
            Objects.equals(approvalDate, other.approvalDate) && 
            Objects.equals(lastReviewDate, other.lastReviewDate) && 
            Objects.equals(effectivePeriod, other.effectivePeriod) && 
            Objects.equals(topic, other.topic) && 
            Objects.equals(author, other.author) && 
            Objects.equals(editor, other.editor) && 
            Objects.equals(reviewer, other.reviewer) && 
            Objects.equals(endorser, other.endorser) && 
            Objects.equals(relatedArtifact, other.relatedArtifact) && 
            Objects.equals(synthesisType, other.synthesisType) && 
            Objects.equals(studyType, other.studyType) && 
            Objects.equals(population, other.population) && 
            Objects.equals(exposure, other.exposure) && 
            Objects.equals(outcome, other.outcome) && 
            Objects.equals(sampleSize, other.sampleSize) && 
            Objects.equals(riskEstimate, other.riskEstimate) && 
            Objects.equals(certainty, other.certainty);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, 
                meta, 
                implicitRules, 
                language, 
                text, 
                contained, 
                extension, 
                modifierExtension, 
                url, 
                identifier, 
                version, 
                name, 
                title, 
                status, 
                date, 
                publisher, 
                contact, 
                description, 
                note, 
                useContext, 
                jurisdiction, 
                copyright, 
                approvalDate, 
                lastReviewDate, 
                effectivePeriod, 
                topic, 
                author, 
                editor, 
                reviewer, 
                endorser, 
                relatedArtifact, 
                synthesisType, 
                studyType, 
                population, 
                exposure, 
                outcome, 
                sampleSize, 
                riskEstimate, 
                certainty);
            hashCode = result;
        }
        return result;
    }

    @Override
    public Builder toBuilder() {
        return new Builder().from(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends DomainResource.Builder {
        private Uri url;
        private List<Identifier> identifier = new ArrayList<>();
        private String version;
        private String name;
        private String title;
        private PublicationStatus status;
        private DateTime date;
        private String publisher;
        private List<ContactDetail> contact = new ArrayList<>();
        private Markdown description;
        private List<Annotation> note = new ArrayList<>();
        private List<UsageContext> useContext = new ArrayList<>();
        private List<CodeableConcept> jurisdiction = new ArrayList<>();
        private Markdown copyright;
        private Date approvalDate;
        private Date lastReviewDate;
        private Period effectivePeriod;
        private List<CodeableConcept> topic = new ArrayList<>();
        private List<ContactDetail> author = new ArrayList<>();
        private List<ContactDetail> editor = new ArrayList<>();
        private List<ContactDetail> reviewer = new ArrayList<>();
        private List<ContactDetail> endorser = new ArrayList<>();
        private List<RelatedArtifact> relatedArtifact = new ArrayList<>();
        private CodeableConcept synthesisType;
        private CodeableConcept studyType;
        private Reference population;
        private Reference exposure;
        private Reference outcome;
        private SampleSize sampleSize;
        private RiskEstimate riskEstimate;
        private List<Certainty> certainty = new ArrayList<>();

        private Builder() {
            super();
        }

        /**
         * The logical id of the resource, as used in the URL for the resource. Once assigned, this value never changes.
         * 
         * @param id
         *     Logical id of this artifact
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        /**
         * The metadata about the resource. This is content that is maintained by the infrastructure. Changes to the content 
         * might not always be associated with version changes to the resource.
         * 
         * @param meta
         *     Metadata about the resource
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder meta(Meta meta) {
            return (Builder) super.meta(meta);
        }

        /**
         * A reference to a set of rules that were followed when the resource was constructed, and which must be understood when 
         * processing the content. Often, this is a reference to an implementation guide that defines the special rules along 
         * with other profiles etc.
         * 
         * @param implicitRules
         *     A set of rules under which this content was created
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder implicitRules(Uri implicitRules) {
            return (Builder) super.implicitRules(implicitRules);
        }

        /**
         * The base language in which the resource is written.
         * 
         * @param language
         *     Language of the resource content
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder language(Code language) {
            return (Builder) super.language(language);
        }

        /**
         * A human-readable narrative that contains a summary of the resource and can be used to represent the content of the 
         * resource to a human. The narrative need not encode all the structured data, but is required to contain sufficient 
         * detail to make it "clinically safe" for a human to just read the narrative. Resource definitions may define what 
         * content should be represented in the narrative to ensure clinical safety.
         * 
         * @param text
         *     Text summary of the resource, for human interpretation
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder text(Narrative text) {
            return (Builder) super.text(text);
        }

        /**
         * These resources do not have an independent existence apart from the resource that contains them - they cannot be 
         * identified independently, and nor can they have their own independent transaction scope.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param contained
         *     Contained, inline Resources
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder contained(Resource... contained) {
            return (Builder) super.contained(contained);
        }

        /**
         * These resources do not have an independent existence apart from the resource that contains them - they cannot be 
         * identified independently, and nor can they have their own independent transaction scope.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param contained
         *     Contained, inline Resources
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder contained(Collection<Resource> contained) {
            return (Builder) super.contained(contained);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder extension(Extension... extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource. To make the 
         * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
         * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
         * of the definition of the extension.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param extension
         *     Additional content defined by implementations
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder extension(Collection<Extension> extension) {
            return (Builder) super.extension(extension);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource and that 
         * modifies the understanding of the element that contains it and/or the understanding of the containing element's 
         * descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe and 
         * manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
         * implementer is allowed to define an extension, there is a set of requirements that SHALL be met as part of the 
         * definition of the extension. Applications processing a resource are required to check for modifier extensions.
         * 
         * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
         * change the meaning of modifierExtension itself).
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param modifierExtension
         *     Extensions that cannot be ignored
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder modifierExtension(Extension... modifierExtension) {
            return (Builder) super.modifierExtension(modifierExtension);
        }

        /**
         * May be used to represent additional information that is not part of the basic definition of the resource and that 
         * modifies the understanding of the element that contains it and/or the understanding of the containing element's 
         * descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe and 
         * manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
         * implementer is allowed to define an extension, there is a set of requirements that SHALL be met as part of the 
         * definition of the extension. Applications processing a resource are required to check for modifier extensions.
         * 
         * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
         * change the meaning of modifierExtension itself).
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param modifierExtension
         *     Extensions that cannot be ignored
         * 
         * @return
         *     A reference to this Builder instance
         */
        @Override
        public Builder modifierExtension(Collection<Extension> modifierExtension) {
            return (Builder) super.modifierExtension(modifierExtension);
        }

        /**
         * An absolute URI that is used to identify this risk evidence synthesis when it is referenced in a specification, model, 
         * design or an instance; also called its canonical identifier. This SHOULD be globally unique and SHOULD be a literal 
         * address at which at which an authoritative instance of this risk evidence synthesis is (or will be) published. This 
         * URL can be the target of a canonical reference. It SHALL remain the same when the risk evidence synthesis is stored on 
         * different servers.
         * 
         * @param url
         *     Canonical identifier for this risk evidence synthesis, represented as a URI (globally unique)
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder url(Uri url) {
            this.url = url;
            return this;
        }

        /**
         * A formal identifier that is used to identify this risk evidence synthesis when it is represented in other formats, or 
         * referenced in a specification, model, design or an instance.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param identifier
         *     Additional identifier for the risk evidence synthesis
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder identifier(Identifier... identifier) {
            for (Identifier value : identifier) {
                this.identifier.add(value);
            }
            return this;
        }

        /**
         * A formal identifier that is used to identify this risk evidence synthesis when it is represented in other formats, or 
         * referenced in a specification, model, design or an instance.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param identifier
         *     Additional identifier for the risk evidence synthesis
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder identifier(Collection<Identifier> identifier) {
            this.identifier = new ArrayList<>(identifier);
            return this;
        }

        /**
         * The identifier that is used to identify this version of the risk evidence synthesis when it is referenced in a 
         * specification, model, design or instance. This is an arbitrary value managed by the risk evidence synthesis author and 
         * is not expected to be globally unique. For example, it might be a timestamp (e.g. yyyymmdd) if a managed version is 
         * not available. There is also no expectation that versions can be placed in a lexicographical sequence.
         * 
         * @param version
         *     Business version of the risk evidence synthesis
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * A natural language name identifying the risk evidence synthesis. This name should be usable as an identifier for the 
         * module by machine processing applications such as code generation.
         * 
         * @param name
         *     Name for this risk evidence synthesis (computer friendly)
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * A short, descriptive, user-friendly title for the risk evidence synthesis.
         * 
         * @param title
         *     Name for this risk evidence synthesis (human friendly)
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * The status of this risk evidence synthesis. Enables tracking the life-cycle of the content.
         * 
         * <p>This element is required.
         * 
         * @param status
         *     draft | active | retired | unknown
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder status(PublicationStatus status) {
            this.status = status;
            return this;
        }

        /**
         * The date (and optionally time) when the risk evidence synthesis was published. The date must change when the business 
         * version changes and it must change if the status code changes. In addition, it should change when the substantive 
         * content of the risk evidence synthesis changes.
         * 
         * @param date
         *     Date last changed
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder date(DateTime date) {
            this.date = date;
            return this;
        }

        /**
         * The name of the organization or individual that published the risk evidence synthesis.
         * 
         * @param publisher
         *     Name of the publisher (organization or individual)
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder publisher(String publisher) {
            this.publisher = publisher;
            return this;
        }

        /**
         * Contact details to assist a user in finding and communicating with the publisher.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param contact
         *     Contact details for the publisher
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder contact(ContactDetail... contact) {
            for (ContactDetail value : contact) {
                this.contact.add(value);
            }
            return this;
        }

        /**
         * Contact details to assist a user in finding and communicating with the publisher.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param contact
         *     Contact details for the publisher
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder contact(Collection<ContactDetail> contact) {
            this.contact = new ArrayList<>(contact);
            return this;
        }

        /**
         * A free text natural language description of the risk evidence synthesis from a consumer's perspective.
         * 
         * @param description
         *     Natural language description of the risk evidence synthesis
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder description(Markdown description) {
            this.description = description;
            return this;
        }

        /**
         * A human-readable string to clarify or explain concepts about the resource.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param note
         *     Used for footnotes or explanatory notes
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder note(Annotation... note) {
            for (Annotation value : note) {
                this.note.add(value);
            }
            return this;
        }

        /**
         * A human-readable string to clarify or explain concepts about the resource.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param note
         *     Used for footnotes or explanatory notes
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder note(Collection<Annotation> note) {
            this.note = new ArrayList<>(note);
            return this;
        }

        /**
         * The content was developed with a focus and intent of supporting the contexts that are listed. These contexts may be 
         * general categories (gender, age, ...) or may be references to specific programs (insurance plans, studies, ...) and 
         * may be used to assist with indexing and searching for appropriate risk evidence synthesis instances.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param useContext
         *     The context that the content is intended to support
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder useContext(UsageContext... useContext) {
            for (UsageContext value : useContext) {
                this.useContext.add(value);
            }
            return this;
        }

        /**
         * The content was developed with a focus and intent of supporting the contexts that are listed. These contexts may be 
         * general categories (gender, age, ...) or may be references to specific programs (insurance plans, studies, ...) and 
         * may be used to assist with indexing and searching for appropriate risk evidence synthesis instances.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param useContext
         *     The context that the content is intended to support
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder useContext(Collection<UsageContext> useContext) {
            this.useContext = new ArrayList<>(useContext);
            return this;
        }

        /**
         * A legal or geographic region in which the risk evidence synthesis is intended to be used.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param jurisdiction
         *     Intended jurisdiction for risk evidence synthesis (if applicable)
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder jurisdiction(CodeableConcept... jurisdiction) {
            for (CodeableConcept value : jurisdiction) {
                this.jurisdiction.add(value);
            }
            return this;
        }

        /**
         * A legal or geographic region in which the risk evidence synthesis is intended to be used.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param jurisdiction
         *     Intended jurisdiction for risk evidence synthesis (if applicable)
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder jurisdiction(Collection<CodeableConcept> jurisdiction) {
            this.jurisdiction = new ArrayList<>(jurisdiction);
            return this;
        }

        /**
         * A copyright statement relating to the risk evidence synthesis and/or its contents. Copyright statements are generally 
         * legal restrictions on the use and publishing of the risk evidence synthesis.
         * 
         * @param copyright
         *     Use and/or publishing restrictions
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder copyright(Markdown copyright) {
            this.copyright = copyright;
            return this;
        }

        /**
         * The date on which the resource content was approved by the publisher. Approval happens once when the content is 
         * officially approved for usage.
         * 
         * @param approvalDate
         *     When the risk evidence synthesis was approved by publisher
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder approvalDate(Date approvalDate) {
            this.approvalDate = approvalDate;
            return this;
        }

        /**
         * The date on which the resource content was last reviewed. Review happens periodically after approval but does not 
         * change the original approval date.
         * 
         * @param lastReviewDate
         *     When the risk evidence synthesis was last reviewed
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder lastReviewDate(Date lastReviewDate) {
            this.lastReviewDate = lastReviewDate;
            return this;
        }

        /**
         * The period during which the risk evidence synthesis content was or is planned to be in active use.
         * 
         * @param effectivePeriod
         *     When the risk evidence synthesis is expected to be used
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder effectivePeriod(Period effectivePeriod) {
            this.effectivePeriod = effectivePeriod;
            return this;
        }

        /**
         * Descriptive topics related to the content of the RiskEvidenceSynthesis. Topics provide a high-level categorization 
         * grouping types of EffectEvidenceSynthesiss that can be useful for filtering and searching.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param topic
         *     The category of the EffectEvidenceSynthesis, such as Education, Treatment, Assessment, etc.
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder topic(CodeableConcept... topic) {
            for (CodeableConcept value : topic) {
                this.topic.add(value);
            }
            return this;
        }

        /**
         * Descriptive topics related to the content of the RiskEvidenceSynthesis. Topics provide a high-level categorization 
         * grouping types of EffectEvidenceSynthesiss that can be useful for filtering and searching.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param topic
         *     The category of the EffectEvidenceSynthesis, such as Education, Treatment, Assessment, etc.
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder topic(Collection<CodeableConcept> topic) {
            this.topic = new ArrayList<>(topic);
            return this;
        }

        /**
         * An individiual or organization primarily involved in the creation and maintenance of the content.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param author
         *     Who authored the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder author(ContactDetail... author) {
            for (ContactDetail value : author) {
                this.author.add(value);
            }
            return this;
        }

        /**
         * An individiual or organization primarily involved in the creation and maintenance of the content.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param author
         *     Who authored the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder author(Collection<ContactDetail> author) {
            this.author = new ArrayList<>(author);
            return this;
        }

        /**
         * An individual or organization primarily responsible for internal coherence of the content.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param editor
         *     Who edited the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder editor(ContactDetail... editor) {
            for (ContactDetail value : editor) {
                this.editor.add(value);
            }
            return this;
        }

        /**
         * An individual or organization primarily responsible for internal coherence of the content.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param editor
         *     Who edited the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder editor(Collection<ContactDetail> editor) {
            this.editor = new ArrayList<>(editor);
            return this;
        }

        /**
         * An individual or organization primarily responsible for review of some aspect of the content.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param reviewer
         *     Who reviewed the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reviewer(ContactDetail... reviewer) {
            for (ContactDetail value : reviewer) {
                this.reviewer.add(value);
            }
            return this;
        }

        /**
         * An individual or organization primarily responsible for review of some aspect of the content.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param reviewer
         *     Who reviewed the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reviewer(Collection<ContactDetail> reviewer) {
            this.reviewer = new ArrayList<>(reviewer);
            return this;
        }

        /**
         * An individual or organization responsible for officially endorsing the content for use in some setting.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param endorser
         *     Who endorsed the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder endorser(ContactDetail... endorser) {
            for (ContactDetail value : endorser) {
                this.endorser.add(value);
            }
            return this;
        }

        /**
         * An individual or organization responsible for officially endorsing the content for use in some setting.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param endorser
         *     Who endorsed the content
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder endorser(Collection<ContactDetail> endorser) {
            this.endorser = new ArrayList<>(endorser);
            return this;
        }

        /**
         * Related artifacts such as additional documentation, justification, or bibliographic references.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param relatedArtifact
         *     Additional documentation, citations, etc.
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder relatedArtifact(RelatedArtifact... relatedArtifact) {
            for (RelatedArtifact value : relatedArtifact) {
                this.relatedArtifact.add(value);
            }
            return this;
        }

        /**
         * Related artifacts such as additional documentation, justification, or bibliographic references.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param relatedArtifact
         *     Additional documentation, citations, etc.
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder relatedArtifact(Collection<RelatedArtifact> relatedArtifact) {
            this.relatedArtifact = new ArrayList<>(relatedArtifact);
            return this;
        }

        /**
         * Type of synthesis eg meta-analysis.
         * 
         * @param synthesisType
         *     Type of synthesis
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder synthesisType(CodeableConcept synthesisType) {
            this.synthesisType = synthesisType;
            return this;
        }

        /**
         * Type of study eg randomized trial.
         * 
         * @param studyType
         *     Type of study
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder studyType(CodeableConcept studyType) {
            this.studyType = studyType;
            return this;
        }

        /**
         * A reference to a EvidenceVariable resource that defines the population for the research.
         * 
         * <p>This element is required.
         * 
         * <p>Allowed resource types for this reference:
         * <ul>
         * <li>{@link EvidenceVariable}</li>
         * </ul>
         * 
         * @param population
         *     What population?
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder population(Reference population) {
            this.population = population;
            return this;
        }

        /**
         * A reference to a EvidenceVariable resource that defines the exposure for the research.
         * 
         * <p>Allowed resource types for this reference:
         * <ul>
         * <li>{@link EvidenceVariable}</li>
         * </ul>
         * 
         * @param exposure
         *     What exposure?
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder exposure(Reference exposure) {
            this.exposure = exposure;
            return this;
        }

        /**
         * A reference to a EvidenceVariable resomece that defines the outcome for the research.
         * 
         * <p>This element is required.
         * 
         * <p>Allowed resource types for this reference:
         * <ul>
         * <li>{@link EvidenceVariable}</li>
         * </ul>
         * 
         * @param outcome
         *     What outcome?
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder outcome(Reference outcome) {
            this.outcome = outcome;
            return this;
        }

        /**
         * A description of the size of the sample involved in the synthesis.
         * 
         * @param sampleSize
         *     What sample size was involved?
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder sampleSize(SampleSize sampleSize) {
            this.sampleSize = sampleSize;
            return this;
        }

        /**
         * The estimated risk of the outcome.
         * 
         * @param riskEstimate
         *     What was the estimated risk
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder riskEstimate(RiskEstimate riskEstimate) {
            this.riskEstimate = riskEstimate;
            return this;
        }

        /**
         * A description of the certainty of the risk estimate.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param certainty
         *     How certain is the risk
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder certainty(Certainty... certainty) {
            for (Certainty value : certainty) {
                this.certainty.add(value);
            }
            return this;
        }

        /**
         * A description of the certainty of the risk estimate.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param certainty
         *     How certain is the risk
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder certainty(Collection<Certainty> certainty) {
            this.certainty = new ArrayList<>(certainty);
            return this;
        }

        /**
         * Build the {@link RiskEvidenceSynthesis}
         * 
         * <p>Required elements:
         * <ul>
         * <li>status</li>
         * <li>population</li>
         * <li>outcome</li>
         * </ul>
         * 
         * @return
         *     An immutable object of type {@link RiskEvidenceSynthesis}
         */
        @Override
        public RiskEvidenceSynthesis build() {
            return new RiskEvidenceSynthesis(this);
        }

        protected Builder from(RiskEvidenceSynthesis riskEvidenceSynthesis) {
            super.from(riskEvidenceSynthesis);
            url = riskEvidenceSynthesis.url;
            identifier.addAll(riskEvidenceSynthesis.identifier);
            version = riskEvidenceSynthesis.version;
            name = riskEvidenceSynthesis.name;
            title = riskEvidenceSynthesis.title;
            status = riskEvidenceSynthesis.status;
            date = riskEvidenceSynthesis.date;
            publisher = riskEvidenceSynthesis.publisher;
            contact.addAll(riskEvidenceSynthesis.contact);
            description = riskEvidenceSynthesis.description;
            note.addAll(riskEvidenceSynthesis.note);
            useContext.addAll(riskEvidenceSynthesis.useContext);
            jurisdiction.addAll(riskEvidenceSynthesis.jurisdiction);
            copyright = riskEvidenceSynthesis.copyright;
            approvalDate = riskEvidenceSynthesis.approvalDate;
            lastReviewDate = riskEvidenceSynthesis.lastReviewDate;
            effectivePeriod = riskEvidenceSynthesis.effectivePeriod;
            topic.addAll(riskEvidenceSynthesis.topic);
            author.addAll(riskEvidenceSynthesis.author);
            editor.addAll(riskEvidenceSynthesis.editor);
            reviewer.addAll(riskEvidenceSynthesis.reviewer);
            endorser.addAll(riskEvidenceSynthesis.endorser);
            relatedArtifact.addAll(riskEvidenceSynthesis.relatedArtifact);
            synthesisType = riskEvidenceSynthesis.synthesisType;
            studyType = riskEvidenceSynthesis.studyType;
            population = riskEvidenceSynthesis.population;
            exposure = riskEvidenceSynthesis.exposure;
            outcome = riskEvidenceSynthesis.outcome;
            sampleSize = riskEvidenceSynthesis.sampleSize;
            riskEstimate = riskEvidenceSynthesis.riskEstimate;
            certainty.addAll(riskEvidenceSynthesis.certainty);
            return this;
        }
    }

    /**
     * A description of the size of the sample involved in the synthesis.
     */
    public static class SampleSize extends BackboneElement {
        private final String description;
        private final Integer numberOfStudies;
        private final Integer numberOfParticipants;

        private volatile int hashCode;

        private SampleSize(Builder builder) {
            super(builder);
            description = builder.description;
            numberOfStudies = builder.numberOfStudies;
            numberOfParticipants = builder.numberOfParticipants;
            ValidationSupport.requireValueOrChildren(this);
        }

        /**
         * Human-readable summary of sample size.
         * 
         * @return
         *     An immutable object of type {@link String}.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Number of studies included in this evidence synthesis.
         * 
         * @return
         *     An immutable object of type {@link Integer}.
         */
        public Integer getNumberOfStudies() {
            return numberOfStudies;
        }

        /**
         * Number of participants included in this evidence synthesis.
         * 
         * @return
         *     An immutable object of type {@link Integer}.
         */
        public Integer getNumberOfParticipants() {
            return numberOfParticipants;
        }

        @Override
        public boolean hasChildren() {
            return super.hasChildren() || 
                (description != null) || 
                (numberOfStudies != null) || 
                (numberOfParticipants != null);
        }

        @Override
        public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
            if (visitor.preVisit(this)) {
                visitor.visitStart(elementName, elementIndex, this);
                if (visitor.visit(elementName, elementIndex, this)) {
                    // visit children
                    accept(id, "id", visitor);
                    accept(extension, "extension", visitor, Extension.class);
                    accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                    accept(description, "description", visitor);
                    accept(numberOfStudies, "numberOfStudies", visitor);
                    accept(numberOfParticipants, "numberOfParticipants", visitor);
                }
                visitor.visitEnd(elementName, elementIndex, this);
                visitor.postVisit(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SampleSize other = (SampleSize) obj;
            return Objects.equals(id, other.id) && 
                Objects.equals(extension, other.extension) && 
                Objects.equals(modifierExtension, other.modifierExtension) && 
                Objects.equals(description, other.description) && 
                Objects.equals(numberOfStudies, other.numberOfStudies) && 
                Objects.equals(numberOfParticipants, other.numberOfParticipants);
        }

        @Override
        public int hashCode() {
            int result = hashCode;
            if (result == 0) {
                result = Objects.hash(id, 
                    extension, 
                    modifierExtension, 
                    description, 
                    numberOfStudies, 
                    numberOfParticipants);
                hashCode = result;
            }
            return result;
        }

        @Override
        public Builder toBuilder() {
            return new Builder().from(this);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends BackboneElement.Builder {
            private String description;
            private Integer numberOfStudies;
            private Integer numberOfParticipants;

            private Builder() {
                super();
            }

            /**
             * Unique id for the element within a resource (for internal references). This may be any string value that does not 
             * contain spaces.
             * 
             * @param id
             *     Unique id for inter-element referencing
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder id(java.lang.String id) {
                return (Builder) super.id(id);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
             * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
             * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
             * of the definition of the extension.
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param extension
             *     Additional content defined by implementations
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder extension(Extension... extension) {
                return (Builder) super.extension(extension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
             * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
             * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
             * of the definition of the extension.
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param extension
             *     Additional content defined by implementations
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder extension(Collection<Extension> extension) {
                return (Builder) super.extension(extension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder modifierExtension(Extension... modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder modifierExtension(Collection<Extension> modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * Human-readable summary of sample size.
             * 
             * @param description
             *     Description of sample size
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder description(String description) {
                this.description = description;
                return this;
            }

            /**
             * Number of studies included in this evidence synthesis.
             * 
             * @param numberOfStudies
             *     How many studies?
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder numberOfStudies(Integer numberOfStudies) {
                this.numberOfStudies = numberOfStudies;
                return this;
            }

            /**
             * Number of participants included in this evidence synthesis.
             * 
             * @param numberOfParticipants
             *     How many participants?
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder numberOfParticipants(Integer numberOfParticipants) {
                this.numberOfParticipants = numberOfParticipants;
                return this;
            }

            /**
             * Build the {@link SampleSize}
             * 
             * @return
             *     An immutable object of type {@link SampleSize}
             */
            @Override
            public SampleSize build() {
                return new SampleSize(this);
            }

            protected Builder from(SampleSize sampleSize) {
                super.from(sampleSize);
                description = sampleSize.description;
                numberOfStudies = sampleSize.numberOfStudies;
                numberOfParticipants = sampleSize.numberOfParticipants;
                return this;
            }
        }
    }

    /**
     * The estimated risk of the outcome.
     */
    public static class RiskEstimate extends BackboneElement {
        private final String description;
        @Binding(
            bindingName = "RiskEstimateType",
            strength = BindingStrength.ValueSet.EXTENSIBLE,
            description = "Whether the risk estimate is dichotomous, continuous or qualitative and the specific type of risk estimate (eg proportion or median).",
            valueSet = "http://hl7.org/fhir/ValueSet/risk-estimate-type"
        )
        private final CodeableConcept type;
        private final Decimal value;
        @Binding(
            bindingName = "UCUMUnits",
            strength = BindingStrength.ValueSet.REQUIRED,
            description = "Unified Code for Units of Measure (UCUM).",
            valueSet = "http://hl7.org/fhir/ValueSet/ucum-units|4.0.1"
        )
        private final CodeableConcept unitOfMeasure;
        private final Integer denominatorCount;
        private final Integer numeratorCount;
        private final List<PrecisionEstimate> precisionEstimate;

        private volatile int hashCode;

        private RiskEstimate(Builder builder) {
            super(builder);
            description = builder.description;
            type = builder.type;
            value = builder.value;
            unitOfMeasure = builder.unitOfMeasure;
            denominatorCount = builder.denominatorCount;
            numeratorCount = builder.numeratorCount;
            precisionEstimate = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.precisionEstimate, "precisionEstimate"));
            ValidationSupport.requireValueOrChildren(this);
        }

        /**
         * Human-readable summary of risk estimate.
         * 
         * @return
         *     An immutable object of type {@link String}.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Examples include proportion and mean.
         * 
         * @return
         *     An immutable object of type {@link CodeableConcept}.
         */
        public CodeableConcept getType() {
            return type;
        }

        /**
         * The point estimate of the risk estimate.
         * 
         * @return
         *     An immutable object of type {@link Decimal}.
         */
        public Decimal getValue() {
            return value;
        }

        /**
         * Specifies the UCUM unit for the outcome.
         * 
         * @return
         *     An immutable object of type {@link CodeableConcept}.
         */
        public CodeableConcept getUnitOfMeasure() {
            return unitOfMeasure;
        }

        /**
         * The sample size for the group that was measured for this risk estimate.
         * 
         * @return
         *     An immutable object of type {@link Integer}.
         */
        public Integer getDenominatorCount() {
            return denominatorCount;
        }

        /**
         * The number of group members with the outcome of interest.
         * 
         * @return
         *     An immutable object of type {@link Integer}.
         */
        public Integer getNumeratorCount() {
            return numeratorCount;
        }

        /**
         * A description of the precision of the estimate for the effect.
         * 
         * @return
         *     An unmodifiable list containing immutable objects of type {@link PrecisionEstimate}.
         */
        public List<PrecisionEstimate> getPrecisionEstimate() {
            return precisionEstimate;
        }

        @Override
        public boolean hasChildren() {
            return super.hasChildren() || 
                (description != null) || 
                (type != null) || 
                (value != null) || 
                (unitOfMeasure != null) || 
                (denominatorCount != null) || 
                (numeratorCount != null) || 
                !precisionEstimate.isEmpty();
        }

        @Override
        public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
            if (visitor.preVisit(this)) {
                visitor.visitStart(elementName, elementIndex, this);
                if (visitor.visit(elementName, elementIndex, this)) {
                    // visit children
                    accept(id, "id", visitor);
                    accept(extension, "extension", visitor, Extension.class);
                    accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                    accept(description, "description", visitor);
                    accept(type, "type", visitor);
                    accept(value, "value", visitor);
                    accept(unitOfMeasure, "unitOfMeasure", visitor);
                    accept(denominatorCount, "denominatorCount", visitor);
                    accept(numeratorCount, "numeratorCount", visitor);
                    accept(precisionEstimate, "precisionEstimate", visitor, PrecisionEstimate.class);
                }
                visitor.visitEnd(elementName, elementIndex, this);
                visitor.postVisit(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RiskEstimate other = (RiskEstimate) obj;
            return Objects.equals(id, other.id) && 
                Objects.equals(extension, other.extension) && 
                Objects.equals(modifierExtension, other.modifierExtension) && 
                Objects.equals(description, other.description) && 
                Objects.equals(type, other.type) && 
                Objects.equals(value, other.value) && 
                Objects.equals(unitOfMeasure, other.unitOfMeasure) && 
                Objects.equals(denominatorCount, other.denominatorCount) && 
                Objects.equals(numeratorCount, other.numeratorCount) && 
                Objects.equals(precisionEstimate, other.precisionEstimate);
        }

        @Override
        public int hashCode() {
            int result = hashCode;
            if (result == 0) {
                result = Objects.hash(id, 
                    extension, 
                    modifierExtension, 
                    description, 
                    type, 
                    value, 
                    unitOfMeasure, 
                    denominatorCount, 
                    numeratorCount, 
                    precisionEstimate);
                hashCode = result;
            }
            return result;
        }

        @Override
        public Builder toBuilder() {
            return new Builder().from(this);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends BackboneElement.Builder {
            private String description;
            private CodeableConcept type;
            private Decimal value;
            private CodeableConcept unitOfMeasure;
            private Integer denominatorCount;
            private Integer numeratorCount;
            private List<PrecisionEstimate> precisionEstimate = new ArrayList<>();

            private Builder() {
                super();
            }

            /**
             * Unique id for the element within a resource (for internal references). This may be any string value that does not 
             * contain spaces.
             * 
             * @param id
             *     Unique id for inter-element referencing
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder id(java.lang.String id) {
                return (Builder) super.id(id);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
             * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
             * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
             * of the definition of the extension.
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param extension
             *     Additional content defined by implementations
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder extension(Extension... extension) {
                return (Builder) super.extension(extension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
             * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
             * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
             * of the definition of the extension.
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param extension
             *     Additional content defined by implementations
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder extension(Collection<Extension> extension) {
                return (Builder) super.extension(extension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder modifierExtension(Extension... modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder modifierExtension(Collection<Extension> modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * Human-readable summary of risk estimate.
             * 
             * @param description
             *     Description of risk estimate
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder description(String description) {
                this.description = description;
                return this;
            }

            /**
             * Examples include proportion and mean.
             * 
             * @param type
             *     Type of risk estimate
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder type(CodeableConcept type) {
                this.type = type;
                return this;
            }

            /**
             * The point estimate of the risk estimate.
             * 
             * @param value
             *     Point estimate
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder value(Decimal value) {
                this.value = value;
                return this;
            }

            /**
             * Specifies the UCUM unit for the outcome.
             * 
             * @param unitOfMeasure
             *     What unit is the outcome described in?
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder unitOfMeasure(CodeableConcept unitOfMeasure) {
                this.unitOfMeasure = unitOfMeasure;
                return this;
            }

            /**
             * The sample size for the group that was measured for this risk estimate.
             * 
             * @param denominatorCount
             *     Sample size for group measured
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder denominatorCount(Integer denominatorCount) {
                this.denominatorCount = denominatorCount;
                return this;
            }

            /**
             * The number of group members with the outcome of interest.
             * 
             * @param numeratorCount
             *     Number with the outcome
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder numeratorCount(Integer numeratorCount) {
                this.numeratorCount = numeratorCount;
                return this;
            }

            /**
             * A description of the precision of the estimate for the effect.
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param precisionEstimate
             *     How precise the estimate is
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder precisionEstimate(PrecisionEstimate... precisionEstimate) {
                for (PrecisionEstimate value : precisionEstimate) {
                    this.precisionEstimate.add(value);
                }
                return this;
            }

            /**
             * A description of the precision of the estimate for the effect.
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param precisionEstimate
             *     How precise the estimate is
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder precisionEstimate(Collection<PrecisionEstimate> precisionEstimate) {
                this.precisionEstimate = new ArrayList<>(precisionEstimate);
                return this;
            }

            /**
             * Build the {@link RiskEstimate}
             * 
             * @return
             *     An immutable object of type {@link RiskEstimate}
             */
            @Override
            public RiskEstimate build() {
                return new RiskEstimate(this);
            }

            protected Builder from(RiskEstimate riskEstimate) {
                super.from(riskEstimate);
                description = riskEstimate.description;
                type = riskEstimate.type;
                value = riskEstimate.value;
                unitOfMeasure = riskEstimate.unitOfMeasure;
                denominatorCount = riskEstimate.denominatorCount;
                numeratorCount = riskEstimate.numeratorCount;
                precisionEstimate.addAll(riskEstimate.precisionEstimate);
                return this;
            }
        }

        /**
         * A description of the precision of the estimate for the effect.
         */
        public static class PrecisionEstimate extends BackboneElement {
            @Binding(
                bindingName = "PrecisionEstimateType",
                strength = BindingStrength.ValueSet.EXTENSIBLE,
                description = "Method of reporting variability of estimates, such as confidence intervals, interquartile range or standard deviation.",
                valueSet = "http://hl7.org/fhir/ValueSet/precision-estimate-type"
            )
            private final CodeableConcept type;
            private final Decimal level;
            private final Decimal from;
            private final Decimal to;

            private volatile int hashCode;

            private PrecisionEstimate(Builder builder) {
                super(builder);
                type = builder.type;
                level = builder.level;
                from = builder.from;
                to = builder.to;
                ValidationSupport.requireValueOrChildren(this);
            }

            /**
             * Examples include confidence interval and interquartile range.
             * 
             * @return
             *     An immutable object of type {@link CodeableConcept}.
             */
            public CodeableConcept getType() {
                return type;
            }

            /**
             * Use 95 for a 95% confidence interval.
             * 
             * @return
             *     An immutable object of type {@link Decimal}.
             */
            public Decimal getLevel() {
                return level;
            }

            /**
             * Lower bound of confidence interval.
             * 
             * @return
             *     An immutable object of type {@link Decimal}.
             */
            public Decimal getFrom() {
                return from;
            }

            /**
             * Upper bound of confidence interval.
             * 
             * @return
             *     An immutable object of type {@link Decimal}.
             */
            public Decimal getTo() {
                return to;
            }

            @Override
            public boolean hasChildren() {
                return super.hasChildren() || 
                    (type != null) || 
                    (level != null) || 
                    (from != null) || 
                    (to != null);
            }

            @Override
            public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
                if (visitor.preVisit(this)) {
                    visitor.visitStart(elementName, elementIndex, this);
                    if (visitor.visit(elementName, elementIndex, this)) {
                        // visit children
                        accept(id, "id", visitor);
                        accept(extension, "extension", visitor, Extension.class);
                        accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                        accept(type, "type", visitor);
                        accept(level, "level", visitor);
                        accept(from, "from", visitor);
                        accept(to, "to", visitor);
                    }
                    visitor.visitEnd(elementName, elementIndex, this);
                    visitor.postVisit(this);
                }
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                PrecisionEstimate other = (PrecisionEstimate) obj;
                return Objects.equals(id, other.id) && 
                    Objects.equals(extension, other.extension) && 
                    Objects.equals(modifierExtension, other.modifierExtension) && 
                    Objects.equals(type, other.type) && 
                    Objects.equals(level, other.level) && 
                    Objects.equals(from, other.from) && 
                    Objects.equals(to, other.to);
            }

            @Override
            public int hashCode() {
                int result = hashCode;
                if (result == 0) {
                    result = Objects.hash(id, 
                        extension, 
                        modifierExtension, 
                        type, 
                        level, 
                        from, 
                        to);
                    hashCode = result;
                }
                return result;
            }

            @Override
            public Builder toBuilder() {
                return new Builder().from(this);
            }

            public static Builder builder() {
                return new Builder();
            }

            public static class Builder extends BackboneElement.Builder {
                private CodeableConcept type;
                private Decimal level;
                private Decimal from;
                private Decimal to;

                private Builder() {
                    super();
                }

                /**
                 * Unique id for the element within a resource (for internal references). This may be any string value that does not 
                 * contain spaces.
                 * 
                 * @param id
                 *     Unique id for inter-element referencing
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder id(java.lang.String id) {
                    return (Builder) super.id(id);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element. To make the 
                 * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
                 * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
                 * of the definition of the extension.
                 * 
                 * <p>Adds new element(s) to the existing list
                 * 
                 * @param extension
                 *     Additional content defined by implementations
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder extension(Extension... extension) {
                    return (Builder) super.extension(extension);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element. To make the 
                 * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
                 * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
                 * of the definition of the extension.
                 * 
                 * <p>Replaces the existing list with a new one containing elements from the Collection
                 * 
                 * @param extension
                 *     Additional content defined by implementations
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder extension(Collection<Extension> extension) {
                    return (Builder) super.extension(extension);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element and that 
                 * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
                 * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
                 * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
                 * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
                 * extension. Applications processing a resource are required to check for modifier extensions.
                 * 
                 * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
                 * change the meaning of modifierExtension itself).
                 * 
                 * <p>Adds new element(s) to the existing list
                 * 
                 * @param modifierExtension
                 *     Extensions that cannot be ignored even if unrecognized
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder modifierExtension(Extension... modifierExtension) {
                    return (Builder) super.modifierExtension(modifierExtension);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element and that 
                 * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
                 * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
                 * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
                 * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
                 * extension. Applications processing a resource are required to check for modifier extensions.
                 * 
                 * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
                 * change the meaning of modifierExtension itself).
                 * 
                 * <p>Replaces the existing list with a new one containing elements from the Collection
                 * 
                 * @param modifierExtension
                 *     Extensions that cannot be ignored even if unrecognized
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder modifierExtension(Collection<Extension> modifierExtension) {
                    return (Builder) super.modifierExtension(modifierExtension);
                }

                /**
                 * Examples include confidence interval and interquartile range.
                 * 
                 * @param type
                 *     Type of precision estimate
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder type(CodeableConcept type) {
                    this.type = type;
                    return this;
                }

                /**
                 * Use 95 for a 95% confidence interval.
                 * 
                 * @param level
                 *     Level of confidence interval
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder level(Decimal level) {
                    this.level = level;
                    return this;
                }

                /**
                 * Lower bound of confidence interval.
                 * 
                 * @param from
                 *     Lower bound
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder from(Decimal from) {
                    this.from = from;
                    return this;
                }

                /**
                 * Upper bound of confidence interval.
                 * 
                 * @param to
                 *     Upper bound
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder to(Decimal to) {
                    this.to = to;
                    return this;
                }

                /**
                 * Build the {@link PrecisionEstimate}
                 * 
                 * @return
                 *     An immutable object of type {@link PrecisionEstimate}
                 */
                @Override
                public PrecisionEstimate build() {
                    return new PrecisionEstimate(this);
                }

                protected Builder from(PrecisionEstimate precisionEstimate) {
                    super.from(precisionEstimate);
                    type = precisionEstimate.type;
                    level = precisionEstimate.level;
                    from = precisionEstimate.from;
                    to = precisionEstimate.to;
                    return this;
                }
            }
        }
    }

    /**
     * A description of the certainty of the risk estimate.
     */
    public static class Certainty extends BackboneElement {
        @Binding(
            bindingName = "QualityOfEvidenceRating",
            strength = BindingStrength.ValueSet.EXTENSIBLE,
            description = "The quality of the evidence described. The code system used specifies the quality scale used to grade this evidence source while the code specifies the actual quality score (represented as a coded value) associated with the evidence.",
            valueSet = "http://hl7.org/fhir/ValueSet/evidence-quality"
        )
        private final List<CodeableConcept> rating;
        private final List<Annotation> note;
        private final List<CertaintySubcomponent> certaintySubcomponent;

        private volatile int hashCode;

        private Certainty(Builder builder) {
            super(builder);
            rating = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.rating, "rating"));
            note = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.note, "note"));
            certaintySubcomponent = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.certaintySubcomponent, "certaintySubcomponent"));
            ValidationSupport.requireValueOrChildren(this);
        }

        /**
         * A rating of the certainty of the effect estimate.
         * 
         * @return
         *     An unmodifiable list containing immutable objects of type {@link CodeableConcept}.
         */
        public List<CodeableConcept> getRating() {
            return rating;
        }

        /**
         * A human-readable string to clarify or explain concepts about the resource.
         * 
         * @return
         *     An unmodifiable list containing immutable objects of type {@link Annotation}.
         */
        public List<Annotation> getNote() {
            return note;
        }

        /**
         * A description of a component of the overall certainty.
         * 
         * @return
         *     An unmodifiable list containing immutable objects of type {@link CertaintySubcomponent}.
         */
        public List<CertaintySubcomponent> getCertaintySubcomponent() {
            return certaintySubcomponent;
        }

        @Override
        public boolean hasChildren() {
            return super.hasChildren() || 
                !rating.isEmpty() || 
                !note.isEmpty() || 
                !certaintySubcomponent.isEmpty();
        }

        @Override
        public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
            if (visitor.preVisit(this)) {
                visitor.visitStart(elementName, elementIndex, this);
                if (visitor.visit(elementName, elementIndex, this)) {
                    // visit children
                    accept(id, "id", visitor);
                    accept(extension, "extension", visitor, Extension.class);
                    accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                    accept(rating, "rating", visitor, CodeableConcept.class);
                    accept(note, "note", visitor, Annotation.class);
                    accept(certaintySubcomponent, "certaintySubcomponent", visitor, CertaintySubcomponent.class);
                }
                visitor.visitEnd(elementName, elementIndex, this);
                visitor.postVisit(this);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Certainty other = (Certainty) obj;
            return Objects.equals(id, other.id) && 
                Objects.equals(extension, other.extension) && 
                Objects.equals(modifierExtension, other.modifierExtension) && 
                Objects.equals(rating, other.rating) && 
                Objects.equals(note, other.note) && 
                Objects.equals(certaintySubcomponent, other.certaintySubcomponent);
        }

        @Override
        public int hashCode() {
            int result = hashCode;
            if (result == 0) {
                result = Objects.hash(id, 
                    extension, 
                    modifierExtension, 
                    rating, 
                    note, 
                    certaintySubcomponent);
                hashCode = result;
            }
            return result;
        }

        @Override
        public Builder toBuilder() {
            return new Builder().from(this);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends BackboneElement.Builder {
            private List<CodeableConcept> rating = new ArrayList<>();
            private List<Annotation> note = new ArrayList<>();
            private List<CertaintySubcomponent> certaintySubcomponent = new ArrayList<>();

            private Builder() {
                super();
            }

            /**
             * Unique id for the element within a resource (for internal references). This may be any string value that does not 
             * contain spaces.
             * 
             * @param id
             *     Unique id for inter-element referencing
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder id(java.lang.String id) {
                return (Builder) super.id(id);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
             * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
             * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
             * of the definition of the extension.
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param extension
             *     Additional content defined by implementations
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder extension(Extension... extension) {
                return (Builder) super.extension(extension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element. To make the 
             * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
             * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
             * of the definition of the extension.
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param extension
             *     Additional content defined by implementations
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder extension(Collection<Extension> extension) {
                return (Builder) super.extension(extension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder modifierExtension(Extension... modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * May be used to represent additional information that is not part of the basic definition of the element and that 
             * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
             * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
             * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
             * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
             * extension. Applications processing a resource are required to check for modifier extensions.
             * 
             * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
             * change the meaning of modifierExtension itself).
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param modifierExtension
             *     Extensions that cannot be ignored even if unrecognized
             * 
             * @return
             *     A reference to this Builder instance
             */
            @Override
            public Builder modifierExtension(Collection<Extension> modifierExtension) {
                return (Builder) super.modifierExtension(modifierExtension);
            }

            /**
             * A rating of the certainty of the effect estimate.
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param rating
             *     Certainty rating
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder rating(CodeableConcept... rating) {
                for (CodeableConcept value : rating) {
                    this.rating.add(value);
                }
                return this;
            }

            /**
             * A rating of the certainty of the effect estimate.
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param rating
             *     Certainty rating
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder rating(Collection<CodeableConcept> rating) {
                this.rating = new ArrayList<>(rating);
                return this;
            }

            /**
             * A human-readable string to clarify or explain concepts about the resource.
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param note
             *     Used for footnotes or explanatory notes
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder note(Annotation... note) {
                for (Annotation value : note) {
                    this.note.add(value);
                }
                return this;
            }

            /**
             * A human-readable string to clarify or explain concepts about the resource.
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param note
             *     Used for footnotes or explanatory notes
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder note(Collection<Annotation> note) {
                this.note = new ArrayList<>(note);
                return this;
            }

            /**
             * A description of a component of the overall certainty.
             * 
             * <p>Adds new element(s) to the existing list
             * 
             * @param certaintySubcomponent
             *     A component that contributes to the overall certainty
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder certaintySubcomponent(CertaintySubcomponent... certaintySubcomponent) {
                for (CertaintySubcomponent value : certaintySubcomponent) {
                    this.certaintySubcomponent.add(value);
                }
                return this;
            }

            /**
             * A description of a component of the overall certainty.
             * 
             * <p>Replaces the existing list with a new one containing elements from the Collection
             * 
             * @param certaintySubcomponent
             *     A component that contributes to the overall certainty
             * 
             * @return
             *     A reference to this Builder instance
             */
            public Builder certaintySubcomponent(Collection<CertaintySubcomponent> certaintySubcomponent) {
                this.certaintySubcomponent = new ArrayList<>(certaintySubcomponent);
                return this;
            }

            /**
             * Build the {@link Certainty}
             * 
             * @return
             *     An immutable object of type {@link Certainty}
             */
            @Override
            public Certainty build() {
                return new Certainty(this);
            }

            protected Builder from(Certainty certainty) {
                super.from(certainty);
                rating.addAll(certainty.rating);
                note.addAll(certainty.note);
                certaintySubcomponent.addAll(certainty.certaintySubcomponent);
                return this;
            }
        }

        /**
         * A description of a component of the overall certainty.
         */
        public static class CertaintySubcomponent extends BackboneElement {
            @Binding(
                bindingName = "CertaintySubcomponentType",
                strength = BindingStrength.ValueSet.EXTENSIBLE,
                description = "The subcomponent classification of quality of evidence rating systems.",
                valueSet = "http://hl7.org/fhir/ValueSet/certainty-subcomponent-type"
            )
            private final CodeableConcept type;
            @Binding(
                bindingName = "CertaintySubcomponentRating",
                strength = BindingStrength.ValueSet.EXTENSIBLE,
                description = "The quality rating of the subcomponent of a quality of evidence rating.",
                valueSet = "http://hl7.org/fhir/ValueSet/certainty-subcomponent-rating"
            )
            private final List<CodeableConcept> rating;
            private final List<Annotation> note;

            private volatile int hashCode;

            private CertaintySubcomponent(Builder builder) {
                super(builder);
                type = builder.type;
                rating = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.rating, "rating"));
                note = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.note, "note"));
                ValidationSupport.requireValueOrChildren(this);
            }

            /**
             * Type of subcomponent of certainty rating.
             * 
             * @return
             *     An immutable object of type {@link CodeableConcept}.
             */
            public CodeableConcept getType() {
                return type;
            }

            /**
             * A rating of a subcomponent of rating certainty.
             * 
             * @return
             *     An unmodifiable list containing immutable objects of type {@link CodeableConcept}.
             */
            public List<CodeableConcept> getRating() {
                return rating;
            }

            /**
             * A human-readable string to clarify or explain concepts about the resource.
             * 
             * @return
             *     An unmodifiable list containing immutable objects of type {@link Annotation}.
             */
            public List<Annotation> getNote() {
                return note;
            }

            @Override
            public boolean hasChildren() {
                return super.hasChildren() || 
                    (type != null) || 
                    !rating.isEmpty() || 
                    !note.isEmpty();
            }

            @Override
            public void accept(java.lang.String elementName, int elementIndex, Visitor visitor) {
                if (visitor.preVisit(this)) {
                    visitor.visitStart(elementName, elementIndex, this);
                    if (visitor.visit(elementName, elementIndex, this)) {
                        // visit children
                        accept(id, "id", visitor);
                        accept(extension, "extension", visitor, Extension.class);
                        accept(modifierExtension, "modifierExtension", visitor, Extension.class);
                        accept(type, "type", visitor);
                        accept(rating, "rating", visitor, CodeableConcept.class);
                        accept(note, "note", visitor, Annotation.class);
                    }
                    visitor.visitEnd(elementName, elementIndex, this);
                    visitor.postVisit(this);
                }
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                CertaintySubcomponent other = (CertaintySubcomponent) obj;
                return Objects.equals(id, other.id) && 
                    Objects.equals(extension, other.extension) && 
                    Objects.equals(modifierExtension, other.modifierExtension) && 
                    Objects.equals(type, other.type) && 
                    Objects.equals(rating, other.rating) && 
                    Objects.equals(note, other.note);
            }

            @Override
            public int hashCode() {
                int result = hashCode;
                if (result == 0) {
                    result = Objects.hash(id, 
                        extension, 
                        modifierExtension, 
                        type, 
                        rating, 
                        note);
                    hashCode = result;
                }
                return result;
            }

            @Override
            public Builder toBuilder() {
                return new Builder().from(this);
            }

            public static Builder builder() {
                return new Builder();
            }

            public static class Builder extends BackboneElement.Builder {
                private CodeableConcept type;
                private List<CodeableConcept> rating = new ArrayList<>();
                private List<Annotation> note = new ArrayList<>();

                private Builder() {
                    super();
                }

                /**
                 * Unique id for the element within a resource (for internal references). This may be any string value that does not 
                 * contain spaces.
                 * 
                 * @param id
                 *     Unique id for inter-element referencing
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder id(java.lang.String id) {
                    return (Builder) super.id(id);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element. To make the 
                 * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
                 * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
                 * of the definition of the extension.
                 * 
                 * <p>Adds new element(s) to the existing list
                 * 
                 * @param extension
                 *     Additional content defined by implementations
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder extension(Extension... extension) {
                    return (Builder) super.extension(extension);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element. To make the 
                 * use of extensions safe and manageable, there is a strict set of governance applied to the definition and use of 
                 * extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part 
                 * of the definition of the extension.
                 * 
                 * <p>Replaces the existing list with a new one containing elements from the Collection
                 * 
                 * @param extension
                 *     Additional content defined by implementations
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder extension(Collection<Extension> extension) {
                    return (Builder) super.extension(extension);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element and that 
                 * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
                 * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
                 * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
                 * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
                 * extension. Applications processing a resource are required to check for modifier extensions.
                 * 
                 * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
                 * change the meaning of modifierExtension itself).
                 * 
                 * <p>Adds new element(s) to the existing list
                 * 
                 * @param modifierExtension
                 *     Extensions that cannot be ignored even if unrecognized
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder modifierExtension(Extension... modifierExtension) {
                    return (Builder) super.modifierExtension(modifierExtension);
                }

                /**
                 * May be used to represent additional information that is not part of the basic definition of the element and that 
                 * modifies the understanding of the element in which it is contained and/or the understanding of the containing 
                 * element's descendants. Usually modifier elements provide negation or qualification. To make the use of extensions safe 
                 * and manageable, there is a strict set of governance applied to the definition and use of extensions. Though any 
                 * implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the 
                 * extension. Applications processing a resource are required to check for modifier extensions.
                 * 
                 * <p>Modifier extensions SHALL NOT change the meaning of any elements on Resource or DomainResource (including cannot 
                 * change the meaning of modifierExtension itself).
                 * 
                 * <p>Replaces the existing list with a new one containing elements from the Collection
                 * 
                 * @param modifierExtension
                 *     Extensions that cannot be ignored even if unrecognized
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                @Override
                public Builder modifierExtension(Collection<Extension> modifierExtension) {
                    return (Builder) super.modifierExtension(modifierExtension);
                }

                /**
                 * Type of subcomponent of certainty rating.
                 * 
                 * @param type
                 *     Type of subcomponent of certainty rating
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder type(CodeableConcept type) {
                    this.type = type;
                    return this;
                }

                /**
                 * A rating of a subcomponent of rating certainty.
                 * 
                 * <p>Adds new element(s) to the existing list
                 * 
                 * @param rating
                 *     Subcomponent certainty rating
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder rating(CodeableConcept... rating) {
                    for (CodeableConcept value : rating) {
                        this.rating.add(value);
                    }
                    return this;
                }

                /**
                 * A rating of a subcomponent of rating certainty.
                 * 
                 * <p>Replaces the existing list with a new one containing elements from the Collection
                 * 
                 * @param rating
                 *     Subcomponent certainty rating
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder rating(Collection<CodeableConcept> rating) {
                    this.rating = new ArrayList<>(rating);
                    return this;
                }

                /**
                 * A human-readable string to clarify or explain concepts about the resource.
                 * 
                 * <p>Adds new element(s) to the existing list
                 * 
                 * @param note
                 *     Used for footnotes or explanatory notes
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder note(Annotation... note) {
                    for (Annotation value : note) {
                        this.note.add(value);
                    }
                    return this;
                }

                /**
                 * A human-readable string to clarify or explain concepts about the resource.
                 * 
                 * <p>Replaces the existing list with a new one containing elements from the Collection
                 * 
                 * @param note
                 *     Used for footnotes or explanatory notes
                 * 
                 * @return
                 *     A reference to this Builder instance
                 */
                public Builder note(Collection<Annotation> note) {
                    this.note = new ArrayList<>(note);
                    return this;
                }

                /**
                 * Build the {@link CertaintySubcomponent}
                 * 
                 * @return
                 *     An immutable object of type {@link CertaintySubcomponent}
                 */
                @Override
                public CertaintySubcomponent build() {
                    return new CertaintySubcomponent(this);
                }

                protected Builder from(CertaintySubcomponent certaintySubcomponent) {
                    super.from(certaintySubcomponent);
                    type = certaintySubcomponent.type;
                    rating.addAll(certaintySubcomponent.rating);
                    note.addAll(certaintySubcomponent.note);
                    return this;
                }
            }
        }
    }
}
