/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.searchparam;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ibm.fhir.model.resource.SearchParameter;
import com.ibm.fhir.search.SearchConstants.Modifier;

public abstract class BaseQueryParameter<T extends BaseQueryParameter<T>> implements IQueryParameter {

    private String name;
    private SearchParameter searchParameter;
    private Boolean missing = null;
    private Modifier modifier = null;

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public SearchParameter getSearchParameter() {
        return searchParameter;
    }

    public void setSearchParameter(SearchParameter searchParameter) {
        this.searchParameter = searchParameter;
    }

    public Boolean getMissing() {
        return missing;
    }

    public void setMissing(Boolean missing) {
        this.missing = missing;
    }

    public Modifier getModifier() {
        return this.modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public boolean equals(Object rhs) {
        return EqualsBuilder.reflectionEquals(this, rhs);
    }
}
