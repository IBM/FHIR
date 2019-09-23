/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.type;

import java.util.Collection;
import java.util.Objects;

public class ActionRequiredBehavior extends Code {
    /**
     * Must
     */
    public static final ActionRequiredBehavior MUST = ActionRequiredBehavior.of(ValueSet.MUST);

    /**
     * Could
     */
    public static final ActionRequiredBehavior COULD = ActionRequiredBehavior.of(ValueSet.COULD);

    /**
     * Must Unless Documented
     */
    public static final ActionRequiredBehavior MUST_UNLESS_DOCUMENTED = ActionRequiredBehavior.of(ValueSet.MUST_UNLESS_DOCUMENTED);

    private volatile int hashCode;

    private ActionRequiredBehavior(Builder builder) {
        super(builder);
    }

    public static ActionRequiredBehavior of(java.lang.String value) {
        return ActionRequiredBehavior.builder().value(value).build();
    }

    public static ActionRequiredBehavior of(ValueSet value) {
        return ActionRequiredBehavior.builder().value(value).build();
    }

    public static String string(java.lang.String value) {
        return ActionRequiredBehavior.builder().value(value).build();
    }

    public static Code code(java.lang.String value) {
        return ActionRequiredBehavior.builder().value(value).build();
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
        ActionRequiredBehavior other = (ActionRequiredBehavior) obj;
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
        public ActionRequiredBehavior build() {
            return new ActionRequiredBehavior(this);
        }
    }

    public enum ValueSet {
        /**
         * Must
         */
        MUST("must"),

        /**
         * Could
         */
        COULD("could"),

        /**
         * Must Unless Documented
         */
        MUST_UNLESS_DOCUMENTED("must-unless-documented");

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
