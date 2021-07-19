/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.translator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.cqframework.cql.cql2elm.LibrarySourceProvider;
import org.hl7.elm.r1.VersionedIdentifier;

import com.ibm.fhir.cql.helpers.ModelHelper;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.type.Attachment;

/**
 * Implement a LibrarySourceProvider for the CQL to ELM translator that
 * can extract the CQL sources from a collection of preloaded FHIR Library
 * resources.
 */
public class FHIRLibraryLibrarySourceProvider implements LibrarySourceProvider {

    Map<String, Map<String, Library>> indexByName;

    public FHIRLibraryLibrarySourceProvider(List<Library> fhirLibraries) {
        indexByName = new HashMap<>();
        for (Library library : fhirLibraries) {
            if (library.getName() == null) {
                throw new IllegalArgumentException("Missing name for Library/" + library.getId());
            } else {
                Map<String, Library> indexByVersion = indexByName.computeIfAbsent(library.getName().getValue(), k -> new TreeMap<>(Comparator.reverseOrder()));

                String version = String.valueOf(Integer.MIN_VALUE);
                if (library.getVersion() != null) {
                    version = library.getVersion().getValue();
                }

                Library previous = indexByVersion.get(version);
                if (previous == null) {
                    indexByVersion.put(version, library);
                } else {
                    throw new IllegalArgumentException(String.format("Attempt was made to load the same library %s.%s twice", library.getName().getValue(), library.getVersion().getValue()));
                }
            }

        }
    }

    @Override
    public InputStream getLibrarySource(VersionedIdentifier libraryIdentifier) {
        Map<String, Library> indexByVersion = indexByName.get(libraryIdentifier.getId());
        if (indexByVersion == null) {
            throw new IllegalArgumentException("No libraries found matching ID " + libraryIdentifier.getId());
        } else {
            Library result = null;
            if (libraryIdentifier.getVersion() == null) {
                result = indexByVersion.values().iterator().next();
            } else {
                result = indexByVersion.get(libraryIdentifier.getVersion());
                if (result == null) {
                    throw new IllegalArgumentException(String.format("No libraries found matching ID %s and version %s", libraryIdentifier.getId(), libraryIdentifier.getVersion()));
                }
            }

            Optional<Attachment> cqlAttachment = ModelHelper.getAttachmentByType(result, "text/cql");
            if( cqlAttachment.isPresent() ) {
                return new ByteArrayInputStream(cqlAttachment.get().getData().getValue());
            } else { 
                throw new IllegalArgumentException("No cql/text attachment found in library " + libraryIdentifier.getId());
            }
        }
    }
}
