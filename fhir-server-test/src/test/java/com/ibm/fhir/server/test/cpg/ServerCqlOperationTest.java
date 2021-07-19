/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.server.test.cpg;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.test.TestUtil;
import com.ibm.fhir.model.type.code.IssueType;

import jakarta.json.JsonObject;

public class ServerCqlOperationTest extends BaseCPGOperationTest {

    private static final String TEST_PATIENT_ID = "Patient/sally-fields";

    @BeforeClass
    public void setup() throws Exception {
        Properties testProperties = TestUtil.readTestProperties("test.properties");
        setUp(testProperties);
        
        JsonObject jsonObject = TestUtil.readJsonObject("testdata/Patient_SallyFields.json");
        Entity<JsonObject> entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = getWebTarget().path("/" + TEST_PATIENT_ID).request().put( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
    }
    
    @Test
    public void testEvaluateArbitraryCql() {
        Response response = getWebTarget().path("$cql").queryParam("expression", "Patient.gender").queryParam("subject", TEST_PATIENT_ID).request().get();
        assertResponse( response, 200 );
        
        Parameters parameters = response.readEntity(Parameters.class);
        assertNotNull(parameters.getParameter(), "Null parameters list");
        assertEquals(parameters.getParameter().size(), 1);
        
        Parameter pReturn = parameters.getParameter().get(0);
        assertEquals(pReturn.getName().getValue(), "return");
    }
    
    @Test
    public void testEvaluateArbitraryCqlCompileError() {
        Response response = getWebTarget().path("$cql").queryParam("expression", "[NonResource]").queryParam("subject", TEST_PATIENT_ID).request().get();
        assertResponse( response, 400 );
        
        OperationOutcome outcome = response.readEntity(OperationOutcome.class);
        System.out.println(outcome.toString());
        
        assertEquals( outcome.getIssue().get(0).getCode(), IssueType.INVALID);
    }
    
    @Test
    public void testEvaluateArbitraryCqlWithDebug() {
        Response response = getWebTarget().path("$cql").queryParam("expression", "Patient.gender").queryParam("subject", TEST_PATIENT_ID).queryParam("debug", "true").request().get();
        assertResponse( response, 200 );
        
        Parameters parameters = response.readEntity(Parameters.class);
        assertNotNull(parameters.getParameter(), "Null parameters list");
        assertEquals(parameters.getParameter().size(), 2);
    }
}
