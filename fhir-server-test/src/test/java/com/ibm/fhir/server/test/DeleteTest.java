/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.test;

import static com.ibm.fhir.model.type.String.string;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ibm.fhir.client.FHIRParameters;
import com.ibm.fhir.client.FHIRResponse;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.MedicationAdministration;
import com.ibm.fhir.model.resource.Observation;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.HTTPVerb;
import com.ibm.fhir.model.type.Id;
import com.ibm.fhir.model.type.Reference;

/**
 * This class tests delete interactions.
 *
 */
public class DeleteTest extends FHIRServerTestBase {

    private String deletedId = null;
    private String deletedType = null;
    private MedicationAdministration deletedResource = null;
    private List<String> patientIds = null;
    private String uniqueFamilyName = null;
    private boolean deleteSupported = false;

    /**
     * Retrieve the server's conformance statement to determine the status of certain runtime options.
     * 
     * @throws Exception
     */
    @BeforeClass
    public void retrieveConfig() throws Exception {
        deleteSupported = isDeleteSupported();
        System.out.println("Delete operation supported?: " + Boolean.valueOf(deleteSupported).toString());
    }

    @Test
    public void testCreateNewResource() throws Exception {
        // Create a MedicationAdministration resource.
        MedicationAdministration ma = readResource(MedicationAdministration.class, "MedicationAdministration.json");
        FHIRResponse response = client.create(ma);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.CREATED.getStatusCode());

        // Obtain the resource type and id from the location string.
        String[] locationTokens = response.parseLocation(response.getLocation());
        deletedType = locationTokens[0];
        deletedId = locationTokens[1];

        // Make sure we can read back the resource.
        response = client.read(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        deletedResource = response.getResource(MedicationAdministration.class);
        assertNotNull(deletedResource);
    }

