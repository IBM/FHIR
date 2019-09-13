/**
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watson.health.fhir.model.util;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.ibm.watson.health.fhir.model.format.Format;
import com.ibm.watson.health.fhir.model.generator.FHIRGenerator;
import com.ibm.watson.health.fhir.model.generator.exception.FHIRGeneratorException;
import com.ibm.watson.health.fhir.model.resource.Resource;

public final class JsonSupport {
    private static final JsonReaderFactory JSON_READER_FACTORY = Json.createReaderFactory(null);
    
    private static final Map<Class<?>, Set<String>> ELEMENT_NAME_MAP = buildElementNameMap(false);
    private static final Map<Class<?>, Set<String>> REQUIRED_ELEMENT_NAME_MAP = buildElementNameMap(true);
    
    private JsonSupport() { }

    /**
     * Calling this method allows us to load/initialize this class during startup.
     */
    public static void init() { }

    private static Map<Class<?>, Set<String>> buildElementNameMap(boolean required) {
        Map<Class<?>, Set<String>> elementNameMap = new LinkedHashMap<>();
        for (Class<?> modelClass : ModelSupport.getModelClasses()) {
            if (ModelSupport.isPrimitiveType(modelClass)) {
                continue;
            }
            Set<String> elementNames = new LinkedHashSet<>();
            for (String elementName : ModelSupport.getElementNames(modelClass)) {
                if (required && !ModelSupport.isRequiredElement(modelClass, elementName)) {
                    continue;
                }
                if (ModelSupport.isChoiceElement(modelClass, elementName)) {
                    for (Class<?> choiceElementType : ModelSupport.getChoiceElementTypes(modelClass, elementName)) {
                        String choiceElementName = ModelSupport.getChoiceElementName(elementName, choiceElementType);
                        elementNames.add(choiceElementName);
                        if (ModelSupport.isPrimitiveType(choiceElementType)) {
                            elementNames.add("_" + choiceElementName);
                        }
                    }
                } else {
                    elementNames.add(elementName);
                    Class<?> elementType = ModelSupport.getElementType(modelClass, elementName);
                    if (ModelSupport.isPrimitiveType(elementType)) {
                        elementNames.add("_" + elementName);
                    }
                }
            }
            elementNameMap.put(modelClass, Collections.unmodifiableSet(elementNames));
        }
        return Collections.unmodifiableMap(elementNameMap);
    }

    public static Set<String> getElementNames(Class<?> type) {
        return ELEMENT_NAME_MAP.getOrDefault(type, Collections.emptySet());
    }
    
    public static Set<String> getRequiredElementNames(Class<?> type) {
        return REQUIRED_ELEMENT_NAME_MAP.getOrDefault(type, Collections.emptySet());
    }
    
    public static JsonArray getJsonArray(JsonObject jsonObject, String key) {
        return getJsonArray(jsonObject, key, false);
    }
    
    public static JsonArray getJsonArray(JsonObject jsonObject, String key, boolean primitive) {
        JsonArray jsonArray = getJsonValue(jsonObject, key, JsonArray.class);
        if (primitive) {
            if (jsonArray == null) {
                JsonArray _jsonArray = jsonObject.getJsonArray("_" + key);
                if (_jsonArray != null) {
                    throw new IllegalArgumentException("Found array with key '_" + key + "' but could not find matching array with key: '" + key + "'");
                }
            }
        }
        return jsonArray;
    }

    public static JsonValue getJsonValue(JsonArray jsonArray, int index) {
        if (jsonArray != null) {
            if (index >= 0 && index < jsonArray.size()) {
                return jsonArray.get(index);
            } else {
                throw new IllegalArgumentException("Could not find element at index: " + index);
            }
        }
        return null;
    }
    
    public static <T extends JsonValue> T getJsonValue(JsonObject jsonObject, String key, Class<T> expectedType) {
        JsonValue jsonValue = jsonObject.get(key);
        if (jsonValue != null && !expectedType.isInstance(jsonValue)) {
            throw new IllegalArgumentException("Expected: " + expectedType.getSimpleName() + " but found: " + jsonValue.getValueType());
        }
        return expectedType.cast(jsonValue);
    }
    
    // TODO: replace this method with a class that converts Resource to JsonObject directly
    public static JsonObject toJsonObject(Resource resource) throws FHIRGeneratorException {
        StringWriter writer = new StringWriter();
        FHIRGenerator.generator(Format.JSON).generate(resource, writer);
        return JSON_READER_FACTORY.createReader(new StringReader(writer.toString())).readObject();
    }
    
    public static Reader nonClosingReader(Reader reader) {
        return new FilterReader(reader) {
            @Override
            public void close() {
                // do nothing
            }
        };
    }
    
    public static InputStream nonClosingInputStream(InputStream in) {
        return new FilterInputStream(in) {
            @Override
            public void close() {
                // do nothing
            }
        };
    }
    
    public static Writer nonClosingWriter(Writer writer) {
        return new FilterWriter(writer) {
            @Override
            public void close() {
                // do nothing
            }
        };
    }
    
    public static OutputStream nonClosingOutputStream(OutputStream out) {
        return new FilterOutputStream(out) {
            @Override
            public void close() {
                // do nothing
            }
        };
    }
    
    public static void checkForUnrecognizedElements(Class<?> type, JsonObject jsonObject) {
        Set<java.lang.String> elementNames = JsonSupport.getElementNames(type);
        for (java.lang.String key : jsonObject.keySet()) {
            if (!elementNames.contains(key) && !"resourceType".equals(key) && !"fhir_comments".equals(key)) {
                throw new IllegalArgumentException("Unrecognized element: '" + key + "'");
            }
        }
    }

    public static Class<?> getResourceType(JsonObject jsonObject) {
        JsonString resourceTypeString = jsonObject.getJsonString("resourceType");
        if (resourceTypeString == null) {
            throw new IllegalArgumentException("Missing required element: 'resourceType'");
        }
        String resourceTypeName = resourceTypeString.getString();
        Class<?> resourceType = ModelSupport.getResourceType(resourceTypeName);
        if (resourceType == null) {
            throw new IllegalArgumentException("Invalid resource type: '" + resourceTypeName + "'");
        }
        return resourceType;
    }
}
