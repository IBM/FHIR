/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cqf;

import static com.ibm.fhir.cql.helpers.ModelHelper.canonical;
import static com.ibm.fhir.cql.helpers.ModelHelper.concept;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhircode;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirinteger;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhiruri;
import static com.ibm.fhir.cql.helpers.ModelHelper.relatedArtifact;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.mockito.MockedStatic;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.cql.helpers.CqlBuilder;
import com.ibm.fhir.cql.helpers.LibraryHelper;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Attachment;
import com.ibm.fhir.model.type.Base64Binary;
import com.ibm.fhir.model.type.DataRequirement;
import com.ibm.fhir.model.type.ParameterDefinition;
import com.ibm.fhir.model.type.code.FHIRAllTypes;
import com.ibm.fhir.model.type.code.ParameterUse;
import com.ibm.fhir.model.type.code.PublicationStatus;
import com.ibm.fhir.model.type.code.RelatedArtifactType;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public abstract class BaseDataRequirementsOperationTest {

    public static final String URL_BASE = "http://test.com/fhir/";

    public abstract AbstractDataRequirementsOperation getOperation();

    public Parameters runTest(FHIROperationContext context, Class<? extends Resource> resource, Function<Library,String> fId, Function<Library, Parameters> fParams) throws Exception {
    
        try (MockedStatic<FHIRRegistry> staticRegistry = mockStatic(FHIRRegistry.class)) {
            FHIRRegistry mockRegistry = spy(FHIRRegistry.class);
            staticRegistry.when(FHIRRegistry::getInstance).thenReturn(mockRegistry);
            
            FHIRResourceHelpers resourceHelper = mock(FHIRResourceHelpers.class);
            
            Library primaryLibrary = initializeLibraries(mockRegistry, resourceHelper);
    
            Parameters outParameters = getOperation().doInvoke(context, resource, fId.apply(primaryLibrary), null, fParams.apply(primaryLibrary), resourceHelper);
            assertNotNull(outParameters);
            
            ParameterMap outMap = new ParameterMap(outParameters);
            Library module = (Library) outMap.getSingletonParameter(LibraryDataRequirementsOperation.PARAM_OUT_RETURN).getResource();
            assertNotNull(module);
            
            assertEquals( module.getType().getCoding().get(0).getCode().getValue(), Constants.LIBRARY_TYPE_MODEL_DEFINITION, "type" );
            assertTrue( module.getRelatedArtifact().size() >= 4, "relatedArtifacts" );
            assertEquals( module.getParameter().size(), 4, "parameters");
            assertEquals( module.getDataRequirement().size(), 2, "dataRequirements");
            
            return outParameters;
        }
    }

    protected Library initializeLibraries(FHIRRegistry mockRegistry, FHIRResourceHelpers resourceHelper) throws Exception {
        String cql = CqlBuilder.builder()
                .library("Test", "1.0.0")
                .using("FHIR", Constants.FHIR_VERSION)
                .include("FHIRHelpers", Constants.FHIR_VERSION)
                .include("SupplementalDataElements", "2.0.0")
                .context("Patient")
                .expression("BirthDate", "Patient.birthDate")
                .build();
    
        Library modelInfo = getFHIRModelInfo();
        Library fhirHelpers = getFHIRHelpers();
        Library sde = getSupplementalDataElementsLibrary();
        Library primaryLibrary = getPrimaryLibrary(cql, modelInfo, fhirHelpers, sde);
        List<Library> fhirLibraries = Arrays.asList(primaryLibrary, getSupplementalDataElementsLibrary(), getFHIRHelpers(), getFHIRModelInfo());
    
        when(resourceHelper.doRead(eq("Library"), eq(primaryLibrary.getId()), anyBoolean(), anyBoolean(), any())).thenAnswer(x -> TestHelper.asResult(primaryLibrary));
        
        fhirLibraries.stream().forEach( l -> when(mockRegistry.getResource( canonical(l.getUrl(), l.getVersion()).getValue(), Library.class )).thenReturn(l) );
        return primaryLibrary;
    }

    protected Library getPrimaryLibrary(String cql, Library modelInfo, Library fhirHelpers, Library sde) {
        Library.Builder builder = getTemplateLibrary(cql)
            .relatedArtifact( relatedArtifact(RelatedArtifactType.DEPENDS_ON, modelInfo.getUrl(), modelInfo.getVersion()) )
            .relatedArtifact( relatedArtifact(RelatedArtifactType.DEPENDS_ON, fhirHelpers.getUrl(), fhirHelpers.getVersion()) )
            .relatedArtifact( relatedArtifact(RelatedArtifactType.DEPENDS_ON, sde.getUrl(), sde.getVersion()) )
            .parameter( ParameterDefinition.builder().name(fhircode("Measurement Period")).min(fhirinteger(0)).max(fhirstring("1")).type(FHIRAllTypes.PERIOD).use(ParameterUse.IN).build())
            .parameter( ParameterDefinition.builder().name(fhircode("Patient")).min(fhirinteger(0)).max(fhirstring("1")).type(FHIRAllTypes.PATIENT).use(ParameterUse.OUT).build() )
            .parameter( ParameterDefinition.builder().name(fhircode("BirthDate")).min(fhirinteger(0)).max(fhirstring("1")).type(FHIRAllTypes.DATE).use(ParameterUse.OUT).build() )
            .dataRequirement( DataRequirement.builder().type( FHIRAllTypes.PATIENT ).profile(canonical("http://hl7.org/fhir/StructureDefinition/Patient")).build() );
        
        Library primaryLibrary = builder.build();
        return primaryLibrary;
    }

    protected Library getFHIRHelpers() {
        CqlBuilder cqlBuilder = CqlBuilder.builder()
                .library("FHIRHelpers", Constants.FHIR_VERSION)
                .using("FHIR", Constants.FHIR_VERSION)
                .context("Patient")
                .expression("ToString(Code code)", "code.value");
        
        String cql = cqlBuilder.build();
        
        return getTemplateLibrary("FHIRHelpers", Constants.FHIR_VERSION, cql)
                .relatedArtifact( relatedArtifact( RelatedArtifactType.DEPENDS_ON, URL_BASE + "FHIR-ModelInfo", Constants.FHIR_VERSION) )
                .build();
    }

    protected Library getFHIRModelInfo() {
        return getTemplateLibrary("FHIRModelInfo", Constants.FHIR_VERSION)
                .build();
    }

    protected Library getSupplementalDataElementsLibrary() {
        CqlBuilder cqlBuilder = CqlBuilder.builder()
                .library("SupplementalDataElements", "2.0.0")
                .using("FHIR", Constants.FHIR_VERSION)
                .include("FHIRHelpers", Constants.FHIR_VERSION)
                .context("Patient")
                .expression("SDE Payer", "[Coverage: type in \"Payer\"] Payer\n return { code: Payer.type, period, Payer.period }");
        
        String cql = cqlBuilder.build();
        
        return getTemplateLibrary("SupplementalDataElements", "2.0.0", cql)
                .parameter( ParameterDefinition.builder().name(fhircode("Patient")).min(fhirinteger(0)).max(fhirstring("1")).type(FHIRAllTypes.PATIENT).use(ParameterUse.OUT).build() )
                .parameter( ParameterDefinition.builder().name(fhircode("SDE Payer")).min(fhirinteger(0)).max(fhirstring("1")).type(FHIRAllTypes.ANY).use(ParameterUse.OUT).build() )
                .relatedArtifact( relatedArtifact( RelatedArtifactType.DEPENDS_ON, URL_BASE + "FHIR-ModelInfo", Constants.FHIR_VERSION) )
                .relatedArtifact( relatedArtifact( RelatedArtifactType.DEPENDS_ON, URL_BASE + "Library/FHIRHelpers", Constants.FHIR_VERSION) )
                .dataRequirement( DataRequirement.builder().type( FHIRAllTypes.PATIENT ).profile(canonical("http://hl7.org/fhir/StructureDefinition/Patient")).build() )
                .dataRequirement( DataRequirement.builder().type( FHIRAllTypes.COVERAGE ).profile(canonical("http://hl7.org/fhir/StructureDefinition/Coverage")).build() )
                .build();
    
    }

    protected Library.Builder getTemplateLibrary(String cql) {
        return getTemplateLibrary("Test", "1.0.0", cql);
    }

    protected Library.Builder getTemplateLibrary(String name, String version) {
        return getTemplateLibrary(name, version, null);
    }

    protected Library.Builder getTemplateLibrary(String name, String version, String cql) {
        Library.Builder builder = Library.builder()
                .id( name + "-" + version )
                .name(fhirstring(name))
                .version(fhirstring(version))
                .url(fhiruri(URL_BASE + "Library/" + name))
    
                .status(PublicationStatus.ACTIVE);
        if( cql != null ) {
            builder.type(LibraryHelper.getLogicLibraryConcept())
                .content( Attachment.builder()
                        .contentType(fhircode("text/cql"))
                        .data(Base64Binary.of(cql.getBytes()))
                        .build());
        } else {
            builder.type( concept( LibraryHelper.getModelDefinitionCoding() ) );
        }
        return builder;
    }

}
