/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.converter.impl;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhiruri;
import static com.ibm.fhir.cql.helpers.ModelHelper.javastring;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.opencds.cqf.cql.engine.exception.InvalidPrecision;
import org.opencds.cqf.cql.engine.runtime.BaseTemporal;
import org.opencds.cqf.cql.engine.runtime.Concept;
import org.opencds.cqf.cql.engine.runtime.CqlType;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.opencds.cqf.cql.engine.runtime.Tuple;

import com.ibm.fhir.cql.engine.converter.FHIRTypeConverter;
import com.ibm.fhir.cql.helpers.DateHelper;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Decimal;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.Id;
import com.ibm.fhir.model.type.Instant;
import com.ibm.fhir.model.type.Integer;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.Quantity;
import com.ibm.fhir.model.type.Range;
import com.ibm.fhir.model.type.Ratio;
import com.ibm.fhir.model.type.SimpleQuantity;
import com.ibm.fhir.model.type.Time;
import com.ibm.fhir.model.type.Uri;

/**
 * This is a default implementation of the FHIRTypeConverter interface that
 * converts IBM FHIR Server model objects to and from CQL types.
 */
public class FHIRTypeConverterImpl implements FHIRTypeConverter {

    @Override
    public Boolean isFhirType(Object value) {
        return (value instanceof Element || value instanceof Resource);
    }

    @Override
    public Object toFhirType(Object value) {
        Object result;
        if (value == null) {
            result = value;
        } else if (isFhirType(value)) {
            result = value;
        } else if (!isCqlType(value)) {
            throw new IllegalArgumentException(String.format("Can't convert %s to FHIR type", value.getClass().getName()));
        } else {
            switch (value.getClass().getSimpleName()) {
            case "Boolean":
                result = toFhirBoolean((java.lang.Boolean) value);
                break;
            case "Integer":
                result = toFhirInteger((java.lang.Integer) value);
                break;
            case "BigDecimal":
                result = toFhirDecimal((BigDecimal) value);
                break;
            case "Date":
                result = toFhirDate((org.opencds.cqf.cql.engine.runtime.Date) value);
                break;
            case "DateTime":
                result = toFhirDateTime((org.opencds.cqf.cql.engine.runtime.DateTime) value);
                break;
            case "Time":
                result = toFhirTime((org.opencds.cqf.cql.engine.runtime.Time) value);
                break;
            case "String":
                result = toFhirString((String) value);
                break;
            case "Quantity":
                result = toFhirQuantity((org.opencds.cqf.cql.engine.runtime.Quantity) value);
                break;
            case "Ratio":
                result = toFhirRatio((org.opencds.cqf.cql.engine.runtime.Ratio) value);
                break;
            // case "Any": result = toFhirAny(value); break;
            case "Code":
                result = toFhirCoding((org.opencds.cqf.cql.engine.runtime.Code) value);
                break;
            case "Concept":
                result = toFhirCodeableConcept((org.opencds.cqf.cql.engine.runtime.Concept) value);
                break;
            case "Interval":
                result = toFhirInterval((org.opencds.cqf.cql.engine.runtime.Interval) value);
                break;
            case "Tuple": result = toFhirTuple((Tuple) value); break;
            default:
                throw new IllegalArgumentException(String.format("missing case statement for: %s", value.getClass().getName()));
            }
        }
        return result;
    }

    @Override
    public Iterable<Object> toFhirTypes(Iterable<?> values) {
        List<Object> converted = new ArrayList<>();
        values.forEach(obj -> converted.add( toFhirType(obj) ));
        return converted;
    }

    @Override
    public Id toFhirId(String value) {
        Id result = null;
        if (value != null) {
            result = Id.of(value);
        }
        return result;
    }

    @Override
    public com.ibm.fhir.model.type.Boolean toFhirBoolean(Boolean value) {
        com.ibm.fhir.model.type.Boolean result = null;
        if (value != null) {
            result = com.ibm.fhir.model.type.Boolean.of(value);
        }
        return result;
    }

    @Override
    public Integer toFhirInteger(java.lang.Integer value) {
        com.ibm.fhir.model.type.Integer result = null;
        if (value != null) {
            result = com.ibm.fhir.model.type.Integer.of(value);
        }
        return result;
    }

    @Override
    public Decimal toFhirDecimal(BigDecimal value) {
        Decimal result = null;
        if (value != null) {
            result = Decimal.of(value);
        }
        return result;
    }

    @Override
    public Date toFhirDate(org.opencds.cqf.cql.engine.runtime.Date value) {
        Date result = null;
        if (value != null) {
            result = Date.of(value.toString());
        }
        return result;
    }

