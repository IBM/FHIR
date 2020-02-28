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

@System("http://hl7.org/fhir/audit-event-action")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class AuditEventAction extends Code {
    /**
     * Create
     */
    public static final AuditEventAction C = AuditEventAction.builder().value(ValueSet.C).build();

    /**
     * Read/View/Print
     */
    public static final AuditEventAction R = AuditEventAction.builder().value(ValueSet.R).build();

    /**
     * Update
     */
    public static final AuditEventAction U = AuditEventAction.builder().value(ValueSet.U).build();

    /**
     * Delete
     */
    public static final AuditEventAction D = AuditEventAction.builder().value(ValueSet.D).build();

    /**
     * Execute
     */
    public static final AuditEventAction E = AuditEventAction.builder().value(ValueSet.E).build();

    private volatile int hashCode;

    private AuditEventAction(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static AuditEventAction of(ValueSet value) {
        switch (value) {
        case C:
            return C;
        case R:
            return R;
        case U:
            return U;
        case D:
            return D;
        case E:
            return E;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static AuditEventAction of(java.lang.String value) {
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
        AuditEventAction other = (AuditEventAction) obj;
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
        public AuditEventAction build() {
            return new AuditEventAction(this);
        }
    }

    public enum ValueSet {
        /**
         * Create
         */
        C("C"),

        /**
         * Read/View/Print
         */
        R("R"),

        /**
         * Update
         */
        U("U"),

        /**
         * Delete
         */
        D("D"),

        /**
         * Execute
         */
        E("E");

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
