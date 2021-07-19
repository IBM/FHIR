/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.rest.terminology;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import com.ibm.fhir.client.FHIRClient;
import com.ibm.fhir.client.FHIRParameters;
import com.ibm.fhir.client.FHIRResponse;
import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.cql.engine.util.FHIRClientUtil;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Parameters.Parameter;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.type.code.BundleType;
import com.ibm.fhir.model.type.code.ResourceType;

/**
 * This is an implementation of a terminology provider for the CQL Engine that uses
 * the IBM FHIR Server REST Client to access the terminology system.
 */
public class RestFHIRTerminologyProvider implements TerminologyProvider {

    private FHIRClient fhirClient;

    public RestFHIRTerminologyProvider(FHIRClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    @Override
    public boolean in(Code code, ValueSetInfo valueSet) {
        resolveByUrl(valueSet);

        try {
            WebTarget target =
                    fhirClient.getWebTarget().path(ResourceType.VALUE_SET.getValue()).path(valueSet.getId()).path("$validate-code").queryParam("code", FHIRClientUtil.urlencode(code.getCode()));

            if (code.getSystem() != null) {
                target = target.queryParam("system", FHIRClientUtil.urlencode(code.getSystem()));
            }

            Response response = target.request(MediaType.APPLICATION_JSON_TYPE).get();
            FHIRClientUtil.handleErrorResponse(response);

            Parameters respParam = response.readEntity(Parameters.class);
            Parameter output = getRequiredParameterByName(respParam, "result");
            return ((com.ibm.fhir.model.type.Boolean) output.getValue()).getValue();

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Iterable<Code> expand(ValueSetInfo valueSet) {
        resolveByUrl(valueSet);

        try {
            Response response =
                    fhirClient.getWebTarget().path(ResourceType.VALUE_SET.getValue()).path(valueSet.getId()).path("$expand").request(MediaType.APPLICATION_JSON_TYPE).get();
            FHIRClientUtil.handleErrorResponse(response);

            ValueSet expanded = response.readEntity(ValueSet.class);

            List<Code> codes = new ArrayList<>();
            for (ValueSet.Expansion.Contains codeInfo : expanded.getExpansion().getContains()) {
                Code nextCode = new Code().withCode(codeInfo.getCode() != null ? codeInfo.getCode().getValue() : null).withSystem(codeInfo.getSystem() != null
                        ? codeInfo.getSystem().getValue() : null).withVersion(codeInfo.getVersion() != null ? codeInfo.getVersion().getValue()
                                : null).withDisplay(codeInfo.getDisplay() != null ? codeInfo.getDisplay().getValue() : null);
                codes.add(nextCode);
            }
            return codes;
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Code lookup(Code code, CodeSystemInfo codeSystem) {
        try {
            Response response =
                    fhirClient.getWebTarget().path(ResourceType.CODE_SYSTEM.getValue()).path("$lookup").queryParam("code", FHIRClientUtil.urlencode(code.getCode())).queryParam("system", FHIRClientUtil.urlencode(code.getSystem())).request(MediaType.APPLICATION_JSON_TYPE).get();
            FHIRClientUtil.handleErrorResponse(response);

            Parameters respParam = response.readEntity(Parameters.class);
            Parameter display = getParameterByName(respParam, "display");
            if (display != null) {
                code.withDisplay(((com.ibm.fhir.model.type.String) display.getValue()).getValue());
            }

        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return code.withSystem(codeSystem.getId());
    }

    /**
     * Lookup a ValueSet that corresponds to the provided CQL ValueSetInfo. Only the 
     * ValueSetInfo.id property is supported at this time. Use of the ValueSetInfo.version
     * or ValueSetInfo.codesystems properties will cause an UnsupportedOperationException.
     * This method uses a search strategy that first treats the ValueSetInfo.id as a URL,
     * then attempts urn:oid or urn:uuid resolution, then finally attempts plain ID-based
     * resolution if nothing has been found and the value appears to be a FHIR ID.
     *  
     * @param valueSet CQL ValueSetInfo with ID property populated
     * @throws UnsupportedOperationException if the ValueSetInfo.codesystem property 
     * is specified.
     * @throws IllegalArgumentException if zero or more than one ValueSet were resolved
     * for the specified ValueSetInfo.id property.
     */
    public void resolveByUrl(ValueSetInfo valueSet) {
        if (valueSet.getVersion() != null
                || (valueSet.getCodeSystems() != null && valueSet.getCodeSystems().size() > 0)) {
            throw new UnsupportedOperationException(String.format("Could not expand value set %s; version and code system bindings are not supported at this time.", valueSet.getId()));
        }

        Bundle searchResults;
        try {
            FHIRResponse response;

            String encodedId = FHIRClientUtil.urlencode(valueSet.getId());

            // https://github.com/DBCG/cql_engine/pull/462 - Use a search path of URL, identifier, and then resource id
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("url", encodedId);
            response = fhirClient.search(ResourceType.VALUE_SET.getValue(), parameters);
            FHIRClientUtil.handleErrorResponse(response);
            searchResults = response.getResource(Bundle.class);
            if (!searchResults.hasChildren() || searchResults.getEntry().isEmpty()) {
                parameters = new FHIRParameters();
                parameters.searchParam("identifier", encodedId);
                searchResults = fhirClient.search(ResourceType.VALUE_SET.getValue(), parameters).getResource(Bundle.class);
                if (!searchResults.hasChildren() || searchResults.getEntry().isEmpty()) {
                    String id = valueSet.getId();
                    if (id.startsWith(Constants.URN_OID)) {
                        id = id.replace(Constants.URN_OID, "");
                    } else if (id.startsWith(Constants.URN_UUID)) {
                        id = id.replace(Constants.URN_UUID, "");
                    }

                    searchResults = Bundle.builder().type(BundleType.SEARCHSET).build();
                    // If we reached this point and it looks like it might
                    // be a FHIR resource ID, we will try to read it.
                    // See https://www.hl7.org/fhir/datatypes.html#id
                    if (id.matches("[A-Za-z0-9\\-\\.]{1,64}")) {
                        response = fhirClient.read(ResourceType.VALUE_SET.getValue(), id);
                        if (response.getStatus() == 200) {
                            ValueSet vs = response.getResource(ValueSet.class);
                            searchResults =
                                    Bundle.builder().type(BundleType.SEARCHSET).total(com.ibm.fhir.model.type.UnsignedInt.of(1)).entry(Bundle.Entry.builder().id(vs.getId()).resource(vs).build()).build();
                        }
                    }
                }
            }
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        if (!searchResults.hasChildren() || searchResults.getEntry().isEmpty()) {
            throw new IllegalArgumentException(String.format("Could not resolve value set %s.", valueSet.getId()));
        } else if (searchResults.getEntry().size() == 1) {
            valueSet.setId(searchResults.getEntry().get(0).getResource().getId());
        } else {
            throw new IllegalArgumentException("Found more than 1 ValueSet with url: " + valueSet.getId());
        }
    }

    private Parameter getRequiredParameterByName(Parameters parameters, String name) {
        Parameter result = getParameterByName(parameters, name);
        if (result == null) {
            throw new IllegalStateException(String.format("Missing required parameter '%s' in response", name));
        }
        return result;
    }

    private Parameter getParameterByName(Parameters parameters, String name) {
        Parameter result = null;
        for (Parameter p : parameters.getParameter()) {
            if (p.getName().getValue().equals(name)) {
                result = p;
                break;
            }
        }
        return result;
    }
}