    @Override
    public DateTime toFhirDateTime(org.opencds.cqf.cql.engine.runtime.DateTime value) {
        DateTime result = null;
        if (value != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
            java.time.Instant instant = java.time.Instant.from(dtf.parse(value.getDateTime().toString()));
            return DateTime.of(instant);
        }
        return result;
    }

    @Override
    public Time toFhirTime(org.opencds.cqf.cql.engine.runtime.Time value) {
        Time result = null;
        if (value != null) {
            result = Time.of(value.toString());
        }
        return result;
    }

    @Override
    public com.ibm.fhir.model.type.String toFhirString(String value) {
        com.ibm.fhir.model.type.String result = null;
        if (value != null) {
            result = com.ibm.fhir.model.type.String.of(value);
        }
        return result;
    }

    @Override
    public SimpleQuantity toFhirQuantity(org.opencds.cqf.cql.engine.runtime.Quantity value) {
        SimpleQuantity result = null;
        if (value != null) {
            result = SimpleQuantity.builder().system(Uri.of("http://unitsofmeasure.org")).code(Code.of(value.getUnit())).value(Decimal.of(value.getValue())).build();
        }
        return result;
    }

    @Override
    public Ratio toFhirRatio(org.opencds.cqf.cql.engine.runtime.Ratio value) {
        Ratio result = null;
        if (value != null) {
            result = Ratio.builder().numerator(toFhirQuantity(value.getNumerator())).denominator(toFhirQuantity(value.getDenominator())).build();
        }
        return result;
    }

    @Override
    public Coding toFhirCoding(org.opencds.cqf.cql.engine.runtime.Code value) {
        Coding result = null;
        if (value != null) {
            result = Coding.builder().system(fhiruri(value.getSystem())).code(Code.of(value.getCode())).display(fhirstring(value.getDisplay())).build();

        }
        return result;
    }

    @Override
    public CodeableConcept toFhirCodeableConcept(Concept value) {
        CodeableConcept result = null;
        if (value != null) {
            CodeableConcept.Builder builder = CodeableConcept.builder();
            for (org.opencds.cqf.cql.engine.runtime.Code code : value.getCodes()) {
                builder.coding(toFhirCoding(code));
            }
            result = builder.build();
        }
        return result;
    }

    @Override
    public Period toFhirPeriod(Interval value) {
        Period result = null;
        if (value != null) {
            Period.Builder builder = Period.builder();
            if (getSimpleName(value.getPointType().getTypeName()).equals("DateTime")) {
                if (value.getStart() != null) {
                    builder.start(toFhirDateTime((org.opencds.cqf.cql.engine.runtime.DateTime) value.getStart()));
                }
                if (value.getEnd() != null) {
                    builder.end(toFhirDateTime((org.opencds.cqf.cql.engine.runtime.DateTime) value.getEnd()));
                }
            } else if (getSimpleName(value.getPointType().getTypeName()).equals("Date")) {
                if (value.getStart() != null) {
                    builder.start(toFhirDate((org.opencds.cqf.cql.engine.runtime.Date) value.getStart()).as(DateTime.class));
                }
                if (value.getEnd() != null) {
                    builder.end(toFhirDate((org.opencds.cqf.cql.engine.runtime.Date) value.getEnd()).as(DateTime.class));
                }
            } else {
                throw new IllegalArgumentException("FHIR Period can only be created from an Interval of Date or DateTime type");
            }
        }
        return result;
    }

    @Override
    public Range toFhirRange(Interval value) {
        Range result = null;
        if (value != null) {
            if (getSimpleName(value.getPointType().getTypeName()).equals("Quantity")) {

                result = Range.builder().low(toFhirQuantity((org.opencds.cqf.cql.engine.runtime.Quantity) value.getLow())).high(toFhirQuantity((org.opencds.cqf.cql.engine.runtime.Quantity) value.getHigh())).build();
            } else {
                throw new IllegalArgumentException("FHIR Range can only be created from an Interval of Quantity type");
            }
        }
        return result;
    }

    @Override
    public Element toFhirInterval(Interval value) {
        Element result = null;
        if (value != null) {
            switch (getSimpleName(value.getPointType().getTypeName())) {
            case "Date":
            case "DateTime":
                return toFhirPeriod(value);
            case "Quantity":
                return toFhirRange(value);
            default:
                throw new IllegalArgumentException(String.format("Unsupported interval point type for FHIR conversion %s", value.getPointType().getTypeName()));
            }
        }
        return result;
    }

    @Override
    public Boolean isCqlType(Object value) {
        boolean result;

        if (value instanceof CqlType) {
            result = true;
        } else if (value instanceof java.math.BigDecimal || value instanceof java.lang.String || value instanceof java.lang.Integer
                || value instanceof java.lang.Boolean) {
            result = true;
        } else {
            result = false;
        }

        return result;
    }
    
