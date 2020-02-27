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

@System("http://hl7.org/fhir/FHIR-version")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class FHIRVersion extends Code {
    /**
     * 0.01
     */
    public static final FHIRVersion VERSION_0_01 = FHIRVersion.builder().value(ValueSet.VERSION_0_01).build();

    /**
     * 0.05
     */
    public static final FHIRVersion VERSION_0_05 = FHIRVersion.builder().value(ValueSet.VERSION_0_05).build();

    /**
     * 0.06
     */
    public static final FHIRVersion VERSION_0_06 = FHIRVersion.builder().value(ValueSet.VERSION_0_06).build();

    /**
     * 0.11
     */
    public static final FHIRVersion VERSION_0_11 = FHIRVersion.builder().value(ValueSet.VERSION_0_11).build();

    /**
     * 0.0.80
     */
    public static final FHIRVersion VERSION_0_0_80 = FHIRVersion.builder().value(ValueSet.VERSION_0_0_80).build();

    /**
     * 0.0.81
     */
    public static final FHIRVersion VERSION_0_0_81 = FHIRVersion.builder().value(ValueSet.VERSION_0_0_81).build();

    /**
     * 0.0.82
     */
    public static final FHIRVersion VERSION_0_0_82 = FHIRVersion.builder().value(ValueSet.VERSION_0_0_82).build();

    /**
     * 0.4.0
     */
    public static final FHIRVersion VERSION_0_4_0 = FHIRVersion.builder().value(ValueSet.VERSION_0_4_0).build();

    /**
     * 0.5.0
     */
    public static final FHIRVersion VERSION_0_5_0 = FHIRVersion.builder().value(ValueSet.VERSION_0_5_0).build();

    /**
     * 1.0.0
     */
    public static final FHIRVersion VERSION_1_0_0 = FHIRVersion.builder().value(ValueSet.VERSION_1_0_0).build();

    /**
     * 1.0.1
     */
    public static final FHIRVersion VERSION_1_0_1 = FHIRVersion.builder().value(ValueSet.VERSION_1_0_1).build();

    /**
     * 1.0.2
     */
    public static final FHIRVersion VERSION_1_0_2 = FHIRVersion.builder().value(ValueSet.VERSION_1_0_2).build();

    /**
     * 1.1.0
     */
    public static final FHIRVersion VERSION_1_1_0 = FHIRVersion.builder().value(ValueSet.VERSION_1_1_0).build();

    /**
     * 1.4.0
     */
    public static final FHIRVersion VERSION_1_4_0 = FHIRVersion.builder().value(ValueSet.VERSION_1_4_0).build();

    /**
     * 1.6.0
     */
    public static final FHIRVersion VERSION_1_6_0 = FHIRVersion.builder().value(ValueSet.VERSION_1_6_0).build();

    /**
     * 1.8.0
     */
    public static final FHIRVersion VERSION_1_8_0 = FHIRVersion.builder().value(ValueSet.VERSION_1_8_0).build();

    /**
     * 3.0.0
     */
    public static final FHIRVersion VERSION_3_0_0 = FHIRVersion.builder().value(ValueSet.VERSION_3_0_0).build();

    /**
     * 3.0.1
     */
    public static final FHIRVersion VERSION_3_0_1 = FHIRVersion.builder().value(ValueSet.VERSION_3_0_1).build();

    /**
     * 3.3.0
     */
    public static final FHIRVersion VERSION_3_3_0 = FHIRVersion.builder().value(ValueSet.VERSION_3_3_0).build();

    /**
     * 3.5.0
     */
    public static final FHIRVersion VERSION_3_5_0 = FHIRVersion.builder().value(ValueSet.VERSION_3_5_0).build();

    /**
     * 4.0.0
     */
    public static final FHIRVersion VERSION_4_0_0 = FHIRVersion.builder().value(ValueSet.VERSION_4_0_0).build();

    /**
     * 4.0.1
     */
    public static final FHIRVersion VERSION_4_0_1 = FHIRVersion.builder().value(ValueSet.VERSION_4_0_1).build();

    private volatile int hashCode;

    private FHIRVersion(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static FHIRVersion of(ValueSet value) {
        switch (value) {
        case VERSION_0_01:
            return VERSION_0_01;
        case VERSION_0_05:
            return VERSION_0_05;
        case VERSION_0_06:
            return VERSION_0_06;
        case VERSION_0_11:
            return VERSION_0_11;
        case VERSION_0_0_80:
            return VERSION_0_0_80;
        case VERSION_0_0_81:
            return VERSION_0_0_81;
        case VERSION_0_0_82:
            return VERSION_0_0_82;
        case VERSION_0_4_0:
            return VERSION_0_4_0;
        case VERSION_0_5_0:
            return VERSION_0_5_0;
        case VERSION_1_0_0:
            return VERSION_1_0_0;
        case VERSION_1_0_1:
            return VERSION_1_0_1;
        case VERSION_1_0_2:
            return VERSION_1_0_2;
        case VERSION_1_1_0:
            return VERSION_1_1_0;
        case VERSION_1_4_0:
            return VERSION_1_4_0;
        case VERSION_1_6_0:
            return VERSION_1_6_0;
        case VERSION_1_8_0:
            return VERSION_1_8_0;
        case VERSION_3_0_0:
            return VERSION_3_0_0;
        case VERSION_3_0_1:
            return VERSION_3_0_1;
        case VERSION_3_3_0:
            return VERSION_3_3_0;
        case VERSION_3_5_0:
            return VERSION_3_5_0;
        case VERSION_4_0_0:
            return VERSION_4_0_0;
        case VERSION_4_0_1:
            return VERSION_4_0_1;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static FHIRVersion of(java.lang.String value) {
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
        FHIRVersion other = (FHIRVersion) obj;
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
        public FHIRVersion build() {
            return new FHIRVersion(this);
        }
    }

    public enum ValueSet {
        /**
         * 0.01
         */
        VERSION_0_01("0.01"),

        /**
         * 0.05
         */
        VERSION_0_05("0.05"),

        /**
         * 0.06
         */
        VERSION_0_06("0.06"),

        /**
         * 0.11
         */
        VERSION_0_11("0.11"),

        /**
         * 0.0.80
         */
        VERSION_0_0_80("0.0.80"),

        /**
         * 0.0.81
         */
        VERSION_0_0_81("0.0.81"),

        /**
         * 0.0.82
         */
        VERSION_0_0_82("0.0.82"),

        /**
         * 0.4.0
         */
        VERSION_0_4_0("0.4.0"),

        /**
         * 0.5.0
         */
        VERSION_0_5_0("0.5.0"),

        /**
         * 1.0.0
         */
        VERSION_1_0_0("1.0.0"),

        /**
         * 1.0.1
         */
        VERSION_1_0_1("1.0.1"),

        /**
         * 1.0.2
         */
        VERSION_1_0_2("1.0.2"),

        /**
         * 1.1.0
         */
        VERSION_1_1_0("1.1.0"),

        /**
         * 1.4.0
         */
        VERSION_1_4_0("1.4.0"),

        /**
         * 1.6.0
         */
        VERSION_1_6_0("1.6.0"),

        /**
         * 1.8.0
         */
        VERSION_1_8_0("1.8.0"),

        /**
         * 3.0.0
         */
        VERSION_3_0_0("3.0.0"),

        /**
         * 3.0.1
         */
        VERSION_3_0_1("3.0.1"),

        /**
         * 3.3.0
         */
        VERSION_3_3_0("3.3.0"),

        /**
         * 3.5.0
         */
        VERSION_3_5_0("3.5.0"),

        /**
         * 4.0.0
         */
        VERSION_4_0_0("4.0.0"),

        /**
         * 4.0.1
         */
        VERSION_4_0_1("4.0.1");

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
