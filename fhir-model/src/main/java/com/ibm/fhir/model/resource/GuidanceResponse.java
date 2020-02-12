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
import com.ibm.fhir.model.annotation.Choice;
import com.ibm.fhir.model.annotation.Required;
import com.ibm.fhir.model.annotation.Summary;
import com.ibm.fhir.model.type.Annotation;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.DataRequirement;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.Identifier;
import com.ibm.fhir.model.type.Meta;
import com.ibm.fhir.model.type.Narrative;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.BindingStrength;
import com.ibm.fhir.model.type.code.GuidanceResponseStatus;
import com.ibm.fhir.model.util.ValidationSupport;
import com.ibm.fhir.model.visitor.Visitor;

/**
 * A guidance response is the formal response to a guidance request, including any output parameters returned by the 
 * evaluation, as well as the description of any proposed actions to be taken.
 */
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class GuidanceResponse extends DomainResource {
    @Summary
    private final Identifier requestIdentifier;
    @Summary
    private final List<Identifier> identifier;
    @Summary
    @Choice({ Uri.class, Canonical.class, CodeableConcept.class })
    @Required
    private final Element module;
    @Summary
    @Binding(
        bindingName = "GuidanceResponseStatus",
        strength = BindingStrength.ValueSet.REQUIRED,
        description = "The status of a guidance response.",
        valueSet = "http://hl7.org/fhir/ValueSet/guidance-response-status|4.0.1"
    )
    @Required
    private final GuidanceResponseStatus status;
    private final Reference subject;
    private final Reference encounter;
    private final DateTime occurrenceDateTime;
    private final Reference performer;
    private final List<CodeableConcept> reasonCode;
    private final List<Reference> reasonReference;
    private final List<Annotation> note;
    private final List<Reference> evaluationMessage;
    private final Reference outputParameters;
    private final Reference result;
    private final List<DataRequirement> dataRequirement;

    private volatile int hashCode;

    private GuidanceResponse(Builder builder) {
        super(builder);
        requestIdentifier = builder.requestIdentifier;
        identifier = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.identifier, "identifier"));
        module = ValidationSupport.requireChoiceElement(builder.module, "module", Uri.class, Canonical.class, CodeableConcept.class);
        status = ValidationSupport.requireNonNull(builder.status, "status");
        subject = builder.subject;
        encounter = builder.encounter;
        occurrenceDateTime = builder.occurrenceDateTime;
        performer = builder.performer;
        reasonCode = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.reasonCode, "reasonCode"));
        reasonReference = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.reasonReference, "reasonReference"));
        note = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.note, "note"));
        evaluationMessage = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.evaluationMessage, "evaluationMessage"));
        outputParameters = builder.outputParameters;
        result = builder.result;
        dataRequirement = Collections.unmodifiableList(ValidationSupport.requireNonNull(builder.dataRequirement, "dataRequirement"));
        ValidationSupport.checkReferenceType(subject, "subject", "Patient", "Group");
        ValidationSupport.checkReferenceType(encounter, "encounter", "Encounter");
        ValidationSupport.checkReferenceType(performer, "performer", "Device");
        ValidationSupport.checkReferenceType(outputParameters, "outputParameters", "Parameters");
        ValidationSupport.checkReferenceType(result, "result", "CarePlan", "RequestGroup");
        ValidationSupport.requireChildren(this);
    }

    /**
     * The identifier of the request associated with this response. If an identifier was given as part of the request, it 
     * will be reproduced here to enable the requester to more easily identify the response in a multi-request scenario.
     * 
     * @return
     *     An immutable object of type {@link Identifier}.
     */
    public Identifier getRequestIdentifier() {
        return requestIdentifier;
    }

    /**
     * Allows a service to provide unique, business identifiers for the response.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Identifier}.
     */
    public List<Identifier> getIdentifier() {
        return identifier;
    }

    /**
     * An identifier, CodeableConcept or canonical reference to the guidance that was requested.
     * 
     * @return
     *     An immutable object of type {@link Element}.
     */
    public Element getModule() {
        return module;
    }

    /**
     * The status of the response. If the evaluation is completed successfully, the status will indicate success. However, in 
     * order to complete the evaluation, the engine may require more information. In this case, the status will be data-
     * required, and the response will contain a description of the additional required information. If the evaluation 
     * completed successfully, but the engine determines that a potentially more accurate response could be provided if more 
     * data was available, the status will be data-requested, and the response will contain a description of the additional 
     * requested information.
     * 
     * @return
     *     An immutable object of type {@link GuidanceResponseStatus}.
     */
    public GuidanceResponseStatus getStatus() {
        return status;
    }

    /**
     * The patient for which the request was processed.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getSubject() {
        return subject;
    }

    /**
     * The encounter during which this response was created or to which the creation of this record is tightly associated.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getEncounter() {
        return encounter;
    }

    /**
     * Indicates when the guidance response was processed.
     * 
     * @return
     *     An immutable object of type {@link DateTime}.
     */
    public DateTime getOccurrenceDateTime() {
        return occurrenceDateTime;
    }

    /**
     * Provides a reference to the device that performed the guidance.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getPerformer() {
        return performer;
    }

    /**
     * Describes the reason for the guidance response in coded or textual form.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link CodeableConcept}.
     */
    public List<CodeableConcept> getReasonCode() {
        return reasonCode;
    }

    /**
     * Indicates the reason the request was initiated. This is typically provided as a parameter to the evaluation and echoed 
     * by the service, although for some use cases, such as subscription- or event-based scenarios, it may provide an 
     * indication of the cause for the response.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Reference}.
     */
    public List<Reference> getReasonReference() {
        return reasonReference;
    }

    /**
     * Provides a mechanism to communicate additional information about the response.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Annotation}.
     */
    public List<Annotation> getNote() {
        return note;
    }

    /**
     * Messages resulting from the evaluation of the artifact or artifacts. As part of evaluating the request, the engine may 
     * produce informational or warning messages. These messages will be provided by this element.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link Reference}.
     */
    public List<Reference> getEvaluationMessage() {
        return evaluationMessage;
    }

    /**
     * The output parameters of the evaluation, if any. Many modules will result in the return of specific resources such as 
     * procedure or communication requests that are returned as part of the operation result. However, modules may define 
     * specific outputs that would be returned as the result of the evaluation, and these would be returned in this element.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getOutputParameters() {
        return outputParameters;
    }

    /**
     * The actions, if any, produced by the evaluation of the artifact.
     * 
     * @return
     *     An immutable object of type {@link Reference}.
     */
    public Reference getResult() {
        return result;
    }

    /**
     * If the evaluation could not be completed due to lack of information, or additional information would potentially 
     * result in a more accurate response, this element will a description of the data required in order to proceed with the 
     * evaluation. A subsequent request to the service should include this data.
     * 
     * @return
     *     An unmodifiable list containing immutable objects of type {@link DataRequirement}.
     */
    public List<DataRequirement> getDataRequirement() {
        return dataRequirement;
    }

    @Override
    public boolean hasChildren() {
        return super.hasChildren() || 
            (requestIdentifier != null) || 
            !identifier.isEmpty() || 
            (module != null) || 
            (status != null) || 
            (subject != null) || 
            (encounter != null) || 
            (occurrenceDateTime != null) || 
            (performer != null) || 
            !reasonCode.isEmpty() || 
            !reasonReference.isEmpty() || 
            !note.isEmpty() || 
            !evaluationMessage.isEmpty() || 
            (outputParameters != null) || 
            (result != null) || 
            !dataRequirement.isEmpty();
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
                accept(requestIdentifier, "requestIdentifier", visitor);
                accept(identifier, "identifier", visitor, Identifier.class);
                accept(module, "module", visitor);
                accept(status, "status", visitor);
                accept(subject, "subject", visitor);
                accept(encounter, "encounter", visitor);
                accept(occurrenceDateTime, "occurrenceDateTime", visitor);
                accept(performer, "performer", visitor);
                accept(reasonCode, "reasonCode", visitor, CodeableConcept.class);
                accept(reasonReference, "reasonReference", visitor, Reference.class);
                accept(note, "note", visitor, Annotation.class);
                accept(evaluationMessage, "evaluationMessage", visitor, Reference.class);
                accept(outputParameters, "outputParameters", visitor);
                accept(result, "result", visitor);
                accept(dataRequirement, "dataRequirement", visitor, DataRequirement.class);
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
        GuidanceResponse other = (GuidanceResponse) obj;
        return Objects.equals(id, other.id) && 
            Objects.equals(meta, other.meta) && 
            Objects.equals(implicitRules, other.implicitRules) && 
            Objects.equals(language, other.language) && 
            Objects.equals(text, other.text) && 
            Objects.equals(contained, other.contained) && 
            Objects.equals(extension, other.extension) && 
            Objects.equals(modifierExtension, other.modifierExtension) && 
            Objects.equals(requestIdentifier, other.requestIdentifier) && 
            Objects.equals(identifier, other.identifier) && 
            Objects.equals(module, other.module) && 
            Objects.equals(status, other.status) && 
            Objects.equals(subject, other.subject) && 
            Objects.equals(encounter, other.encounter) && 
            Objects.equals(occurrenceDateTime, other.occurrenceDateTime) && 
            Objects.equals(performer, other.performer) && 
            Objects.equals(reasonCode, other.reasonCode) && 
            Objects.equals(reasonReference, other.reasonReference) && 
            Objects.equals(note, other.note) && 
            Objects.equals(evaluationMessage, other.evaluationMessage) && 
            Objects.equals(outputParameters, other.outputParameters) && 
            Objects.equals(result, other.result) && 
            Objects.equals(dataRequirement, other.dataRequirement);
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
                requestIdentifier, 
                identifier, 
                module, 
                status, 
                subject, 
                encounter, 
                occurrenceDateTime, 
                performer, 
                reasonCode, 
                reasonReference, 
                note, 
                evaluationMessage, 
                outputParameters, 
                result, 
                dataRequirement);
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
        private Identifier requestIdentifier;
        private List<Identifier> identifier = new ArrayList<>();
        private Element module;
        private GuidanceResponseStatus status;
        private Reference subject;
        private Reference encounter;
        private DateTime occurrenceDateTime;
        private Reference performer;
        private List<CodeableConcept> reasonCode = new ArrayList<>();
        private List<Reference> reasonReference = new ArrayList<>();
        private List<Annotation> note = new ArrayList<>();
        private List<Reference> evaluationMessage = new ArrayList<>();
        private Reference outputParameters;
        private Reference result;
        private List<DataRequirement> dataRequirement = new ArrayList<>();

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
         * The identifier of the request associated with this response. If an identifier was given as part of the request, it 
         * will be reproduced here to enable the requester to more easily identify the response in a multi-request scenario.
         * 
         * @param requestIdentifier
         *     The identifier of the request associated with this response, if any
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder requestIdentifier(Identifier requestIdentifier) {
            this.requestIdentifier = requestIdentifier;
            return this;
        }

        /**
         * Allows a service to provide unique, business identifiers for the response.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param identifier
         *     Business identifier
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
         * Allows a service to provide unique, business identifiers for the response.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param identifier
         *     Business identifier
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder identifier(Collection<Identifier> identifier) {
            this.identifier = new ArrayList<>(identifier);
            return this;
        }

        /**
         * An identifier, CodeableConcept or canonical reference to the guidance that was requested.
         * 
         * <p>This element is required.
         * 
         * <p>This is a choice element with the following allowed types:
         * <ul>
         * <li>{@link Uri}</li>
         * <li>{@link Canonical}</li>
         * <li>{@link CodeableConcept}</li>
         * </ul>
         * 
         * @param module
         *     What guidance was requested
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder module(Element module) {
            this.module = module;
            return this;
        }

        /**
         * The status of the response. If the evaluation is completed successfully, the status will indicate success. However, in 
         * order to complete the evaluation, the engine may require more information. In this case, the status will be data-
         * required, and the response will contain a description of the additional required information. If the evaluation 
         * completed successfully, but the engine determines that a potentially more accurate response could be provided if more 
         * data was available, the status will be data-requested, and the response will contain a description of the additional 
         * requested information.
         * 
         * <p>This element is required.
         * 
         * @param status
         *     success | data-requested | data-required | in-progress | failure | entered-in-error
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder status(GuidanceResponseStatus status) {
            this.status = status;
            return this;
        }

        /**
         * The patient for which the request was processed.
         * 
         * @param subject
         *     Patient the request was performed for
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder subject(Reference subject) {
            this.subject = subject;
            return this;
        }

        /**
         * The encounter during which this response was created or to which the creation of this record is tightly associated.
         * 
         * @param encounter
         *     Encounter during which the response was returned
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder encounter(Reference encounter) {
            this.encounter = encounter;
            return this;
        }

        /**
         * Indicates when the guidance response was processed.
         * 
         * @param occurrenceDateTime
         *     When the guidance response was processed
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder occurrenceDateTime(DateTime occurrenceDateTime) {
            this.occurrenceDateTime = occurrenceDateTime;
            return this;
        }

        /**
         * Provides a reference to the device that performed the guidance.
         * 
         * @param performer
         *     Device returning the guidance
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder performer(Reference performer) {
            this.performer = performer;
            return this;
        }

        /**
         * Describes the reason for the guidance response in coded or textual form.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param reasonCode
         *     Why guidance is needed
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reasonCode(CodeableConcept... reasonCode) {
            for (CodeableConcept value : reasonCode) {
                this.reasonCode.add(value);
            }
            return this;
        }

        /**
         * Describes the reason for the guidance response in coded or textual form.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param reasonCode
         *     Why guidance is needed
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reasonCode(Collection<CodeableConcept> reasonCode) {
            this.reasonCode = new ArrayList<>(reasonCode);
            return this;
        }

        /**
         * Indicates the reason the request was initiated. This is typically provided as a parameter to the evaluation and echoed 
         * by the service, although for some use cases, such as subscription- or event-based scenarios, it may provide an 
         * indication of the cause for the response.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param reasonReference
         *     Why guidance is needed
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reasonReference(Reference... reasonReference) {
            for (Reference value : reasonReference) {
                this.reasonReference.add(value);
            }
            return this;
        }

        /**
         * Indicates the reason the request was initiated. This is typically provided as a parameter to the evaluation and echoed 
         * by the service, although for some use cases, such as subscription- or event-based scenarios, it may provide an 
         * indication of the cause for the response.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param reasonReference
         *     Why guidance is needed
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder reasonReference(Collection<Reference> reasonReference) {
            this.reasonReference = new ArrayList<>(reasonReference);
            return this;
        }

        /**
         * Provides a mechanism to communicate additional information about the response.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param note
         *     Additional notes about the response
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
         * Provides a mechanism to communicate additional information about the response.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param note
         *     Additional notes about the response
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder note(Collection<Annotation> note) {
            this.note = new ArrayList<>(note);
            return this;
        }

        /**
         * Messages resulting from the evaluation of the artifact or artifacts. As part of evaluating the request, the engine may 
         * produce informational or warning messages. These messages will be provided by this element.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param evaluationMessage
         *     Messages resulting from the evaluation of the artifact or artifacts
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder evaluationMessage(Reference... evaluationMessage) {
            for (Reference value : evaluationMessage) {
                this.evaluationMessage.add(value);
            }
            return this;
        }

        /**
         * Messages resulting from the evaluation of the artifact or artifacts. As part of evaluating the request, the engine may 
         * produce informational or warning messages. These messages will be provided by this element.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param evaluationMessage
         *     Messages resulting from the evaluation of the artifact or artifacts
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder evaluationMessage(Collection<Reference> evaluationMessage) {
            this.evaluationMessage = new ArrayList<>(evaluationMessage);
            return this;
        }

        /**
         * The output parameters of the evaluation, if any. Many modules will result in the return of specific resources such as 
         * procedure or communication requests that are returned as part of the operation result. However, modules may define 
         * specific outputs that would be returned as the result of the evaluation, and these would be returned in this element.
         * 
         * @param outputParameters
         *     The output parameters of the evaluation, if any
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder outputParameters(Reference outputParameters) {
            this.outputParameters = outputParameters;
            return this;
        }

        /**
         * The actions, if any, produced by the evaluation of the artifact.
         * 
         * @param result
         *     Proposed actions, if any
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder result(Reference result) {
            this.result = result;
            return this;
        }

        /**
         * If the evaluation could not be completed due to lack of information, or additional information would potentially 
         * result in a more accurate response, this element will a description of the data required in order to proceed with the 
         * evaluation. A subsequent request to the service should include this data.
         * 
         * <p>Adds new element(s) to the existing list
         * 
         * @param dataRequirement
         *     Additional required data
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder dataRequirement(DataRequirement... dataRequirement) {
            for (DataRequirement value : dataRequirement) {
                this.dataRequirement.add(value);
            }
            return this;
        }

        /**
         * If the evaluation could not be completed due to lack of information, or additional information would potentially 
         * result in a more accurate response, this element will a description of the data required in order to proceed with the 
         * evaluation. A subsequent request to the service should include this data.
         * 
         * <p>Replaces the existing list with a new one containing elements from the Collection
         * 
         * @param dataRequirement
         *     Additional required data
         * 
         * @return
         *     A reference to this Builder instance
         */
        public Builder dataRequirement(Collection<DataRequirement> dataRequirement) {
            this.dataRequirement = new ArrayList<>(dataRequirement);
            return this;
        }

        /**
         * Build the {@link GuidanceResponse}
         * 
         * <p>Required elements:
         * <ul>
         * <li>module</li>
         * <li>status</li>
         * </ul>
         * 
         * @return
         *     An immutable object of type {@link GuidanceResponse}
         */
        @Override
        public GuidanceResponse build() {
            return new GuidanceResponse(this);
        }

        protected Builder from(GuidanceResponse guidanceResponse) {
            super.from(guidanceResponse);
            requestIdentifier = guidanceResponse.requestIdentifier;
            identifier.addAll(guidanceResponse.identifier);
            module = guidanceResponse.module;
            status = guidanceResponse.status;
            subject = guidanceResponse.subject;
            encounter = guidanceResponse.encounter;
            occurrenceDateTime = guidanceResponse.occurrenceDateTime;
            performer = guidanceResponse.performer;
            reasonCode.addAll(guidanceResponse.reasonCode);
            reasonReference.addAll(guidanceResponse.reasonReference);
            note.addAll(guidanceResponse.note);
            evaluationMessage.addAll(guidanceResponse.evaluationMessage);
            outputParameters = guidanceResponse.outputParameters;
            result = guidanceResponse.result;
            dataRequirement.addAll(guidanceResponse.dataRequirement);
            return this;
        }
    }
}
