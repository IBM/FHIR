/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.opencds.cqf.cql.engine.runtime.Code;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Measure;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.type.Attachment;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.RelatedArtifact;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.PublicationStatus;
import com.ibm.fhir.model.type.code.RelatedArtifactType;

/**
 * Utilities for working with FHIR model objects. This consists
 * mainly of null-safe factory objects for creating FHIR types
 * from Java types, but also some null-safe helpers for going the
 * opposite direction (FHIR to Java), and some factory methods
 * for creating resources that are commonly used in the execution
 * of CQL and FHIR Quality Measure operations.
 */
public class ModelHelper {

    /**
     * Convert a CQL Code object into a FHIR code object
     * 
     * @param code
     *            CQL Code
     * @return FHIR Code
     */
    public static com.ibm.fhir.model.type.Code fhircode(Code code) {
        return fhircode(code.getCode());
    }

    /**
     * Perform null-safe conversion of a Java String to a FHIR Code
     * 
     * @param code
     *            Java code (null-safe)
     * @return FHIR Code or null if the input was null
     */
    public static com.ibm.fhir.model.type.Code fhircode(String code) {
        com.ibm.fhir.model.type.Code fhirCode = com.ibm.fhir.model.type.Code.builder().value(code).build();
        return fhirCode;
    }
    
    /**
     * Perform null-safe conversion of a Java Integer to a FHIR Integer
     * 
     * @param value
     *            Java Integer (null-safe)
     * @return FHIR Integer or null if the input was null
     */
    public static com.ibm.fhir.model.type.Integer fhirinteger(Integer value) {
        if (value != null) {
            return com.ibm.fhir.model.type.Integer.of(value);
        } else {
            return null;
        }
    }

    /**
     * Perform null-safe conversion of a Java String to a FHIR String
     * 
     * @param str
     *            Java string (null-safe)
     * @return FHIR String or null if the input was null
     */
    public static com.ibm.fhir.model.type.String fhirstring(String str) {
        if (str != null) {
            return com.ibm.fhir.model.type.String.of(str);
        } else {
            return null;
        }
    }

    /**
     * Perform null-safe conversion of a Java String to a FHIR Uri
     * 
     * @param uri
     *            Java string (null-safe)
     * @return FHIR Uri or null if the input was null
     */
    public static com.ibm.fhir.model.type.Uri fhiruri(String uri) {
        if (uri != null) {
            return com.ibm.fhir.model.type.Uri.of(uri);
        } else {
            return null;
        }
    }

    /**
     * Perform null-safe conversion of a Java boolean to a FHIR String
     * 
     * @param Java
     *            string (null-safe)
     * @return FHIR String or null if the input was null
     */
    public static com.ibm.fhir.model.type.Boolean fhirboolean(Boolean bool) {
        if (bool != null) {
            return com.ibm.fhir.model.type.Boolean.of(bool);
        } else {
            return null;
        }
    }

    /**
     * Perform null-safe conversion of a FHIR String to a Java String
     * 
     * @param str
     *            FHIR string (null-safe)
     * @return Java String or null if the input was null
     */
    public static String javastring(com.ibm.fhir.model.type.String str) {
        if (str != null) {
            return str.getValue();
        } else {
            return null;
        }
    }

    /**
     * Perform null-safe conversion of a FHIR Uri to a Java String
     * 
     * @param uri
     *            FHIR uri (null-safe)
     * @return Java String or null if the input was null
     */
    public static String javastring(com.ibm.fhir.model.type.Uri uri) {
        if (uri != null) {
            return uri.getValue();
        } else {
            return null;
        }
    }

    /**
     * Perform null-safe conversion of a FHIR Boolean to a Java Boolean
     * 
     * @param bool
     *            FHIR boolean (null-safe)
     * @return Java Boolean or null if the input was null
     */
    public static Boolean javaboolean(com.ibm.fhir.model.type.Boolean bool) {
        if (bool != null) {
            return bool.getValue();
        } else {
            return null;
        }
    }
    
    /**
     * Factory method for creating a simple Coding object
     * 
     * @param codesystem
     *            CodeSystem URI
     * @param code
     *            Code
     * @param display
     *            Display
     * @return FHIR Coding
     */
    public static Coding coding(String codesystem, String code, String display) {
        return Coding.builder().system(fhiruri(codesystem)).code(fhircode(code)).display(fhirstring(display)).build();
    }    

    /**
     * Factory method for creating a simple Coding object
     * 
     * @param codesystem
     *            CodeSystem URI
     * @param code
     *            Code
     * @return FHIR Coding
     */
    public static Coding coding(String codesystem, String code) {
        return Coding.builder().system(fhiruri(codesystem)).code(fhircode(code)).build();
    }
    
