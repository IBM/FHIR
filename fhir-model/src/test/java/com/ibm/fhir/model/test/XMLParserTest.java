/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.test;

import java.io.StringReader;

import org.testng.annotations.Test;

import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Practitioner;

public class XMLParserTest {
    @Test
    public void testXMLParser1() throws Exception {
        // FHIR elements are in default namespace (no prefix), XHTML elements are in default namespace (no prefix)
        StringReader reader = new StringReader("<Practitioner xmlns=\"http://hl7.org/fhir\"><id value=\"0\"/><text><status value=\"generated\"/><div xmlns=\"http://www.w3.org/1999/xhtml\">Narrative TBD</div></text></Practitioner>");
        Practitioner practitioner = FHIRParser.parser(Format.XML).parse(reader);
        FHIRGenerator.generator(Format.XML, true).generate(practitioner, System.out);
    }

    @Test
    public void testXMLParser2() throws Exception {
        // FHIR elements are prefixed, XHTML elements are prefixed, XHTML namespace is declared on root element
        StringReader reader = new StringReader("<f:Practitioner xmlns:f=\"http://hl7.org/fhir\" xmlns:h=\"http://www.w3.org/1999/xhtml\"><f:id value=\"0\"/><f:text><f:status value=\"generated\"/><h:div>Narrative TBD</h:div></f:text></f:Practitioner>");
        Practitioner practitioner = FHIRParser.parser(Format.XML).parse(reader);
        FHIRGenerator.generator(Format.XML, true).generate(practitioner, System.out);
    }

    @Test
    public void testXMLParser3() throws Exception {
        // FHIR elements are prefixed, XHTML elements are prefixed, XHTML namespace is declared on "div" element
        StringReader reader = new StringReader("<f:Practitioner xmlns:f=\"http://hl7.org/fhir\"><f:id value=\"0\"/><f:text><f:status value=\"generated\"/><h:div xmlns:h=\"http://www.w3.org/1999/xhtml\">Narrative TBD</h:div></f:text></f:Practitioner>");
        Practitioner practitioner = FHIRParser.parser(Format.XML).parse(reader);
        FHIRGenerator.generator(Format.XML, true).generate(practitioner, System.out);
    }
}
