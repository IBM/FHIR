/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.cql.engine.server.terminology;

import static com.ibm.fhir.cql.helpers.ModelHelper.fhircode;
import static com.ibm.fhir.cql.helpers.ModelHelper.fhirstring;

import java.util.ArrayList;
import java.util.List;

import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.terminology.CodeSystemInfo;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.cql.engine.terminology.ValueSetInfo;

import com.ibm.fhir.cql.Constants;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.resource.ValueSet;
import com.ibm.fhir.model.type.Uri;
import com.ibm.fhir.model.type.code.ResourceType;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.term.service.FHIRTermService;
import com.ibm.fhir.term.service.LookupOutcome;
import com.ibm.fhir.term.service.ValidationOutcome;
import com.ibm.fhir.term.util.ValueSetSupport;

/**
 * This is an implementation of a terminology provider for the CQL Engine that uses
 * the IBM FHIR Server FHIRTermService API to access the terminology data.
 */
public class ServerFHIRTerminologyProvider implements TerminologyProvider {
    
    private FHIRResourceHelpers resourceHelper;

    public ServerFHIRTerminologyProvider(FHIRResourceHelpers resourceHelper) {
        this.resourceHelper = resourceHelper;
    }

    @Override
    public boolean in(Code code, ValueSetInfo valueSetInfo) {
        boolean result = false;

        ValueSet valueSet = resolveByUrl(valueSetInfo);
        ValidationOutcome outcome = FHIRTermService.getInstance().validateCode(valueSet, fhircode(code));
        result = outcome.getResult().getValue();

        return result;
    }

    @Override
    public Iterable<Code> expand(ValueSetInfo valueSetInfo) {
        List<Code> codes;

        ValueSet valueSet = resolveByUrl(valueSetInfo); 
        ValueSet expanded = FHIRTermService.getInstance().expand(valueSet);

        codes = new ArrayList<>();
        for (ValueSet.Expansion.Contains codeInfo : expanded.getExpansion().getContains()) {
            Code nextCode = new Code().withCode(codeInfo.getCode() != null ? codeInfo.getCode().getValue() : null).withSystem(codeInfo.getSystem() != null
                    ? codeInfo.getSystem().getValue() : null).withVersion(codeInfo.getVersion() != null ? codeInfo.getVersion().getValue()
                            : null).withDisplay(codeInfo.getDisplay() != null ? codeInfo.getDisplay().getValue() : null);
            codes.add(nextCode);
        }

        return codes;
    }

    @Override
    public Code lookup(Code code, CodeSystemInfo codeSystem) {
        LookupOutcome outcome = FHIRTermService.getInstance().lookup(Uri.of(codeSystem.getId()), fhirstring(codeSystem.getVersion()), fhircode(code));

        if (outcome.getDisplay() != null) {
            code.setDisplay(outcome.getDisplay().getValue());
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
     * @return resolved ValueSet resource
     * @throws IllegalArgumentException if the ValueSetInfo.id could not be resolved
     * @throws UnsupportedOperationException if the ValueSetInfo.codesystem property
     * is specified.
     */
    protected ValueSet resolveByUrl(ValueSetInfo valueSetInfo) {
        if (valueSetInfo.getVersion() != null
                || (valueSetInfo.getCodeSystems() != null && valueSetInfo.getCodeSystems().size() > 0)) {
            throw new UnsupportedOperationException(String.format("Could not expand value set %s; version and code system bindings are not supported at this time.", valueSetInfo.getId()));
        }

        StringBuilder url = new StringBuilder(valueSetInfo.getId());
        if (valueSetInfo.getVersion() != null) {
            url.append("|");
            url.append(valueSetInfo.getVersion());
        }

        ValueSet valueSet = ValueSetSupport.getValueSet(url.toString());
        if (valueSet == null) {
            String id = valueSetInfo.getId();
            if (id.startsWith(Constants.URN_OID)) {
                id = id.replace(Constants.URN_OID, "");
            } else if (id.startsWith(Constants.URN_UUID)) {
                id = id.replace(Constants.URN_UUID, "");
            }

            // If we reached this point and it looks like it might
            // be a FHIR resource ID, we will try to read it.
            // See https://www.hl7.org/fhir/datatypes.html#id
            if (id.matches("[A-Za-z0-9\\-\\.]{1,64}")) {
                try {
                    SingleResourceResult<? extends Resource> result = resourceHelper.doRead(ResourceType.VALUE_SET.getValue(), id, /*throwExOnMissing=*/false, /*includeDeleted=*/false, /*contextResource=*/null);
                    if( result.isSuccess() ) {
                        valueSet = (ValueSet) result.getResource();
                    }
                } catch( Exception ex ) {
                    throw new RuntimeException("Failed to read ValueSet", ex);
                }
            }
        }
        
        if( valueSet == null ) {
            throw new IllegalArgumentException(String.format("Could not resolve value set %s.", valueSetInfo.getId()));
        }
        
        return valueSet;
    }
}
