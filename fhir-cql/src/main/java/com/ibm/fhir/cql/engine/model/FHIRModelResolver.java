/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.model;

import java.lang.reflect.Field;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.opencds.cqf.cql.engine.exception.InvalidCast;
import org.opencds.cqf.cql.engine.model.ModelResolver;

import com.ibm.fhir.cql.helpers.DateHelper;
import com.ibm.fhir.model.annotation.ReferenceTarget;
import com.ibm.fhir.model.type.Age;
import com.ibm.fhir.model.type.Canonical;
import com.ibm.fhir.model.type.Code;
import com.ibm.fhir.model.type.Count;
import com.ibm.fhir.model.type.Distance;
import com.ibm.fhir.model.type.Duration;
import com.ibm.fhir.model.type.Id;
import com.ibm.fhir.model.type.Markdown;
import com.ibm.fhir.model.type.MoneyQuantity;
import com.ibm.fhir.model.type.Oid;
import com.ibm.fhir.model.type.PositiveInt;
import com.ibm.fhir.model.type.Quantity;
import com.ibm.fhir.model.type.Reference;
import com.ibm.fhir.model.type.SimpleQuantity;
import com.ibm.fhir.model.type.UnsignedInt;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.Url;
import com.ibm.fhir.model.type.Uuid;
import com.ibm.fhir.model.util.ModelSupport;
import com.ibm.fhir.model.util.ModelSupport.ElementInfo;
import com.ibm.fhir.model.visitor.Visitable;
import com.ibm.fhir.path.FHIRPathNode;
import com.ibm.fhir.path.FHIRPathSystemValue;
import com.ibm.fhir.path.evaluator.FHIRPathEvaluator;
import com.ibm.fhir.path.exception.FHIRPathException;

/**
 * This is an implementation of the CQL Engine Model Resolver that supports
 * model resolution for IBM FHIR Server model objects.
 */
public class FHIRModelResolver implements ModelResolver {
    private static final Logger log = Logger.getLogger(FHIRModelResolver.class.getName());

    public static final String BASE_PACKAGE_NAME = "com.ibm.fhir.model";
    public static final String RESOURCE_PACKAGE_NAME = BASE_PACKAGE_NAME + ".resource";
    public static final String TYPE_PACKAGE_NAME = BASE_PACKAGE_NAME + ".type";
    public static final String CODE_PACKAGE_NAME = TYPE_PACKAGE_NAME + ".code";
    
    
    
    public static final String[] ALL_PACKAGES = new String[] {
            RESOURCE_PACKAGE_NAME,
            TYPE_PACKAGE_NAME,
            CODE_PACKAGE_NAME }; 

    public static final Pattern urlPattern = Pattern.compile("(^|.+\\.)url$");

    private static final Map<String, Class<?>> TYPE_MAP = buildTypeMap();

    private String packageName = BASE_PACKAGE_NAME;

    @Override
    public String getPackageName() {
        return this.packageName;
    }

    private static Map<String, Class<?>> buildTypeMap() {
        Map<String, Class<?>> typeMap = new LinkedHashMap<>();

        // add all model classes (resource / data types)
        for (Class<?> modelClass : ModelSupport.getModelClasses()) {
            String typeName = modelClass.getName()
                    .replace("com.ibm.fhir.model.resource.", "")
                    .replace("com.ibm.fhir.model.type.", "")
                    .replace("$", ".");
            if (ModelSupport.isPrimitiveType(modelClass)) {
                String lcfirst = typeName.substring(0, 1)
                        .toLowerCase()
                        .concat(typeName.substring(1));
                typeMap.put(lcfirst, modelClass);
            }
            typeMap.put(typeName, modelClass);
        }

        // add all code subtypes
        for (Class<?> codeSubtype : ModelSupport.getCodeSubtypes()) {
            typeMap.put(codeSubtype.getSimpleName(), codeSubtype);
        }

        return Collections.unmodifiableMap(typeMap);
    }