    /**
     * Factory method for creating a simple Coding object consisting
     * only of a code value
     * 
     * @param code
     *            Code
     * @return FHIR Coding
     */
    public static Coding coding(String code) {
        return Coding.builder().code(fhircode(code)).build();
    }
    
    /**
     * Factory method for creating a Concept consisting of a single Coding object
     * with only a code value
     * 
     * @param code
     *            Code
     * @return FHIR Concept
     */
    public static CodeableConcept concept(String code) {
        return concept(coding(code));
    }

    /**
     * Factory method for creating a Concept consisting of a single Coding object
     * 
     * @param codesystem
     *            CodeSystem URI
     * @param code
     *            Code
     * @return FHIR Concept
     */
    public static CodeableConcept concept(String codesystem, String code) {
        return concept(coding(codesystem, code));
    }

    /**
     * Factory method for creating a simple Concept object
     * 
     * @param coding
     *            Coding
     * @return FHIR Concept
     */
    public static CodeableConcept concept(Coding coding) {
        return CodeableConcept.builder().coding(coding).build();
    }

    /**
     * Factory method for creating a RelatedArtifact
     * 
     * @param type
     *            RelatedArtifactType
     * @param uri
     *            URI of the related resource
     * @param version
     *            version of the related resource
     * @return RelatedArtifact
     */
    public static RelatedArtifact relatedArtifact(RelatedArtifactType type, com.ibm.fhir.model.type.Uri uri, com.ibm.fhir.model.type.String version) {
        return RelatedArtifact.builder().type(type).resource(canonical(uri, version)).build();
    }
    

    /**
     * Factory method for creating a RelatedArtifact
     * 
     * @param type
     *            RelatedArtifactType
     * @param uri
     *            URI of the related resource
     * @return RelatedArtifact
     */
    public static RelatedArtifact relatedArtifact(RelatedArtifactType type, String uri) {
        return RelatedArtifact.builder().type(type).resource(canonical(Uri.of(uri))).build();
    }    

    /**
     * Factory method for creating a RelatedArtifact
     * 
     * @param type
     *            RelatedArtifactType
     * @param uri
     *            URI of the related resource
     * @param version
     *            version of the related resource
     * @return RelatedArtifact
     */
    public static RelatedArtifact relatedArtifact(RelatedArtifactType type, String uri, String version) {
        return RelatedArtifact.builder().type(type).resource(canonical(Uri.of(uri), fhirstring(version))).build();
    }
    
    /**
     * Factory method for creating a RelatedArtifact
     * 
     * @param type
     *            RelatedArtifactType
     * @param uri
     *            URI of the related resource
     * @return RelatedArtifact
     */
    public static RelatedArtifact relatedArtifact(RelatedArtifactType type, Canonical uri) {
        return RelatedArtifact.builder().type(type).resource(uri).build();
    }

    /**
     * Factory method for creating a Canonical
     * 
     * @param measure
     *            Measure resource
     * @return Canonical URL
     */
    public static Canonical canonical(Measure measure) {
        return canonical( measure.getUrl(), measure.getVersion() );
    }  
    
    /**
     * Factory method for creating a Canonical
     * 
     * @param library
     *            Library resource
     * @return Canonical URL
     */
    public static Canonical canonical(Library library) {
        return canonical( library.getUrl(), library.getVersion() );
    }    


    /**
     * Factory method for creating a Canonical
     * 
     * @param uri
     *            URI of the related resource
     * @return Canonical URL
     */
    public static Canonical canonical(String uri) {
        return canonical( fhiruri(uri), null );
    }    
    
    /**
     * Factory method for creating a Canonical
     * 
     * @param uri
     *            URI of the related resource
     * @return Canonical URL
     */
    public static Canonical canonical(Uri uri) {
        return canonical( uri, null );
    }
    
    /**
     * Factory method for creating a Canonical
     * 
     * @param uri
     *            URI of the related resource
     * @param version
     *            null-safe version of the related resource
     * @return Canonical URL consisting of URL and then "|" + version if version is non-null
     */
    public static Canonical canonical(Uri uri, com.ibm.fhir.model.type.String version) {
        StringBuilder url = new StringBuilder(uri.getValue());
        if (version != null) {
            url.append("|");
            url.append(version.getValue());
        }
        return Canonical.of(url.toString());
    }
    
    /**
     * Factory method for creating a server-relative Reference to a Resource
     * 
     * @param resource
     *            resource
     * @return server-relative Reference
     */
    public static Reference reference(Resource resource) {
        StringBuilder ref = new StringBuilder(resource.getClass().getSimpleName());
        ref.append("/");
        ref.append(resource.getId());

        return Reference.builder().reference(fhirstring(ref.toString())).build();
    }

