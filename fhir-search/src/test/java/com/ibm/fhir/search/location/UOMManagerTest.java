/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.search.location;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.ibm.fhir.search.location.uom.UOMManager;
import com.ibm.fhir.search.location.uom.standard.ImperialUnits;
import com.ibm.fhir.search.location.uom.standard.MetricUnits;
import com.ibm.fhir.search.location.uom.standard.StatuteLengthUnits;

public class UOMManagerTest {

    @Test
    public void testLocationUnitFoundUS() {
        assertEquals(UOMManager.getUnitToMetersFactor("mi_us"), 1609.344);
    }

    @Test
    public void testLocationUnitFoundBR() {
        assertEquals(UOMManager.getUnitToMetersFactor("mi_br"), 1609.3427328000002);
    }

    @Test
    public void testLocationUnitFoundBRWithBrackets() {
        assertEquals(UOMManager.getUnitToMetersFactor("[mi_br]"), 1609.3427328000002);
    }

    @Test
    public void testLocationUnitFoundVariants() {
        assertEquals(UOMManager.getUnitToMetersFactor("fts"), UOMManager.getUnitToMetersFactor("[FT_US]"));
    }

    @Test
    public void testLocationUnitNotFound() {
        assertNull(UOMManager.getUnitToMetersFactor("zzzzzzz"));
    }

    @Test
    public void testLocations() {
        assertEquals(UOMManager.getUnitToMetersFactor("km"), 1000.0);
    }

    @Test
    public void testImperialUnits() {
        assertEquals(ImperialUnits.fromValue("ft_br"), ImperialUnits.FOOT);
    }

    @Test
    public void testStatuteLengthUnits() {
        assertEquals(StatuteLengthUnits.fromValue("ft_us"), StatuteLengthUnits.FOOT);
    }

    @Test
    public void testStatuteLengthUnitsFactor() {
        assertEquals(StatuteLengthUnits.FOOT.factor(), 1.0);
    }

    @Test
    public void testImperialUnitsFactor() {
        assertEquals(ImperialUnits.MILE.factor(), 1609.3427328000002);
    }

    @Test
    public void testMetricUnitsFactor() {
        assertEquals(MetricUnits.ATTOMETRE.factor(), -18.0);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testStatuteLengthUnitsExceptions() {
        assertEquals(StatuteLengthUnits.fromValue("funny"), ImperialUnits.FOOT);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testImperialUnitsExceptions() {
        assertEquals(ImperialUnits.fromValue("funny"), ImperialUnits.FOOT);
    }

    @Test
    public void testMetricUnits() {
        assertEquals(MetricUnits.fromValue("Mm"), MetricUnits.MEGAMETRE);
    }

    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testMetricUnitsExceptions() {
        assertEquals(MetricUnits.fromValue("FRED"), MetricUnits.MEGAMETRE);
    }

    @Test
    public void testConvertUnitToValue() {
        assertEquals(UOMManager.convertUnitsToMeters("km", 1.0), 1000.0);
    }

    @Test
    public void testConvertUnitToValueInvalidUnit() {
        assertNull(UOMManager.convertUnitsToMeters("failed", 1.0));
    }

    @Test
    public void testConvertUnitToKiloValue() {
        assertEquals(UOMManager.convertUnitsToKiloMeters("km", 1.0), 1.0);
        assertEquals(UOMManager.convertUnitsToKiloMeters("m", 1.0), 0.001);
    }

    @Test
    public void testConvertUnitToKiloValueInvalidUnit() {
        assertNull(UOMManager.convertUnitsToKiloMeters("failed", 1.0));
    }
}