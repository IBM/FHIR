/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.bulkdata.jbatch.export.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.batch.api.partition.PartitionAnalyzer;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.context.JobContext;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.ibm.fhir.bulkdata.jbatch.export.data.ExportCheckpointUserData;

@Dependent
public class ExportPartitionAnalyzer implements PartitionAnalyzer {
    @Inject
    JobContext jobContext;

    private List<ExportCheckpointUserData> partitionSummaries = new ArrayList<>();

    public ExportPartitionAnalyzer() {
        // No Operation
    }

    @Override
    public void analyzeStatus(BatchStatus batchStatus, String exitStatus) {
        // No Operation
    }

    @Override
    public void analyzeCollectorData(Serializable data) {
        if (data == null) {
            return;
        }

        ExportCheckpointUserData partitionSummary  = (ExportCheckpointUserData) data;
        partitionSummaries.add(partitionSummary);
        jobContext.setTransientUserData(partitionSummaries);
    }
}