    /**
     * Helper method for retrieving a Library attachment with a specific content type. Attachment
     * types are assumed to be singleton and an exception will be thrown if more than one Attachment
     * with the same type is found.
     * 
     * @param library
     *            Library resource
     * @param contentType
     *            ContentType string for the attachment to retrieve
     * 
     * @return Optional Attachment
     */
    public static Optional<Attachment> getAttachmentByType(Library library, String contentType) {
        Optional<Attachment> result = library.getContent().stream().filter(a -> a.getContentType().getValue().equals(contentType)).reduce((a, b) -> {
            throw new IllegalArgumentException(String.format("Found more than one attachment with the content type %s in library %s", contentType, library.getId()));
        });
        return result;
    }

    /**
     * Factory method for constructing a SearchSet bundle from an array of resources
     * 
     * @param resources
     *            Resources
     * @return Bundle
     */
    public static Bundle bundle(Resource... resources) {
        return bundle(BundleType.SEARCHSET, resources);
    }

    /**
     * Factory method for constructing a bundle from an array of resources
     * 
     * @param bundleType
     *            BundleType
     * @param resources
     *            Resources
     * @return Bundle
     */
    public static Bundle bundle(BundleType type, Resource... resources) {
        Bundle.Builder builder = Bundle.builder().type(type);
        builder.total(UnsignedInt.of(resources.length));
        for (Resource resource : resources) {
            builder.entry(Bundle.Entry.builder().resource(resource).build());
        }
        return builder.build();
    }

    /**
     * Factory method for constructing a ValueSet resource that contains
     * an expansion with a single code.
     * 
     * @param codesystem
     *            CodeSystem URI
     * @param code
     *            Code
     * @return ValueSet
     */
    public static ValueSet valueset(String codesystem, String code) {
        return ValueSet.builder().id(UUID.randomUUID().toString()).status(PublicationStatus.ACTIVE).expansion(ValueSet.Expansion.builder().timestamp(DateTime.now()).contains(ValueSet.Expansion.Contains.builder().system(fhiruri(codesystem)).code(fhircode(code)).build()).build()).build();
    }

    /**
     * Helper method for retrieving a Bundle link by a specific link type. Bundle links
     * are assumed to be singleton instances and an exception will be thrown if more
     * than one of the same type are found.
     * 
     * @param bundle
     *            Bundle
     * @param type
     *            link type to retrieve
     * @return Optional link
     */
    public static Optional<String> getLinkByType(Bundle bundle, String type) {
        return bundle.getLink().stream().filter(l -> l.getRelation().getValue().equals(type)).map(l -> l.getUrl().getValue()).reduce((a, b) -> {
            throw new IllegalStateException(String.format("Multiple '%s' links found", type));
        });
    }

    /**
     * Retrieve the Element extension that matches the given URL. Extension elements
     * are assumed to be singleton values.
     * 
     * @param element
     *            FHIR Element
     * @return Optional containing the extension if found
     */
    public static Optional<Extension> getExtensionByUrl(Element element, String url) {
        Optional<Extension> result = Optional.empty();
        if (element.getExtension() != null) {
            result = element.getExtension().stream().filter(e -> e.getUrl().equals(url)).reduce((x, y) -> {
                throw new IllegalStateException(url + " must occur only once");
            });
        }
        return result;
    }

    /**
     * For the given Element, retrieve the timezone offset from the Element's extensions based on
     * HL7 tz-offset and tz-code extensions.
     * 
     * See <a href="https://www.hl7.org/fhir/extension-tz-offset-definitions.html">FHIR Extension Timezone Offset</a>
     * 
     * @param element
     *            FHIR Element (date, dateTime, instant)
     * @return Java ZoneOffset using data from the provided Element's extensions
     */
    public static ZoneOffset getZoneOffset(Element element) {
        ZoneOffset result = null;

        Optional<Extension> timezoneExt = ModelHelper.getExtensionByUrl(element, Constants.EXT_TIMEZONE_OFFSET);

        if (timezoneExt.isPresent()) {
            com.ibm.fhir.model.type.String timezoneCode = (com.ibm.fhir.model.type.String) timezoneExt.get().getValue();
            result = ZoneOffset.of(timezoneCode.getValue());
        } else {
            timezoneExt = ModelHelper.getExtensionByUrl(element, Constants.EXT_TIMEZONE_CODE);
            if (timezoneExt.isPresent()) {
                com.ibm.fhir.model.type.String timezoneCode = (com.ibm.fhir.model.type.String) timezoneExt.get().getValue();
                ZoneId zoneId = ZoneId.of(timezoneCode.getValue());
                result = LocalDateTime.now().atZone(zoneId).getOffset();
            }
        }
        return result;
    }
}
