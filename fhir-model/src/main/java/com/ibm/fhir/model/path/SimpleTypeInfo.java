/*
 * (C) Copyright IBM Corp. 2019
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.path;

import java.util.Objects;

/**
 * This class is part of the implementation for the Types and Reflection section of the FHIRPath specification:
 * @see <a href="http://hl7.org/fhirpath/2018Sep/index.html#types-and-reflection">FHIRPath Types and Reflection</a>
 */
public class SimpleTypeInfo implements TypeInfo {
    private final String namespace;
    private final String name;
    private final String baseType;
    
    public SimpleTypeInfo(String namespace, String name, String baseType) {
        this.namespace = Objects.requireNonNull(namespace);
        this.name = Objects.requireNonNull(name);
        this.baseType = Objects.requireNonNull(baseType);
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public String getName() {
        return name;
    }

    public String getBaseType() {
        return baseType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SimpleTypeInfo other = (SimpleTypeInfo) obj;
        return Objects.equals(namespace, other.namespace) && 
                Objects.equals(name, other.name) && 
                Objects.equals(baseType, other.baseType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(namespace, name, baseType);
    }
    
    @Override
    public String toString() {
        return String.format("SimpleTypeInfo { namespace: '%s', name: '%s', baseType: '%s' }", namespace, name, baseType);
    }
}