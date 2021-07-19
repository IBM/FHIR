/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.helpers;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhircode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opencds.cqf.cql.engine.execution.CqlLibraryReader;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.cql.translator.CqlTranslationException;
import com.ibm.fhir.cql.translator.CqlTranslationProvider;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.type.Attachment;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.RelatedArtifact;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.RelatedArtifactType;
import com.ibm.fhir.registry.FHIRRegistry;

/**
 * Helper methods for working with FHIR Library resources and 
 * specifically those that pertain to Clinical Quality Language (CQL)
 * evaluation.
 */
public class LibraryHelper {

    /**
     * Create a collection of the given library resource and all of its dependencies.
     * 
     * @param fhirLibrary
     *            root library in the tree of dependencies
     * @return List of Library resources corresponding to all dependencies.
     */
    public static List<Library> loadLibraries(Library fhirLibrary) {
        Set<String> visited = new HashSet<>();
        return loadLibraries(fhirLibrary, visited);
    }

    /**
     * Create a collection of the given library resource and all of its dependencies.
     * 
     * @param fhirLibrary
     *            root library in the tree of dependencies
     * @param visited
     *            set of canonical URLs corresponding to the library resources
     *            that have already been loaded. This allows us to
     *            prevent infinite loops.
     * @return List of Library resources corresponding to all dependencies.
     */
    protected static List<Library> loadLibraries(Library fhirLibrary, Set<String> visited) {
        List<Library> libraries = new ArrayList<>();
        libraries.add(fhirLibrary);
        visited.add(canonicalUrl(fhirLibrary));

        if (fhirLibrary.getRelatedArtifact() != null) {
            for (RelatedArtifact related : fhirLibrary.getRelatedArtifact()) {
                if (related.getType().equals(RelatedArtifactType.DEPENDS_ON)) {
                    Canonical canonical = related.getResource();
                    String canonicalURL = canonical.getValue();
                    if (!visited.contains(canonicalURL)) {
                        if (isLibraryReference(canonicalURL)) {
                            Library dependency = FHIRRegistry.getInstance().getResource(canonicalURL, Library.class);
                            if (dependency != null) {
                                if (isLogicLibrary(dependency)) {
                                    libraries.addAll(loadLibraries(dependency, visited));
                                }
                            } else {
                                throw new IllegalArgumentException("Failed to load dependency " + canonicalURL);
                            }
                        }
                    }
                }
            }
        }

        return libraries;
    }

    /**
     * Load the CQL Library from the attachment data associated with a FHIR Library resource.
     * 
     * @param translator
     *            CQL Translation Provider that will be used to translate CQL for Library resources with only CQL
     *            attachment data
     * @param fhirLibrary
     *            Library resource containing the attachment data to load
     * @return List of CQL Libraries that were translated. A single CQL file might output multiple Library objects based
     *         on the include definitions in the CQL.
     */
    public static List<org.cqframework.cql.elm.execution.Library> loadLibrary(CqlTranslationProvider translator, Library fhirLibrary) {
        List<org.cqframework.cql.elm.execution.Library> result = null;

        if (fhirLibrary.getContent() != null) {
            Optional<Attachment> elm = ModelHelper.getAttachmentByType(fhirLibrary, Constants.MIME_TYPE_APPLICATION_ELM_XML);
            if (elm.isPresent()) {
                result = Collections.singletonList(deserializeElm(fhirLibrary, elm.get()));
            } else {
                Optional<Attachment> cql = ModelHelper.getAttachmentByType(fhirLibrary, Constants.MIME_TYPE_TEXT_CQL);
                if (cql.isPresent()) {
                    try {
                        result = translator.translate(getAttachmentData(cql.get()));
                    } catch (CqlTranslationException cex) {
                        throw cex;
                    } catch( Exception ex ) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    throw new IllegalArgumentException(String.format("Library %s must contain either an application/elm+xml or text/cql attachment", fhirLibrary.getId()));
                }
            }
        }

        return result;
    }

