/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.type;

import java.util.Collection;
import java.util.Objects;

public class MedicationAdministrationStatus extends Code {
    /**
     * In Progress
     */
    public static final MedicationAdministrationStatus IN_PROGRESS = MedicationAdministrationStatus.of(ValueSet.IN_PROGRESS);

    /**
     * Not Done
     */
    public static final MedicationAdministrationStatus NOT_DONE = MedicationAdministrationStatus.of(ValueSet.NOT_DONE);

    /**
     * On Hold
     */
    public static final MedicationAdministrationStatus ON_HOLD = MedicationAdministrationStatus.of(ValueSet.ON_HOLD);

    /**
     * Completed
     */
    public static final MedicationAdministrationStatus COMPLETED = MedicationAdministrationStatus.of(ValueSet.COMPLETED);

    /**
     * Entered in Error
     */
    public static final MedicationAdministrationStatus ENTERED_IN_ERROR = MedicationAdministrationStatus.of(ValueSet.ENTERED_IN_ERROR);

    /**
     * Stopped
     */
    public static final MedicationAdministrationStatus STOPPED = MedicationAdministrationStatus.of(ValueSet.STOPPED);

    /**
     * Unknown
     */
    public static final MedicationAdministrationStatus UNKNOWN = MedicationAdministrationStatus.of(ValueSet.UNKNOWN);

    private volatile int hashCode;

    private MedicationAdministrationStatus(Builder builder) {
        super(builder);
    }

    public static MedicationAdministrationStatus of(java.lang.String value) {
        return MedicationAdministrationStatus.builder().value(value).build();
    }

    public static MedicationAdministrationStatus of(ValueSet value) {
        return MedicationAdministrationStatus.builder().value(value).build();
    }

    public static String string(java.lang.String value) {
        return MedicationAdministrationStatus.builder().value(value).build();
    }

    public static Code code(java.lang.String value) {
        return MedicationAdministrationStatus.builder().value(value).build();
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
        MedicationAdministrationStatus other = (MedicationAdministrationStatus) obj;
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
        public MedicationAdministrationStatus build() {
            return new MedicationAdministrationStatus(this);
        }
    }

    public enum ValueSet {
        /**
         * In Progress
         */
        IN_PROGRESS("in-progress"),

        /**
         * Not Done
         */
        NOT_DONE("not-done"),

        /**
         * On Hold
         */
        ON_HOLD("on-hold"),

        /**
         * Completed
         */
        COMPLETED("completed"),

        /**
         * Entered in Error
         */
        ENTERED_IN_ERROR("entered-in-error"),

        /**
         * Stopped
         */
        STOPPED("stopped"),

        /**
         * Unknown
         */
        UNKNOWN("unknown");

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
