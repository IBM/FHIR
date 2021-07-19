/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.rest.retrieve;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;

import com.ibm.fhir.client.FHIRClient;
import com.ibm.fhir.cql.engine.rest.R4RestFHIRTest;
import com.ibm.fhir.cql.engine.searchparam.SearchParameterResolver;
import com.ibm.fhir.cql.engine.util.FHIRClientUtil;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Bundle.Link;
import com.ibm.fhir.model.resource.Condition;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.BundleType;

public class RestFHIRRetrieveProviderTest extends R4RestFHIRTest {

    SearchParameterResolver RESOLVER;
    FHIRClient CLIENT;

    RestFHIRRetrieveProvider provider;

    @BeforeMethod
    public void setUp() throws Exception {
        CLIENT = newClient();
        RESOLVER = new SearchParameterResolver();

        this.provider = new RestFHIRRetrieveProvider(RESOLVER, CLIENT);
    }

    @Test
    public void userSpecifiedPageSizeIsUsedWhenCodeBasedQuery() throws Exception {
        Code code = new Code().withSystem("http://mysystem.com").withCode("mycode");
        List<Code> codes = Arrays.asList(code);

        mockFhirSearch(get(urlPathEqualTo("/Condition")).withQueryParam("code", equalTo(code.getSystem() + "|"
                + code.getCode())).withQueryParam("subject:Patient", equalTo("123")).withQueryParam("_count", equalTo("500")));

        provider.setPageSize(500);
        provider.retrieve("Patient", "subject", "123", "Condition", null, "code", codes, null, null, null, null, null);
    }

    @Test
    public void userSpecifiedPageSizeIsUsedWhenValueSetQuery() throws Exception {

        String valueSetUrl = "http://myterm.com/fhir/ValueSet/MyValueSet";

        mockFhirSearch(get(urlPathEqualTo("/Condition")).withQueryParam("code:in", equalTo(valueSetUrl)).withQueryParam("subject:Patient", equalTo("123")).withQueryParam("_count", equalTo("500")));

        provider.setPageSize(500);
        provider.retrieve("Patient", "subject", "123", "Condition", "http://hl7.org/fhir/StructureDefinition/Condition", "code", null, valueSetUrl, null, null, null, null);
    }

    @Test
    public void userSpecifiedPageSizeIsUsedWhenDateQuery() throws Exception {
        /**
         * As best as I can tell, the date range optimized queries are broken right now.
         * See https://github.com/DBCG/cql_engine/issues/467.
         */

        OffsetDateTime start = OffsetDateTime.of(2020, 11, 12, 1, 2, 3, 0, ZoneOffset.UTC);
        OffsetDateTime end = start.plusYears(1);

        Interval interval = new Interval(new DateTime(start), true, new DateTime(end), false);

        // The dates will be rendered in the URL in the build machine's local
        // time zone, so the URL value might fluctuate from one environment to another.
        // We could try to match that formatting logic to get an exact URL, but it isn't
        // necessary for what we are trying to achieve here, so we just accept any date
        // string.
        mockFhirSearch(get(urlPathEqualTo("/Condition")).withQueryParam("onset-date", matching("ge2020.*")).withQueryParam("onset-date", matching("le.*")).withQueryParam("subject:Patient", equalTo("123")).withQueryParam("_count", equalTo("500")));

        // mockFhirInteraction(get(urlMatching("/Condition\\?subject=" + escapeUrlParam("Patient/123") +
        // "&onset-date=ge2020[^&]+&onset-date=le[^&]+" +
        // "&_count=500")), makeBundle());

        provider.setPageSize(500);
        provider.retrieve("Patient", "subject", "123", "Condition", null, "code", null, null, "onset", null, null, interval);
    }

    @Test
    public void userSpecifiedPageSizeNotUsedWhenIDQuery() throws Exception {
        mockFhirRead("/Patient/123", Patient.builder().id("123").birthDate(Date.of("1979-12-01")).build());

        provider.setPageSize(500);
        provider.retrieve("Patient", "id", "123", "Patient", "http://hl7.org/fhir/StructureDefinition/Patient", null, null, null, null, null, null, null);
    }

    @Test
    public void noUserSpecifiedPageSizeSpecifiedNoCountInURL() throws Exception {
        Code code = new Code().withSystem("http://mysystem.com").withCode("mycode");
        List<Code> codes = Arrays.asList(code);

        mockFhirSearch("/Condition?code=" + FHIRClientUtil.urlencode(code.getSystem() + "|" + code.getCode()) + "&subject:Patient=123");

        provider.retrieve("Patient", "subject", "123", "Condition", null, "code", codes, null, null, null, null, null);
    }

    @Test
    public void multiplePagesAllReadSuccess() throws Exception {
        Condition c = Condition.builder().id("c1").subject(Reference.builder().reference(com.ibm.fhir.model.type.String.of("Patient/123")).build()).build();

        Bundle page1 =
                Bundle.builder().type(BundleType.SEARCHSET).total(UnsignedInt.of(1)).entry(Bundle.Entry.builder().resource(c).build()).link(Link.builder().relation(com.ibm.fhir.model.type.String.of("next")).url(Uri.of(getBaseUrl()
                        + "/Condition?subject:Patient=123&_page=2")).build()).build();

        Bundle page2 = Bundle.builder().type(BundleType.SEARCHSET).total(UnsignedInt.of(1)).entry(Bundle.Entry.builder().resource(c).build()).build();

        mockFhirInteraction(get(urlPathEqualTo("/Condition")).withQueryParam("subject:Patient", equalTo("123")), page1);

        mockFhirInteraction(get(urlPathEqualTo("/Condition")).withQueryParam("subject:Patient", equalTo("123")).withQueryParam("_page", equalTo("2")), page2);

        Iterable<Object> results = provider.retrieve("Patient", "subject", "123", "Condition", null, null, null, null, null, null, null, null);
        final AtomicInteger count = new AtomicInteger(0);
        results.forEach(o -> {
            count.incrementAndGet();
        });
        assertEquals(count.get(), 2);

        verify(2, getRequestedFor(urlPathEqualTo("/Condition")));
    }
}
