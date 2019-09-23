/*
 * (C) Copyright IBM Corp. 2019
 * 
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.path;

import java.util.Collection;
import java.util.stream.Stream;

import com.ibm.fhir.model.path.visitor.FHIRPathNodeVisitor;

public interface FHIRPathNode extends Comparable<FHIRPathNode> {
    String name();
    String path();
    FHIRPathType type();
    boolean hasValue();
    FHIRPathPrimitiveValue getValue();
    Collection<FHIRPathNode> children();
    Stream<FHIRPathNode> stream();
    Collection<FHIRPathNode> descendants();
    default boolean isComparableTo(FHIRPathNode other) {
        return false;
    }
    <T extends FHIRPathNode> boolean is(Class<T> nodeType);
    <T extends FHIRPathNode> T as(Class<T> nodeType);
    default boolean isElementNode() {
        return false;
    }
    default boolean isResourceNode() {
        return false;
    }
    default boolean isPrimitiveValue() {
        return false;
    }
    default boolean isTypeInfoNode() {
        return false;
    }
    default FHIRPathElementNode asElementNode() {
        return as(FHIRPathElementNode.class);
    }
    default FHIRPathResourceNode asResourceNode() {
        return as(FHIRPathResourceNode.class);
    }
    default FHIRPathPrimitiveValue asPrimitiveValue() {
        return as(FHIRPathPrimitiveValue.class);
    }
    default FHIRPathTypeInfoNode asTypeInfoNode() {
        return as(FHIRPathTypeInfoNode.class);
    }
    interface Builder { 
        Builder name(String name);
        Builder path(String path);
        Builder value(FHIRPathPrimitiveValue value);
        Builder children(FHIRPathNode... children);
        Builder children(Collection<FHIRPathNode> children);
        FHIRPathNode build();
    }
    <T> void accept(T param, FHIRPathNodeVisitor<T> visitor);
    default <T> void accept(FHIRPathNodeVisitor<T> visitor) {
        accept(null, visitor);
    }
}