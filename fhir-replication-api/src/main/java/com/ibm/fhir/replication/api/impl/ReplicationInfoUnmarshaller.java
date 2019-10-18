/*
 * (C) Copyright IBM Corp. 2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.replication.api.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.replication.api.Unmarshaller;
import com.ibm.fhir.replication.api.model.ReplicationInfo;

public class ReplicationInfoUnmarshaller implements Unmarshaller<ReplicationInfo>{
    @Override
    public ReplicationInfo unmarshall(String json) throws FHIROperationException {
        final Gson gson = new GsonBuilder()
                   .setDateFormat(ISO_8601_GMT_DATE_FORMAT).create();
        
        if(json == null || json.isEmpty()) {
            throw new FHIROperationException("Error while unmarshalling ReplicationInfo. Obj cannot be null.");
        }
            
        return gson.fromJson(json, ReplicationInfo.class);
    }

}
