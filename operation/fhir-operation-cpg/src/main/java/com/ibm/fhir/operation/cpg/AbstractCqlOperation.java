/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;
import static com.ibm.fhir.cql.helpers.ModelHelper.javastring;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.debug.DebugLibraryResultEntry;
import org.opencds.cqf.cql.engine.debug.DebugLocator;
import org.opencds.cqf.cql.engine.debug.DebugMap;
import org.opencds.cqf.cql.engine.debug.DebugResult;
import org.opencds.cqf.cql.engine.debug.DebugResultEntry;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.InMemoryLibraryLoader;
import org.opencds.cqf.cql.engine.execution.LibraryLoader;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;

import com.ibm.fhir.core.FHIRConstants;
import com.ibm.fhir.cql.engine.converter.FHIRTypeConverter;
import com.ibm.fhir.cql.engine.converter.impl.FHIRTypeConverterImpl;
import com.ibm.fhir.cql.engine.searchparam.SearchParameterResolver;
import com.ibm.fhir.cql.engine.server.retrieve.ServerFHIRRetrieveProvider;
import com.ibm.fhir.cql.engine.server.terminology.ServerFHIRTerminologyProvider;
import com.ibm.fhir.cql.helpers.DataProviderFactory;
import com.ibm.fhir.cql.helpers.LibraryHelper;
import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.cql.translator.CqlTranslationProvider;
import com.ibm.fhir.cql.translator.FHIRLibraryLibrarySourceProvider;
import com.ibm.fhir.cql.translator.impl.InJVMCqlTranslationProvider;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.server.operation.spi.AbstractOperation;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public abstract class AbstractCqlOperation extends AbstractOperation {
    
    public static final String PARAM_IN_EXPRESSION = "expression";
    public static final String PARAM_IN_PARAMETERS = "parameters";
    public static final String PARAM_IN_SUBJECT = "subject";
    public static final String PARAM_IN_DEBUG= "debug";
    public static final String PARAM_OUT_RETURN = "return";
    public static final String PARAM_OUT_DEBUG_RESULT = "debugResult";
    
    protected Parameters doEvaluation(FHIRResourceHelpers resourceHelper, ParameterMap paramMap, Library primaryLibrary) {
        List<Library> libraries = LibraryHelper.loadLibraries(primaryLibrary);
        return doEvaluation(resourceHelper, paramMap, libraries);
    }
    
    protected Parameters doEvaluation(FHIRResourceHelpers resourceHelper, ParameterMap paramMap, List<Library> libraries) {
        Library primaryLibrary = libraries.get(0);
        LibraryLoader ll = createLibraryLoader(libraries);
        
        FHIRTypeConverter typeConverter = new FHIRTypeConverterImpl();
        ParameterConverter parameterConverter = new ParameterConverter(typeConverter);

        TerminologyProvider termProvider = new ServerFHIRTerminologyProvider(resourceHelper);

        SearchParameterResolver resolver = new SearchParameterResolver();
        ServerFHIRRetrieveProvider retrieveProvider = new ServerFHIRRetrieveProvider(resourceHelper, resolver);
        retrieveProvider.setExpandValueSets(false); // TODO - use server config settings
        retrieveProvider.setTerminologyProvider(termProvider);
        retrieveProvider.setPageSize(FHIRConstants.FHIR_PAGE_SIZE_DEFAULT_MAX); // TODO - use server config settings?

        Map<String, DataProvider> dataProviders = DataProviderFactory.createDataProviders(retrieveProvider);

        VersionedIdentifier vid = new VersionedIdentifier();
        vid.withId(javastring(primaryLibrary.getName()));
        vid.withVersion(javastring(primaryLibrary.getVersion()));

        Pair<String, Object> context = getCqlContext(paramMap);

        Map<String, Object> engineParameters = getCqlParameters(parameterConverter, paramMap);

        Set<String> expressions = getCqlExpressionsToEvaluate(paramMap);
        
        DebugMap debugMap = getDebugMap(paramMap);

        // TODO - add configuration support for the CQL engine's local time zone
        CqlEngine engine = new CqlEngine(ll, dataProviders, termProvider);
        EvaluationResult evaluationResult = engine.evaluate(vid, expressions, context, engineParameters, debugMap);
        
        Parameters.Builder output = Parameters.builder()
                .parameter(parameterConverter.toParameter(PARAM_OUT_RETURN, evaluationResult.expressionResults));
        
        if( debugMap != null) {
            // Need to experiment with how valuable this is right now. There are a couple of issues 
            // that I noted. Most importantly, I don't think this information will be available when
            // the engine throws an exception (e.g. NPE). Beyond that, the information that is available
            // is not ordered, so it can't be used to track the actual path through the engine. Lastly,
            // the data captured is incomplete. The best path currently is to use the logging produced 
            // in System.out
            Parameter debugResult = convertDebugResultToParameter(evaluationResult);
            output.parameter(debugResult);
        }
        
        return output.build();
    }

    @SuppressWarnings("unused")
    private Parameter convertDebugResultToParameter(EvaluationResult evaluationResult) {
        Parameter.Builder debugResultBuilder = Parameter.builder().name(fhirstring(PARAM_OUT_DEBUG_RESULT));
        
        Parameter debugResultParameter = null;
        DebugResult debugResult = evaluationResult.getDebugResult();
        if( debugResult != null ) {
            Parameters.Builder debugResultEntries = Parameters.builder();
            for( Map.Entry<String,DebugLibraryResultEntry> libraryResult : debugResult.getLibraryResults().entrySet() ) {
                String libraryId = libraryResult.getKey();
                DebugLibraryResultEntry librayResultEntry = libraryResult.getValue();
                
                for( Map.Entry<DebugLocator,List<DebugResultEntry>> e : librayResultEntry.getResults().entrySet() ) {
                    DebugLocator dl = e.getKey();
                    //The DebugResultEntry class captures no object state right now, so it is useless
                    debugResultEntries.parameter( Parameter.builder().name(fhirstring(libraryId)).value(fhirstring(dl.getLocatorType().toString() + "|" + dl.getLocator())).build() );
                }
                debugResultBuilder.resource(debugResultEntries.build());
            }
            debugResultParameter = debugResultBuilder.build();
        }
        return debugResultParameter;
    }



    protected Pair<String, Object> getCqlContext(ParameterMap paramMap) {
        Pair<String, Object> context = null;
        Parameter subjectParam = paramMap.getSingletonParameter(PARAM_IN_SUBJECT);
        if (subjectParam != null) {
            context = getCqlContext(context, subjectParam);
        }
        return context;
    }

    protected Pair<String, Object> getCqlContext(Pair<String, Object> context, Parameter subjectParam) {
        String ref = javastring(((com.ibm.fhir.model.type.String) subjectParam.getValue()));
        String[] parts = ref.split("/");
        if (parts.length >= 2) {
            context = Pair.of(parts[parts.length - 2], parts[parts.length - 1]);
        } else {
            throw new IllegalArgumentException(String.format("Invalid format for subject parameter '%s'. Value must contain a resource type and ID.", ref));
        }
        return context;
    }

    protected Map<String, Object> getCqlParameters(ParameterConverter converter, ParameterMap paramMap) {
        Map<String, Object> engineParameters = null;
        Parameter parametersParam = paramMap.getOptionalSingletonParameter(PARAM_IN_PARAMETERS);
        if (parametersParam != null) {
            engineParameters = getCqlEngineParameters(converter, parametersParam);
        }
        return engineParameters;
    }

    protected Map<String, Object> getCqlEngineParameters(ParameterConverter converter, Parameter parametersParam) {
        Map<String, Object> engineParameters;
        engineParameters = new HashMap<>();

        Parameters nested = (Parameters) parametersParam.getResource();
        for (Parameter engineParam : nested.getParameter()) {
            Object converted = converter.toCql(engineParam);
            engineParameters.put(javastring(engineParam.getName()), converted);
        }
        return engineParameters;
    }

    protected abstract Set<String> getCqlExpressionsToEvaluate(ParameterMap paramMap);

    protected LibraryLoader createLibraryLoader(List<Library> libraries) {  
        FHIRLibraryLibrarySourceProvider sourceProvider = new FHIRLibraryLibrarySourceProvider(libraries);
        CqlTranslationProvider translator = new InJVMCqlTranslationProvider(sourceProvider);
        
        Collection<org.cqframework.cql.elm.execution.Library> result = libraries.stream()
                .flatMap( fl -> LibraryHelper.loadLibrary(translator, fl).stream() )
                .filter( Objects::nonNull )
                .collect(Collectors.toCollection(
                    () -> new TreeSet<>( CqlLibraryComparator.INSTANCE ) ));
        return new InMemoryLibraryLoader(result);
    }
    
    protected DebugMap getDebugMap(ParameterMap paramMap) {
        DebugMap debugMap = null;
        Parameter pDebug = paramMap.getOptionalSingletonParameter(PARAM_IN_DEBUG);
        if( pDebug != null && ((com.ibm.fhir.model.type.Boolean) pDebug.getValue()).getValue().equals(Boolean.TRUE) ) {
            debugMap = new DebugMap();
            debugMap.setIsLoggingEnabled(true);
            //debugMap.setIsCoverageEnabled(true);
        }
        return debugMap;
    }
}
