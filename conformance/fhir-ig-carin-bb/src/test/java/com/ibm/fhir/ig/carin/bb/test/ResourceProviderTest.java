/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.ig.carin.bb.test;

import static org.junit.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.ibm.fhir.ig.carin.bb.BBResourceProvider;
import com.ibm.fhir.registry.resource.FHIRRegistryResource;
import com.ibm.fhir.registry.spi.FHIRRegistryResourceProvider;

public class ResourceProviderTest {
    @Test
    public void testResourceProvider() {
        FHIRRegistryResourceProvider provider = new BBResourceProvider();
        Collection<FHIRRegistryResource> registryResources = provider.getRegistryResources();
        assertNotNull(registryResources);
        assertTrue(!registryResources.isEmpty());
        for (FHIRRegistryResource fhirRegistryResource : registryResources) {
            assertNotNull(fhirRegistryResource.getResource());
        }
    }
}
