/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.helpers;

/**
 * This is a very rudimentary attempt at providing an interface for code to 
 * build CQL on the fly. There is a lot more to the CQL grammar than what is
 * supported here, but this hits the high points.
 */
public class CqlBuilder {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private StringBuilder sb = new StringBuilder();
    
    public static CqlBuilder builder() {
        return new CqlBuilder();
    }
    
    private CqlBuilder() {
        sb = new StringBuilder();
    }
    
    public CqlBuilder library(String name) {
        sb.append("library \"" + name + "\"");
        return this.endline();
    }
    
    public CqlBuilder library(String name, String version) {
        sb.append("library \"" + name + "\" version '" + version + "'");
        return this.endline();
    }
    
    public CqlBuilder using(String model) {
        sb.append("using " + model);
        return this.endline();
    }

    public CqlBuilder using(String model, String version) {
        sb.append("using " + model + " version '" + version + "'");
        return this.endline();
    }
    
    public CqlBuilder include(String libName) { 
        return includes(libName, null, null);
    }
    
    public CqlBuilder include(String libName, String version) { 
        return includes(libName, version, null);
    }
    
    public CqlBuilder includes(String libName, String version, String alias) { 
        sb.append("include " + libName );
        if( version != null ) {
            sb.append( " version '" + version + "'");
        }
        if( alias != null ) {
            sb.append( " called \"" + alias + "\"");
        }
        return this.endline();
    }
    
    public CqlBuilder parameter(String paramName, String type) {
        return this.parameter(paramName, type, null);
    }
    
    public CqlBuilder parameter(String paramName, String type, String defaultValue) {
        sb.append("parameter \"" + paramName + "\" type " + type);
        if( defaultValue != null ) {
            sb.append(" default " + defaultValue);
        }
        return this.endline();
    }
    
    public CqlBuilder valueset(String vsId, String externalRef) {
        // there is more to the grammar than this, but this is all I'm going to support for now
        sb.append("valueset \"" + vsId + "\" : '" + externalRef + "'");
        return this.endline();
    }
    
    public CqlBuilder context(String context) { 
        sb.append("context " + context);
        return this.endline();
    }
    
    public CqlBuilder expression(String exprName, String exprValue) { 
        sb.append("define \"" + exprName + "\":");
        endline();
        sb.append(exprValue);
        return this.endline();
    }
    
    private CqlBuilder endline() {
        sb.append( LINE_SEP );
        return this;
    }
    
    public String build() {
        return sb.toString();
    }
}
