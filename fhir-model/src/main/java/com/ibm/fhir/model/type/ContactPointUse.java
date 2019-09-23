/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.type;

import java.util.Collection;
import java.util.Objects;

public class ContactPointUse extends Code {
    /**
     * Home
     */
    public static final ContactPointUse HOME = ContactPointUse.of(ValueSet.HOME);

    /**
     * Work
     */
    public static final ContactPointUse WORK = ContactPointUse.of(ValueSet.WORK);

    /**
     * Temp
     */
    public static final ContactPointUse TEMP = ContactPointUse.of(ValueSet.TEMP);

    /**
     * Old
     */
    public static final ContactPointUse OLD = ContactPointUse.of(ValueSet.OLD);

    /**
     * Mobile
     */
    public static final ContactPointUse MOBILE = ContactPointUse.of(ValueSet.MOBILE);

    private volatile int hashCode;

    private ContactPointUse(Builder builder) {
        super(builder);
    }

    public static ContactPointUse of(java.lang.String value) {
        return ContactPointUse.builder().value(value).build();
    }

    public static ContactPointUse of(ValueSet value) {
        return ContactPointUse.builder().value(value).build();
    }

    public static String string(java.lang.String value) {
        return ContactPointUse.builder().value(value).build();
    }

    public static Code code(java.lang.String value) {
        return ContactPointUse.builder().value(value).build();
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
        ContactPointUse other = (ContactPointUse) obj;
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
        public ContactPointUse build() {
            return new ContactPointUse(this);
        }
    }

    public enum ValueSet {
        /**
         * Home
         */
        HOME("home"),

        /**
         * Work
         */
        WORK("work"),

        /**
         * Temp
         */
        TEMP("temp"),

        /**
         * Old
         */
        OLD("old"),

        /**
         * Mobile
         */
        MOBILE("mobile");

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
