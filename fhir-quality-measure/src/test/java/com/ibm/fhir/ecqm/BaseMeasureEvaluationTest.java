/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.ecqm;

import java.io.ByteArrayInputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.cqframework.cql.elm.execution.Library;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.cql.translator.CqlTranslationProvider;
import com.ibm.fhir.cql.translator.impl.InJVMCqlTranslationProvider;

public class BaseMeasureEvaluationTest {
    protected static final String OMB_CATEGORY_RACE_BLACK = "2054-5";
    protected static final String BLACK_OR_AFRICAN_AMERICAN = "Black or African American";
    protected static final String URL_SYSTEM_RACE = "urn:oid:2.16.840.1.113883.6.238";
    protected static final String OMB_CATEGORY = "ombCategory";
    protected static final String EXT_URL_US_CORE_RACE = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race";

    protected List<Library> translate(String cql) throws Exception {
        CqlTranslationProvider translator = new InJVMCqlTranslationProvider();
        return translator.translate(new ByteArrayInputStream(cql.getBytes()));
    }

    protected Interval measurementPeriod(String periodStart, String periodEnd) {
        ZoneOffset offset = ZonedDateTime.now().getOffset();
        
        DateTime start = new DateTime(periodStart, offset);
        DateTime end = new DateTime(periodEnd, offset);
        
        return new Interval( start, true, end, true );
    }

    public String skeleton_cql() {
        return String.format("library Test version '1.0.0'\n\nusing FHIR version '%1$s'\ninclude FHIRHelpers version '%1$s'\n\ncontext Patient\n", getFhirVersion());
    }
    
    public String getFhirVersion() {
        return Constants.FHIR_VERSION;
    }

    protected String sde_race() {
        return "define \"SDE Race\":\n" + 
                "  (flatten (\n" + 
                "    Patient.extension Extension\n" + 
                "      where Extension.url = 'http://hl7.org/fhir/us/core/StructureDefinition/us-core-race'\n" + 
                "        return Extension.extension\n" + 
                "  )) E\n" + 
                "    where E.url = 'ombCategory'\n" + 
                "      or E.url = 'detailed'\n" + 
                "    return E.value as Coding\n\n";
    }
}