    private String toKey(String typeName) {
        switch (typeName) {
        case "NutritiionOrderIntent":
            return "NutritionOrderIntent";
        case "Messageheader_Response_Request":
        case "messageheaderResponseRequest":
            return "MessageHeaderResponseRequest";
        case "ConfidentialityClassification":
        case "vConfidentialityClassification":
            return "DocumentConfidentiality";
        case "ContractResourceStatusCodes":
            return "ContractStatus";
        case "EventStatus":
            return "ProcedureStatus";
        case "FinancialResourceStatusCodes":
            return "ClaimResponseStatus";
        case "SampledDataDataType":
            return "string";
        case "ClaimProcessingCodes":
            return "RemittanceOutcome";
        case "ContractResourcePublicationStatusCodes":
            return "ContractPublicationStatus";
        case "MedicationStatusCodes":
            return "MedicationStatus";
        case "ImmunizationEvaluationStatusCodes":
            return "ImmunizationEvaluationStatus";
        case "ImmunizationStatusCodes":
            return "ImmunizationStatus";
        case "ExpressionLanguage":
            return "Expression";
        case "CurrencyCode":
        case "MimeType":
        case "RequestResourceType":
            return "code";
        case "strandType":
        case "orientationType":
        case "repositoryType":
        case "qualityType":
        case "status":
        case "sequenceType":
            return typeName.substring(0, 1)
                    .toUpperCase()
                    .concat(typeName.substring(1));
        }
        return typeName;
    }

    @Override
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public Object resolvePath(Object target, String path ) {
        String[] identifiers = path.split("\\.");
        for (String identifier : identifiers) {
            // handling indexes: i.e. item[0].code
            if (identifier.contains("[")) {
                int index = Character.getNumericValue(identifier.charAt(identifier.indexOf("[") + 1));
                target = resolveProperty(target, identifier.replaceAll("\\[\\d\\]", ""));
                target = ((ArrayList<?>) target).get(index);
            } else {
                target = resolveProperty(target, identifier);
            }
        }
        
        return target;
    }
    
    private Object resolveProperty(Object target, String path) {
        Object value = null;
        
        if( target != null ) {
            if( target instanceof Visitable) {
                Visitable visitable = (Visitable) target;
                try {
                    // is there such a thing as a compile step? Do we need to cache the compile like we do with regex?
                    Collection<FHIRPathNode> result = FHIRPathEvaluator.evaluator().evaluate(visitable, path);
                    if( ! result.isEmpty() ) {
                        Class<?> clazz = target.getClass();
                        if( Code.class.isAssignableFrom(clazz) ) {
                            clazz = Code.class;
                        }
                        
                        ElementInfo elementInfo = ModelSupport.getElementInfo(clazz, path);
                        if( elementInfo.isRepeating() ) {
                            value = result.stream().map( n -> unpack(n, path) ).collect(Collectors.toList());
                        } else { 
                            value = unpack( result.iterator().next(), path);
                        }
                    }
                } catch( FHIRPathException fpex ) {
                    // intentionally empty
                }
            }
        }
        
        return value;
    }
    
    protected Object unpack(FHIRPathNode node, String path) {
        Object result = null;
        
        if( node.isResourceNode() ) {
            result = node.asResourceNode().resource();
        } else if( node.isElementNode() ) {
            result = node.asElementNode().element();
        } else if( node.isSystemValue() ) {
            FHIRPathSystemValue system = node.asSystemValue();
            if( system.isBooleanValue() ) {
                result = system.asBooleanValue()._boolean();
            } else if( system.isNumberValue() ) {
                result = system.asNumberValue().number();
            } else if( system.isQuantityValue() ) { 
                result = system.asQuantityValue().value();
            } else if( system.isStringValue() ) {
                result = system.asStringValue().string();
            } else if( system.isTemporalValue() ) {
                result = system.asTemporalValue().temporal();
            } else {
                throw new IllegalArgumentException("Unexpected node type " + node.type().toString() );
            }
        } else if( node.isTermServiceNode() ) {
            throw new UnsupportedOperationException("TermServiceNode");
        } else if( node.isTypeInfoNode() ) {
            result = node.asTypeInfoNode().typeInfo();
        }
        
        result = patchResult(path, result);
        
        return result;
    }



