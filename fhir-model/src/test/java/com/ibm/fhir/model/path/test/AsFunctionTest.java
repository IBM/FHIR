/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.model.path.test;

import static com.ibm.fhir.model.type.String.string;
import static org.testng.Assert.assertEquals;

import java.util.Collection;

import org.testng.annotations.Test;

import com.ibm.fhir.model.path.FHIRPathNode;
import com.ibm.fhir.model.path.evaluator.FHIRPathEvaluator;
import com.ibm.fhir.model.resource.Condition;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.type.Boolean;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Reference;

public class AsFunctionTest {
    @Test
    void testAsOperation() throws Exception {
        Patient patient = Patient.builder()
                                 .deceased(Boolean.TRUE)
                                 .build();

        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(patient, "Patient.deceased as dateTime");

        assertEquals(result.size(), 0, "Number of selected nodes");
    }
    
    @Test
    void testResolveAsOperation() throws Exception {
        Patient patient = Patient.builder()
                .generalPractitioner(Reference.builder().reference(string("http://example.com/dummyReference")).build())
                .build();

        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(patient, "Patient.generalPractitioner.resolve() as Basic");

        assertEquals(result.size(), 1, "Number of selected nodes");
    }

    @Test
    void testAsFunction() throws Exception {
        Condition condition = Condition.builder()
                                       .subject(Reference.builder().display(string("dummy reference")).build())
                                       .onset(DateTime.now())
                                       .build();

        FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
        Collection<FHIRPathNode> result = evaluator.evaluate(condition, "Condition.onset.as(Age) | Condition.onset.as(Range)");

        assertEquals(result.size(), 0, "Number of selected nodes");
    }
}