    @Override
    public Object toFhirTuple(Tuple value) {
        Object result = null;
        if (value != null) {
            throw new NotImplementedException("can't convert Tuples");
        } 
        return result;
    }

    @Override
    public Object toCqlType(Object value) {
        Object result = null;

        if (isCqlType(value)) {
            return value;
        }

        if (!isFhirType(value)) {
            throw new IllegalArgumentException(String.format("can't convert %s to CQL type", value.getClass().getName()));
        }

        switch (value.getClass().getSimpleName()) {
        // NOTE: There's no first class IdType in CQL, so the conversion to CQL Ids and
        // back is asymmetric
        case "Id":
            result = toCqlId((Id) value);
            break;
        case "Boolean":
            result = toCqlBoolean((com.ibm.fhir.model.type.Boolean) value);
            break;
        case "Integer":
            result = toCqlInteger((com.ibm.fhir.model.type.Integer) value);
            break;
        case "Decimal":
            result = toCqlDecimal((com.ibm.fhir.model.type.Decimal) value);
            break;
        case "Date":
            result = toCqlDate((com.ibm.fhir.model.type.Date) value);
            break;
        // NOTE: There's no first class InstantType in CQL, so the conversation to CQL
        // DateTime and back is asymmetric
        case "Instant":
        case "DateTime":
            result = toCqlDateTime((com.ibm.fhir.model.type.DateTime) value);
            break;
        case "Time":
            result = toCqlTime((com.ibm.fhir.model.type.Time) value);
            break;
        case "String":
            result = toCqlString((com.ibm.fhir.model.type.String) value);
            break;
        case "Quantity":
            result = toCqlQuantity((com.ibm.fhir.model.type.Quantity) value);
            break;
        case "Ratio":
            result = toCqlRatio((com.ibm.fhir.model.type.Ratio) value);
            break;
        // case "AnyType": result = toCqlAny((IBase)value);break;
        case "Coding":
            result = toCqlCode((com.ibm.fhir.model.type.Coding) value);
            break;
        case "CodeableConcept":
            result = toCqlConcept((com.ibm.fhir.model.type.CodeableConcept) value);
            break;
        case "Period":
        case "Range":
            result = toCqlInterval((com.ibm.fhir.model.type.Element) value);
            break;
        // case "Tuple": return toCqlTuple((IBase) value);
        default:
            throw new IllegalArgumentException(String.format("missing case statement for: %s", value.getClass().getName()));
        }

        return result;
    }

    @Override
    public Iterable<Object> toCqlTypes(Iterable<?> values) {
        List<Object> result = new ArrayList<>();
        values.forEach(obj -> toCqlType(obj));
        return result;
    }

    @Override
    public String toCqlId(Id value) {
        String result = null;
        if (value != null) {
            result = value.getValue();
        }
        return result;
    }

    @Override
    public Boolean toCqlBoolean(com.ibm.fhir.model.type.Boolean value) {
        Boolean result = null;
        if (value != null) {
            result = value.getValue();
        }
        return result;
    }

    @Override
    public java.lang.Integer toCqlInteger(Integer value) {
        java.lang.Integer result = null;
        if (value != null) {
            result = value.getValue();
        }
        return result;
    }

    @Override
    public BigDecimal toCqlDecimal(Decimal value) {
        BigDecimal result = null;
        if (value != null) {
            result = value.getValue();
        }
        return result;
    }

    @Override
    public org.opencds.cqf.cql.engine.runtime.Date toCqlDate(Date value) {
        org.opencds.cqf.cql.engine.runtime.Date result = null;
        if (value != null) {
            TemporalAccessor ta = value.getValue();
            TemporalUnit precision = ta.query(TemporalQueries.precision());
            if (precision.equals(ChronoUnit.YEARS)) {
                result = new org.opencds.cqf.cql.engine.runtime.Date(ta.get(ChronoField.YEAR));
            } else if (precision.equals(ChronoUnit.MONTHS)) {
                result = new org.opencds.cqf.cql.engine.runtime.Date(ta.get(ChronoField.YEAR), ta.get(ChronoField.MONTH_OF_YEAR) + 1);
            } else if (precision.equals(ChronoUnit.DAYS)) {
                result = new org.opencds.cqf.cql.engine.runtime.Date(ta.get(ChronoField.YEAR), ta.get(ChronoField.MONTH_OF_YEAR)
                        + 1, ta.get(ChronoField.DAY_OF_MONTH));
            } else {
                throw new InvalidPrecision(String.format("Invalid temporal precision %s", precision.toString()));
            }
        }
        return result;
    }

