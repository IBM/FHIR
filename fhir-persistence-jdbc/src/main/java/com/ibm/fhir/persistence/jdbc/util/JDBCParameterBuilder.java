/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.persistence.jdbc.util;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.fhir.model.path.FHIRPathAbstractNode;
import com.ibm.fhir.model.path.FHIRPathBooleanValue;
import com.ibm.fhir.model.path.FHIRPathDateTimeValue;
import com.ibm.fhir.model.path.FHIRPathDecimalValue;
import com.ibm.fhir.model.path.FHIRPathElementNode;
import com.ibm.fhir.model.path.FHIRPathIntegerValue;
import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.FHIRPathQuantityNode;
import com.ibm.fhir.model.path.FHIRPathResourceNode;
import com.ibm.fhir.model.path.FHIRPathStringValue;
import com.ibm.fhir.model.path.FHIRPathTimeValue;
import com.ibm.fhir.model.resource.Location;
import com.ibm.fhir.model.resource.SearchParameter;
import com.ibm.fhir.model.resource.StructureDefinition;
import com.ibm.fhir.model.resource.SubstanceSpecification;
import com.ibm.fhir.model.type.Address;
import com.ibm.fhir.model.type.Annotation;
import com.ibm.fhir.model.type.Attachment;
import com.ibm.fhir.model.type.BackboneElement;
import com.ibm.fhir.model.type.Base64Binary;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.ContactDetail;
import com.ibm.fhir.model.type.ContactPoint;
import com.ibm.fhir.model.type.Contributor;
import com.ibm.fhir.model.type.DataRequirement;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Decimal;
import com.ibm.fhir.model.type.Expression;
import com.ibm.fhir.model.type.HumanName;
import com.ibm.fhir.model.type.Id;
import com.ibm.fhir.model.type.Identifier;
import com.ibm.fhir.model.type.Instant;
import com.ibm.fhir.model.type.Markdown;
import com.ibm.fhir.model.type.Meta;
import com.ibm.fhir.model.type.Money;
import com.ibm.fhir.model.type.Narrative;
import com.ibm.fhir.model.type.Oid;
import com.ibm.fhir.model.type.ParameterDefinition;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.PositiveInt;
import com.ibm.fhir.model.type.Quantity;
import com.ibm.fhir.model.type.Range;
import com.ibm.fhir.model.type.Ratio;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.RelatedArtifact;
import com.ibm.fhir.model.type.SampledData;
import com.ibm.fhir.model.type.Signature;
import com.ibm.fhir.model.type.Time;
import com.ibm.fhir.model.type.Timing;
import com.ibm.fhir.model.type.TriggerDefinition;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.Url;
import com.ibm.fhir.model.type.UsageContext;
import com.ibm.fhir.model.type.Uuid;
import com.ibm.fhir.persistence.exception.FHIRPersistenceProcessorException;
import com.ibm.fhir.persistence.jdbc.dto.Parameter;
import com.ibm.fhir.persistence.jdbc.dto.Parameter.TimeType;
import com.ibm.fhir.persistence.util.AbstractProcessor;

/**
 * This class is the JDBC persistence layer implementation for transforming SearchParameters into Parameter DTOs for
 * persistence.
 *
 * @author markd
 *
 */
public class JDBCParameterBuilder extends AbstractProcessor<List<Parameter>> {
    private static final Logger log = Logger.getLogger(JDBCParameterBuilder.class.getName());
    private static final String CLASSNAME = JDBCParameterBuilder.class.getName();

    public static final String EXCEPTION_MSG = "Unexpected error while processing parameter [%s] with value [%s]";
    public static final String EXCEPTION_MSG_NAME_ONLY = "Unexpected error while processing parameter [%s]";

    // Datetime Limits from
    // DB2: https://www.ibm.com/support/knowledgecenter/en/SSEPGG_10.5.0/com.ibm.db2.luw.sql.ref.doc/doc/r0001029.html
    // Derby: https://db.apache.org/derby/docs/10.0/manuals/reference/sqlj271.html
    private static final Timestamp SMALLEST_TIMESTAMP = Timestamp.valueOf("0001-01-01 00:00:00.000000");
    private static final Timestamp LARGEST_TIMESTAMP = Timestamp.valueOf("9999-12-31 23:59:59.999999");

