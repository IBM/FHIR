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

@System("http://hl7.org/fhir/request-intent")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class NutritionOrderIntent extends Code {
    /**
     * Proposal
     */
    public static final NutritionOrderIntent PROPOSAL = NutritionOrderIntent.builder().value(ValueSet.PROPOSAL).build();

    /**
     * Plan
     */
    public static final NutritionOrderIntent PLAN = NutritionOrderIntent.builder().value(ValueSet.PLAN).build();

    /**
     * Directive
     */
    public static final NutritionOrderIntent DIRECTIVE = NutritionOrderIntent.builder().value(ValueSet.DIRECTIVE).build();

    /**
     * Order
     */
    public static final NutritionOrderIntent ORDER = NutritionOrderIntent.builder().value(ValueSet.ORDER).build();

    /**
     * Original Order
     */
    public static final NutritionOrderIntent ORIGINAL_ORDER = NutritionOrderIntent.builder().value(ValueSet.ORIGINAL_ORDER).build();

    /**
     * Reflex Order
     */
    public static final NutritionOrderIntent REFLEX_ORDER = NutritionOrderIntent.builder().value(ValueSet.REFLEX_ORDER).build();

    /**
     * Filler Order
     */
    public static final NutritionOrderIntent FILLER_ORDER = NutritionOrderIntent.builder().value(ValueSet.FILLER_ORDER).build();

    /**
     * Instance Order
     */
    public static final NutritionOrderIntent INSTANCE_ORDER = NutritionOrderIntent.builder().value(ValueSet.INSTANCE_ORDER).build();

    /**
     * Option
     */
    public static final NutritionOrderIntent OPTION = NutritionOrderIntent.builder().value(ValueSet.OPTION).build();

    private volatile int hashCode;

    private NutritionOrderIntent(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static NutritionOrderIntent of(ValueSet value) {
        switch (value) {
        case PROPOSAL:
            return PROPOSAL;
        case PLAN:
            return PLAN;
        case DIRECTIVE:
            return DIRECTIVE;
        case ORDER:
            return ORDER;
        case ORIGINAL_ORDER:
            return ORIGINAL_ORDER;
        case REFLEX_ORDER:
            return REFLEX_ORDER;
        case FILLER_ORDER:
            return FILLER_ORDER;
        case INSTANCE_ORDER:
            return INSTANCE_ORDER;
        case OPTION:
            return OPTION;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static NutritionOrderIntent of(java.lang.String value) {
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
        NutritionOrderIntent other = (NutritionOrderIntent) obj;
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
        public NutritionOrderIntent build() {
            return new NutritionOrderIntent(this);
        }
    }

    public enum ValueSet {
        /**
         * Proposal
         */
        PROPOSAL("proposal"),

        /**
         * Plan
         */
        PLAN("plan"),

        /**
         * Directive
         */
        DIRECTIVE("directive"),

        /**
         * Order
         */
        ORDER("order"),

        /**
         * Original Order
         */
        ORIGINAL_ORDER("original-order"),

        /**
         * Reflex Order
         */
        REFLEX_ORDER("reflex-order"),

        /**
         * Filler Order
         */
        FILLER_ORDER("filler-order"),

        /**
         * Instance Order
         */
        INSTANCE_ORDER("instance-order"),

        /**
         * Option
         */
        OPTION("option");

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
