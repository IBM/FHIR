/*
 * (C) Copyright IBM Corp. 2016,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.operation.util;

import static com.ibm.fhir.model.type.String.string;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.Date;
import com.ibm.fhir.model.type.DateTime;
import com.ibm.fhir.model.type.Id;
import com.ibm.fhir.model.type.Instant;
import com.ibm.fhir.model.type.Oid;
import com.ibm.fhir.model.type.PositiveInt;
import com.ibm.fhir.model.type.Time;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.Url;
import com.ibm.fhir.model.type.Uuid;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.model.type.code.OperationParameterUse;

public class FHIROperationUtil {

    private FHIROperationUtil() {
    }

    public static Parameters getInputParameters(OperationDefinition definition,
        Map<String, List<String>> queryParameters) throws FHIROperationException {
        try {
            Parameters.Builder parametersBuilder = Parameters.builder();
            parametersBuilder.id("InputParameters");
            if (definition != null) {
                for (OperationDefinition.Parameter parameter : definition.getParameter()) {
                    if (OperationParameterUse.IN.getValue().equals(parameter.getUse().getValue())) {
                        String name = parameter.getName().getValue();
                        String typeName = parameter.getType().getValue();
                        List<String> values = queryParameters.get(name);
                        if (values != null) {
                            String value = values.get(0);
                            Parameter.Builder parameterBuilder =
                                    Parameter.builder().name(string(name));
                            if ("boolean".equals(typeName)) {
                                parameterBuilder.value(com.ibm.fhir.model.type.Boolean.of(value));
                            } else if ("integer".equals(typeName)) {
                                parameterBuilder.value(com.ibm.fhir.model.type.Integer.of(value));
                            } else if ("string".equals(typeName)) {
                                parameterBuilder.value(com.ibm.fhir.model.type.String.of(value));
                            } else if ("decimal".equals(typeName)) {
                                parameterBuilder.value(com.ibm.fhir.model.type.Decimal.of(value));
                            } else if ("uri".equals(typeName)) {
                                parameterBuilder.value(Uri.of(value));
                            } else if ("url".equals(typeName)) {
                                parameterBuilder.value(Url.of(value));
                            } else if ("canonical".equals(typeName)) {
                                parameterBuilder.value(Canonical.of(value));
                            } else if ("instant".equals(typeName)) {
                                parameterBuilder.value(Instant.of(value));
                            } else if ("date".equals(typeName)) {
                                parameterBuilder.value(Date.of(value));
                            } else if ("dateTime".equals(typeName)) {
                                parameterBuilder.value(DateTime.of(value));
                            } else if ("time".equals(typeName)) {
                                parameterBuilder.value(Time.of(value));
                            } else if ("code".equals(typeName)) {
                                parameterBuilder.value(Code.of(value));
                            } else if ("oid".equals(typeName)) {
                                parameterBuilder.value(Oid.of(value));
                            } else if ("id".equals(typeName)) {
                                parameterBuilder.value(Id.of(value));
                            } else if ("unsignedInt".equals(typeName)) {
                                parameterBuilder.value(UnsignedInt.of(value));
                            } else if ("positiveInt".equals(typeName)) {
                                parameterBuilder.value(PositiveInt.of(value));
                            } else if ("uuid".equals(typeName)) {
                                parameterBuilder.value(Uuid.of(value));
                            } else {
                                throw new FHIROperationException("Invalid parameter type: '"
                                        + typeName + "'");
                            }
                            parametersBuilder.parameter(parameterBuilder.build());
                        }
                    }
                }
            }
            return parametersBuilder.build();
        } catch (FHIROperationException e) {
            throw e;
        } catch (Exception e) {
            // Originally returned 500 when it should be 400 (it's on the client). 
            
            FHIROperationException operationException =
                    new FHIROperationException("Unable to process query parameters", e);

            List<Issue> issues = new ArrayList<>();
            Issue.Builder builder = Issue.builder();
            builder.code(IssueType.INVALID);
            builder.diagnostics(string("Unable to process query parameters"));
            builder.severity(IssueSeverity.ERROR);
            issues.add(builder.build());

            operationException.setIssues(issues);
            
            throw operationException;
        }
    }


    public static Parameters getInputParameters(OperationDefinition definition, Resource resource)
        throws Exception {
        Parameters.Builder parametersBuilder = Parameters.builder();
        parametersBuilder.id("InputParameters");
        for (OperationDefinition.Parameter parameterDefinition : definition.getParameter()) {
            String parameterTypeName = parameterDefinition.getType().getValue();
            String resourceTypeName = resource.getClass().getSimpleName();
            if ((resourceTypeName.equals(parameterTypeName) || "Resource".equals(parameterTypeName))
                    && OperationParameterUse.IN.getValue().equals(parameterDefinition.getUse().getValue())) {
                Parameter.Builder parameterBuilder =
                        Parameter.builder().name(string(parameterDefinition.getName().getValue()));
                parametersBuilder.parameter(parameterBuilder.resource(resource).build());
            }
        }
        return parametersBuilder.build();
    }

    public static Parameters getOutputParameters(Resource resource) throws Exception {
        Parameters.Builder parametersBuilder = Parameters.builder();
        parametersBuilder.parameter(Parameter.builder().name(string("return")).resource(resource).build());
        return parametersBuilder.build();
    }
    
    public static Parameters getOutputParameters(String name, Resource resource) throws Exception {
        Parameters.Builder parametersBuilder = Parameters.builder();
        parametersBuilder.parameter(Parameter.builder().name(string(name)).resource(resource).build());
        return parametersBuilder.build();
    }

    public static boolean hasSingleResourceOutputParameter(Parameters parameters) {
        if (parameters == null) {
            return false;
        }
        return parameters.getParameter().size() == 1 && parameters.getParameter().get(0).getResource() != null;
    }

    public static Resource getSingleResourceOutputParameter(Parameters parameters)
        throws Exception {
        return parameters.getParameter().get(0).getResource();
    }
}
