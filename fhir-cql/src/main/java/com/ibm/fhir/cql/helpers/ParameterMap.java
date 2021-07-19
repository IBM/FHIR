/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.helpers;

import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;

/**
 * This is a helper class for indexing the contents of a FHIR Parameters
 * resource by name and accessing the contents of that index.
 */
public class ParameterMap extends ArrayListValuedHashMap<String, Parameter> {

    private static final long serialVersionUID = -6143143827215676862L;

    public ParameterMap(Parameters parameters) {
        indexParametersByName(parameters.getParameter());
    }

    public ParameterMap(List<Parameter> parameters) {
        indexParametersByName(parameters);
    }

    public MultiValuedMap<String, Parameter> indexParametersByName(Parameters parameters) {
        return indexParametersByName(parameters.getParameter());
    }

    public MultiValuedMap<String, Parameters.Parameter> indexParametersByName(List<Parameter> parameters) {
        parameters.stream().forEach(p -> put(p.getName().getValue(), p));
        return this;
    }

    public List<Parameter> getParameter(String paramName) {
        return get(paramName);
    }
    
    public Parameter getSingletonParameter(String paramName) {
        List<Parameter> values = getRequiredParameter(paramName);
        if( values.size() == 1 ) {
            return values.get(0);
        } else {
            throw new IllegalArgumentException(String.format("Found more than one value for parameter %s which was expected to be a singleton", paramName));
        }
    }

    public List<Parameter> getRequiredParameter(String paramName) {
        if ( ! containsKey(paramName) ) {
            throw new IllegalArgumentException("Missing required parameter " + paramName);
        }
        return get(paramName);
    }
    
    public Parameter getOptionalSingletonParameter(String paramName) {
        if ( containsKey(paramName) ) {
            return getSingletonParameter( paramName );
        } else {
            return null;
        }
    }
}
