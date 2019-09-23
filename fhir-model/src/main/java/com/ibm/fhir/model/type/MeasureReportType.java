/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.type;

import java.util.Collection;
import java.util.Objects;

public class MeasureReportType extends Code {
    /**
     * Individual
     */
    public static final MeasureReportType INDIVIDUAL = MeasureReportType.of(ValueSet.INDIVIDUAL);

    /**
     * Subject List
     */
    public static final MeasureReportType SUBJECT_LIST = MeasureReportType.of(ValueSet.SUBJECT_LIST);

    /**
     * Summary
     */
    public static final MeasureReportType SUMMARY = MeasureReportType.of(ValueSet.SUMMARY);

    /**
     * Data Collection
     */
    public static final MeasureReportType DATA_COLLECTION = MeasureReportType.of(ValueSet.DATA_COLLECTION);

    private volatile int hashCode;

    private MeasureReportType(Builder builder) {
        super(builder);
    }

    public static MeasureReportType of(java.lang.String value) {
        return MeasureReportType.builder().value(value).build();
    }

    public static MeasureReportType of(ValueSet value) {
        return MeasureReportType.builder().value(value).build();
    }

    public static String string(java.lang.String value) {
        return MeasureReportType.builder().value(value).build();
    }

    public static Code code(java.lang.String value) {
        return MeasureReportType.builder().value(value).build();
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
        MeasureReportType other = (MeasureReportType) obj;
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
        builder.id = id;
        builder.extension.addAll(extension);
        builder.value = value;
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
            return (Builder) super.value(ValueSet.from(value).value());
        }

        public Builder value(ValueSet value) {
            return (Builder) super.value(value.value());
        }

        @Override
        public MeasureReportType build() {
            return new MeasureReportType(this);
        }
    }

    public enum ValueSet {
        /**
         * Individual
         */
        INDIVIDUAL("individual"),

        /**
         * Subject List
         */
        SUBJECT_LIST("subject-list"),

        /**
         * Summary
         */
        SUMMARY("summary"),

        /**
         * Data Collection
         */
        DATA_COLLECTION("data-collection");

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
