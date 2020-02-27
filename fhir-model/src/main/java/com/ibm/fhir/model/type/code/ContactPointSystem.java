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

@System("http://hl7.org/fhir/contact-point-system")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class ContactPointSystem extends Code {
    /**
     * Phone
     */
    public static final ContactPointSystem PHONE = ContactPointSystem.builder().value(ValueSet.PHONE).build();

    /**
     * Fax
     */
    public static final ContactPointSystem FAX = ContactPointSystem.builder().value(ValueSet.FAX).build();

    /**
     * Email
     */
    public static final ContactPointSystem EMAIL = ContactPointSystem.builder().value(ValueSet.EMAIL).build();

    /**
     * Pager
     */
    public static final ContactPointSystem PAGER = ContactPointSystem.builder().value(ValueSet.PAGER).build();

    /**
     * URL
     */
    public static final ContactPointSystem URL = ContactPointSystem.builder().value(ValueSet.URL).build();

    /**
     * SMS
     */
    public static final ContactPointSystem SMS = ContactPointSystem.builder().value(ValueSet.SMS).build();

    /**
     * Other
     */
    public static final ContactPointSystem OTHER = ContactPointSystem.builder().value(ValueSet.OTHER).build();

    private volatile int hashCode;

    private ContactPointSystem(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static ContactPointSystem of(ValueSet value) {
        switch (value) {
        case PHONE:
            return PHONE;
        case FAX:
            return FAX;
        case EMAIL:
            return EMAIL;
        case PAGER:
            return PAGER;
        case URL:
            return URL;
        case SMS:
            return SMS;
        case OTHER:
            return OTHER;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static ContactPointSystem of(java.lang.String value) {
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
        ContactPointSystem other = (ContactPointSystem) obj;
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
        public ContactPointSystem build() {
            return new ContactPointSystem(this);
        }
    }

    public enum ValueSet {
        /**
         * Phone
         */
        PHONE("phone"),

        /**
         * Fax
         */
        FAX("fax"),

        /**
         * Email
         */
        EMAIL("email"),

        /**
         * Pager
         */
        PAGER("pager"),

        /**
         * URL
         */
        URL("url"),

        /**
         * SMS
         */
        SMS("sms"),

        /**
         * Other
         */
        OTHER("other");

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
