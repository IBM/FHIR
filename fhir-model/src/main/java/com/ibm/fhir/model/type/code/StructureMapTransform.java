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

@System("http://hl7.org/fhir/map-transform")
@Generated("com.ibm.fhir.tools.CodeGenerator")
public class StructureMapTransform extends Code {
    /**
     * create
     */
    public static final StructureMapTransform CREATE = StructureMapTransform.builder().value(ValueSet.CREATE).build();

    /**
     * copy
     */
    public static final StructureMapTransform COPY = StructureMapTransform.builder().value(ValueSet.COPY).build();

    /**
     * truncate
     */
    public static final StructureMapTransform TRUNCATE = StructureMapTransform.builder().value(ValueSet.TRUNCATE).build();

    /**
     * escape
     */
    public static final StructureMapTransform ESCAPE = StructureMapTransform.builder().value(ValueSet.ESCAPE).build();

    /**
     * cast
     */
    public static final StructureMapTransform CAST = StructureMapTransform.builder().value(ValueSet.CAST).build();

    /**
     * append
     */
    public static final StructureMapTransform APPEND = StructureMapTransform.builder().value(ValueSet.APPEND).build();

    /**
     * translate
     */
    public static final StructureMapTransform TRANSLATE = StructureMapTransform.builder().value(ValueSet.TRANSLATE).build();

    /**
     * reference
     */
    public static final StructureMapTransform REFERENCE = StructureMapTransform.builder().value(ValueSet.REFERENCE).build();

    /**
     * dateOp
     */
    public static final StructureMapTransform DATE_OP = StructureMapTransform.builder().value(ValueSet.DATE_OP).build();

    /**
     * uuid
     */
    public static final StructureMapTransform UUID = StructureMapTransform.builder().value(ValueSet.UUID).build();

    /**
     * pointer
     */
    public static final StructureMapTransform POINTER = StructureMapTransform.builder().value(ValueSet.POINTER).build();

    /**
     * evaluate
     */
    public static final StructureMapTransform EVALUATE = StructureMapTransform.builder().value(ValueSet.EVALUATE).build();

    /**
     * cc
     */
    public static final StructureMapTransform CC = StructureMapTransform.builder().value(ValueSet.CC).build();

    /**
     * c
     */
    public static final StructureMapTransform C = StructureMapTransform.builder().value(ValueSet.C).build();

    /**
     * qty
     */
    public static final StructureMapTransform QTY = StructureMapTransform.builder().value(ValueSet.QTY).build();

    /**
     * id
     */
    public static final StructureMapTransform ID = StructureMapTransform.builder().value(ValueSet.ID).build();

    /**
     * cp
     */
    public static final StructureMapTransform CP = StructureMapTransform.builder().value(ValueSet.CP).build();

    private volatile int hashCode;

    private StructureMapTransform(Builder builder) {
        super(builder);
    }

    public ValueSet getValueAsEnumConstant() {
        return (value != null) ? ValueSet.from(value) : null;
    }

    public static StructureMapTransform of(ValueSet value) {
        switch (value) {
        case CREATE:
            return CREATE;
        case COPY:
            return COPY;
        case TRUNCATE:
            return TRUNCATE;
        case ESCAPE:
            return ESCAPE;
        case CAST:
            return CAST;
        case APPEND:
            return APPEND;
        case TRANSLATE:
            return TRANSLATE;
        case REFERENCE:
            return REFERENCE;
        case DATE_OP:
            return DATE_OP;
        case UUID:
            return UUID;
        case POINTER:
            return POINTER;
        case EVALUATE:
            return EVALUATE;
        case CC:
            return CC;
        case C:
            return C;
        case QTY:
            return QTY;
        case ID:
            return ID;
        case CP:
            return CP;
        default:
            throw new IllegalStateException(value.name());
        }
    }

    public static StructureMapTransform of(java.lang.String value) {
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
        StructureMapTransform other = (StructureMapTransform) obj;
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
        public StructureMapTransform build() {
            return new StructureMapTransform(this);
        }
    }

    public enum ValueSet {
        /**
         * create
         */
        CREATE("create"),

        /**
         * copy
         */
        COPY("copy"),

        /**
         * truncate
         */
        TRUNCATE("truncate"),

        /**
         * escape
         */
        ESCAPE("escape"),

        /**
         * cast
         */
        CAST("cast"),

        /**
         * append
         */
        APPEND("append"),

        /**
         * translate
         */
        TRANSLATE("translate"),

        /**
         * reference
         */
        REFERENCE("reference"),

        /**
         * dateOp
         */
        DATE_OP("dateOp"),

        /**
         * uuid
         */
        UUID("uuid"),

        /**
         * pointer
         */
        POINTER("pointer"),

        /**
         * evaluate
         */
        EVALUATE("evaluate"),

        /**
         * cc
         */
        CC("cc"),

        /**
         * c
         */
        C("c"),

        /**
         * qty
         */
        QTY("qty"),

        /**
         * id
         */
        ID("id"),

        /**
         * cp
         */
        CP("cp");

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
