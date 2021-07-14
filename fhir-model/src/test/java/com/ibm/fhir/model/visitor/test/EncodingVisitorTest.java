/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.model.visitor.test;

import static com.ibm.fhir.model.type.String.string;
import static org.testng.Assert.assertFalse;

import java.io.StringWriter;
import java.util.Collections;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.test.TestUtil;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.Narrative;
import com.ibm.fhir.model.type.Xhtml;
import com.ibm.fhir.model.type.code.NarrativeStatus;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.model.visitor.EncodingVisitor;
import com.ibm.fhir.model.visitor.EncodingVisitor.EncodingContext;

public class EncodingVisitorTest {
    private static OperationOutcome oo;

    @BeforeClass
    public static void setup() throws Exception {
        oo = TestUtil.getMinimalResource(ResourceType.OPERATION_OUTCOME);
        Issue issue = oo.getIssue().get(0).toBuilder()
                .extension(Extension.builder()
                    .id("\" onload=\"alert('test1')")
                    .url("http://example.com/evil")
                    .value(string("<script>alert('powned')</script>"))
                    .build())
                .details(CodeableConcept.builder()
                    .text(string("<body onload=alert('powned2')>"))
                    .build())
                .diagnostics(string("<META HTTP-EQUIV=\"refresh\"\n"
                        + "CONTENT=\"0;url=data:text/html;base64,PHNjcmlwdD5hbGVydCgndGVzdDMnKTwvc2NyaXB0Pg\">"))
                .build();

        oo = oo.toBuilder()
                .text(Narrative.builder()
                    .id("\" onmouseover=\"alert('Wufff!')")
                    .extension(Collections.singleton(Extension.builder()
                        .url("http://example.com/evil")
                        .value(string("<script>alert('powned')</script>"))
                        .build()))
                    .status(NarrativeStatus.EMPTY)
                    .div(Xhtml.from("Narrative text is tested elsewhere"))
                    .build())
                .issue(Collections.singleton(issue))
                .build();
    }

    @Test
    public void testEscape() throws Exception {
        EncodingVisitor<OperationOutcome> v = new EncodingVisitor<>(EncodingContext.HTML_CONTENT);
        oo.accept(v);

        OperationOutcome result = v.getResult();

        StringWriter writer = new StringWriter();
        FHIRGenerator.generator(Format.JSON, true).generate(result, writer);
        System.out.println(writer);

        String resultSansXhtml = writer.toString()
                .replace("<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">", "")
                .replace("</div>", "");
        assertFalse(resultSansXhtml.contains("<"));
    }
}
