/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.test.profiles;

import static com.ibm.fhir.model.type.String.string;
import static com.ibm.fhir.model.type.Uri.uri;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ibm.fhir.client.FHIRParameters;
import com.ibm.fhir.client.FHIRResponse;
import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.CareTeam;
import com.ibm.fhir.model.resource.Coverage;
import com.ibm.fhir.model.resource.ExplanationOfBenefit;
import com.ibm.fhir.model.resource.Location;
import com.ibm.fhir.model.resource.Organization;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.Practitioner;
import com.ibm.fhir.model.test.TestUtil;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.Identifier;
import com.ibm.fhir.model.type.Meta;
import com.ibm.fhir.model.type.Reference;

/**
 * Tests using C4BB STU1 And the given profile.
 */
public class CarinBlueButtonTest extends ProfilesTestBase {

    private static final String CLASSNAME = CarinBlueButtonTest.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);

    private String coverageId = null;
    private String careTeamId = null;
    private String organizationId = null;
    private String organizationOrg1Id = null;
    private String organizationOrg45Id = null;
    private String locationId = null;
    private String patientId = null;
    private String practitionerId = null;

    private String explanationOfBenefitInPatientId = null;
    private String explanationOfBenefitsOutPatient = null;
    private String explanationOfBenefitsPharmacyId = null;
    private String explanationOfBenefitsProfessionalId = null;
    private String eobInpatientInstitutionalEx1id = null;

    public Boolean skip = Boolean.TRUE;

    @Override
    public List<String> getRequiredProfiles() {
        //@formatter:off
        return Arrays.asList(
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Coverage|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Inpatient-Institutional|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Outpatient-Institutional|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Pharmacy|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-ExplanationOfBenefit-Professional-NonClinician|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Organization|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Patient|1.0.0",
            "http://hl7.org/fhir/us/carin-bb/StructureDefinition/C4BB-Practitioner|1.0.0");
        //@formatter:on
    }

    @Override
    public void setCheck(Boolean check) {
        this.skip = check;

        if (!skip) {
            logger.info("Skipping Tests");
        }
    }

    // Load Organization Resources
    public void loadOrganization() throws Exception {
        String resource = "json/spec/organization-example.json";
        WebTarget target = getWebTarget();
        Organization organization = TestUtil.readExampleResource(resource);

        // Add profile + extra fields required by profile.
        Canonical profile = Canonical.of("http://hl7.org/fhir/us/C4BB/StructureDefinition/C4BB-Organization");
        Meta meta = organization.getMeta().toBuilder().profile(profile).build();

        // @formatter:off
        CodeableConcept type =
                CodeableConcept.builder()
                    .coding(Coding.builder()
                        .code(Code.of("NPI"))
                        .display(string("National provider identifier"))
                        .system(uri("http://terminology.hl7.org/CodeSystem/v2-0203"))
                        .build())
                    .build();

        Identifier identifierNPI =
                Identifier.builder()
                    .system(uri("http://hl7.org/fhir/sid/us-npi"))
                    .value(string("1234556"))
                    .type(type)
                    .build();

        type = CodeableConcept.builder()
                .coding(Coding.builder()
                    .code(Code.of("TAX"))
                    .display(string("Tax ID number"))
                    .system(uri("http://terminology.hl7.org/CodeSystem/v2-0203"))
                    .build())
                .build();

        Identifier identifierTaxId =
                Identifier.builder()
                    .system(uri("urn:oid:2.16.840.1.113883.4.4"))
                    .value(string("1234567"))
                    .type(type)
                    .build();

        organization = organization.toBuilder()
                            .meta(meta)
                            .identifier(identifierNPI, identifierTaxId)
                            .active(com.ibm.fhir.model.type.Boolean.TRUE)
                            .build();
        // @formatter:on

        Entity<Organization> entity = Entity.entity(organization, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Organization").request().post(entity, Response.class);

        assertResponse(response, Response.Status.CREATED.getStatusCode());
        organizationId = getLocationLogicalId(response);
        response = target.path("Organization/" + organizationId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadOrganizationOrg1() throws Exception {
        String resource = "json/profiles/fhir-ig-carin-bb/Organization-OrganizationProvider1.json";
        WebTarget target = getWebTarget();
        Organization organization = TestUtil.readExampleResource(resource);
        Entity<Organization> entity = Entity.entity(organization, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Organization").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        organizationOrg1Id = getLocationLogicalId(response);
        response = target.path("Organization/" + organizationOrg1Id).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadOrganizationOrg45() throws Exception {
        String resource = "json/profiles/fhir-ig-carin-bb/Organization-Payer1.json";
        WebTarget target = getWebTarget();
        Organization organization = TestUtil.readExampleResource(resource);
        Entity<Organization> entity = Entity.entity(organization, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Organization").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        organizationOrg45Id = getLocationLogicalId(response);

        response = target.path("Organization/" + organizationOrg45Id).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Load Coverage Resources
    public void loadCoverage() throws Exception {
        WebTarget target = getWebTarget();
        Coverage coverage = TestUtil.readExampleResource("json/profiles/fhir-ig-carin-bb/Coverage-Coverage1.json");

        Reference org45ref = Reference.builder().reference(com.ibm.fhir.model.type.String.of("Organization/" + organizationOrg45Id)).build();
        coverage = coverage.toBuilder().payor(Arrays.asList(org45ref)).build();

        Entity<Coverage> entity = Entity.entity(coverage, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Coverage").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        coverageId = getLocationLogicalId(response);

        // Next, call the 'read' API to retrieve the new Location and verify it.
        response = target.path("Coverage/" + coverageId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Load Patient Resources
    public void loadPatient() throws Exception {
        WebTarget target = getWebTarget();
        Patient patient = TestUtil.readExampleResource("json/profiles/fhir-ig-carin-bb/Patient-Patient1.json");

        Entity<Patient> entity = Entity.entity(patient, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Patient").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        patientId = getLocationLogicalId(response);

        // Next, call the 'read' API to retrieve the new Location and verify it.
        response = target.path("Patient/" + patientId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Load Provider Resources
    public void loadProvider() throws Exception {
        WebTarget target = getWebTarget();
        Practitioner practitioner = TestUtil.readExampleResource("json/spec/practitioner-example.json");

        Entity<Practitioner> entity = Entity.entity(practitioner, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Practitioner").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        practitionerId = getLocationLogicalId(response);

        response = target.path("Practitioner/" + practitionerId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Load CareTeam Resources
    public void loadCareteam() throws Exception {
        WebTarget target = getWebTarget();
        CareTeam careTeam = TestUtil.readExampleResource("json/spec/careteam-example.json");

        Entity<CareTeam> entity = Entity.entity(careTeam, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("CareTeam").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        careTeamId = getLocationLogicalId(response);

        response = target.path("CareTeam/" + careTeamId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Load Location Resources
    public void loadLocation() throws Exception {
        WebTarget target = getWebTarget();

        Location location = TestUtil.readExampleResource("json/spec/location-example.json");
        Entity<Location> entity = Entity.entity(location, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("Location").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());

        locationId = getLocationLogicalId(response);

        response = target.path("Location/" + locationId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Load Explanation of Benefits Resources
    public void loadExplanationOfBenefitsInPatient() throws Exception {
        WebTarget target = getWebTarget();

        ExplanationOfBenefit eob = TestUtil.readExampleResource("json/profiles/fhir-ig-carin-bb/ExplanationOfBenefit-EOBInpatient1.json");
        Entity<ExplanationOfBenefit> entity = Entity.entity(eob, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("ExplanationOfBenefit").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());

        explanationOfBenefitInPatientId = getLocationLogicalId(response);

        response = target.path("ExplanationOfBenefit/" + explanationOfBenefitInPatientId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadExplanationOfBenefitsOutPatient() throws Exception {
        WebTarget target = getWebTarget();

        ExplanationOfBenefit eob = TestUtil.readExampleResource("json/profiles/fhir-ig-carin-bb/ExplanationOfBenefit-EOBOutpatientInstitutional1.json");
        Entity<ExplanationOfBenefit> entity = Entity.entity(eob, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("ExplanationOfBenefit").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());

        explanationOfBenefitsOutPatient = getLocationLogicalId(response);

        response = target.path("ExplanationOfBenefit/" + explanationOfBenefitsOutPatient).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadExplanationOfBenefitsPharmacy() throws Exception {
        WebTarget target = getWebTarget();

        ExplanationOfBenefit eob = TestUtil.readExampleResource("json/profiles/fhir-ig-carin-bb/ExplanationOfBenefit-EOBPharmacy1.json");
        Entity<ExplanationOfBenefit> entity = Entity.entity(eob, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("ExplanationOfBenefit").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());

        explanationOfBenefitsPharmacyId = getLocationLogicalId(response);

        response = target.path("ExplanationOfBenefit/" + explanationOfBenefitsPharmacyId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadExplanationOfBenefitsProfessional() throws Exception {
        WebTarget target = getWebTarget();

        ExplanationOfBenefit eob = TestUtil.readExampleResource("json/profiles/fhir-ig-carin-bb/ExplanationOfBenefit-EOBProfessional1a.json");
        Entity<ExplanationOfBenefit> entity = Entity.entity(eob, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("ExplanationOfBenefit").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());

        explanationOfBenefitsProfessionalId = getLocationLogicalId(response);

        response = target.path("ExplanationOfBenefit/" + explanationOfBenefitsProfessionalId).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // ExplanationOfBenefit-EOBInpatient1.json
    public void loadExplanationOfBenefitsInpatientInstitutionalEx1() throws Exception {
        WebTarget target = getWebTarget();

        ExplanationOfBenefit eob = TestUtil.readExampleResource("json/profiles/fhir-ig-carin-bb/ExplanationOfBenefit-EOBInpatient1.json");
        Entity<ExplanationOfBenefit> entity = Entity.entity(eob, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("ExplanationOfBenefit").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());

        eobInpatientInstitutionalEx1id = getLocationLogicalId(response);

        response = target.path("ExplanationOfBenefit/" + eobInpatientInstitutionalEx1id).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }
    // Load Resources
    @BeforeClass
    public void loadResources() throws Exception {
        if (!skip) {
            loadLocation();
            loadOrganization();
            loadOrganizationOrg1();
            loadOrganizationOrg45();
            loadProvider();
            loadCoverage();
            loadPatient();
            loadCareteam();
            loadExplanationOfBenefitsInPatient();
            loadExplanationOfBenefitsOutPatient();
            loadExplanationOfBenefitsPharmacy();
            loadExplanationOfBenefitsProfessional();
            loadExplanationOfBenefitsInpatientInstitutionalEx1();
        }
    }

    // Delete Resources
    public void deleteOrganization() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("Organization/" + organizationId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteOrganizationOrg1() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("Organization/" + organizationId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteOrganizationOrg45() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("Organization/" + organizationOrg45Id).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Coverage Resources
    public void deleteCoverage() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("Coverage/" + coverageId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Patient Resources
    public void deletePatient() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("Patient/" + patientId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Provider Resources
    public void deleteProvider() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("Practitioner/" + practitionerId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete CareTeam Resources
    public void deleteCareteam() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("CareTeam/" + careTeamId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Location Resources
    public void deleteLocation() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("Location/" + locationId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Explanation of Benefits Resources
    public void deleteExplanationOfBenefits1() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("ExplanationOfBenefit/" + explanationOfBenefitInPatientId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Explanation of Benefits Resources
    public void deleteExplanationOfBenefits2() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("ExplanationOfBenefit/" + explanationOfBenefitsOutPatient).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Explanation of Benefits Resources
    public void deleteExplanationOfBenefits3() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("ExplanationOfBenefit/" + explanationOfBenefitsPharmacyId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Delete Explanation of Benefits Resources
    public void deleteExplanationOfBenefits4() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("ExplanationOfBenefit/" + explanationOfBenefitsProfessionalId).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteExplanationOfBenefitsInpatientInstitutionalEx1() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("ExplanationOfBenefit/" + eobInpatientInstitutionalEx1id).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    @AfterClass
    public void deleteResources() throws Exception {
        if (!skip) {
            deleteLocation();
            deleteOrganization();
            deleteOrganizationOrg1();
            deleteOrganizationOrg45();
            deleteCoverage();
            deletePatient();
            deleteProvider();
            deleteCareteam();
            deleteExplanationOfBenefits1();
            deleteExplanationOfBenefits2();
            deleteExplanationOfBenefits3();
            deleteExplanationOfBenefits4();
            deleteExplanationOfBenefitsInpatientInstitutionalEx1();
        }
    }

    @Test
    public void testLocationId() throws Exception {
        if (!skip) {
            // A common call -> GET [base]/Location?_id=[id]
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_id", locationId);
            FHIRResponse response = client.search(Location.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, locationId);
        }
    }

    @Test
    public void testPractitionerId() throws Exception {
        if (!skip) {
            // A common call -> GET [base]/Practitioner?_id=[id]
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_id", practitionerId);
            FHIRResponse response = client.search(Practitioner.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, practitionerId);
        }
    }

    @Test
    public void testPatientId() throws Exception {
        if (!skip) {
            // A common call -> GET [base]/Patient?_id=[id]
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_id", patientId);
            FHIRResponse response = client.search(Patient.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, patientId);
        }
    }

    @Test
    public void testOrganizationId() throws Exception {
        if (!skip) {
            // A common call -> GET [base]/Organization?_id=[id],[id],[id]
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_id", String.join(",", organizationId, organizationOrg1Id, organizationOrg45Id));
            FHIRResponse response = client.search(Organization.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, organizationId);
        }
    }

    @Test
    public void testCoverageIdWithInclude() throws Exception {
        if (!skip) {
            // A common call -> GET [base]/Coverage?_id=[id]&_include=Coverage:payor
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_id", coverageId);
            parameters.searchParam("_include", "Coverage:payor");
            FHIRResponse response = client.search(Coverage.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, coverageId, organizationOrg45Id);
        }
    }

    @Test
    public void testExplanationOfBenefitId() throws Exception {
        if (!skip) {
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_id", this.explanationOfBenefitInPatientId + "," + this.explanationOfBenefitsOutPatient + ","
                    + this.explanationOfBenefitsPharmacyId + "," + this.explanationOfBenefitsProfessionalId);
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 4);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
            assertContainsIds(bundle, explanationOfBenefitsOutPatient);
            assertContainsIds(bundle, explanationOfBenefitsPharmacyId);
            assertContainsIds(bundle, explanationOfBenefitsProfessionalId);
        }
    }

    @Test
    public void testExplanationOfBenefitIdentifierNoSystem() throws Exception {
        if (!skip) {
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("identifier", "|AW123412341234123412341234123412");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }

    @Test
    public void testExplanationOfBenefitIdentifierWithSystem() throws Exception {
        if (!skip) {
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("identifier", "https://www.xxxplan.com/fhir/EOBIdentifier|AW123412341234123412341234123412");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }

    @Test
    public void testExplanationOfBenefitServiceDate_BillablePeriod() throws Exception {
        // Service Date using ExplanationOfBenefit.billablePeriod
        // EoB - Pharmacy Benefits
        // "start": "2019-10-30",
        // "end": "2019-10-31"
        if (!skip) {
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("service-date", "ge2019-10-01");
            parameters.searchParam("service-date", "le2019-11-01");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, this.explanationOfBenefitsPharmacyId);
        }
    }

    @Test
    public void testCARINBlueButton_ExplanationOfBenefit_Patient_SearchParameter() throws Exception {
        if (!skip) {
            // This is now enabled due to HL7 FHIR Jira - https://jira.hl7.org/browse/FHIR-27739
            // https://github.com/IBM/FHIR/issues/1157
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("patient", "Patient/Patient1");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }

    @Test
    public void testExplanationOfBenefitLastUpdated() throws Exception {
        if (!skip) {
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_lastUpdated", "le1990");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() == 0);

            // Sometimes we have too many resources accumulated over time, and then we need to
            // do some sorting to ensure a deterministic outcome.
            parameters = new FHIRParameters();
            parameters.searchParam("_lastUpdated", "gt1990");
            parameters.searchParam("_count", "10");
            parameters.searchParam("_sort", "-_lastUpdated");
            response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }

    @Test
    public void testCARINBlueButton_Coverage_Patient_SearchParameter() throws Exception {
        // Name: CARINBlueButton_Coverage_Patient_SearchParameter
        if (!skip) {
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("patient", "Patient/Patient1");
            FHIRResponse response = client.search(Coverage.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, coverageId);
        }
    }

    @Test
    public void testCARINBlueButton_ExplanationOfBenefit_Created_SearchParameter() throws Exception {
        if (!skip) {
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("created", "2019-11-02T00:00:00+00:00");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }

    @Test
    public void testComplicatedInclude() throws Exception {
        if (!skip) {
            /*
             * This example is per:
             * https://confluence.hl7.org/pages/viewpage.action?pageId=82911348&preview=/82911348/82911352/CARIN%20BB%
             * 20RESTful%20API%20Combined%20-%20FHIR-26702%20-%20FHIR-26693%200513%202020.docx RESTFUL API SHALL
             */
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("patient", "Patient/Patient1");
            parameters.searchParam("_lastUpdated", "ge2020");
            parameters.searchParam("_include", "ExplanationOfBenefit:patient");
            parameters.searchParam("_include", "ExplanationOfBenefit:provider");
            parameters.searchParam("_include", "ExplanationOfBenefit:care-team");
            parameters.searchParam("_include", "ExplanationOfBenefit:coverage");
            // parameters.searchParam("_include:iterate", "Coverage:payor");

            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }

    @Test(enabled = false)
    public void testExplanationOfBenefitServiceDate() throws Exception {
        if (!skip) {
            /*
             * This should be coming in a new CARIN BB Rev.
             * https://confluence.hl7.org/pages/viewpage.action?pageId=82911348&preview=/82911348/82911352/CARIN%20BB%
             * 20RESTful%20API%20Combined%20-%20FHIR-26702%20-%20FHIR-26693%200513%202020.docx RESTFUL API SHALL
             * service-date date GET [base]/ExplanationOfBenefit?service-date=[prefix][date] Shall only be supported in
             * a combination with patient
             */
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("service-date", "ge2014");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }

    @Test(enabled = false)
    public void testExplanationOfBenefitType() throws Exception {
        if (!skip) {
            /*
             * This should be coming in a new CARIN BB Rev.
             * https://confluence.hl7.org/pages/viewpage.action?pageId=82911348&preview=/82911348/82911352/CARIN%20BB%
             * 20RESTful%20API%20Combined%20-%20FHIR-26702%20-%20FHIR-26693%200513%202020.docx RESTFUL API SHALL type
             * token GET [base]/ExplanationOfBenefit?type=[system]|[code] Shall only be supported in a combination with
             * patient
             */
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("type", "http://terminology.hl7.org/CodeSystem/claim-type|institutional");
            FHIRResponse response = client.search(ExplanationOfBenefit.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, explanationOfBenefitInPatientId);
        }
    }
}