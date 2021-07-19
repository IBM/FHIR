/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.model;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.elm_modelinfo.r1.ClassInfo;
import org.hl7.elm_modelinfo.r1.TypeInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.opencds.cqf.cql.engine.model.ModelResolver;

import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Encounter;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.resource.TestReport;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Decimal;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.HumanName;
import com.ibm.fhir.model.type.Markdown;
import com.ibm.fhir.model.type.Quantity;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.AdministrativeGender;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.EncounterStatus;
import com.ibm.fhir.model.type.code.TestReportActionResult;
import com.ibm.fhir.model.type.code.TestReportResult;
import com.ibm.fhir.model.type.code.TestReportStatus;
import com.ibm.fhir.search.compartment.CompartmentUtil;

public class FHIRModelResolverTest {
  
    ModelResolver resolver = null;
    
    @BeforeMethod
    public void setup() {
        this.resolver = new FHIRModelResolver();
        resolver.setPackageName("com.ibm.fhir.model");
    }
    
    @Test
    public void testResolveAll400ModelInfoTypesSuccessfully() {
        resolveModelInfoTypes("FHIR", "4.0.0");
    }
    
    @Test
    public void testResolveAll401ModelInfoTypesSuccessfully() {
        resolveModelInfoTypes("FHIR", "4.0.1");
    }

    private void resolveModelInfoTypes(String id, String version) {
        final Set<String> nullResults = new HashSet<String>();
        
        visitModelInfoTypes(id, version, (ci) -> { 
            if( resolver.resolveType(ci.getName()) == null ) {
                nullResults.add(ci.getName());
            }
        });
       
        assertEquals( nullResults.size(), 0, StringUtils.join("\n", nullResults) );
    }
    
    private void visitModelInfoTypes(String id, String version, Consumer<ClassInfo> visitor) {
        ModelManager mm = new ModelManager();
        Model m = mm.resolveModel(new VersionedIdentifier().withId(id).withVersion(version));

        List<TypeInfo> typeInfos = m.getModelInfo().getTypeInfo();

        for (TypeInfo ti : typeInfos) {
            ClassInfo ci = (ClassInfo) ti;
            if (ci != null) {
                //log.info("Resolving {}", ci.getName());

                switch (ci.getName()) {
                // TODO: IBM FHIR Doesn't have a ResourceContainer type
                case "ResourceContainer":
                // Bugs in 4.0.1 model info
                case "DataElement constraint on ElementDefinition data type":
                case "question":
                case "allowedUnits":
                    continue;
                }

                // Also bugs in the 4.0.1 model info
                if (ci.getBaseType() == null) {
                    continue;
                }

                switch (ci.getBaseType()) {
                // Abstract classes
                case "FHIR.BackboneElement":
                //case "FHIR.Element":
                    continue;
                }

                // TODO: The cause of failure for this is unknown.
                // Need to figure out if it's a gap in HAPI,
                // or if a manual mapping is required, or what.
                switch (ci.getName()) {
                case "ItemInstance":
                    continue;
                }

                visitor.accept(ci);
            }
        }
    }
    
    @Test
    public void testUpconvertUrlTypesSuccessfully() throws Exception {
        Map<String,String> targets = new LinkedHashMap<>();
        targets.put("Url", "http://nowhere.io");
        targets.put("Canonical", "http://somewhere.com/fhir/Resource/123|1.2.3");
        targets.put("Uuid", "urn:uuid:1020304f-9999-aaaa-0a1b-1234567890ab");
        targets.put("Oid", "urn:oid:2.16.840.1.113762.1.4.1114.7");
        
        
        for( Map.Entry<String,String> target : targets.entrySet() ) {
            Uri expected = Uri.of(target.getValue());

            Class<?> clazz = Class.forName( "com.ibm.fhir.model.type." + target.getKey() );
            Uri result = (Uri) resolver.as(expected, clazz, false);
            assertNotNull(result);
            assertTrue( clazz.isInstance( result ) );
            assertEquals( expected.getValue(), result.getValue() );
        }
    }
    
    @Test
    public void testUpconvertIntegerTypesSuccessfully() throws Exception {
        Map<String,Integer> targets = new LinkedHashMap<>();
        targets.put("PositiveInt", 1234567890);
        targets.put("UnsignedInt", 987654321);
        
        
        for( Map.Entry<String,Integer> target : targets.entrySet() ) {
            com.ibm.fhir.model.type.Integer expected = com.ibm.fhir.model.type.Integer.of(target.getValue());

            Class<?> clazz = Class.forName( "com.ibm.fhir.model.type." + target.getKey() );
            com.ibm.fhir.model.type.Integer result = (com.ibm.fhir.model.type.Integer) resolver.as(expected, clazz, false);
            assertNotNull(result);
            assertTrue( clazz.isInstance( result ) );
            assertEquals( expected.getValue(), result.getValue() );
        }
    }
    
