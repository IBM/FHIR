/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.client.test.mains;

import static com.ibm.fhir.model.type.String.string;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Observation;
import com.ibm.fhir.model.resource.Observation.Component;
import com.ibm.fhir.model.resource.Patient;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.ContactPoint;
import com.ibm.fhir.model.type.Decimal;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.HumanName;
import com.ibm.fhir.model.type.Quantity;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.ContactPointSystem;
import com.ibm.fhir.model.type.code.ContactPointUse;
import com.ibm.fhir.model.type.code.ObservationStatus;
import com.ibm.fhir.provider.FHIRProvider;

public class JaxrsClientTestMain {
    
    public static void main(String[] args) throws Exception {
        Patient patient = buildPatient();
        System.out.println("\nJSON:");
        
        FHIRGenerator.generator( Format.JSON, false).generate(patient, System.out);
        System.out.println("\nXML:");
        FHIRGenerator.generator( Format.XML, false).generate(patient, System.out);
        
        Client client = ClientBuilder.newBuilder()
                .register(new FHIRProvider())
                .build();
        
        WebTarget target = client.target("http://localhost:9080/fhir-server/api/v4");
        Entity<Patient> entity = Entity.entity(patient, FHIRMediaType.APPLICATION_FHIR_XML);
        Response response = target.path("Patient").request().post(entity, Response.class);
        
        if (Response.Status.CREATED.getStatusCode() == response.getStatus()) {
            System.out.println("");
            System.out.println(response.getStatus());
            System.out.println(response.getStatusInfo().getReasonPhrase());
            String location = response.getLocation().toString();
            System.out.println("location: " + location);
            
            String id = location.substring(location.lastIndexOf("/") + 1);
            response = target.path("Patient/" + id).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
            patient = response.readEntity(Patient.class);
            System.out.println("\nJSON:");
            FHIRGenerator.generator( Format.JSON, false).generate(patient, System.out);
            System.out.println("\nXML:");
            FHIRGenerator.generator( Format.XML, false).generate(patient, System.out);
                        
            Observation observation = buildObservation(id);
            System.out.println("\nJSON:");
            FHIRGenerator.generator( Format.JSON, false).generate(observation, System.out);
            System.out.println("\nXML:");
            FHIRGenerator.generator( Format.XML, false).generate(observation, System.out);
            
            Entity<Observation> observationEntity = Entity.entity(observation, FHIRMediaType.APPLICATION_FHIR_JSON);
            response = target.path("Observation").request().post(observationEntity, Response.class);
            
            if (Response.Status.CREATED.getStatusCode() == response.getStatus()) {
                System.out.println("");
                System.out.println(response.getStatus());
                System.out.println(response.getStatusInfo().getReasonPhrase());
                location = response.getLocation().toString();
                System.out.println("location: " + location);
                
                response = target.path("Observation").queryParam("subject", "Patient/" + id).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
                Bundle bundle = response.readEntity(Bundle.class);
                System.out.println("\nJSON:");
                FHIRGenerator.generator( Format.JSON, false).generate(bundle, System.out);
                System.out.println("\nXML:");
                FHIRGenerator.generator( Format.XML, false).generate(bundle, System.out);
            } else {
                System.out.println("");
                System.out.println(response.getStatus());
                System.out.println(response.getStatusInfo().getReasonPhrase());
                System.out.println(response.readEntity(String.class));
            }
        } else {
            System.out.println("");
            System.out.println(response.getStatus());
            System.out.println(response.getStatusInfo().getReasonPhrase());
            System.out.println(response.readEntity(String.class));
        }
    }
    
    public static Patient buildPatient() {
        Patient patient = Patient.builder().name(HumanName.builder()
                        .family(string("Doe"))
                        .given(string("John")).build())
                .birthDate(com.ibm.fhir.model.type.Date.of("1950-08-15"))
                .telecom(ContactPoint.builder().system(ContactPointSystem.PHONE)
                        .use(ContactPointUse.HOME).value(string("555-1234")).build())
                .extension(Extension.builder().url("http://ibm.com/fhir/extension/Patient/favorite-color")
                        .value(string("blue")).build()).build();        
        return patient;
    }
    
    public static Observation buildObservation(String patientId) {
        Observation observation = Observation.builder().status(ObservationStatus.FINAL).bodySite( 
                CodeableConcept.builder().coding(Coding.builder().code(Code.of("55284-4"))
                        .system(Uri.of("http://loinc.org")).build())
                        .text(string("Blood pressure systolic & diastolic")).build())
                .category(CodeableConcept.builder().coding(Coding.builder().code(Code.of("signs"))
                        .system(Uri.of("http://hl7.org/fhir/observation-category")).build())
                        .text(string("Vital Signs")).build())
                .subject(Reference.builder().reference(string("Patient/" + patientId)).build())
                .component(Component.builder().code(CodeableConcept.builder().coding(Coding.builder().code(Code.of("8459-0"))
                        .system(Uri.of("http://loinc.org")).build())
                        .text(string("Systolic")).build())
                        .value(Quantity.builder().value(Decimal.of(124.9)).unit(string("mmHg")).build()).build())
                .component(Component.builder().code(CodeableConcept.builder().coding(Coding.builder().code(Code.of("8453-3"))
                        .system(Uri.of("http://loinc.org")).build())
                        .text(string("Diastolic")).build())
                        .value(Quantity.builder().value(Decimal.of(93.7)).unit(string("mmHg")).build()).build())
             .build();           

        return observation;
    } 
}
