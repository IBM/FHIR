/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.persistence.SingleResourceResult;

public class TestHelper {
    
    public static Resource getTestResource(String path) throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if( is != null ) {
                return FHIRParser.parser(Format.JSON).parse(is);
            } else {
                throw new IllegalArgumentException("No resource found with at path " + path);
            }
        }
    }
    
    public static <T> List<T> getBundleResources(String bundlePath, Class<T> clazz) throws Exception {
        try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(bundlePath)) {
            final Bundle bundle = (Bundle) FHIRParser.parser(Format.JSON).parse(is);

            return getBundleResources( bundle, clazz );
        }
    }
    
    public static <T> List<T> getBundleResources(Bundle bundle, Class<T> clazz) {
        return bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> clazz.isInstance(r)).map(r -> clazz.cast(r) ).collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    public static SingleResourceResult<? extends Resource> asResult(Resource patient) {
        SingleResourceResult<Resource> result = (SingleResourceResult<Resource>) mock(SingleResourceResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.getResource()).thenReturn(patient);
        return result;
    }
}
