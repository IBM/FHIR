/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.term.service.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.type.Element;

public final class FHIRTermServiceUtil {
    private FHIRTermServiceUtil() { }

    public static Parameter getParameter(Parameters parameters, String name) {
        return parameters.getParameter().stream()
                .filter(parameter -> parameter.getName() != null)
                .filter(parameter -> parameter.getName().getValue().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static <T extends Element> T getParameterValue(Parameters parameters, String name, Class<T> elementType) {
        return elementType.cast(getParameter(parameters, name));
    }

    public static List<Parameter> getParameters(Parameters parameters, String name) {
        return parameters.getParameter().stream()
                .filter(parameter -> parameter.getName() != null)
                .filter(parameter -> parameter.getName().getValue().equals(name))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public static <T extends Element> List<T> getParameterValues(Parameters parameters, String name, Class<T> elementType) {
        return getParameters(parameters, name).stream()
                .map(parameter -> elementType.cast(parameter.getValue()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public static Parameter getPart(Parameter parameter, String name) {
        return parameter.getPart().stream()
                .filter(part -> part.getName() != null)
                .filter(part -> part.getName().getValue().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static <T extends Element> T getPartValue(Parameter parameter, String name, Class<T> elementType) {
        return elementType.cast(getPart(parameter, name));
    }

    public static List<Parameter> getParts(Parameter parameter, String name) {
        return parameter.getPart().stream()
                .filter(part -> part.getName() != null)
                .filter(part -> part.getName().getValue().equals(name))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public static <T extends Element> List<T> getPartValues(Parameter parameter, String name, Class<T> elementType) {
        return getParts(parameter, name).stream()
                .map(part -> elementType.cast(part.getValue()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }
}
