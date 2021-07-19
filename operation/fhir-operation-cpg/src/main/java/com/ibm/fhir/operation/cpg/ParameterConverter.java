/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import com.ibm.fhir.cql.engine.converter.FHIRTypeConverter;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.BackboneElement;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.code.BundleType;

public class ParameterConverter {
    private FHIRTypeConverter typeConverter;

    public ParameterConverter(FHIRTypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }
    
    public Parameter toParameter(String paramName, Map<String, Object> expressionResults) {
        Parameter.Builder returnParameter = Parameter.builder().name(fhirstring(paramName));
        for (Map.Entry<String, Object> entry : expressionResults.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            if( value != null && value instanceof Iterable ) {
                Iterable<?> iterable = (Iterable<?>) value;
                if( iterable.iterator().hasNext() ) {
                    // TODO - this is non-atomic in that a nested Tuple would cause an incomplete return value
                    iterable.forEach(v -> returnParameter.part(toParameter(v).name(fhirstring(name)).build()));
                } else { 
                    // this is what the cqf-ruler implementation does, though it is kind of funky
                    returnParameter.part(toParameter(fhirstring(iterable.toString())).name(fhirstring(name)).build());
                }
            } else {
                returnParameter.part(toParameter(value).name(fhirstring(name)).build());
            }
        }
        return returnParameter.build();
    }

    public Parameter.Builder toParameter(Object value) {
        Parameter.Builder builder = Parameter.builder();
        toParameter(builder, value);
        return builder;
    }

    @SuppressWarnings("unchecked")
    public Parameter.Builder toParameter(Parameter.Builder p, Object value) {
        if (value != null) {
            Object obj;
            
            try { 
                if( value instanceof Iterable ) {
                    obj = typeConverter.toFhirTypes((Iterable<Object>)value);
                } else { 
                    obj = typeConverter.toFhirType(value);
                }
            } catch(NotImplementedException nex) { 
                obj = fhirstring( nex.getMessage() );
            }

            if (obj instanceof BackboneElement ) {
                throw new IllegalArgumentException("Backbone elements are not supported for FHIR parameter conversion");
            } else if (obj instanceof Element ) {
                p.value((Element) obj);
            } else if (obj instanceof Resource) {
                p.resource((Resource) obj);
            } else if (obj instanceof Iterable) {
                Bundle.Builder bundle = Bundle.builder().type(BundleType.COLLECTION);
                
                Iterable<?> it = (Iterable<?>) obj;
                for (Object nestedObj : it) {
                    if( nestedObj instanceof Resource ) {
                        bundle.entry(Bundle.Entry.builder().resource((Resource)nestedObj).build());
                    } else {
                        // TODO - what do we do with a list of primitives?
                        throw new IllegalArgumentException("Only lists of resources are supported");
                    }
                }
                p.resource(bundle.build());
            } else {
                throw new IllegalArgumentException(String.format("Unexpected object of type '%s' produced by fhir type conversion"));
            }
        } else { 
            p.value(fhirstring("null"));
        }
        return p;
    }

    public Object toCql(Parameter p) {
        Object result = null;
        if (p.getValue() != null) {
            Element e = (Element) p.getValue();
            result = typeConverter.toCqlType(e);
        } else if (p.getResource() != null) {
            Resource r = (Resource) p.getResource();
            result = typeConverter.toCqlType(r);
        } else if (p.getPart() != null) {
            result = p.getPart().stream().map(np -> toCql(np)).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(String.format("Unexpected object of type %s specified as input parameter"));
        }
        return result;
    }
}
