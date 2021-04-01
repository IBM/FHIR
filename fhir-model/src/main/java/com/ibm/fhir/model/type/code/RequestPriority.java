/*
 * (C) Copyright IBM Corp. 2019, 2021
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

@System("http://hl7.org/fhir/request-priority")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class RequestPriority extends Code {
    /**
     * Routine
     * 
     * <p>The request has normal priority.
     */
    public static final RequestPriority ROUTINE = RequestPriority.builder().value(ValueSet.ROUTINE).build();

    /**
     * Urgent
     * 
     * <p>The request should be actioned promptly - higher priority than routine.
     */
    public static final RequestPriority URGENT = RequestPriority.builder().value(ValueSet.URGENT).build();

    /**
     * ASAP
     * 
     * <p>The request should be actioned as soon as possible - higher priority than urgent.
     */
    public static final RequestPriority ASAP = RequestPriority.builder().value(ValueSet.ASAP).build();

    /**
     * STAT
     * 
     * <p>The request should be actioned immediately - highest possible priority. E.g. an emergency.
     */
    public static final RequestPriority STAT = RequestPriority.builder().value(ValueSet.STAT).build();

    private volatile int hashCode;

    private RequestPriority(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    /**
     * Factory method for creating RequestPriority objects from a passed enum value.
     */
    public static RequestPriority of(ValueSet value) {
        switch (value) {
        case ROUTINE:
            return ROUTINE;
        case URGENT:
            return URGENT;
        case ASAP:
            return ASAP;
        case STAT:
            return STAT;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    /**
     * Factory method for creating RequestPriority objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static RequestPriority of(java.lang.String value) {
        return of(ValueSet.from(value));
    }

    /**
     * Inherited factory method for creating RequestPriority objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
    public static String string(java.lang.String value) {
        return of(ValueSet.from(value));
    }

    /**
     * Inherited factory method for creating RequestPriority objects from a passed string value.
     * 
     * @param value
     *     A string that matches one of the allowed code values
     * @throws IllegalArgumentException
     *     If the passed string cannot be parsed into an allowed code value
     */
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
        RequestPriority other = (RequestPriority) obj;
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
        public RequestPriority build() {
            return new RequestPriority(this);
        }
    }

    public enum ValueSet {
        /**
         * Routine
         * 
         * <p>The request has normal priority.
         */
        ROUTINE("routine"),

        /**
         * Urgent
         * 
         * <p>The request should be actioned promptly - higher priority than routine.
         */
        URGENT("urgent"),

        /**
         * ASAP
         * 
         * <p>The request should be actioned as soon as possible - higher priority than urgent.
         */
        ASAP("asap"),

        /**
         * STAT
         * 
         * <p>The request should be actioned immediately - highest possible priority. E.g. an emergency.
         */
        STAT("stat");

        private final java.lang.String value;

        ValueSet(java.lang.String value) {
            this.value = value;
        }

        /**
         * @return
         *     The java.lang.String value of the code represented by this enum
         */
        public java.lang.String value() {
            return value;
        }

        /**
         * Factory method for creating RequestPriority.ValueSet values from a passed string value.
         * 
         * @param value
         *     A string that matches one of the allowed code values
         * @throws IllegalArgumentException
         *     If the passed string cannot be parsed into an allowed code value
         */
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
