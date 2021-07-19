/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.helpers.ModelHelper.concept;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.opencds.cqf.cql.engine.runtime.Tuple;

import com.ibm.fhir.cql.engine.converter.FHIRTypeConverter;
import com.ibm.fhir.cql.engine.converter.impl.FHIRTypeConverterImpl;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Patient;

public class ParameterConverterTest {

    ParameterConverter converter;

    @BeforeMethod
    public void setup() {
        FHIRTypeConverter typeConverter = new FHIRTypeConverterImpl();
        converter = new ParameterConverter(typeConverter);
    }

    @Test
    public void testParameterConversion() throws Exception {

        Patient p = TestHelper.john_doe().build();

        LinkedHashMap<String, Object> tupleParts = new LinkedHashMap<>();
        tupleParts.put("Part1", "I am a String");
        tupleParts.put("Part2", p);

        List<?> resources = Arrays.asList(p, p, p);

        Map<String, Object> input = new HashMap<>();
        input.put("SystemString", "I am SystemString");
        input.put("FHIRElement", fhirstring("I am FHIRElement"));
        input.put("Resources", resources);
        input.put("Tuple", new Tuple().withElements(tupleParts));
        input.put("EmptyList", Collections.emptyList());
        input.put("NullValue", null);

        Parameter result = converter.toParameter("return", input);
        assertNotNull(result, "Null result");
        System.out.println(result.toString());

        ParameterMap indexByName = new ParameterMap(result.getPart());
        assertEquals(indexByName.size(), input.size() + resources.size() - 1);

        List<Parameter> params = indexByName.getRequiredParameter("Resources");
        assertEquals(resources.size(), params.size());
        params.forEach(part -> assertNotNull(part.getResource()));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParameterConversionBackboneElement() throws Exception {
        Patient.Communication c = Patient.Communication.builder().language(concept("urn:ietf:bcp:47", "en")).build();

        converter.toParameter(c);
        fail("Unexpected success");
    }
}
