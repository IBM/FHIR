/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.test;

import static com.ibm.fhir.model.type.String.string;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.tyrus.client.SslContextConfigurator;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.testng.annotations.BeforeClass;

import com.ibm.fhir.client.FHIRClient;
import com.ibm.fhir.client.FHIRClientFactory;
import com.ibm.fhir.client.FHIRResponse;
import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.core.FHIRUtilities;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.CapabilityStatement;
import com.ibm.fhir.model.resource.CapabilityStatement.Rest;
import com.ibm.fhir.model.resource.CapabilityStatement.Rest.Resource.Interaction;
import com.ibm.fhir.model.resource.Condition;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.test.TestUtil;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.HumanName;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.ConditionalReadStatus;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.model.type.code.RestfulCapabilityMode;
import com.ibm.fhir.model.type.code.SystemRestfulInteraction;
import com.ibm.fhir.model.type.code.TypeRestfulInteraction;
import com.ibm.fhir.server.test.websocket.FHIRNotificationServiceClientEndpoint;
import com.ibm.fhir.validation.FHIRValidator;

/**
 * Base class for fhir-server unit tests.
 */
public abstract class FHIRServerTestBase {

    protected FHIRClient client = null;

    // Default values for test properties.
    private static final String DEFAULT_REST_BASE_URL = "https://localhost:9443/fhir-server/api/v4";
    private static final String DEFAULT_WEBSOCKET_URL = "wss://localhost:9443/fhir-server/api/v4/notification";
    private static final String DEFAULT_KAFKA_CONNINFO = "localhost:9092";
    private static final String DEFAULT_KAFKA_TOPICNAME = "fhirNotifications";

    // Default values for FHIRClient properties that we use here.
    private static final String DEFAULT_TRUSTSTORE_LOCATION = "fhirClientTruststore.jks";
    private static final String DEFAULT_TRUSTSTORE_PASSWORD = "change-password";
    private static final String DEFAULT_KEYSTORE_LOCATION = "fhirClientKeystore.jks";
    private static final String DEFAULT_KEYSTORE_PASSWORD = "change-password";
    private static final String DEFAULT_USERNAME = "fhiruser";
    private static final String DEFAULT_PASSWORD = "change-password";

    // Constants that define test property names.
    private static final String PROPNAME_REST_BASE_URL = "fhirclient.rest.base.url";
    private static final String PROPNAME_WEBSOCKET_URL = "test.websocket.url";
    private static final String PROPNAME_KAFKA_CONNINFO = "test.kafka.connectionInfo";
    private static final String PROPNAME_KAFKA_TOPICNAME = "test.kafka.topicName";

    // for secure WebSocket Client
    private static final String PROPNAME_SSL_ENGINE_CONFIGURATOR = "org.glassfish.tyrus.client.sslEngineConfigurator";

    // These are values of test-specific properties.
    private String restBaseUrl = null;
    private String websocketUrl = null;
    private String kafkaConnectionInfo = null;
    private String kafkaTopicName = null;

    // These are values of FHIRClient properties that we need here also.
    private String fhirUser = null;
    private String fhirPassword = null;
    private String tsLocation = null;
    private String tsPassword = null;
    private String ksLocation = null;
    private String ksPassword = null;

    protected static final String MEDIATYPE_JSON = FHIRMediaType.APPLICATION_JSON;
    protected static final String MEDIATYPE_JSON_FHIR = FHIRMediaType.APPLICATION_FHIR_JSON;
    protected static final String MEDIATYPE_XML = FHIRMediaType.APPLICATION_XML;
    protected static final String MEDIATYPE_XML_FHIR = FHIRMediaType.APPLICATION_FHIR_XML;

    private CapabilityStatement conformanceStmt = null;

    protected String getRestBaseURL() {
        return restBaseUrl;
    }

    protected String getWebSocketURL() {
        return websocketUrl;
    }

    public String getKafkaConnectionInfo() {
        return kafkaConnectionInfo;
    }

    public String getKafkaTopicName() {
        return kafkaTopicName;
    }

    protected String getFhirUser() {
        return fhirUser;
    }

    protected String getFhirPassword() {
        return fhirPassword;
    }

    protected String getTsLocation() {
        return tsLocation;
    }

