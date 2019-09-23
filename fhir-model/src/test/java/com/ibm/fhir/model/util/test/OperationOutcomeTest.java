/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.util.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.type.IssueSeverity;
import com.ibm.fhir.model.type.IssueType;
import com.ibm.fhir.model.util.FHIRUtil;

/**
 * @author rarnold
 *
 */
public class OperationOutcomeTest {

    @Test
    public void buildOperationOutcomeIssue() {
        OperationOutcome.Issue issue = FHIRUtil.buildOperationOutcomeIssue("test", IssueType.ValueSet.INVALID);
        assertNotNull(issue);
        assertEquals(issue.getSeverity(), IssueSeverity.FATAL);
        assertEquals(issue.getCode(), IssueType.INVALID);
        assertEquals(issue.getDetails().getText().getValue(), "test");
    }
}
