/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.fhir.search.SearchConstants.Modifier;

public class OrParameter<T extends IQueryParameter> implements IQueryParameterOr<T> {

    private String name;
    private Modifier modifier;
    private List<T> parts;

    public OrParameter() {
        super();
    }

    public OrParameter(List<T> parts) {
        setParts(parts);
    }

    public OrParameter(Modifier modifier, List<T> parts) {
        setModifier(modifier);
        setParts(parts);
    }

    @SafeVarargs
    public OrParameter(T... parts) {
        this.parts = Arrays.asList(parts);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public List<T> getParts() {
        return parts;
    }

    public void setParts(List<T> parts) {
        this.parts = parts;
    }

    public void addOr(T part) {
        if (this.parts == null) {
            this.parts = new ArrayList<>();
        }
        this.parts.add(part);
    }

    @Override
    public List<T> getParameterValues() {
        return parts;
    }
}
