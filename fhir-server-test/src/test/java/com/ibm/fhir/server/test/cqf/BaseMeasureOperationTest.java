package com.ibm.fhir.server.test.cqf;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeClass;

import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.model.test.TestUtil;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Period;
import com.ibm.fhir.server.test.FHIRServerTestBase;

import jakarta.json.JsonObject;

public class BaseMeasureOperationTest extends FHIRServerTestBase {
    protected static final String TEST_PATIENT_ID = "Patient/sally-fields";
    protected static final String TEST_MEASURE_ID = "8-auq9X1o0TxTYLhRGlt8ISNC1w7ELS5sKyhmUTj4SE";
    protected static final String TEST_MEASURE_URL = "http://ibm.com/health/Measure/EXM74";
    protected static final String TEST_PERIOD_START = "2010-01-01";
    protected static final String TEST_PERIOD_END = "2010-12-31";

    @BeforeClass
    public void setup() throws Exception {
        Properties testProperties = TestUtil.readTestProperties("test.properties");
        setUp(testProperties);

        JsonObject jsonObject = TestUtil.readJsonObject("testdata/Patient_SallyFields.json");
        Entity<JsonObject> entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = getWebTarget().path("/" + TEST_PATIENT_ID).request().put(entity);
        assertResponse(response, Response.Status.Family.SUCCESSFUL);
        
        jsonObject = TestUtil.readJsonObject("testdata/Bundle-EXM74-10.2.000.json");
        entity = Entity.entity(jsonObject, FHIRMediaType.APPLICATION_FHIR_JSON);
        response = getWebTarget().request().post( entity );
        assertResponse( response, Response.Status.Family.SUCCESSFUL );
    }
    
    public Period getPeriod(String start, String end) {
        ZoneOffset zoneOffset = OffsetDateTime.now().getOffset();
        OffsetDateTime expectedStart = OffsetDateTime.of(LocalDate.parse(start), LocalTime.MIN, zoneOffset);
        OffsetDateTime expectedEnd = OffsetDateTime.of(LocalDate.parse(end), LocalTime.MAX, zoneOffset);
        Period expectedPeriod = Period.builder().start(DateTime.of(expectedStart.toZonedDateTime())).end(DateTime.of(expectedEnd.toZonedDateTime())).build();
        return expectedPeriod;
    }
}