    @Test
    public void testUpconvertStringTypesSuccessfully() throws Exception {
        Map<String,String> targets = new LinkedHashMap<>();
        targets.put("Code", "married");
        targets.put("Markdown", "##Instructions\n1. Do something\n2.Do Another Thing\n");
        targets.put("Id", "1829384756");
        
        
        for( Map.Entry<String,String> target : targets.entrySet() ) {
            com.ibm.fhir.model.type.String expected = com.ibm.fhir.model.type.String.of(target.getValue());

            Class<?> clazz = Class.forName( "com.ibm.fhir.model.type." + target.getKey() );
            com.ibm.fhir.model.type.String result = (com.ibm.fhir.model.type.String) resolver.as(expected, clazz, false);
            assertNotNull(result);
            assertTrue( clazz.isInstance( result ) );
            assertEquals( expected.getValue(), result.getValue() );
        }
    }
    
    @Test
    public void testUpconvertQuantityTypesSuccessfully() throws Exception {
        Map<String,Triple<String,Integer,String>> targets = new LinkedHashMap<>();
        targets.put("Age", Triple.of("age", 10, "years"));
        targets.put("Distance", Triple.of("distance", 50, "miles"));
        targets.put("Duration", Triple.of("duration", 100, "ms"));
        targets.put("Count", Triple.of("count", 150, "each"));
        targets.put("SimpleQuantity", Triple.of("simple", 200, "code"));
        targets.put("MoneyQuantity", Triple.of("money", 250, "USD"));
        
        
        for( Map.Entry<String,Triple<String,Integer,String>> target : targets.entrySet() ) {
            Code code = Code.of(target.getValue().getLeft());
            Decimal value = Decimal.of(target.getValue().getMiddle());
            com.ibm.fhir.model.type.String unit = com.ibm.fhir.model.type.String.of(target.getValue().getRight());
            Quantity expected = Quantity.builder().code(code).unit(unit).value(value).build();

            Class<?> clazz = Class.forName( "com.ibm.fhir.model.type." + target.getKey() );
            Quantity result = (Quantity) resolver.as(expected, clazz, false);
            assertNotNull(result);
            assertTrue( clazz.isInstance( result ) );
            assertEquals( expected.getCode(), result.getCode() );
            assertEquals( expected.getValue(), result.getValue() );
        }
    }
    
    @Test
    public void testResolvePatientContextSuccess() throws Exception {
        //List of resources that don't have a clear patient reference
        Set<String> excludes = new HashSet<>();
        excludes.add("Provenance");
        excludes.add("SupplyRequest");
        excludes.add("Group");
        excludes.add("Schedule");
        excludes.add("AuditEvent");
        
        String contextType = "Patient";
        for( String resourceType : CompartmentUtil.getCompartmentResourceTypes(contextType) ) {
            if( ! excludes.contains(resourceType) ) {
                System.out.println("Checking " + resourceType);
                Object path = resolver.getContextPath(contextType, resourceType);
                assertNotNull(path);
                System.out.println("\t"+path);
            }
        }
    }
    
    @Test
    public void testResolvePatientContextForAppointmentSuccess() throws Exception {
        String result = (String) resolver.getContextPath("Patient", "Appointment");
        assertNotNull(result);
        assertEquals( "participant.actor", result );
    }
    
    @Test
    public void testResolvePatientContextForAllergyIntoleranceSuccess() throws Exception {
        String result = (String) resolver.getContextPath("Patient", "AllergyIntolerance");
        assertNotNull(result);
        assertEquals( "patient", result );
    }
    
    @Test
    public void testResolvePatientContextForCommunicationRequestSuccess() throws Exception {
        String result = (String) resolver.getContextPath("Patient", "CommunicationRequest");
        assertNotNull(result);
        assertEquals( "subject", result );
    }
    
    @Test
    public void testResolvePatientContextForConditionSuccess() throws Exception {
        Object o = resolver.getContextPath("Patient", "Condition");
        assertEquals( "subject", o );
    }
    
    @Test
    public void testResolvePatientGenderSuccess() throws Exception {
        Patient p = Patient.builder().gender(AdministrativeGender.MALE).build();
        Object o = resolver.resolvePath(p, "gender.value");
        assertEquals( "male", o );
    }
    
    @Test
    public void testResolveCurrencyCode() throws Exception {
        Class<?> clazz = resolver.resolveType("CurrencyCode");
        assertNotNull( clazz );
    }
    
    @Test
    public void testResolveTypeUri() throws Exception {
        Class<?> clazz = resolver.resolveType("Uri");
        assertNotNull( clazz, "Failed to resolve type" );
    }

    @Test
    public void testResolveTypeString() throws Exception {
        Class<?> clazz = resolver.resolveType("string");
        assertNotNull( clazz, "Failed to resolve type" );
    }    
    
    @Test
    public void testResolvePathExtensionUrlSimple() throws Exception {
        Extension extension = Extension.builder().url("http://somewhere.com/profile/Something").build();
        Object result = resolver.resolvePath(extension, "url");
        assertNotNull( result, "Null result" );
        assertEquals( result.getClass(), com.ibm.fhir.model.type.Uri.class );
    }
    
