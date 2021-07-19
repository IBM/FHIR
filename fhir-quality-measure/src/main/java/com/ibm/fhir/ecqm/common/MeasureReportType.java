/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.ecqm.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of the standard FHIR MeasureReport types.
 */
public enum MeasureReportType {
    INDIVIDUAL("individual", "Individual","An individual report that provides information on the performance for a given measure with respect to a single patient"), 
    PATIENTLIST("patient-list", "Patient List","A patient list report that includes a listing of patients that satisfied each population criteria in the measure"), 
    SUBJECTLIST("subject-list", "Subject List","A subject list report that includes a listing of subjects that satisfied each population criteria in the measure."),
    SUMMARY("summary", "Summary","A summary report that returns the number of patients in each population criteria for the measure"),
    DATACOLLECTION("data-collection", "Data Collection", "A data collection report that contains data-of-interest for the measure.");

    private String code;
    private String display;
    private String definition;

    MeasureReportType(String code, String display, String definition) {
        this.code = code;
        this.display = display;
        this.definition = definition;
    }

    private static final Map<String, MeasureReportType> lookup = new HashMap<>();

    static {
        for (MeasureReportType mpt : MeasureReportType.values()) {
            lookup.put(mpt.toCode(), mpt);
        }
    }

    // This method can be used for reverse lookup purpose
    public static MeasureReportType fromCode(String code) {
        if (code != null && !code.isEmpty()) {
            if (lookup.containsKey(code)) {
                return lookup.get(code);
            }
        }

        return null;
    }

    public String getSystem() {
        return "http://hl7.org/fhir/measure-report-type";
    }

    public String toCode() {
        return this.code;
    }

    public String getDisplay() {
        return this.display;
    }

    public String getDefinition() {
        return this.definition;
    }
}