    @Test(dependsOnMethods = {
            "testCreateNewResource"
    })
    public void testDeleteNewResource() throws Exception {
        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.delete(deletedType, deletedId);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());
            assertNotNull(response.getETag());
            assertEquals("W/\"2\"", response.getETag());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }

    @Test(dependsOnMethods = {
            "testDeleteNewResource"
    })
    public void testReadDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.read(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.GONE.getStatusCode());
    }

    @Test(dependsOnMethods = {
            "testReadDeletedResource"
    })
    public void testVreadDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.vread(deletedType, deletedId, "2");
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.GONE.getStatusCode());
    }

    @Test(dependsOnMethods = {
            "testVreadDeletedResource"
    })
    public void testDeleteDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.delete(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());
        assertNotNull(response.getETag());
        assertEquals("W/\"2\"", response.getETag());
    }

    /**
     * Ensure we get back a 404 Not Found for deleting a resource with an invalid id
     * @throws Exception
     */
    @Test()
    public void testDeleteInvalidResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        FHIRResponse response = client.delete(MedicationAdministration.class.getSimpleName(), "invalid-resource-id-testDeleteInvalidResource");
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.NOT_FOUND.getStatusCode());
        // 404 doesn't have an etag in the response
    }


    @Test(dependsOnMethods = {
            "testDeleteDeletedResource"
    })
    public void testHistory1() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.history(deletedType, deletedId, null);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertNotNull(bundle.getEntry());
        assertEquals(2, bundle.getEntry().size());
        assertBundleEntry(bundle.getEntry().get(0), deletedType + "/" + deletedId, "2", HTTPVerb.ValueSet.DELETE);
        assertBundleEntry(bundle.getEntry().get(1), deletedType, "1", HTTPVerb.ValueSet.POST);
    }

    private void assertBundleEntry(Bundle.Entry entry, String expectedURL, String expectedVersionId, HTTPVerb.ValueSet expectedMethod) throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(entry);
        Bundle.Entry.Request request = entry.getRequest();
        assertNotNull(request);
        assertEquals(expectedURL, request.getUrl().getValue());
        assertEquals(expectedMethod.toString(), request.getMethod().getValue());
        Resource rc = entry.getResource();
        assertNotNull(rc);
        MedicationAdministration ma = (MedicationAdministration) rc;
        String actualVersionId = ma.getMeta().getVersionId().getValue();
        assertEquals(expectedVersionId, actualVersionId);
    }

    @Test(dependsOnMethods = {
            "testHistory1"
    })
    public void testUndeleteDeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.update(deletedResource);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
    }

    @Test(dependsOnMethods = {
            "testUndeleteDeletedResource"
    })
    public void testReadUndeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.read(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
    }

    @Test(dependsOnMethods = {
            "testReadUndeletedResource"
    })
    public void testVreadUndeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.vread(deletedType, deletedId, "3");
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
    }

    @Test(dependsOnMethods = {
            "testVreadUndeletedResource"
    })
    public void testHistory2() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.history(deletedType, deletedId, null);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertNotNull(bundle.getEntry());
        assertEquals(3, bundle.getEntry().size());
        assertBundleEntry(bundle.getEntry().get(0), deletedType + "/" + deletedId, "3", HTTPVerb.ValueSet.PUT);
        assertBundleEntry(bundle.getEntry().get(1), deletedType + "/" + deletedId, "2", HTTPVerb.ValueSet.DELETE);
        assertBundleEntry(bundle.getEntry().get(2), deletedType, "1", HTTPVerb.ValueSet.POST);
    }

    @Test(dependsOnMethods = {
            "testHistory2"
    })
    public void testDeleteUndeletedResource() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.delete(deletedType, deletedId);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test(dependsOnMethods = {
            "testDeleteUndeletedResource"
    })
    public void testHistory3() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(deletedType);
        assertNotNull(deletedId);

        FHIRResponse response = client.history(deletedType, deletedId, null);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle bundle = response.getResource(Bundle.class);
        assertNotNull(bundle);
        assertNotNull(bundle.getEntry());
        assertEquals(4, bundle.getEntry().size());
        assertBundleEntry(bundle.getEntry().get(0), deletedType + "/" + deletedId, "4", HTTPVerb.ValueSet.DELETE);
        assertBundleEntry(bundle.getEntry().get(1), deletedType + "/" + deletedId, "3", HTTPVerb.ValueSet.PUT);
        assertBundleEntry(bundle.getEntry().get(2), deletedType + "/" + deletedId, "2", HTTPVerb.ValueSet.DELETE);
        assertBundleEntry(bundle.getEntry().get(3), deletedType, "1", HTTPVerb.ValueSet.POST);
    }

    @Test
    public void testSearch1() throws Exception {
        if (!deleteSupported) {
            return;
        }

        // Create a collection of resources then perform searches and verify that deleted
        // resources are not included in search results.
        FHIRResponse response;
        uniqueFamilyName = UUID.randomUUID().toString();
        patientIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // Read in the resource template.
            Patient patient = readResource(Patient.class, "Patient_MookieBetts.json");

            // Add the uniqueFamily name         
            patient = setUniqueFamilyName(patient, uniqueFamilyName);

            response = client.create(patient);
            assertResponse(response.getResponse(), Response.Status.CREATED.getStatusCode());
            String[] tokens = response.parseLocation(response.getLocation());
            patientIds.add(tokens[1]);
        }

        FHIRParameters parameters = new FHIRParameters().searchParam("name", uniqueFamilyName);
        response = client.search("Patient", parameters);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle searchResults = response.getResource(Bundle.class);
        assertEquals(10, searchResults.getEntry().size());
    }

    @Test(dependsOnMethods = {
            "testSearch1"
    })
    public void testSearch2() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(uniqueFamilyName);
        assertNotNull(patientIds);
        FHIRResponse response;

        // Delete a couple of resources created by the search test, then re-invoke the search.
        response = client.delete("Patient", patientIds.get(3));
        assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());

        response = client.delete("Patient", patientIds.get(7));
        assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());

        FHIRParameters parameters = new FHIRParameters().searchParam("name", uniqueFamilyName);
        response = client.search("Patient", parameters);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle searchResults = response.getResource(Bundle.class);
        assertEquals(8, searchResults.getEntry().size());
    }

    @Test(dependsOnMethods = {
            "testSearch2"
    })
    public void testSearch3() throws Exception {
        if (!deleteSupported) {
            return;
        }

        assertNotNull(uniqueFamilyName);
        assertNotNull(patientIds);
        FHIRResponse response;

        // Delete 3 more patients.
        response = client.delete("Patient", patientIds.get(2));
        assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());

        response = client.delete("Patient", patientIds.get(8));
        assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());

        response = client.delete("Patient", patientIds.get(9));
        assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());

        FHIRParameters parameters = new FHIRParameters().searchParam("name", uniqueFamilyName);
        response = client.search("Patient", parameters);
        assertResponse(response.getResponse(), Response.Status.OK.getStatusCode());
        Bundle searchResults = response.getResource(Bundle.class);
        assertEquals(5, searchResults.getEntry().size());
    }

    @Test
    public void testConditionalDeleteResource() throws Exception {
        if (!deleteSupported) {
            return;
        }
        
        String fakePatientRef = "Patient/" + UUID.randomUUID().toString();
        String obsId = UUID.randomUUID().toString();
        Observation obs = readResource(Observation.class, "Observation1.json");
        
        obs = obs.toBuilder().id(Id.of(obsId)).subject(Reference.builder().reference(string(fakePatientRef)).build()).build();

        
        // First conditional delete should find no matches, so we should get back a 404.
        FHIRParameters query = new FHIRParameters().searchParam("_id", obsId);
        FHIRResponse response = client.conditionalDelete("Observation", query);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.NOT_FOUND.getStatusCode());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
        
        // Next, create an Observation (using update for create) so that we can test conditional delete.
        response = client.update(obs);
        assertNotNull(response);
        assertResponse(response.getResponse(), Response.Status.CREATED.getStatusCode());
        
        // Second conditional delete should find 1 match, so we should get back a 200.
        response = client.conditionalDelete("Observation", query);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.NO_CONTENT.getStatusCode());
            assertNotNull(response.getETag());
            assertEquals("W/\"2\"", response.getETag());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
        
        // A search that results in multiple matches should result in a 412 status code.
        FHIRParameters multipleMatches = new FHIRParameters().searchParam("status", "final");
        response = client.conditionalUpdate(obs, multipleMatches);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.PRECONDITION_FAILED.getStatusCode());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
        
        // Finally, an invalid search should result in a 400 status code.
        FHIRParameters badSearch = new FHIRParameters().searchParam("invalid:search", "foo");
        response = client.conditionalUpdate(obs, badSearch);
        assertNotNull(response);
        if (deleteSupported) {
            assertResponse(response.getResponse(), Response.Status.BAD_REQUEST.getStatusCode());
        } else {
            assertResponse(response.getResponse(), Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
        }
    }    
}