    protected Object patchResult(String path, Object result) {
        if( result instanceof java.lang.String && urlPattern.matcher(path).matches() ) {
            // Patch the model to match the CQL translator modelinfo expectations
            result = Uri.of((String) result);
        } else if( result instanceof TemporalAccessor ) {
            TemporalAccessor ta = (TemporalAccessor) result;
            result = DateHelper.toCqlTemporal(ta);
        } else if ( result instanceof byte[] ) {
            result = Base64.getEncoder().encode((byte[])result);
        } else if ( result instanceof Id ) {
            result = ((Id)result).getValue();
        }
        return result;
    }

    @Override
    public Object getContextPath(String contextType, String targetType) {
        Object result = null;
        if (targetType != null && contextType != null) {

            if (contextType != null && !(contextType.equals("Unspecified") || contextType.equals("Population"))) {
                if (targetType != null && contextType.equals(targetType)) {
                    result = "id";
                } else {
                    Class<?> clazz = resolveType(targetType);

                    if (clazz != null) {
                        Set<String> possibleAnswers = new HashSet<String>();
                        getReferenceTargets(clazz, contextType, "", possibleAnswers);

                        if (possibleAnswers.isEmpty()) {
                            throw new IllegalArgumentException(String.format("Unable to determine context path value for context '%s' in type '%s'", contextType, targetType));
                        } else if (possibleAnswers.size() == 1) {
                            result = possibleAnswers.iterator().next();
                        } else {
                            if (contextType.equals("Patient")) {
                                String[] bestGuesses = new String[] { "subject", "patient", "beneficiary" };
                                for (String guess : bestGuesses) {
                                    if (possibleAnswers.contains(guess)) {
                                        result = guess;
                                        break;
                                    }
                                }
                            }
                            if (result == null) {
                                throw new IllegalArgumentException(String.format("ContextPath '%s' is ambiguous for type '%s'. Possible answers are '%s'.", contextType, targetType, possibleAnswers.toString()));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private void getReferenceTargets(Class<?> clazz, String contextType, String prefix, Set<String> possibleAnswers) {
        while (clazz != null && clazz.getPackage().getName().startsWith(this.packageName)) {

            getReferenceTargetFromFields(clazz, contextType, prefix, possibleAnswers);

            for (Class<?> subclass : clazz.getDeclaredClasses()) {
                if (!subclass.getName().endsWith("$Builder")) {
                    String newPrefix = getPrefixed(prefix, StringUtils.uncapitalize(subclass.getSimpleName()));
                    getReferenceTargets(subclass, contextType, newPrefix, possibleAnswers);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    private void getReferenceTargetFromFields(Class<?> clazz, String contextType, String prefix, Set<String> possibleAnswers) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().isAssignableFrom(Reference.class)) {

                ReferenceTarget refTarget = field.getAnnotation(ReferenceTarget.class);
                if (refTarget != null) {
                    for (String target : refTarget.value()) {
                        if (target.equals(contextType)) {
                            possibleAnswers.add(getPrefixed(prefix, field.getName()));
                            break;
                        }
                    }
                } else {
                    // This handles the "Any" reference type
                    possibleAnswers.add(getPrefixed(prefix, field.getName()));
                }
            }
        }
    }

    private String getPrefixed(String prefix, String suffix) {
        if (prefix != null && prefix.length() > 0) {
            return prefix + "." + suffix;
        } else {
            return suffix;
        }
    }

    @Override
    public Class<?> resolveType(String typeName) {
        Class<?> result = TYPE_MAP.get(toKey(typeName));
        if (result == null) {
            if (log.isLoggable(Level.WARNING)) {
                log.warning("Failed to resolve type '" + typeName + "'");
            }
        }
        return result;
    }

    @Override
    public Class<?> resolveType(Object value) {
        Class<?> result = null;

        if (value != null) {
            result = resolveType(value.getClass().getSimpleName());
        } else {
            result = Object.class;
        }

        return result;
    }

    @Override
    public Boolean is(Object value, Class<?> type) {
        Boolean result = null;
        if (value != null) {
            result = type.isAssignableFrom(value.getClass());
        }

        return result;
    }

    @Override
    public Object as(Object value, Class<?> type, boolean isStrict) {
        Object result = null;
        if (value != null) {
            if (type.isAssignableFrom(value.getClass())) {
                result = value;
            } else {
                if (value instanceof Uri) {
                    Uri uri = (Uri) value;
                    switch (type.getSimpleName()) {
                    case "Url":
                        result = Url.of(uri.getValue());
                        break;
                    case "Canonical":
                        result = Canonical.of(uri.getValue());
                        break;
                    case "Uuid":
                        result = Uuid.of(uri.getValue());
                        break;
                    case "Oid":
                        result = Oid.of(uri.getValue());
                    }
                } else if (value instanceof com.ibm.fhir.model.type.Integer) {
                    com.ibm.fhir.model.type.Integer integer = (com.ibm.fhir.model.type.Integer) value;
                    switch (type.getSimpleName()) {
                    case "PositiveInt":
                        result = PositiveInt.of(integer.getValue());
                        break;
                    case "UnsignedInt":
                        result = UnsignedInt.of(integer.getValue());
                        break;
                    }
                } else if (value instanceof com.ibm.fhir.model.type.String) {
                    com.ibm.fhir.model.type.String string = (com.ibm.fhir.model.type.String) value;
                    switch (type.getSimpleName()) {
                    case "Code":
                        result = Code.of(string.getValue());
                        break;
                    case "Markdown":
                        result = Markdown.of(string.getValue());
                        break;
                    case "Id":
                        result = Id.of(string.getValue());
                        break;
                    }
                } else if (value instanceof Quantity) {
                    Quantity quantity = (Quantity) value;
                    Quantity.Builder builder = null;
                    switch (type.getSimpleName()) {
                    case "Age":
                        builder = Age.builder();
                        break;
                    case "Distance":
                        builder = Distance.builder();
                        break;
                    case "Duration":
                        builder = Duration.builder();
                        break;
                    case "Count":
                        builder = Count.builder();
                        break;
                    case "SimpleQuantity":
                        builder = SimpleQuantity.builder();
                        break;
                    case "MoneyQuantity":
                        builder = MoneyQuantity.builder();
                        break;
                    }
                    if (builder != null) {
                        // TODO - what about units? Should we also set those. The original code doesn't
                        result = builder.code(quantity.getCode()).value(quantity.getValue()).build();
                    }
                }
            }
        }

        if (result == null && isStrict) {
            throw new InvalidCast(String.format("Cannot cast a value of type %s as %s.", value.getClass().getName(), type.getName()));
        }

        return result;
    }

    @Override
    public Object createInstance(String typeName) {
        throw new UnsupportedOperationException("IBM FHIR model types must be constructed with a value or children");
        // Object result = null;
        //
        // Class<?> clazz = resolveType(typeName);
        // if (clazz != null) {
        // // IBM FHIR model objects are constructed using nested builder classes
        // // They require a value and it isn't clear how we would provide that given
        // // the inputs provided.
        // try {
        // Method builderMethod = clazz.getMethod("builder");
        // if( builderMethod != null ) {
        // Builder<?> builder = (Builder<?>) builderMethod.invoke(null);
        // builder.id( UUID.randomUUID().toString() );
        // result = builder.build();
        // }
        // } catch (Exception iaex) {
        // throw new RuntimeException(iaex);
        // }
        // }
        //
        // return result;
    }

    @Override
    public void setValue(Object target, String path, Object value) {
        // TODO - consider implementing a pattern where new objects are constructed
        // each time this method is called. The models have the ability
        // to convert themselves to a builder using a toBuilder() function
        // which can them be appended to as needed.
        throw new UnsupportedOperationException("IBM FHIR model types are immutable");
    }

    @Override
    public Boolean objectEqual(Object left, Object right) {
        Boolean result = null;
        if (left != null && right != null) {
            result = left.equals(right);
        }
        return result;
    }

    @Override
    public Boolean objectEquivalent(Object left, Object right) {
        Boolean result = null;

        if (left == null && right == null) {
            result = true;
        } else if (left == null) {
            result = false;
        } else {
            result = objectEqual(left, right);
        }

        return result;
    }

}
