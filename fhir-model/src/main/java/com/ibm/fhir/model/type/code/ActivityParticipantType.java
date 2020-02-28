/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.type.code;

import com.ibm.fhir.model.annotation.System;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.String;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Generated;

@System("http://hl7.org/fhir/action-participant-type")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class ActivityParticipantType extends Code {
    /**
     * Patient
     */
    public static final ActivityParticipantType PATIENT = ActivityParticipantType.builder().value(ValueSet.PATIENT).build();

    /**
     * Practitioner
     */
    public static final ActivityParticipantType PRACTITIONER = ActivityParticipantType.builder().value(ValueSet.PRACTITIONER).build();

    /**
     * Related Person
     */
    public static final ActivityParticipantType RELATED_PERSON = ActivityParticipantType.builder().value(ValueSet.RELATED_PERSON).build();

    /**
     * Device
     */
    public static final ActivityParticipantType DEVICE = ActivityParticipantType.builder().value(ValueSet.DEVICE).build();

    private volatile int hashCode;

    private ActivityParticipantType(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static ActivityParticipantType of(ValueSet value) {
        switch (value) {
        case PATIENT:
            return PATIENT;
        case PRACTITIONER:
            return PRACTITIONER;
        case RELATED_PERSON:
            return RELATED_PERSON;
        case DEVICE:
            return DEVICE;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static ActivityParticipantType of(java.lang.String value) {
        return of(ValueSet.from(value));
    }

    public static String string(java.lang.String value) {
        return of(ValueSet.from(value));
    }

    public static Code code(java.lang.String value) {
        return of(ValueSet.from(value));
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
        ActivityParticipantType other = (ActivityParticipantType) obj;
        return Objects.equals(id, other.id) && Objects.equals(extension, other.extension) && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = Objects.hash(id, extension, value);
            hashCode = result;
        }
        return result;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.id(id);
        builder.extension(extension);
        builder.value(value);
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Code.Builder {
        private Builder() {
            super();
        }

        @Override
        public Builder id(java.lang.String id) {
            return (Builder) super.id(id);
        }

        @Override
        public Builder extension(Extension... extension) {
            return (Builder) super.extension(extension);
        }

        @Override
        public Builder extension(Collection<Extension> extension) {
            return (Builder) super.extension(extension);
        }

        @Override
        public Builder value(java.lang.String value) {
            return (value != null) ? (Builder) super.value(ValueSet.from(value).value()) : this;
        }

        public Builder value(ValueSet value) {
            return (value != null) ? (Builder) super.value(value.value()) : this;
        }

        @Override
        public ActivityParticipantType build() {
            return new ActivityParticipantType(this);
        }
    }

    public enum ValueSet {
        /**
         * Patient
         */
        PATIENT("patient"),

        /**
         * Practitioner
         */
        PRACTITIONER("practitioner"),

        /**
         * Related Person
         */
        RELATED_PERSON("related-person"),

        /**
         * Device
         */
        DEVICE("device");

        private final java.lang.String value;

        ValueSet(java.lang.String value) {
            this.value = value;
        }

        public java.lang.String value() {
            return value;
        }

        public static ValueSet from(java.lang.String value) {
            for (ValueSet c : ValueSet.values()) {
                if (c.value.equals(value)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(value);
        }
    }
}
