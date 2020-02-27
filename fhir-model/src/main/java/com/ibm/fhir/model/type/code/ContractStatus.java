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

@System("http://hl7.org/fhir/contract-status")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class ContractStatus extends Code {
    /**
     * Amended
     */
    public static final ContractStatus AMENDED = ContractStatus.builder().value(ValueSet.AMENDED).build();

    /**
     * Appended
     */
    public static final ContractStatus APPENDED = ContractStatus.builder().value(ValueSet.APPENDED).build();

    /**
     * Cancelled
     */
    public static final ContractStatus CANCELLED = ContractStatus.builder().value(ValueSet.CANCELLED).build();

    /**
     * Disputed
     */
    public static final ContractStatus DISPUTED = ContractStatus.builder().value(ValueSet.DISPUTED).build();

    /**
     * Entered in Error
     */
    public static final ContractStatus ENTERED_IN_ERROR = ContractStatus.builder().value(ValueSet.ENTERED_IN_ERROR).build();

    /**
     * Executable
     */
    public static final ContractStatus EXECUTABLE = ContractStatus.builder().value(ValueSet.EXECUTABLE).build();

    /**
     * Executed
     */
    public static final ContractStatus EXECUTED = ContractStatus.builder().value(ValueSet.EXECUTED).build();

    /**
     * Negotiable
     */
    public static final ContractStatus NEGOTIABLE = ContractStatus.builder().value(ValueSet.NEGOTIABLE).build();

    /**
     * Offered
     */
    public static final ContractStatus OFFERED = ContractStatus.builder().value(ValueSet.OFFERED).build();

    /**
     * Policy
     */
    public static final ContractStatus POLICY = ContractStatus.builder().value(ValueSet.POLICY).build();

    /**
     * Rejected
     */
    public static final ContractStatus REJECTED = ContractStatus.builder().value(ValueSet.REJECTED).build();

    /**
     * Renewed
     */
    public static final ContractStatus RENEWED = ContractStatus.builder().value(ValueSet.RENEWED).build();

    /**
     * Revoked
     */
    public static final ContractStatus REVOKED = ContractStatus.builder().value(ValueSet.REVOKED).build();

    /**
     * Resolved
     */
    public static final ContractStatus RESOLVED = ContractStatus.builder().value(ValueSet.RESOLVED).build();

    /**
     * Terminated
     */
    public static final ContractStatus TERMINATED = ContractStatus.builder().value(ValueSet.TERMINATED).build();

    private volatile int hashCode;

    private ContractStatus(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static ContractStatus of(ValueSet value) {
        switch (value) {
        case AMENDED:
            return AMENDED;
        case APPENDED:
            return APPENDED;
        case CANCELLED:
            return CANCELLED;
        case DISPUTED:
            return DISPUTED;
        case ENTERED_IN_ERROR:
            return ENTERED_IN_ERROR;
        case EXECUTABLE:
            return EXECUTABLE;
        case EXECUTED:
            return EXECUTED;
        case NEGOTIABLE:
            return NEGOTIABLE;
        case OFFERED:
            return OFFERED;
        case POLICY:
            return POLICY;
        case REJECTED:
            return REJECTED;
        case RENEWED:
            return RENEWED;
        case REVOKED:
            return REVOKED;
        case RESOLVED:
            return RESOLVED;
        case TERMINATED:
            return TERMINATED;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static ContractStatus of(java.lang.String value) {
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
        ContractStatus other = (ContractStatus) obj;
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
        public ContractStatus build() {
            return new ContractStatus(this);
        }
    }

    public enum ValueSet {
        /**
         * Amended
         */
        AMENDED("amended"),

        /**
         * Appended
         */
        APPENDED("appended"),

        /**
         * Cancelled
         */
        CANCELLED("cancelled"),

        /**
         * Disputed
         */
        DISPUTED("disputed"),

        /**
         * Entered in Error
         */
        ENTERED_IN_ERROR("entered-in-error"),

        /**
         * Executable
         */
        EXECUTABLE("executable"),

        /**
         * Executed
         */
        EXECUTED("executed"),

        /**
         * Negotiable
         */
        NEGOTIABLE("negotiable"),

        /**
         * Offered
         */
        OFFERED("offered"),

        /**
         * Policy
         */
        POLICY("policy"),

        /**
         * Rejected
         */
        REJECTED("rejected"),

        /**
         * Renewed
         */
        RENEWED("renewed"),

        /**
         * Revoked
         */
        REVOKED("revoked"),

        /**
         * Resolved
         */
        RESOLVED("resolved"),

        /**
         * Terminated
         */
        TERMINATED("terminated");

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
