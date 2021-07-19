/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.server.test.cpg;

import static com.ibm.fhir.model.type.String.string;
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
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.model.type.code.IssueType;

import jakarta.json.JsonObject;

public class ServerLibraryEvaluateOperationTest extends BaseCPGOperationTest {
    private static final String TEST_PATIENT_ID = "Patient/sally-fields";

    @BeforeClass
    public void setup() throws Exception {
        Properties testProperties = TestUtil.readTestProperties("test.properties");
        setUp(testProperties);
        
        JsonObject jsonObject = TestUtil.readJsonObject("testdata/Patient_SallyFields.json");
        Entity<JsonObject> entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = getWebTarget().path("/" + TEST_PATIENT_ID).request().put( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
        
        jsonObject = TestUtil.readJsonObject("testdata/Library-CompileFailure.json");
        entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = getWebTarget().path("/Library/CompileFailure").request().put( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
        
        jsonObject = TestUtil.readJsonObject("testdata/Bundle-EXM74-10.2.000.json");
        entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = getWebTarget().request().post( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
    }
    
    @Test
    public void testLibraryEvaluateGet() {
        Response response = getWebTarget().path("/Library/$evaluate").queryParam("library", "http://ibm.com/health/Library/EXM74|10.2.000").queryParam("subject", TEST_PATIENT_ID).request().get();
        assertResponse( response, 200 );
        
        Parameters parameters = response.readEntity(Parameters.class);
        assertNotNull(parameters.getParameter(), "Null parameters list");
        assertEquals(parameters.getParameter().size(), 1);
        
        Parameter pReturn = parameters.getParameter().get(0);
        assertEquals(pReturn.getName().getValue(), "return");
    }
    
    @Test
    public void testLibraryEvaluatePost() {
        Parameters cqlParams = Parameters.builder()
                .parameter(Parameter.builder().name(string("Measurement Period")).value(Period.builder().start(DateTime.of("2020-06-27")).end(DateTime.of("2021-06-27")).build()).build())
                .build();
        
        Parameters inParams = Parameters.builder()
                .parameter(Parameter.builder().name(string("library")).value(Canonical.of("http://ibm.com/health/Library/EXM74|10.2.000")).build())
                .parameter(Parameter.builder().name(string("subject")).value(string(TEST_PATIENT_ID)).build())
                .parameter(Parameter.builder().name(string("expression")).value(string("Qualifying Encounters")).build())
                .parameter(Parameter.builder().name(string("parameters")).resource(cqlParams).build())
                .build();
        
        Response response = getWebTarget()
                .path("/Library/$evaluate")
                .request()
                .post(Entity.json(inParams));
        
        assertResponse( response, 200 );
        
        Parameters parameters = response.readEntity(Parameters.class);
        assertNotNull(parameters.getParameter(), "Null parameters list");
        assertEquals(parameters.getParameter().size(), 1);
        
        Parameter pReturn = parameters.getParameter().get(0);
        assertEquals(pReturn.getName().getValue(), "return");
        assertEquals(pReturn.getPart().size(), 1);
        assertEquals(pReturn.getPart().get(0).getName().getValue(), "Qualifying Encounters");
        assertEquals(((com.ibm.fhir.model.type.String)pReturn.getPart().get(0).getValue()).getValue(), "[]");
    }
    
    @Test
    public void testLibraryEvaluateCompileError() {
        Response response = getWebTarget().path("/Library/$evaluate").queryParam("library", "http://ibm.com/fhir/Library/CompileFailure|1.0.0").queryParam("subject", TEST_PATIENT_ID).request().get();
        assertResponse( response, 400 );
        
        OperationOutcome outcome = response.readEntity(OperationOutcome.class);
        System.out.println(outcome.toString());
        
        assertEquals( outcome.getIssue().get(0).getCode(), IssueType.INVALID);
    }
    
    @Test
    public void testLibraryEvaluateWithDebug() {
        Response response = getWebTarget().path("/Library/$evaluate").queryParam("library", "http://ibm.com/health/Library/EXM74|10.2.000").queryParam("subject", TEST_PATIENT_ID).queryParam("debug", "true").request().get();
        assertResponse( response, 200 );
        
        Parameters parameters = response.readEntity(Parameters.class);
        assertNotNull(parameters.getParameter(), "Null parameters list");
        assertEquals(parameters.getParameter().size(), 2);
    }
}