    protected String getTsPassword() {
        return tsPassword;
    }

    private String getKsLocation() {
        return ksLocation;
    }

    private String getKsPassword() {
        return ksPassword;
    }

    /**
     * We'll resolve all the supported test properties. We have two ways of setting
     * properties: 1) store them in a file called "test.properties" which is in the
     * classpath. 2) set each individual property as a JVM system property.
     *
     * Supported property names: test.host test.port test.urlprefix
     */
    @BeforeClass
    public void setUp() throws Exception {
        Properties properties = new Properties();
        try {
            // Read test.properties file if present
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.properties");
            if (is != null) {
                properties.load(is);
                is.close();
            }
        } catch (Exception e) {
            // Ignore any errors while trying to load the file.
        }

        setUp(properties);
    }

    /**
     * We'll resolve all the supported test properties. We have two ways of setting
     * properties: 1) store them in a file called "test.properties" which is in the
     * classpath. 2) set each individual property as a JVM system property.
     *
     * Supported property names: test.host test.port test.urlprefix
     */
    public void setUp(Properties properties) throws Exception {

        // Create our FHIRClient instance based on the properties we just read in.
        client = FHIRClientFactory.getClient(properties);

        restBaseUrl = getProperty(properties, PROPNAME_REST_BASE_URL, DEFAULT_REST_BASE_URL);
        websocketUrl = getProperty(properties, PROPNAME_WEBSOCKET_URL, DEFAULT_WEBSOCKET_URL);

        // Retrieve the info needed by a Kafka consumer.
        kafkaConnectionInfo = getProperty(properties, PROPNAME_KAFKA_CONNINFO, DEFAULT_KAFKA_CONNINFO);
        kafkaTopicName = getProperty(properties, PROPNAME_KAFKA_TOPICNAME, DEFAULT_KAFKA_TOPICNAME);

        fhirUser = getProperty(properties, FHIRClient.PROPNAME_CLIENT_USERNAME, DEFAULT_USERNAME);
        fhirPassword = FHIRUtilities
                .decode(getProperty(properties, FHIRClient.PROPNAME_CLIENT_PASSWORD, DEFAULT_PASSWORD));
        tsLocation = getProperty(properties, FHIRClient.PROPNAME_TRUSTSTORE_LOCATION, DEFAULT_TRUSTSTORE_LOCATION);
        tsPassword = FHIRUtilities
                .decode(getProperty(properties, FHIRClient.PROPNAME_TRUSTSTORE_PASSWORD, DEFAULT_TRUSTSTORE_PASSWORD));
        ksLocation = getProperty(properties, FHIRClient.PROPNAME_KEYSTORE_LOCATION, DEFAULT_KEYSTORE_LOCATION);
        ksPassword = FHIRUtilities
                .decode(getProperty(properties, FHIRClient.PROPNAME_KEYSTORE_PASSWORD , DEFAULT_KEYSTORE_PASSWORD));

    }

    /**
     * Tries to find the property named "propertyName" first as a System property,
     * then as a property within the "properties" object. If neither are found, then
     * the "defaultValue" is returned.
     *
     * @param properties   Properties object containing the properties loaded from
     *                     our properties file.
     * @param propertyName the name of the property to retrieve
     * @param defaultValue the default value to use if the property can't be found
     */
    protected String getProperty(Properties properties, String propertyName, String defaultValue) {
        return System.getProperty(propertyName, properties.getProperty(propertyName, defaultValue));
    }

    //
    // JAX-RS 2.0 utility functions.
    //

    /**
     * Returns a WebTarget to be used for a REST API invocation.
     */
    public WebTarget getWebTarget() {
        try {
            return client.getWebTarget();
        } catch (Throwable t) {
            fail("Unexpected exception while retrieving WebTarget: ",t);

            // This is here to appease the java compiler so we don't need to declare a
            // throws clause.
            throw new RuntimeException("Shouldn't get here as the fail() will throw an exception...");
        }
    }

    /**
     * Returns the FHIRClient instance being used for this testcase instance.
     */
    protected FHIRClient getFHIRClient() {
        return client;
    }

