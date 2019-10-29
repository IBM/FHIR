/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.model.resource.Observation;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.test.TestUtil;
import com.ibm.fhir.notification.FHIRNotificationEvent;

/**
 * 
 * The following tests are intentionally marked as singleThreaded. The singleThreaded property addresses an issue where
 * the session does not start-connect-receive a message.
 *
 */
public class WebSocketNotificationsTest extends FHIRServerTestBase {

    // Add -DskipWebSocketTest
    public static final String SKIP_TESTS = "skipWebSocketTest";

    private boolean skip = false;

    private Patient savedCreatedPatient;
    private Observation savedCreatedObservation;

    private FHIRNotificationServiceClientEndpoint endpoint = null;
    private WebTarget target = null;

    @BeforeClass
    public void startup() throws InterruptedException {
        
        // A specific CI pipeline issue triggered adding this value
        // as such this a conditional ignore. 
        // -DskipWebSocketTest=true
        String shouldSkip = System.getProperty(SKIP_TESTS, "false");
        if (shouldSkip.compareTo("true") == 0) {
            skip = true;
        } else { 
            target = getWebTarget();
            endpoint = getWebsocketClientEndpoint();
            assertNotNull(endpoint);
        }
    }

    @AfterClass
    public void shutdown() {
        if(!skip) {
            endpoint.close();
        }
    }

    public FHIRNotificationEvent getEvent(String id) throws InterruptedException {
        FHIRNotificationEvent event = null;
        int checkCount = 30;
        while (event == null && checkCount > 0) {
            // Only if null, we're going to wait.
            endpoint.getLatch().await(1, TimeUnit.SECONDS);
            event = endpoint.checkForEvent(id);
            checkCount--;
        }
        assertNotNull(event);
        return event;
    }

    /**
     * Create a Patient, then make sure we can retrieve it.
     */
    @Test(groups = { "websocket-notifications" })
    public void testCreatePatient() throws Exception {

        if (skip) {
            System.out.println("skipping this test ");
        } else {

            // Build a new Patient and then call the 'create' API.
            Patient patient = TestUtil.readLocalResource("Patient_JohnDoe.json");
            Entity<Patient> entity = Entity.entity(patient, FHIRMediaType.APPLICATION_FHIR_JSON);
            Response response = target.path("Patient").request().post(entity, Response.class);
            assertResponse(response, Response.Status.CREATED.getStatusCode());

            // Get the patient's logical id value.
            String patientId = getLocationLogicalId(response);
            System.out.println(">>> [CREATE] Patient Resource -> Id: " + patientId);

            // Next, call the 'read' API to retrieve the new patient and verify it.
            response = target.path("Patient/"
                    + patientId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
            assertResponse(response, Response.Status.OK.getStatusCode());

            Patient responsePatient = response.readEntity(Patient.class);
            savedCreatedPatient = responsePatient;

            FHIRNotificationEvent event = getEvent(responsePatient.getId().getValue());
            assertEquals(event.getResourceId(), responsePatient.getId().getValue());
            TestUtil.assertResourceEquals(patient, responsePatient);
        }

    }

    /**
     * Create an Observation and make sure we can retrieve it.
     */
    @Test(groups = { "websocket-notifications" }, dependsOnMethods = { "testCreatePatient" })
    public void testCreateObservation() throws Exception {
        if (skip) {
            System.out.println("skipping this test ");
        } else {
            // Next, create an Observation belonging to the new patient.
            String patientId = savedCreatedPatient.getId().getValue();
            Observation observation = TestUtil.buildPatientObservation(patientId, "Observation1.json");
            Entity<Observation> obs =
                    Entity.entity(observation, FHIRMediaType.APPLICATION_FHIR_JSON);
            Response response = target.path("Observation").request().post(obs, Response.class);
            assertResponse(response, Response.Status.CREATED.getStatusCode());

            String observationId = getLocationLogicalId(response);

            // Next, retrieve the new Observation with a read operation and verify it.
            response = target.path("Observation/"
                    + observationId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
            assertResponse(response, Response.Status.OK.getStatusCode());

            Observation responseObs = response.readEntity(Observation.class);
            savedCreatedObservation = responseObs;

            FHIRNotificationEvent event = getEvent(savedCreatedObservation.getId().getValue());

            assertEquals(event.getResourceId(), responseObs.getId().getValue());
            TestUtil.assertResourceEquals(observation, responseObs);
        }
    }

    /**
     * Tests the update of the original observation that was previously created.
     */
    @Test(groups = { "websocket-notifications" }, dependsOnMethods = {
            "testCreateObservation" }, singleThreaded = true)
    public void testUpdateObservation() throws Exception {
        if (skip) {
            System.out.println("skipping this test ");
        } else {
            // Create an updated Observation based on the original saved observation
            String patientId = savedCreatedPatient.getId().getValue();
            Observation observation = TestUtil.buildPatientObservation(patientId, "Observation2.json");
            observation = observation.toBuilder().id(savedCreatedObservation.getId()).build();
            Entity<Observation> obs =
                    Entity.entity(observation, FHIRMediaType.APPLICATION_FHIR_JSON);

            // Call the 'update' API.
            String targetPath = "Observation/" + observation.getId().getValue();
            Response response = target.path(targetPath).request().put(obs, Response.class);
            assertResponse(response, Response.Status.OK.getStatusCode());
            String observationId = getLocationLogicalId(response);

            // Next, call the 'read' API to retrieve the updated observation and verify it.
            response = target.path("Observation/"
                    + observationId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
            assertResponse(response, Response.Status.OK.getStatusCode());

            Observation responseObservation = response.readEntity(Observation.class);

            FHIRNotificationEvent event = getEvent(responseObservation.getId().getValue());

            assertEquals(event.getResourceId(), responseObservation.getId().getValue());
            assertNotNull(responseObservation);
            TestUtil.assertResourceEquals(observation, responseObservation);
        }
    }
}
