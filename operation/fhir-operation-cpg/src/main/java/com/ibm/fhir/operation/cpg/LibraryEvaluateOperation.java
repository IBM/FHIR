/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ibm.fhir.cql.helpers.ParameterMap;
import com.ibm.fhir.cql.translator.CqlTranslationException;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.Library;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.CodeableConcept;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.registry.FHIRRegistry;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;

public class LibraryEvaluateOperation extends AbstractCqlOperation {

    private static final Logger logger = Logger.getLogger(LibraryEvaluateOperation.class.getName());

    @Override
    protected OperationDefinition buildOperationDefinition() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("OperationDefinition-cpg-library-evaluate.json")) {
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
        String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper)
        throws FHIROperationException {

        Parameters result = null;

        ParameterMap paramMap = new ParameterMap(parameters);

        try {
            Library primaryLibrary = null;
            if (operationContext.getType().equals(FHIROperationContext.Type.INSTANCE)) {
                SingleResourceResult<?> readResult = resourceHelper.doRead(ResourceType.LIBRARY.getValue(), logicalId, true, false, null);
                primaryLibrary = (Library) readResult.getResource();
            } else if (operationContext.getType().equals(FHIROperationContext.Type.RESOURCE_TYPE)) {
                Parameter param = paramMap.getSingletonParameter("library");
                String canonicalURL = ((com.ibm.fhir.model.type.Uri) param.getValue()).getValue();
                primaryLibrary = FHIRRegistry.getInstance().getResource(canonicalURL, Library.class);
            } else {
                throw new UnsupportedOperationException("This operation must be invoked in the context of the Library resource");
            }

            if (primaryLibrary == null) {
                throw new IllegalArgumentException("failed to resolve library");
            } else {          
                result = doEvaluation(resourceHelper, paramMap, primaryLibrary);
            }

        } catch (FHIROperationException fex) {
            throw fex;
        } catch (IllegalArgumentException | CqlTranslationException iex) {
            logger.log(Level.SEVERE, "Bad Request", iex);
            throw new FHIROperationException(iex.getMessage(), iex).withIssue(Issue.builder().severity(IssueSeverity.ERROR).code(IssueType.INVALID).details(CodeableConcept.builder().text(fhirstring(iex.getMessage())).build()).build());
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Evaluation failed", ex);
            throw new FHIROperationException("Evaluation failed", ex);
        }

        return result;
    }
    
    @Override
    protected Set<String> getCqlExpressionsToEvaluate(ParameterMap paramMap) {
        Set<String> expressions = null;
        if( paramMap.containsKey(PARAM_IN_EXPRESSION) ) {
            List<Parameter> expressionsParams = paramMap.getParameter(PARAM_IN_EXPRESSION);
            if (expressionsParams != null) {
                expressions = expressionsParams.stream().map(p -> ((com.ibm.fhir.model.type.String) p.getValue()).getValue()).collect(Collectors.toSet());
            }
        }
        return expressions;
    }
    
}
