/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import com.ibm.fhir.client.FHIRClient;
import com.ibm.fhir.client.FHIRClientFactory;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.CapabilityStatement;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Narrative;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.Xhtml;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.CapabilityStatementKind;
import com.ibm.fhir.model.type.code.FHIRVersion;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.model.type.code.NarrativeStatus;
import com.ibm.fhir.model.type.code.PublicationStatus;

/**
 * This class provides a simple framework for building test cases
 * for code that expects to interact with a live FHIR server through
 * REST API calls. Wiremock uses Jetty under the containers as a web
 * container and then the methods on this class provide the ability
 * to configure that web container to respond to specific URL patterns
 * with specific responses. The simplest request mapping is for a client
 * to provide a URL and a resource and the container will return that
 * resource in response to a request for that URL. More complex 
 * URL-building is possible using the Wiremock MappingBuilder class.
 * See Wiremocks documentation for full details. 
 */
public abstract class R4RestFHIRTest {
    
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 7070;

    protected static WireMockServer wireMockServer;
    protected static WireMock wireMock;
    
    @BeforeClass
    public static void setupServer() {
        
        WireMockConfiguration wireMockConfig = WireMockConfiguration.wireMockConfig();
        wireMockConfig.port(PORT);
        
        wireMockServer = new WireMockServer(wireMockConfig);
        wireMockServer.start();
        
        WireMock.configureFor(HOSTNAME, PORT);
        wireMock = new WireMock(HOSTNAME, PORT);
    }
    
    @AfterClass
    public static void serverShutdown() {
        wireMockServer.stop();
    }
    
    public static String getHttpHost() {
        return wireMockServer.baseUrl();
    }

    public static String getBaseUrl() {
        return wireMockServer.baseUrl();
    }
    
    @BeforeMethod
    public void init() throws InterruptedException {
        WireMock.resetToDefault();
    }

    @BeforeMethod
    public void mockMetadata() throws Exception {
        mockFhirRead("/metadata", getCapabilityStatement());
    }

    public FHIRParser getFhirParser() {
        return FHIRParser.parser(Format.JSON);
    }

    public FHIRGenerator getFhirGenerator() {
        return FHIRGenerator.generator(Format.JSON, /* prettyPrint= */true);
    }

    public static int getHttpPort() {
        return wireMockServer.port();
    }

    public FHIRClient newClient() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(FHIRClient.PROPNAME_BASE_URL, getBaseUrl());
        properties.setProperty(FHIRClient.PROPNAME_LOGGING_ENABLED, "true");
        FHIRClient client = FHIRClientFactory.getClient(properties);

        return client;
    }

    public void mockNotFound(String resource) throws Exception {
        OperationOutcome outcome =
                OperationOutcome.builder().text(Narrative.builder().div(Xhtml.from("<div>generated</div>")).status(NarrativeStatus.builder().value("generated").build()).build()).issue(OperationOutcome.Issue.builder().severity(IssueSeverity.ERROR).code(IssueType.PROCESSING).diagnostics(com.ibm.fhir.model.type.String.of(resource)).build()).build();

        mockFhirRead(resource, outcome, 404);
    }

    public void mockFhirRead(Resource resource) throws Exception {
        String resourcePath = "/" + resource.getClass().getSimpleName() + "/" + resource.getId();
        mockFhirInteraction(resourcePath, resource);
    }

    public void mockFhirRead(String path, Resource resource) throws Exception {
        mockFhirRead(path, resource, 200);
    }

    public void mockFhirRead(String path, Resource resource, int statusCode) throws Exception {
        MappingBuilder builder = get(urlEqualTo(path));
        mockFhirInteraction(builder, resource, statusCode);
    }

    public void mockFhirSearch(String path, Resource... resources) throws Exception {
        MappingBuilder builder = get(urlEqualTo(path));
        mockFhirInteraction(builder, makeBundle(resources));
    }

    public void mockFhirSearch(MappingBuilder builder, Resource... resources) throws Exception {
        mockFhirInteraction(builder, makeBundle(resources));
    }

    public void mockFhirPost(String path, Resource resource) throws Exception {
        mockFhirInteraction(post(urlEqualTo(path)), resource, 200);
    }

    public void mockFhirInteraction(String path, Resource resource) throws Exception {
        mockFhirRead(path, resource, 200);
    }

    public void mockFhirInteraction(MappingBuilder builder, Resource resource) throws Exception {
        mockFhirInteraction(builder, resource, 200);
    }

    public void mockFhirInteraction(MappingBuilder builder, Resource resource, int statusCode) throws Exception {
        String body = null;
        if (resource != null) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                getFhirGenerator().generate(resource, os);
                body = os.toString();
            }
        }

        stubFor(builder.willReturn(aResponse().withStatus(statusCode).withHeader("Content-Type", "application/json").withBody(body)));
    }

    public CapabilityStatement getCapabilityStatement() throws Exception {
        CapabilityStatement metadata =
                CapabilityStatement.builder().fhirVersion(FHIRVersion.VERSION_4_0_1).status(PublicationStatus.ACTIVE).date(DateTime.now()).kind(CapabilityStatementKind.CAPABILITY).format(Code.of("application/json")).build();
        return metadata;
    }

    public Bundle makeBundle(List<? extends Resource> resources) {
        return makeBundle(resources.toArray(new Resource[resources.size()]));
    }

    public Bundle makeBundle(Resource... resources) {

        List<Bundle.Entry> entries = new ArrayList<>();
        if (resources != null) {
            for (Resource l : resources) {
                entries.add(Bundle.Entry.builder().resource(l).fullUrl(Uri.of("/" + l.getClass().getSimpleName() + "/" + l.getId())).build());
            }
        }

        Bundle bundle = Bundle.builder().type(BundleType.SEARCHSET).total(UnsignedInt.of(resources != null ? resources.length : 0)).entry(entries).build();
        return bundle;
    }
}