    /**
     * Creates and returns an "endpoint" to be used to receive notifications via a
     * websocket.
     */
    protected FHIRNotificationServiceClientEndpoint getWebsocketClientEndpoint() {
        try {
            FHIRNotificationServiceClientEndpoint endpoint = new FHIRNotificationServiceClientEndpoint();
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(new Configurator() {
                @Override
                public void beforeRequest(Map<String, List<String>> headers) {
                    String userpw = getFhirUser() + ":" + getFhirPassword();
                    String encoding = Base64.getEncoder().encodeToString(userpw.getBytes());
                    List<String> values = new ArrayList<String>();
                    values.add("Basic " + encoding);
                    headers.put("Authorization", values);
                }
            }).build();

            String webSocketURL = getWebSocketURL();
            if (webSocketURL.startsWith("wss")) {
                String tsLoc = getAbsoluteFilename(getTsLocation());
                if (tsLoc == null) {
                    throw new FileNotFoundException("Truststore file not found: " + getTsLocation());
                }

                String ksLoc = getAbsoluteFilename(getKsLocation());
                if (ksLoc == null) {
                    throw new FileNotFoundException("Keystore file not found: " + getKsLocation());
                }

                System.getProperties().put(SslContextConfigurator.KEY_STORE_FILE, ksLoc);
                System.getProperties().put(SslContextConfigurator.KEY_STORE_PASSWORD, getKsPassword());
                System.getProperties().put(SslContextConfigurator.TRUST_STORE_FILE, tsLoc);
                System.getProperties().put(SslContextConfigurator.TRUST_STORE_PASSWORD, getTsPassword());

                final SslContextConfigurator defaultConfig = new SslContextConfigurator();
                defaultConfig.retrieve(System.getProperties());
                SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(defaultConfig, true, false,
                        false);
                sslEngineConfigurator.setHostVerificationEnabled(false);
                config.getUserProperties().put(PROPNAME_SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);

            }
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            Session session = container.connectToServer(endpoint, config, new URI(webSocketURL));

            // Add a Delay
            int count = 10;
            while ( !session.isOpen() && count > 0) {
                System.out.println(">>> " + count + " waiting");
                Thread.sleep(1000l);
                count--;
            }

            endpoint.setSession(session);
            return endpoint;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Common function to invoke the 'metadata' operation and return the
     * CapabilityStatement object.
     */
    protected CapabilityStatement retrieveConformanceStatement() throws Exception {
        if (conformanceStmt == null) {
            FHIRClient client = getFHIRClient();
            FHIRResponse response = client.metadata();
            assertNotNull(response);
            assertEquals(200, response.getStatus());

            conformanceStmt = response.getResource(CapabilityStatement.class);
            assertNotNull(conformanceStmt);
        }

        return conformanceStmt;
    }

    /**
     * Determines whether or not the server supports the "conditional-read" feature by
     * examining the conformance statement.
     */
    protected boolean isConditionalReadSupported() throws Exception {
        Boolean conditionalReadSupported = Boolean.FALSE;
        CapabilityStatement conf = retrieveConformanceStatement();

        List<CapabilityStatement.Rest> restList = conf.getRest();
        if (restList != null) {
            CapabilityStatement.Rest rest = restList.get(0);
            if (rest != null) {
                assertEquals(RestfulCapabilityMode.SERVER.getValue(), rest.getMode().getValue());
                List<Rest.Resource> resources = rest.getResource();
                if (resources != null) {
                    Rest.Resource resource = resources.get(0);
                    if (resource != null && resource.getConditionalRead()!= null) {
                        conditionalReadSupported = resource.getConditionalRead().equals(ConditionalReadStatus.FULL_SUPPORT);
                    }
                }
            }
        }

        return conditionalReadSupported.booleanValue();
    }

    /**
     * Determines whether or not the server supports the "update/create" feature by
     * examining the conformance statement.
     */
    protected boolean isUpdateCreateSupported() throws Exception {
        Boolean updateCreateSupported = Boolean.FALSE;
        CapabilityStatement conf = retrieveConformanceStatement();

        List<CapabilityStatement.Rest> restList = conf.getRest();
        if (restList != null) {
            CapabilityStatement.Rest rest = restList.get(0);
            if (rest != null) {
                assertEquals(RestfulCapabilityMode.SERVER.getValue(), rest.getMode().getValue());
                List<Rest.Resource> resources = rest.getResource();
                if (resources != null) {
                    Rest.Resource resource = resources.get(0);
                    if (resource != null && resource.getUpdateCreate() != null) {
                        updateCreateSupported = resource.getUpdateCreate().getValue();
                    }
                }
            }
        }

        return updateCreateSupported.booleanValue();
    }

    /**
     * Determines whether or not the server supports the "delete" operation by
     * examining the conformance statement.
     */
    protected boolean isDeleteSupported() throws Exception {
        CapabilityStatement conf = retrieveConformanceStatement();

        List<CapabilityStatement.Rest> restList = conf.getRest();
        if (restList != null) {
            CapabilityStatement.Rest rest = restList.get(0);
            if (rest != null) {
                assertEquals(RestfulCapabilityMode.SERVER.getValue(), rest.getMode().getValue());
                List<Rest.Resource> resources = rest.getResource();
                if (resources != null) {
                    Rest.Resource resource = resources.get(0);
                    if (resource != null) {
                        List<Interaction> interactions = resource.getInteraction();
                        for (Interaction interaction : interactions) {
                            if (interaction.getCode().getValue() == TypeRestfulInteraction.DELETE.getValue()) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    protected boolean isComparmentSearchSupported() throws Exception {
        boolean compartmentSearchSupported = false;
        CapabilityStatement conf = retrieveConformanceStatement();

        for (Extension e : conf.getExtension()) {
            if (e.getUrl().contains("persistenceType") && (((com.ibm.fhir.model.type.String) e.getValue())
                    .getValue().contains("FHIRPersistenceJDBCImpl")
                    || ((com.ibm.fhir.model.type.String) e.getValue()).getValue()
                            .contains("FHIRPersistenceJDBCNormalizedImpl"))) {
                compartmentSearchSupported = true;
                break;
            }
        }
        return compartmentSearchSupported;
    }

    protected boolean isSearchAllSupported() throws Exception {
        return isComparmentSearchSupported();
    }

    protected boolean isSortingSupported() throws Exception {
        return isComparmentSearchSupported();
    }

    /**
     * Determines whether or not the server supports the "update/create" feature by
     * examining the conformance statement.
     */
    protected boolean isTransactionSupported() throws Exception {
        SystemRestfulInteraction transactionMode = null;
        CapabilityStatement conf = retrieveConformanceStatement();

        List<CapabilityStatement.Rest> restList = conf.getRest();
        if (restList != null) {
            CapabilityStatement.Rest rest = restList.get(0);
            if (rest != null) {
                assertEquals(RestfulCapabilityMode.SERVER.getValue(), rest.getMode().getValue());
                if (rest.getInteraction() != null) {
                    transactionMode = rest.getInteraction().get(0).getCode();
                }
            }
        }

        if (transactionMode == null) {
            transactionMode = SystemRestfulInteraction.BATCH;
        }

        boolean txnSupported = false;

        if (transactionMode.getValue().equals(SystemRestfulInteraction.TRANSACTION.getValue())) {
            txnSupported = true;
        }

        return txnSupported;
    }

    //
    // Some homegrown assert-type common functions.
    //

    /**
     * Verify that the status code in the Response is equal to the expected status
     * code.
     *
     * @param response           the Response to verify
     * @param expectedStatusCode the expected status code value
     */
    public void assertResponse(Response response, int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatus());
    }
    
    /**
     * Verify that the status code in the Response is of the expected status
     * family.
     *
     * @param response           the Response to verify
     * @param family             the expected status code family
     */
    protected void assertResponse(Response response, Response.Status.Family family) {
        assertNotNull(response);
        String message = "";
        if( ! response.getStatusInfo().getFamily().equals(family) ) {
            message = response.readEntity(String.class);
        }
        assertEquals(message, response.getStatusInfo().getFamily(), family);
    }

    /**
     * Verify that a FHIRClient response has the expected status code.
     *
     * @param response           the FHIRResponse that contains the response for a
     *                           FHIRClient API invocation
     * @param expectedStatusCode the status code that we expect in the response
     */
    protected void assertResponse(FHIRResponse response, int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatus());
    }

    /**
     * Validate the specified OperationOutcome object to make sure it contains an
     * exception.
     *
     * @param msgPart a string which should be found in the exception message.
     */
    protected void assertExceptionOperationOutcome(OperationOutcome oo, String msgPart) {
        assertNotNull(oo);

        // Make sure the OperationOutcomeIssue has a message containing 'msgPart'.
        assertNotNull(oo.getIssue());
        assertEquals(1, oo.getIssue().size());
        OperationOutcome.Issue ooi = oo.getIssue().get(0);
        assertNotNull(ooi);
        assertNotNull(ooi.getCode());
        assertNotNull(ooi.getCode().getValue());

        assertNotNull(ooi.getSeverity());
        assertNotNull(ooi.getSeverity().getValue());

        assertNotNull(ooi.getDetails());
        String msg = ooi.getDetails().getText().getValue();
        assertNotNull(msg);
        assertTrue("Error message '" + msg + "' does not contain expected text '" + msgPart + "'", msg.contains(msgPart));
    }

    protected void assertValidationOperationOutcome(OperationOutcome oo, String msgPart) {
        assertNotNull(oo);

        // Make sure that we can find the 'msgPart' in one of the OperationOutcomeIssue
        // objects.
        boolean foundIt = false;
        assertNotNull(oo.getIssue());
        assertFalse(oo.getIssue().isEmpty());
        for (OperationOutcome.Issue ooi : oo.getIssue()) {
            assertNotNull(ooi.getCode());
            assertNotNull(ooi.getCode().getValue());
            assertEquals(IssueType.INVALID.getValue(), ooi.getCode().getValue());

            assertNotNull(ooi.getSeverity());
            assertNotNull(ooi.getSeverity().getValue());
            assertTrue(IssueSeverity.ERROR.equals(ooi.getSeverity()) || IssueSeverity.FATAL.equals(ooi.getSeverity()));

            assertNotNull(ooi.getDetails());
            String msg = ooi.getDetails().getText().getValue();
            assertNotNull(msg);

            if (msg.contains(msgPart)) {
                foundIt = true;
            }
        }
        assertTrue("Could not find '" + msgPart + "' in OperationOutcomeIssue list!", foundIt);
    }

    //
    // Misc. common functions used by testcases.
    //

    /**
     * For the specified response, this function will extract the logical id value
     * from the response's Location header. The format of a location header value
     * should be: <code>[base]/<resource-type>/<id>/_history/<version></code>
     *
     * @param response the response object for a REST API invocation
     * @return the logical id value
     */
    public String getLocationLogicalId(Response response) {
        String location = response.getLocation().toString();
        assertNotNull(location);
        assertFalse(location.isEmpty());
        // System.out.println("Location value: " + location);

        String[] tokens = location.split("/");
        assertNotNull(tokens);
        assertTrue(tokens.length >= 4);

        String logicalId = tokens[tokens.length - 3];
        assertNotNull(logicalId);
        assertFalse(logicalId.isEmpty());

        return logicalId;
    }

    /**
     * For the specified response, this function will extract the version id value
     * from the response's Location header. The format of a location header value
     * should be: <code>[base]/<resource-type>/<id>/_history/<version></code>
     *
     * @param response the response object for a REST API invocation
     * @return the version id value
     */
    public String getLocationVersionId(Response response) {
        String location = response.getLocation().toString();
        assertNotNull(location);
        assertFalse(location.isEmpty());

        String[] tokens = location.split("/");
        assertNotNull(tokens);
        assertTrue(tokens.length >= 4);

        String versionId = tokens[tokens.length - 1];
        assertNotNull(versionId);
        assertFalse(versionId.isEmpty());

        return versionId;
    }

    /**
     * Returns the absolute filename associated with 'filename'.
     */
    private String getAbsoluteFilename(String filename) {
        // First, look in the classpath for the file.
        URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
        if (url != null) {
            return url.getFile();
        } else {
            File f = new File(filename);
            if (f.exists()) {
                return f.getAbsolutePath();
            }
        }

        return null;
    }

    protected String[] getLocationURITokens(String locationURI) {
        String[] temp = locationURI.split("/");
        String[] tokens;
        if (temp.length > 4) {
            tokens = new String[4];
            for (int i = 0; i < 4; i++) {
                tokens[i] = temp[temp.length - 4 + i];
            }
        } else {
            tokens = temp;
        }
        return tokens;
    }

    protected Patient setUniqueFamilyName(Patient patient, String uniqueName) {
        List <HumanName> nameList = new ArrayList<HumanName>();
        for(HumanName humanName: patient.getName()) {
            nameList.add(humanName.toBuilder().family(string(uniqueName)).build());
        }

        patient = patient.toBuilder().name(nameList).build();
        return patient;
    }

    public void checkForIssuesWithValidation(Resource resource, boolean failOnValidationException, boolean failOnWarning, boolean debug) {

        List<Issue> issues = Collections.emptyList();
        try {
            issues = FHIRValidator.validator().validate(resource);
        } catch(Exception e) {
            if(failOnValidationException) {
                fail("Unable to validate the resource", e);
            }
        }

        if (!issues.isEmpty()) {
            System.out.println("Printing Issue with Validation");
            int nonWarning = 0;
            int allOtherIssues = 0;
            for (Issue issue : issues) {
                if(IssueSeverity.ERROR.getValue().compareTo(issue.getSeverity().getValue()) == 0
                        || IssueSeverity.FATAL.getValue().compareTo(issue.getSeverity().getValue()) == 0 ) {
                    nonWarning++;
                    System.out.println("severity: " + issue.getSeverity().getValue() + ", details: " + issue.getDetails().getText().getValue() + ", expression: " + issue.getExpression().get(0).getValue());
                } else {
                    allOtherIssues++;
                    if (debug) {
                        System.out.println("severity: " + issue.getSeverity().getValue() + ", details: " + issue.getDetails().getText().getValue() + ", expression: " + issue.getExpression().get(0).getValue());
                    }
                }
            }

            System.out.println("count = [" + issues.size() + "]");
            assertEquals(nonWarning,0);

            if(failOnWarning) {
                assertEquals(allOtherIssues,0);
            }
        }
        else {
            assertTrue("Passed with no issues in validation", true);
        }
    }

    /**
     * Parses a location URI into the resourceType, resourceId, and (optionally) the version id.
     * @param location
     * @return
     */
    public static String[] parseLocationURI(String location) {
        String[] result = null;
        if (location == null) {
            throw new NullPointerException("The 'location' parameter was specified as null.");
        }

        String[] tokens = location.split("/");
        // Check if we should expect 4 tokens or only 2.
        if (location.contains("_history")) {
            if (tokens.length >= 4) {
                result = new String[3];
                result[0] = tokens[tokens.length - 4];
                result[1] = tokens[tokens.length - 3];
                result[2] = tokens[tokens.length - 1];
            } else {
                throw new IllegalArgumentException("Incorrect location value specified: " + location);
            }
        } else {
            if (tokens.length >= 2) {
                result = new String[2];
                result[0] = tokens[tokens.length - 2];
                result[1] = tokens[tokens.length - 1];
            } else {
                throw new IllegalArgumentException("Incorrect location value specified: " + location);
            }
        }
        return result;
    }

    protected Condition buildCondition(String patientId, String fileName) throws Exception {
        Condition condition = TestUtil.readLocalResource(fileName);
        condition = condition.toBuilder().subject(Reference.builder().reference(string("Patient/" + patientId)).build()).build();

        return condition;
    }

    /**
     * Assert the the given {@link Bundle} is of the expected {@link BundleType} and has the expected entry count.
     *
     * @param bundle the bundle to tet
     * @param expectedType
     * @param expectedEntryCount
     */
    public static void assertResponseBundle(Bundle bundle, BundleType expectedType, int expectedEntryCount) {
        assertNotNull(bundle);
        assertNotNull(bundle.getType());
        assertNotNull(bundle.getType().getValue());
        assertEquals(expectedType.getValue(), bundle.getType().getValue());
        if (expectedEntryCount > 0) {
            assertNotNull(bundle.getEntry());
            assertEquals(expectedEntryCount, bundle.getEntry().size());
        }
    }

    /**
     * Gets the self link of the bundle.
     * @param bundle the bundle
     * @return the self link, or null
     */
    protected String getSelfLink(Bundle bundle) {
        for (Bundle.Link link : bundle.getLink()) {
            String type = link.getRelation().getValue();
            String uri = link.getUrl().getValue();
            if ("self".equals(type)) {
                return uri;
            }
        }
        return null;
    }
}