    @Test
    public void testResolvePathExtensionUrlCompound() throws Exception {
        Extension extension = Extension.builder().url("http://somewhere.com/profile/Something").build();
        Patient patient = john_doe().extension(extension).build();

        Object result = resolver.resolvePath(patient, "extension[0].url");
        assertNotNull( result, "Null result" );
        assertEquals( result.getClass(), com.ibm.fhir.model.type.Uri.class );
    }

    @Test
    public void testResolveResourceIdSimple() throws Exception {
        Patient patient = john_doe().build();
        Object result = resolver.resolvePath(patient, "id");
        assertNotNull( result, "Null result" );
        assertEquals( result.getClass(), java.lang.String.class );
    }
    
    @Test
    public void testResolveResourceIdCompound() throws Exception {
        Patient patient = john_doe().build();
        Bundle bundle = Bundle.builder().type(BundleType.SEARCHSET).entry(Bundle.Entry.builder().resource(patient).build()).build();
        Object result = resolver.resolvePath(bundle, "entry[0].resource.id");
        assertNotNull( result, "Null result" );
        assertEquals( result.getClass(), java.lang.String.class );
    }

    @Test
    public void testResolveElementIdSimple() throws Exception {
        Patient patient = john_doe().build();
        Object result = resolver.resolvePath(patient, "name[0].id");
        assertNotNull( result, "Null result" );
        assertEquals( result.getClass(), java.lang.String.class );
    }
    
    @Test
    public void testResolveElementIdCompound() throws Exception {
        Patient patient = john_doe().build();
        Object result = resolver.resolvePath( patient.getName().get(0), "id");
        assertNotNull( result, "Null result" );
        assertEquals( result.getClass(), java.lang.String.class );
    }
    
    @Test
    public void resolveContextPathPatientAppointment() {
        String result = (String) resolver.getContextPath("Patient", "Appointment");
        assertNotNull(result);
        assertEquals("participant.actor", result);
    }

    @Test
    public void testResolvePathPatientGender() {
        Patient p = john_doe().build();

        String result = (String) resolver.resolvePath(p, "gender.value");
        assertNotNull(result);
        assertEquals("male", result);
    }
    
    @Test
    public void testResolvePathPatientBirthDateValue() {
        Patient p = john_doe().build();

        Object result = resolver.resolvePath(p, "birthDate.value");
        assertNotNull(result);
        assertTrue(result instanceof org.opencds.cqf.cql.engine.runtime.Date, "Unexpected class " + result.getClass().getName());
    }
    
    @Test
    public void resolvePathPatientDeceasedChoice() {
        Patient p = john_doe().deceased(DateTime.now()).build();
        
        Object result = resolver.resolvePath(p, "deceased");
        assertNotNull( result, "Null result" );
    }
    
    @Test
    public void resolveEncounterClass() {
        Encounter enc = Encounter.builder()
                .status( EncounterStatus.FINISHED )
                .clazz( Coding.builder()
                    .code( Code.of("test") ).build() )
                .build();
                
        Object result = resolver.resolvePath(enc, "class");
        assertNotNull( result, "Null result" );
    }
    
    @Test
    public void resolveTestReportAssertClass() {
        TestReport.Setup.Action action = TestReport.Setup.Action.builder()
            ._assert(
                TestReport.Setup.Action.Assert.builder()
                    .message(Markdown.of("markdown"))
                    .result(TestReportActionResult.PASS)
                    .build()
                ).build();
        
        TestReport report = TestReport.builder()
                .status(TestReportStatus.COMPLETED)
                .setup( TestReport.Setup.builder().action(action).build() )
                .testScript(Reference.builder().reference(fhirstring("TestScript/123")).build())
                .result(TestReportResult.PASS)
                .build();

        for( String path : Arrays.asList("setup", "setup.action", "setup.action[0].assert" ) ) {
            assertNotNull(resolver.resolvePath(report, path), path);
        }
    }
    
    @Test
    public void testToCqlTemporalLocalDate() {
        LocalDate expected = LocalDate.of(2013, 12, 6);
        Date date = Date.of(expected);
        
        Patient pat = john_doe().birthDate(date).build();
        Object resolved = resolver.resolvePath(pat, "birthDate.value");
        assertNotNull(resolved);
        
        assertTrue( resolved instanceof org.opencds.cqf.cql.engine.runtime.Date );
        org.opencds.cqf.cql.engine.runtime.Date actual = (org.opencds.cqf.cql.engine.runtime.Date) resolved;
        assertEquals( actual.getDate(), expected );
    }

    
    protected Patient.Builder john_doe() {
        return Patient.builder().id("123")
                .gender(AdministrativeGender.MALE)
                .name(HumanName.builder().id("human-name").text(fhirstring("John Doe")).build())
                .birthDate(Date.of("1969-02-15"));
    }
}
 