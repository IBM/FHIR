/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.helpers;

import static com.ibm.fhir.cql.helpers.ModelHelper.bundle;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhiruri;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.type.HumanName;

public class FHIRBundleCursorTest {
   @Test
   public void testSinglePage() {
        
        Patient patient = john_doe();
        
        Bundle b1 = bundle(patient, patient);
        
        FHIRBundleCursor cursor = new FHIRBundleCursor( (url) -> {
            throw new IllegalStateException("Should not reach this point");
        }, b1 );
        
        final AtomicInteger count = new AtomicInteger(0);
        cursor.forEach( x -> count.incrementAndGet() );
        assertEquals( count.get(), 2 );
    }
    
    @Test
    public void testMultiplePages() {
        
        Patient patient = john_doe();
        
        Bundle.Link link = Bundle.Link.builder()
                .relation(fhirstring("next"))
                .url(fhiruri("http://dummy.com/fhir/Something?_page=X"))
                .build();
        
        Bundle b1 = bundle(patient, patient).toBuilder().link(link).build();
        Bundle b2 = bundle(patient, patient).toBuilder().link(link).build();;
        Bundle b3 = bundle(patient);
        
        ArrayList<Bundle> bundles = new ArrayList<>();
        bundles.addAll(Arrays.asList(b2,b3));
        
        FHIRBundleCursor cursor = new FHIRBundleCursor( (url) -> {
            return bundles.remove(0);
        }, b1 );
        
        final AtomicInteger count = new AtomicInteger(0);
        cursor.forEach( x -> count.incrementAndGet() );
        assertEquals( count.get(), 5 );
    }
    
    @Test
    public void testMultiplePagesLastPageHasZero() {
        
        Patient patient = john_doe();
        
        Bundle.Link link = Bundle.Link.builder()
                .relation(fhirstring("next"))
                .url(fhiruri("http://dummy.com/fhir/Something?_page=X"))
                .build();
        
        Bundle b1 = bundle(patient, patient).toBuilder().link(link).build();
        Bundle b2 = bundle(patient, patient).toBuilder().link(link).build();;
        Bundle b3 = bundle();
        
        ArrayList<Bundle> bundles = new ArrayList<>();
        bundles.addAll(Arrays.asList(b2,b3));
        
        FHIRBundleCursor cursor = new FHIRBundleCursor( (url) -> {
            return bundles.remove(0);
        }, b1 );
        
        final AtomicInteger count = new AtomicInteger(0);
        cursor.forEach( x -> count.incrementAndGet() );
        assertEquals( count.get(), 4 );
    }

    private Patient john_doe() {
        return Patient.builder().id("123")
                .name(HumanName.builder().family(fhirstring("Doe")).given(fhirstring("John")).build())
                .build();
    }
}