    @Override
    public org.opencds.cqf.cql.engine.runtime.DateTime toCqlDateTime(DateTime value) {
        org.opencds.cqf.cql.engine.runtime.DateTime result = null;
        if (value != null) {
            TemporalAccessor ta = value.getValue();
            org.opencds.cqf.cql.engine.runtime.BaseTemporal temporal = DateHelper.toCqlTemporal(ta);
            if( temporal instanceof org.opencds.cqf.cql.engine.runtime.Date ) {
                org.opencds.cqf.cql.engine.runtime.Date cqlDate = (org.opencds.cqf.cql.engine.runtime.Date) temporal;                
                result = org.opencds.cqf.cql.engine.runtime.DateTime.fromJavaDate(cqlDate.toJavaDate());
            } else if( temporal instanceof org.opencds.cqf.cql.engine.runtime.DateTime ) {
                result = (org.opencds.cqf.cql.engine.runtime.DateTime) temporal;
            } else { 
                assert false;
            }
        }
        return result;
    }

    @Override
    public org.opencds.cqf.cql.engine.runtime.DateTime toCqlDateTime(Instant value) {
        org.opencds.cqf.cql.engine.runtime.DateTime result = null;
        if (value != null) {
            ZonedDateTime zdt = value.getValue();
            OffsetDateTime odt = OffsetDateTime.from(zdt);
            result = new org.opencds.cqf.cql.engine.runtime.DateTime(odt);
        }
        return result;
    }

    @Override
    public BaseTemporal toCqlTemporal(Element value) {
        BaseTemporal result = null;
        if (value != null) {
            if (value instanceof Instant) {
                result = toCqlDateTime((Instant) value);
            } else if (value instanceof DateTime) {
                result = toCqlDateTime((DateTime) value);
            } else if (value instanceof Date) {
                result = toCqlDate((Date) value);
            } else {
                throw new IllegalArgumentException("value is not a FHIR Instant, DateTime, or Date");
            }
        }
        return result;
    }

    @Override
    public org.opencds.cqf.cql.engine.runtime.Time toCqlTime(Time value) {
        org.opencds.cqf.cql.engine.runtime.Time result = null;
        if (value != null) {
            LocalTime ta = value.getValue();
            result = new org.opencds.cqf.cql.engine.runtime.Time(ta.getHour(), ta.getMinute(), ta.getSecond(), ta.getNano());
        }
        return result;
    }

    @Override
    public String toCqlString(com.ibm.fhir.model.type.String value) {
        String result = null;
        if (value != null) {
            result = value.getValue();
        }
        return result;
    }

    @Override
    public org.opencds.cqf.cql.engine.runtime.Quantity toCqlQuantity(Quantity value) {
        org.opencds.cqf.cql.engine.runtime.Quantity result = null;
        if (value != null) {
            result = new org.opencds.cqf.cql.engine.runtime.Quantity().withUnit(javastring(value.getUnit())).withValue(value.getValue().getValue());
        }
        return result;
    }

    @Override
    public org.opencds.cqf.cql.engine.runtime.Ratio toCqlRatio(Ratio value) {
        org.opencds.cqf.cql.engine.runtime.Ratio result = null;
        if (value != null) {
            result = new org.opencds.cqf.cql.engine.runtime.Ratio().setNumerator(toCqlQuantity(value.getNumerator())).setDenominator(toCqlQuantity(value.getDenominator()));
        }
        return result;
    }

    @Override
    public org.opencds.cqf.cql.engine.runtime.Code toCqlCode(Coding value) {
        org.opencds.cqf.cql.engine.runtime.Code result = null;
        if (value != null) {
            result = new org.opencds.cqf.cql.engine.runtime.Code().withSystem(javastring(value.getSystem())).withCode(javastring(value.getCode())).withVersion(javastring(value.getVersion())).withDisplay(javastring(value.getDisplay()));
        }
        return result;
    }

    @Override
    public Concept toCqlConcept(CodeableConcept value) {
        org.opencds.cqf.cql.engine.runtime.Concept result = null;
        if (value != null) {
            result = new org.opencds.cqf.cql.engine.runtime.Concept().withDisplay(javastring(value.getText())).withCodes(value.getCoding().stream().map(coding -> toCqlCode(coding)).collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public Interval toCqlInterval(Element value) {
        org.opencds.cqf.cql.engine.runtime.Interval result = null;
        if (value != null) {
            if (value instanceof Range) {
                Range range = (Range) value;
                result = new Interval(toCqlQuantity(range.getLow()), true, toCqlQuantity(range.getHigh()), true);
            } else if (value instanceof Period) {
                Period period = (Period) value;
                result = new Interval(toCqlTemporal(period.getStart()), true, toCqlTemporal(period.getEnd()), true);
            } else {
                throw new IllegalArgumentException("value is not a FHIR Range or Period");
            }
        }
        return result;
    }

    protected String getSimpleName(String typeName) {
        String[] nameParts = typeName.split("\\.");
        return nameParts[nameParts.length - 1];
    }

}
