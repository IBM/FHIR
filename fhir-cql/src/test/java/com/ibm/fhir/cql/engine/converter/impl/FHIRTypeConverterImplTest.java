/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.converter.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ibm.fhir.cql.engine.converter.FHIRTypeConverter;
import com.ibm.fhir.model.type.DateTime;

public class FHIRTypeConverterImplTest {
    
    FHIRTypeConverter typeConverter;
    
    @BeforeMethod
    public void setup() {
        typeConverter = new FHIRTypeConverterImpl();
    }
    
    @Test
    public void testIntegerIsCqlType() {
        assertTrue( typeConverter.isCqlType(Integer.valueOf(100)) );
    }
    
    @Test
    public void testStringIsCqlType() {
        assertTrue( typeConverter.isCqlType("Hello,World!") );
    }
    
    @Test
    public void testConvertDateTimeWithLocalDateValue() {
        LocalDate expected = LocalDate.of(2020,06,27);
        DateTime fhirDateTime = DateTime.of( expected );
        org.opencds.cqf.cql.engine.runtime.DateTime cqlDateTime = typeConverter.toCqlDateTime(fhirDateTime);
        assertNotNull(cqlDateTime);
        assertEquals( cqlDateTime.getDateTime().toLocalDate(), expected );
    }
}
