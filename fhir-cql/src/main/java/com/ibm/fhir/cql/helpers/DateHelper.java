/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.helpers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;

import org.opencds.cqf.cql.engine.exception.InvalidPrecision;
import org.opencds.cqf.cql.engine.runtime.BaseTemporal;

/**
 * Helper logic for working with java.util.time objects and the CQL
 * internal date and time types.
 */
public class DateHelper {

    private static int MIN_DAY = 1;
    private static int MAX_DAY = 31;
    
    public static LocalDateTime dateFloor(TemporalAccessor ta) {
        LocalDate date = getDateWithDefaults(ta, 1, MIN_DAY);
        return LocalDateTime.of( date, LocalTime.MIN );
    }

    public static LocalDateTime dateCeiling(TemporalAccessor ta) {
        LocalDate date = getDateWithDefaults(ta, 12, MAX_DAY);
        return LocalDateTime.of( date, LocalTime.MAX );
    }

    protected static LocalDate getDateWithDefaults(TemporalAccessor ta, int defaultMonth, int defaultDay) {
        ChronoUnit precision = (ChronoUnit) ta.query(TemporalQueries.precision());
        
        int year;
        int month;
        int day;
        
        switch (precision) {
        case DAYS:
            day = ta.get(ChronoField.DAY_OF_MONTH);
            month = ta.get(ChronoField.MONTH_OF_YEAR);
            year = ta.get(ChronoField.YEAR);
            break;
        case MONTHS:
            year = ta.get(ChronoField.YEAR);
            month = ta.get(ChronoField.MONTH_OF_YEAR);;
            day = (defaultDay == MAX_DAY) ? lastDayOfMonth(year, month) : defaultDay;
            break;            
        case YEARS:
            year = ta.get(ChronoField.YEAR);
            month = defaultMonth;
            day = (defaultDay == MAX_DAY) ? lastDayOfMonth(year, month) : defaultDay;
            break;
        default:
            throw new IllegalArgumentException("Unexpected date precision " + precision.name());
        }
        
        return LocalDate.of(year, month, day);
    }
    
    public static int lastDayOfMonth(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return ym.lengthOfMonth();
    }
    
    public static BaseTemporal toCqlTemporal(TemporalAccessor ta) {
        BaseTemporal result = null;
        
        if( ta instanceof ZonedDateTime ) {
            ZonedDateTime zdt = (ZonedDateTime) ta;
            OffsetDateTime odt = OffsetDateTime.from(zdt);
            result = new org.opencds.cqf.cql.engine.runtime.DateTime(odt);
        } else if( ta instanceof OffsetDateTime ) {
            OffsetDateTime odt = (OffsetDateTime) ta;
            result = new org.opencds.cqf.cql.engine.runtime.DateTime(odt);
        } else if( ta instanceof LocalTime ) {
            LocalTime lt = (LocalTime) ta;
            result = new org.opencds.cqf.cql.engine.runtime.Time(lt.getHour(), lt.getMinute(), lt.getSecond(), lt.getNano());
        } else {
            TemporalUnit precision = ta.query(TemporalQueries.precision());
            if (precision.equals(ChronoUnit.YEARS)) {
                result = new org.opencds.cqf.cql.engine.runtime.Date(ta.get(ChronoField.YEAR));
            } else if (precision.equals(ChronoUnit.MONTHS)) {
                result = new org.opencds.cqf.cql.engine.runtime.Date(ta.get(ChronoField.YEAR), ta.get(ChronoField.MONTH_OF_YEAR));
            } else if (precision.equals(ChronoUnit.DAYS)) {
                result = new org.opencds.cqf.cql.engine.runtime.Date(ta.get(ChronoField.YEAR), ta.get(ChronoField.MONTH_OF_YEAR), ta.get(ChronoField.DAY_OF_MONTH));
            } else {
                throw new InvalidPrecision(String.format("Invalid temporal precision %s", precision.toString()));
            }
        }
        return result;
    }
}
