/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhircode;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.Test;
import org.mockito.MockedStatic;

import com.ibm.fhir.cql.helpers.LibraryHelper;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.type.Attachment;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.Coding;
import com.ibm.fhir.model.type.RelatedArtifact;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.RelatedArtifactType;
import com.ibm.fhir.registry.FHIRRegistry;

public class LibraryHelperTest {

    @Test
    public void testDeserializeElmAttachment() throws Exception {
        Library library = TestHelper.getTestLibraryResource("library-EXM104-8.2.000.json");

        List<org.cqframework.cql.elm.execution.Library> cqlLibrary = LibraryHelper.loadLibrary(null, library);
        assertNotNull(cqlLibrary);
    }

    @Test
    public void testLoadLibrariesIgnoreNonLogic() throws Exception {
        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = mock(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);

            Library template = TestHelper.getTestLibraryResource("library-EXM104-8.2.000.json");

            CodeableConcept valid = LibraryHelper.getLogicLibraryConcept();

            // valid type, valid content
            Library l1 =
                    TestHelper.buildBasicLibrary("1", "http://localhost/fhir/Library/first", "first", "1.0.0").type(valid).content(template.getContent()).build();
            when(mockRegistry.getResource(l1.getUrl().getValue(), Library.class)).thenReturn(l1);

            CodeableConcept irrelevant = getIrrelevantConcept();

            // irrelevant type, valid content
            Library l2 =
                    TestHelper.buildBasicLibrary("2", "http://localhost/fhir/Library/second", "second", "1.0.0").type(irrelevant).content(template.getContent()).build();
            when(mockRegistry.getResource(l2.getUrl().getValue(), Library.class)).thenReturn(l2);

            // all of the above plus a ValueSet
            Collection<RelatedArtifact> related =
                    Arrays.asList(l1.getUrl(), l2.getUrl(), Uri.of("http://localhost/fhir/ValueSet/1.2.3")).stream().map(url -> RelatedArtifact.builder().type(RelatedArtifactType.DEPENDS_ON).resource(Canonical.of(url.getValue())).build()).collect(Collectors.toList());

            // add in an irrelevant link type for completeness
            RelatedArtifact otherRelationType = RelatedArtifact.builder().type(RelatedArtifactType.CITATION).resource(Canonical.of("http://docs.org")).build();
            related.add(otherRelationType);

            Library parent =
                    TestHelper.buildBasicLibrary("parent", "http://localhost/fhir/Library/parent", "parent", "1.0.1").type(valid).relatedArtifact(related).content(template.getContent()).build();

            List<Library> actual = LibraryHelper.loadLibraries(parent);
            assertEquals(actual.size(), 2);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnsupportedContent() {
        Library library =
                TestHelper.buildBasicLibrary("3", "http://localhost/fhir/Library/FHIR-Model", "FHIR-Model", "4.0.1").type(LibraryHelper.getLogicLibraryConcept()).content(Attachment.builder().contentType(fhircode("text/xml")).build()).build();

        LibraryHelper.loadLibrary(null, library);
    }

    private CodeableConcept getIrrelevantConcept() {
        CodeableConcept irrelevant =
                CodeableConcept.builder().coding(Coding.builder().system(Uri.of("http://nothing")).code(fhircode("ignore-me")).build()).build();
        return irrelevant;
    }

}
