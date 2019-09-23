/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.bulkexport;

import java.util.List;
import com.ibm.cloud.objectstorage.services.s3.model.PartETag;

/**
 * Bulk export Chunk implementation - job check point data.
 * 
 * @author Albert Wang
 */
public class CheckPointUserData implements java.io.Serializable {
    private static final long serialVersionUID = 5722923276076940517L;
    protected int pageNum;
    protected int lastPageNum;
    protected int partNum;
    protected String uploadId;
    protected boolean isSingleCosObject = false;
    protected List<PartETag> cosDataPacks;

    public CheckPointUserData(int pageNum, String uploadId, List<PartETag> cosDataPacks, int partNum) {
        super();
        this.pageNum = pageNum;
        this.uploadId = uploadId;
        this.cosDataPacks = cosDataPacks;
        this.partNum = partNum;
    }

    public static CheckPointUserData fromTransientUserData(TransientUserData userData) {
        return new CheckPointUserData(userData.getPageNum(), userData.getUploadId(), userData.getCosDataPacks(),
                userData.getPartNum());
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public List<PartETag> getCosDataPacks() {
        return cosDataPacks;
    }

    public void setCosDataPacks(List<PartETag> cosDataPacks) {
        this.cosDataPacks = cosDataPacks;
    }

    public boolean isSingleCosObject() {
        return isSingleCosObject;
    }

    public void setSingleCosObject(boolean isSingleCosObject) {
        this.isSingleCosObject = isSingleCosObject;
    }

    public int getPartNum() {
        return partNum;
    }

    public void setPartNum(int partNum) {
        this.partNum = partNum;
    }

    public int getLastPageNum() {
        return lastPageNum;
    }

    public void setLastPageNum(int lastPageNum) {
        this.lastPageNum = lastPageNum;
    }

}
