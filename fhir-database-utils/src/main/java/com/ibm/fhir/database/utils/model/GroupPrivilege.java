/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.database.utils.model;

/**
 * @author rarnold
 *
 */
public class GroupPrivilege {
    private final String groupName;
    private final Privilege privilege;
    
    public GroupPrivilege(String groupName, Privilege p) {
        this.groupName = groupName;
        this.privilege = p;
    }

    /**
     * Add this privilege to the object
     * @param obj
     */
    public void addToObject(BaseObject obj) {
        obj.addPrivilege(this.groupName, this.privilege);
    }
}