    /**
     * Deserialize the contents of an application/elm+xml attachment.
     * 
     * @param fhirLibrary
     *            FHIR Library that contains the attachment
     * @param elm
     *            Attachment that contains the ELM data
     * @return CQL Library
     */
    public static org.cqframework.cql.elm.execution.Library deserializeElm(Library fhirLibrary, Attachment elm) {
        try {
            return CqlLibraryReader.read(getAttachmentData(elm));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to to deserialize ELM Attachment data for Library/" + fhirLibrary.getId(), ex);
        }
    }

    /**
     * Helper function for determining if a reference resource is a Library resource
     * 
     * @param resource
     *            Resource reference
     * @return true if the resource string refers to a library. Otherwise, false.
     */
    public static boolean isLibraryReference(String resource) {
        return resource.startsWith("Library/") || !resource.contains("/") || resource.contains("/Library/");
    }

    /**
     * Helper function for determining if a Library resource contains CQL logic. This is most correctly determined by
     * the Library.type property, but, in the absence of a type field, any Library that contains a CQL or ELM attachment
     * will be considered a logic library.
     * 
     * @param resource
     *            Resource reference
     * @return true if the resource string refers to a library. Otherwise, false.
     */
    public static boolean isLogicLibrary(Library fhirLibrary) {
        boolean result = false;

        if (fhirLibrary.getType() == null) {
            if (fhirLibrary.getContent() != null) {
                result = fhirLibrary.getContent().stream().filter(a -> a.getContentType() != null &&
                        (a.getContentType().equals(fhircode(Constants.MIME_TYPE_APPLICATION_ELM_XML)) ||
                                a.getContentType().equals(fhircode(Constants.MIME_TYPE_TEXT_CQL)))).collect(Collectors.counting()) > 0;
            }
        } else if (fhirLibrary.getType().getCoding() != null) {
            Coding needed = getLogicLibraryCoding();
            // can't use equals because we want to ignore display name for this purpose
            result = fhirLibrary.getType().getCoding().stream().filter(c -> c.getSystem().equals(needed.getSystem())
                    && c.getCode().equals(needed.getCode())).collect(Collectors.counting()) > 0;
        }
        return result;
    }

    /**
     * Helper method for generating a CodeableConcept that contains the logic-library
     * code.
     *  
     * @return CodeableConcept containing the logic-library code 
     */
    public static CodeableConcept getLogicLibraryConcept() {
        return CodeableConcept.builder().coding(getLogicLibraryCoding()).build();
    }

    /**
     * Helper method for generating a Coding that contains the logic-library
     * code.
     *  
     * @return Coding containing the logic-library code 
     */
    public static Coding getLogicLibraryCoding() {
        return getLibraryCoding(Constants.LIBRARY_TYPE_LOGIC_LIBRARY);
    }
    
    /**
     * Helper method for generating a Coding that contains the model-definition
     * code.
     *  
     * @return Coding containing the logic-library code 
     */
    public static Coding getModelDefinitionCoding() {
        return getLibraryCoding(Constants.LIBRARY_TYPE_MODEL_DEFINITION);
    }
    
    /**
     * Helper method for generating a Coding that contains the model-definition
     * code.
     *  
     * @return Coding containing the logic-library code 
     */
    public static Coding getLibraryCoding(String code) {
        return Coding.builder().system(Uri.of(Constants.HL7_TERMINOLOGY_LIBRARY_TYPE)).code(fhircode(code)).build();
    }

    /**
     * Helper method for converting an Attachment resource's contents into an InputStream
     *  
     * @return InputStream for reading the attachment data 
     */
    public static InputStream getAttachmentData(Attachment attachment) throws IOException {
        return new ByteArrayInputStream(attachment.getData().getValue());
    }

    /**
     * Helper method for building a canonical URL string from the relevant
     * fields in a Library resource.
     *  
     * @return canonical URL 
     */
    public static String canonicalUrl(Library library) {
        StringBuilder sb = new StringBuilder();
        
        if( library.getUrl() != null ) {
            sb.append(library.getUrl().getValue());
            if (library.getVersion() != null) {
                sb.append("|");
                sb.append(library.getVersion().getValue());
            }
        } else { 
            sb.append( library.getClass().getSimpleName() );
            sb.append( "/" );
            sb.append( library.getId() );
        }
        return sb.toString();
    }
}