    private FHIRPersistenceProcessorException buildCodeOnlyNewException(SearchParameter parameter, Exception e) {
        // this used to use "SearchParameter.name" but now properly uses "SearchParameter.code"
        return new FHIRPersistenceProcessorException(String.format(EXCEPTION_MSG_NAME_ONLY, parameter.getCode().getValue()), e);
    }

    private List<Parameter> buildUnsupportedTypeResponse(Class<?> cls) {
        if (log.isLoggable(Level.FINE)) {
            log.fine(String.format("The processing of %s is unsupported", cls.getName()));
        }
        return Collections.emptyList();
    }

    private List<Parameter> processChildren(SearchParameter parameter, Collection<FHIRPathNode> children, String path)
        throws FHIRPersistenceProcessorException {
        if (children == null || children.isEmpty()) {
            throw new FHIRPersistenceProcessorException(String.format("unknown value in fhir path element node %s", path));
        }

        List<Parameter> parameters = new ArrayList<>();
        for (FHIRPathNode node : children) {
            parameters.addAll(process(parameter, node));
        }

        return parameters;
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathAbstractNode value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter, FHIRPathAbstractNode)";
        log.entering(CLASSNAME, methodName);

        List<Parameter> parameters = new ArrayList<>();

        if (value instanceof FHIRPathBooleanValue) {
            FHIRPathBooleanValue v = ((FHIRPathBooleanValue) value).asBooleanValue();
            parameters.addAll(process(parameter, v._boolean()));

        } else if (value instanceof FHIRPathDateTimeValue) {
            FHIRPathDateTimeValue v = ((FHIRPathDateTimeValue) value).asDateTimeValue();
            parameters.addAll(process(parameter, v.dateTime()));

        } else if (value instanceof FHIRPathDecimalValue) {
            FHIRPathDecimalValue v = ((FHIRPathDecimalValue) value).asDecimalValue();
            parameters.addAll(process(parameter, v.decimal()));

        } else if (value instanceof FHIRPathIntegerValue) {
            FHIRPathIntegerValue v = ((FHIRPathIntegerValue) value).asIntegerValue();
            parameters.addAll(process(parameter, v.integer()));

        } else if (value instanceof FHIRPathResourceNode) {
            FHIRPathResourceNode v = value.asResourceNode();
            parameters.addAll(processChildren(parameter, v.children(), v.path()));

        } else if (value instanceof FHIRPathStringValue) {
            FHIRPathStringValue v = ((FHIRPathStringValue) value).asStringValue();
            parameters.addAll(process(parameter, v.string()));

        } else if (value instanceof FHIRPathTimeValue) {
            FHIRPathTimeValue v = ((FHIRPathTimeValue) value).asTimeValue();
            parameters.addAll(process(parameter, v.time()));

        } else if (value instanceof FHIRPathElementNode) {
            if (value instanceof FHIRPathQuantityNode) {
                FHIRPathQuantityNode quantity = ((FHIRPathQuantityNode) value).asQuantityNode();
                parameters.addAll(process(parameter, quantity.quantity()));

            } else {
                parameters.addAll(processChildren(parameter, value.children(), value.path()));
            }
        } else {
            // Unknown type
            throw new FHIRPersistenceProcessorException("Unable to process value into fhir path subtype");
        }

        log.exiting(CLASSNAME, methodName);

