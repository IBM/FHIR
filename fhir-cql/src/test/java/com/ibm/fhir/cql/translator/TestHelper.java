/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.translator;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhircode;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhiruri;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Library.Builder;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Attachment;
import com.ibm.fhir.model.type.Base64Binary;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.PublicationStatus;

public class TestHelper {

    public static Builder buildBasicLibrary(String id, String url, String name, String version) {
        Library.Builder builder =
                Library.builder().id(id).url(Uri.of(url)).name(fhirstring(name)).version(fhirstring(version)).status(PublicationStatus.ACTIVE);
        return builder;
    }

    public static Library getTestLibraryResource(String path) throws Exception {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path)) {
            return (Library) FHIRParser.parser(Format.JSON).parse(is);
        }
    }

    public static Resource getTestResource(String path) throws Exception {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(path)) {
            return FHIRParser.parser(Format.JSON).parse(is);
        }
    }
    
    public static <T> List<T> getBundleResources(Bundle bundle, Class<T> clazz) {
        return bundle.getEntry().stream().map(e -> e.getResource()).filter(r -> clazz.isInstance(r)).map(r -> clazz.cast(r) ).collect(Collectors.toList());
    }

    public static Attachment attachment(String mimeType, String libraryResource) throws Exception {
        byte[] buffer = null;
        try (InputStream is = ClassLoader.getSystemResourceAsStream(libraryResource)) {
            buffer = IOUtils.toByteArray(is);
        }

        return extracted(mimeType, buffer);
    }

    public static Attachment extracted(String mimeType, byte[] buffer) {
        return Attachment.builder().contentType(fhircode(mimeType)).data(Base64Binary.of(buffer)).build();
    }
    
    public static Bundle bundle(Resource... resources) {
        Bundle.Builder builder = Bundle.builder().type(BundleType.SEARCHSET);
        builder.total(UnsignedInt.of(resources.length));
        for( Resource resource : resources ) {
            builder.entry(Bundle.Entry.builder().resource(resource).build());
        }
        return builder.build();
    }
    
    public static Coding coding(String codesystem, String code) {
        return Coding.builder().system(fhiruri(codesystem)).code(fhircode(code)).build();
    }
    
    public static CodeableConcept concept(String codesystem, String code) {
        return concept( coding( codesystem, code ) );
    }
    
    public static CodeableConcept concept(Coding coding) {
        return CodeableConcept.builder().coding(coding).build();
    }
}
