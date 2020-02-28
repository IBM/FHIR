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

@System("http://hl7.org/fhir/audit-event-outcome")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class AuditEventOutcome extends Code {
    /**
     * Success
     */
    public static final AuditEventOutcome OUTCOME_0 = AuditEventOutcome.builder().value(ValueSet.OUTCOME_0).build();

    /**
     * Minor failure
     */
    public static final AuditEventOutcome OUTCOME_4 = AuditEventOutcome.builder().value(ValueSet.OUTCOME_4).build();

    /**
     * Serious failure
     */
    public static final AuditEventOutcome OUTCOME_8 = AuditEventOutcome.builder().value(ValueSet.OUTCOME_8).build();

    /**
     * Major failure
     */
    public static final AuditEventOutcome OUTCOME_12 = AuditEventOutcome.builder().value(ValueSet.OUTCOME_12).build();

    private volatile int hashCode;

    private AuditEventOutcome(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static AuditEventOutcome of(ValueSet value) {
        switch (value) {
        case OUTCOME_0:
            return OUTCOME_0;
        case OUTCOME_4:
            return OUTCOME_4;
        case OUTCOME_8:
            return OUTCOME_8;
        case OUTCOME_12:
            return OUTCOME_12;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static AuditEventOutcome of(java.lang.String value) {
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
        AuditEventOutcome other = (AuditEventOutcome) obj;
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
        public AuditEventOutcome build() {
            return new AuditEventOutcome(this);
        }
    }

    public enum ValueSet {
        /**
         * Success
         */
        OUTCOME_0("0"),

        /**
         * Minor failure
         */
        OUTCOME_4("4"),

        /**
         * Serious failure
         */
        OUTCOME_8("8"),

        /**
         * Major failure
         */
        OUTCOME_12("12");

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