        return parameters;
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathElementNode value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathDateTimeValue value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathStringValue value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathBooleanValue value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathDecimalValue value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathIntegerValue value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathResourceNode value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, FHIRPathTimeValue value) throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, com.ibm.fhir.model.path.FHIRPathQuantityNode value)
        throws FHIRPersistenceProcessorException {
        return process(parameter, (FHIRPathAbstractNode) value);
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.time.ZonedDateTime value) throws FHIRPersistenceProcessorException {
        final String methodName = "process(SearchParameter,ZonedDateTime)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            // We've decided not to exact match the string
            // as it came in to store into string representation of LocalDate
            // instead all we do is use the valueDate

            // Range match as it came in to store into TIMESTAMP
            Parameter pDate = new Parameter();
            pDate.setName(parameter.getCode().getValue());

            // Forces to the first day of the month.
            // Honors the zone
            pDate.setValueDate(java.sql.Timestamp.from(value.toInstant()));
            pDate.setTimeType(Parameter.TimeType.ZONE_DATE);
            parameters.add(pDate);

            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.time.LocalDate value) throws FHIRPersistenceProcessorException {
        final String methodName = "process(SearchParameter,LocalDate)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            // We've decided not to exact match the string
            // as it came in to store into string representation of LocalDate
            // instead all we do is use the valueDate

            // Range match as it came in to store into TIMESTAMP
            Parameter pDate = new Parameter();
            pDate.setName(parameter.getCode().getValue());

            // Forces to the first day of the month.
            // Default to UTC as this is not Zoned.
            pDate.setValueDate(java.sql.Timestamp.from(QueryBuilderUtil.getInstantFromPartial(value)));
            pDate.setTimeType(Parameter.TimeType.LOCAL_DATE);
            parameters.add(pDate);

            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.time.YearMonth value) throws FHIRPersistenceProcessorException {
        final String methodName = "process(SearchParameter,YearMonth)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {

            // We've decided not to exact match the string
            // as it came in to store into string representation of YYYY/MM
            // instead all we do is use the valueYYYYMM

            // Range match as it came in to store into TIMESTAMP
            Parameter pDate = new Parameter();
            pDate.setName(parameter.getCode().getValue());
            pDate.setTimeType(Parameter.TimeType.YEAR_MONTH);

            // Forces to the first day of the month.
            pDate.setValueDate(java.sql.Timestamp.from(QueryBuilderUtil.getInstantFromPartial(value)));
            parameters.add(pDate);

            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }

    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.time.Year value) throws FHIRPersistenceProcessorException {
        final String methodName = "process(SearchParameter,Year)";

        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {

            // We've decided not to exact match the string
            // as it came in to store into string representation of YYYY
            // instead all we do is use the valueYYYY

            // Range match as it came in to store into TIMESTAMP
            Parameter pDate = new Parameter();
            pDate.setName(parameter.getCode().getValue());

            // Forces to the first day of the month.
            pDate.setValueDate(java.sql.Timestamp.from(QueryBuilderUtil.getInstantFromPartial(value)));
            pDate.setTimeType(Parameter.TimeType.YEAR);
            parameters.add(pDate);

            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }

    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.lang.String value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,java.lang.String)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            if ("token".equals(parameter.getType().getValue())) {
                p.setValueCode(value);
            } else {
                p.setValueString(value);
            }
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw new FHIRPersistenceProcessorException(String.format(EXCEPTION_MSG, parameter.getCode(), value), e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, com.ibm.fhir.model.type.String value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,com.ibm.fhir.model.type.String)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {

            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueString(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Address value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Address)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();

            String paramName = parameter.getCode().getValue();

            if (value.getCity() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getCity().getValue());
                parameters.add(p);
            }
            if (value.getCountry() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getCountry().getValue());
                parameters.add(p);
            }

            if (value.getDistrict() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getDistrict().getValue());
                parameters.add(p);
            }

            if (value.getLine() != null) {
                for (com.ibm.fhir.model.type.String aLine : value.getLine()) {
                    p = new Parameter();
                    p.setName(paramName);
                    p.setValueString(aLine.getValue());
                    parameters.add(p);
                }

            }

            if (value.getPostalCode() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getPostalCode().getValue());
                parameters.add(p);
            }

            if (value.getState() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getState().getValue());
                parameters.add(p);
            }

            if (value.getUse() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getUse().getValue());
                parameters.add(p);
            }

            if (value.getType() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getType().getValue());
                parameters.add(p);
            }

            if (value.getText() != null) {
                p = new Parameter();
                p.setName(paramName);
                p.setValueString(value.getText().getValue());
                parameters.add(p);
            }

            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Annotation value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Attachment value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Base64Binary value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.lang.Boolean value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,java.lang.Boolean)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            if (value) {
                p.setValueCode("true");
            } else {
                p.setValueCode("false");
            }
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, com.ibm.fhir.model.type.Boolean value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,com.ibm.fhir.model.type.Boolean)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            if (value.getValue()) {
                p.setValueCode("true");
            } else {
                p.setValueCode("false");
            }
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Code value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Code)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueCode(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, CodeableConcept value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,CodeableConcept)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            for (Coding c : value.getCoding()) {
                parameters.addAll(this.process(parameter, c));
            }
            return parameters;
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Coding value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Coding)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            if (value.getCode() != null || value.getSystem() != null) {
                Parameter p = new Parameter();
                p.setName(parameter.getCode().getValue());
                if (value.getSystem() != null) {
                    p.setValueSystem(value.getSystem().getValue());
                }
                if (value.getCode() != null) {
                    p.setValueCode(value.getCode().getValue());
                }
                parameters.add(p);
            }
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, ContactPoint value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,ContactPoint)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            if (value.getValue() != null) {
                Parameter telecom = new Parameter();
                telecom.setName(parameter.getCode().getValue());
                telecom.setValueCode(value.getValue().getValue());
                if (value.getSystem() != null && value.getSystem().getValue() != null) {
                    // according to spec, this should be
                    // "http://hl7.org/fhir/contact-point-system/" + ContactPoint.use
                    telecom.setValueSystem(value.getSystem().getValue());
                }
                parameters.add(telecom);
            }

            // WHY ARE WE CREATING A SECOND PARAMETER WITH NAME = ContactPoint.system ?
            if (value.getSystem() != null && value.getValue() != null) {
                Parameter phone = new Parameter();
                // phone | fax | email | pager | other
                phone.setValueCode(value.getValue().getValue());
                phone.setName(value.getSystem().getValue());
                phone.setValueSystem(value.getSystem().getValue());
                parameters.add(phone);
            }

            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Date value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Date)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            // handles all the variants of partial dates
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            setDateValues(p, value);
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    /**
     * Configure the date values in the parameter based on the model {@link DateTime} and the type of date it
     * represents.
     *
     * @param p
     * @param instant
     */
    private void setDateValues(Parameter p, DateTime dateTime) {

        if (!dateTime.isPartial()) {
            // fully specified time including zone, so we can interpret as an instant
            p.setValueDate(java.sql.Timestamp.from(java.time.Instant.from(dateTime.getValue())));
        } else {
            java.time.Instant start = QueryBuilderUtil.getStart(dateTime);
            java.time.Instant end = QueryBuilderUtil.getEnd(dateTime);
            setDateValues(p, start, end);
        }
    }

    /**
     * Set the date values on the {@link Parameter}, adjusting the end time slightly to make it exclusive (which is a TODO to fix).
     *
     * @param p
     * @param start
     * @param end
     */
    private void setDateValues(Parameter p, java.time.Instant start, java.time.Instant end) {
        Timestamp startTime = Timestamp.from(start);
        p.setValueDateStart(startTime);
        p.setValueDate(startTime);
        p.setTimeType(TimeType.UNKNOWN);

        Timestamp implicitEndExclusive = Timestamp.from(end);
        // TODO: Is it possible to avoid this by using <= or BETWEEN instead of < when constructing the query?
        Timestamp implicitEndInclusive = convertToInclusiveEnd(implicitEndExclusive);
        p.setValueDateEnd(implicitEndInclusive);
    }

    /**
     * Configure the date values in the parameter based on the model {@link Date} which again might be partial
     * (Year/YearMonth/LocalDate)
     *
     * @param p
     * @param date
     */
    private void setDateValues(Parameter p, Date date) {
        java.time.Instant start = QueryBuilderUtil.getStart(date);
        java.time.Instant end = QueryBuilderUtil.getEnd(date);
        setDateValues(p, start, end);
    }

    /**
     * Convert a period's end timestamp from an exclusive end timestamp to an inclusive one
     *
     * @param exlusiveEndTime
     * @return inclusiveEndTime
     */
    private Timestamp convertToInclusiveEnd(Timestamp exlusiveEndTime) {
        // Our current schema uses the db2/derby default of 6 decimal places (1000 nanoseconds) for fractional seconds.
        return Timestamp.from(exlusiveEndTime.toInstant().minusNanos(1000));
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, DateTime value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,DateTime)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            setDateValues(p, value);
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Decimal value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Decimal)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueNumber(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, HumanName value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,HumanName)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            String paramname = parameter.getCode().getValue();

            Parameter p = new Parameter();
            if (value.getFamily() != null) {
                // family just a string in R4 (not a list)
                p = new Parameter();
                p.setName(paramname);
                p.setValueString(value.getFamily().getValue());
                parameters.add(p);
            }
            if (value.getGiven() != null) {
                for (com.ibm.fhir.model.type.String given : value.getGiven()) {
                    p = new Parameter();
                    p.setName(paramname);
                    p.setValueString(given.getValue());
                    parameters.add(p);
                }
            }

            if (value.getText() != null) {
                p = new Parameter();
                p.setName(paramname);
                p.setValueString(value.getText().getValue());

                parameters.add(p);
            }

            if (value.getUse() != null) {
                p = new Parameter();
                p.setName(paramname);
                p.setValueString(value.getUse().getValue());

                parameters.add(p);
            }

            return parameters;

        } catch (Exception e) {

            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Id value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Id)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueCode(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {

            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Identifier value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Identifier)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            if (Objects.nonNull(value) && Objects.nonNull(value.getValue())) {
                Parameter p = new Parameter();
                p.setName(parameter.getCode().getValue());
                if (value.getSystem() != null) {
                    p.setValueSystem(value.getSystem().getValue());
                }
                p.setValueCode(value.getValue().getValue());
                parameters.add(p);
            }
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Instant value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Instant)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueDate(Timestamp.from(value.getValue().toInstant()));
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.lang.Integer value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,java.lang.Integer)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            // TODO: consider moving integer values to separate column so they can be searched different from decimals
            p.setValueNumber(new BigDecimal(value));
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, com.ibm.fhir.model.type.Integer value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,com.ibm.fhir.model.type.Integer)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            // TODO: consider moving integer values to separate column so they can be searched different from decimals
            p.setValueNumber(new BigDecimal(value.getValue()));
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    /**
     *
     * Parameter Name = postition Value = System|code = Longitude|Latitude
     *
     * @throws FHIRPersistenceProcessorException
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, Location.Position value) throws FHIRPersistenceProcessorException {

        String methodName = "process(SearchParameter,Location.Position)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            if (value.getLatitude() != null) {
                p.setValueLatitude(value.getLatitude().getValue().doubleValue());
            }
            if (value.getLongitude() != null) {
                p.setValueLongitude(value.getLongitude().getValue().doubleValue());
            }
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Markdown value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Markdown)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueString(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Meta value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Oid value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Oid)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueString(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Period value) throws FHIRPersistenceProcessorException {
        final String methodName = "process(SearchParameter,Period)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            if (value.getStart() == null && value.getEnd() == null) {
                return parameters;
            }
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            if (value.getStart() == null || value.getStart().getValue() == null) {
                p.setValueDateStart(SMALLEST_TIMESTAMP);
            } else {
                java.time.Instant startInst = QueryBuilderUtil.getInstant(value.getStart());
                p.setValueDateStart(Timestamp.from(startInst));
            }
            if (value.getEnd() == null || value.getEnd().getValue() == null) {
                p.setValueDateEnd(LARGEST_TIMESTAMP);
            } else {
                java.time.Instant endInst = QueryBuilderUtil.getInstant(value.getEnd());
                p.setValueDateEnd(Timestamp.from(endInst));
            }
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, PositiveInt value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,PositiveInt)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            // TODO: consider moving integer values to separate column so they can be searched different from decimals
            p.setValueNumber(new BigDecimal(value.getValue()));
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, java.math.BigDecimal value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,PositiveInt)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            // TODO: consider moving integer values to separate column so they can be searched different from decimals
            p.setValueNumber(value);
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Quantity value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Quantity)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            if (Objects.nonNull(value) && Objects.nonNull(value.getValue()) && Objects.nonNull(value.getValue().getValue())
                    && (Objects.nonNull(value.getCode()) || Objects.nonNull(value.getUnit()))) {
                Parameter p = new Parameter();
                p.setName(parameter.getCode().getValue());
                BigDecimal bd = value.getValue().getValue();
                p.setValueNumber(bd);
                if (value.getCode() != null) {
                    p.setValueCode(value.getCode().getValue());
                } else if (value.getUnit() != null) {
                    p.setValueCode(value.getUnit().getValue());
                }
                if (value.getSystem() != null) {
                    p.setValueSystem(value.getSystem().getValue());
                }
                parameters.add(p);
            }
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Range value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Range)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            if (value.getLow() != null && value.getLow().getValue() != null && value.getLow().getValue().getValue() != null) {
                if (value.getLow().getSystem() != null) {
                    p.setValueSystem(value.getLow().getSystem().getValue());
                }
                if (value.getLow().getCode() != null) {
                    p.setValueCode(value.getLow().getCode().getValue());
                } else if (value.getLow().getUnit() != null) {
                    p.setValueCode(value.getLow().getUnit().getValue());
                }
                p.setValueNumberLow(value.getLow().getValue().getValue());

                // The unit and code/system elements of the low or high elements SHALL match
                if (value.getHigh() != null && value.getHigh().getValue() != null && value.getHigh().getValue().getValue() != null) {
                    p.setValueNumberHigh(value.getHigh().getValue().getValue());
                }

                parameters.add(p);
            } else if (value.getHigh() != null && value.getHigh().getValue() != null && value.getHigh().getValue().getValue() != null) {
                if (value.getHigh().getSystem() != null) {
                    p.setValueSystem(value.getHigh().getSystem().getValue());
                }
                if (value.getHigh().getCode() != null) {
                    p.setValueCode(value.getHigh().getCode().getValue());
                } else if (value.getHigh().getUnit() != null) {
                    p.setValueCode(value.getHigh().getUnit().getValue());
                }
                p.setValueNumberHigh(value.getHigh().getValue().getValue());
                parameters.add(p);
            }

            // The parameter isn't added unless either low or high holds a value
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Ratio value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Reference value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Reference)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            if (value.getReference() != null) {
                Parameter p = new Parameter();
                p.setName(parameter.getCode().getValue());
                p.setValueString(value.getReference().getValue());
                parameters.add(p);
            }
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, SampledData value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Signature value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Time value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Timing value) throws FHIRPersistenceProcessorException {
        /*
         * The specified scheduling details are ignored and only the outer limits matter. For instance, a schedule that
         * specifies every second day between 31-Jan 2013 and 24-Mar 2013 includes 1-Feb 2013, even though that is on an
         * odd day that is not specified by the period. This is to keep the server load processing queries reasonable.
         */

        Timing.Repeat repeat = value.getRepeat();
        if (repeat != null) {
            // Timing.repeat.period is the # of times the event occurs per period.
            // Instead, I think this should be using the bounds[x] element if present.
            // If not present, how can we know the "outer limits"?
            // Maybe try to compute the "outer limits" from the events?
            return process(parameter, value.getRepeat().getPeriod());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, UnsignedInt value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Uri value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Uri)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueString(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.Url)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, Url value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Url)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {
            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueString(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    @Override
    public List<Parameter> process(SearchParameter parameter, Uuid value) {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.BackboneElement)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, BackboneElement value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.ContactDetail)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, ContactDetail value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.Contributor)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, Contributor value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.DataRequirement)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, DataRequirement value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.Expression)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, Expression value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.Money)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, Money value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.Narrative)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, Narrative value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.ParameterDefinition)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, ParameterDefinition value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.RelatedArtifact)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, RelatedArtifact value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.TriggerDefinition)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, TriggerDefinition value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.UsageContext)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, UsageContext value) throws FHIRPersistenceProcessorException {
        return buildUnsupportedTypeResponse(value.getClass());
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.fhir.persistence.util.Processor#process(com.ibm.fhir.model.resource.
     * SearchParameter, com.ibm.fhir.model.type.Canonical)
     */
    @Override
    public List<Parameter> process(SearchParameter parameter, Canonical value) throws FHIRPersistenceProcessorException {
        String methodName = "process(SearchParameter,Canonical)";
        log.entering(CLASSNAME, methodName);
        List<Parameter> parameters = new ArrayList<>();
        try {

            Parameter p = new Parameter();
            p.setName(parameter.getCode().getValue());
            p.setValueString(value.getValue());
            parameters.add(p);
            return parameters;
        } catch (Exception e) {
            throw buildCodeOnlyNewException(parameter, e);
        } finally {
            log.exiting(CLASSNAME, methodName);
        }
    }

    /*
     * Workarounds for https://gforge.hl7.org/gf/project/fhir/tracker/?action=TrackerItemEdit&tracker_item_id=24917
     */
    public List<Parameter> process(SearchParameter parameter, StructureDefinition.Context value) throws FHIRPersistenceProcessorException {
        return process(parameter, value.getType());
    }
    public List<Parameter> process(SearchParameter parameter, SubstanceSpecification.Code value) throws FHIRPersistenceProcessorException {
        return process(parameter, value.getCode());
    }
}
