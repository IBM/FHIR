/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.fhir.operation.cpg;

import java.util.Comparator;
import org.cqframework.cql.elm.execution.Library;

public class CqlLibraryComparator implements Comparator<Library> {

    public static CqlLibraryComparator INSTANCE = new CqlLibraryComparator();
    
    private CqlLibraryComparator() {
        //No Operation
    }
    
    @Override
    public int compare(Library o1, Library o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 != null && o2 == null) {
            return -1;
        } else if (o1 == null && o2 != null) {
            return 1;
        } else {
            return o1.getIdentifier().getId().compareTo(o2.getIdentifier().getId());
        }
    }
}
