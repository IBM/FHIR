/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.helpers;

import static org.testng.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;

import org.testng.annotations.Test;

public class DateHelperTest {
    @Test
    public void testFloorYear() {
        Temporal temporal = Year.of(2000);
        LocalDateTime expected = LocalDateTime.of( LocalDate.of(2000, 1, 1), LocalTime.MIN );
        LocalDateTime result = DateHelper.dateFloor(temporal);
        assertEquals( result, expected );
    }
    
    @Test
    public void testCeilingYear() {
        Temporal temporal = Year.of(2000);
        LocalDateTime expected = LocalDateTime.of( LocalDate.of(2000, 12, 31), LocalTime.MAX );
        LocalDateTime result = DateHelper.dateCeiling(temporal);
        assertEquals( result, expected );
    }
    
    @Test
    public void testFloorYearMonth() {
        Temporal temporal = YearMonth.of(2000, 6);
        LocalDateTime expected = LocalDateTime.of( LocalDate.of(2000, 6, 1), LocalTime.MIN );
        LocalDateTime result = DateHelper.dateFloor(temporal);
        assertEquals( result, expected );
    }
    
    @Test
    public void testCeilingYearMonth() {
        Temporal temporal = YearMonth.of(2000, 6);
        LocalDateTime expected = LocalDateTime.of( LocalDate.of(2000, 6, 30), LocalTime.MAX );        
        LocalDateTime result = DateHelper.dateCeiling(temporal);
        assertEquals( result, expected );
    }
    
    @Test
    public void testFloorYearMonthDay() {
        LocalDate temporal = LocalDate.of(2020, 2, 29);
        LocalDateTime expected = LocalDateTime.of( temporal, LocalTime.MIN );
        LocalDateTime result = DateHelper.dateFloor(temporal);
        assertEquals( result, expected );
    }
    
    @Test
    public void testCeilingYearMonthDay() {
        LocalDate temporal = LocalDate.of(2020, 2, 29);
        LocalDateTime expected = LocalDateTime.of( temporal, LocalTime.MAX );
        LocalDateTime result = DateHelper.dateCeiling(temporal);
        assertEquals( result, expected );
    }
}
