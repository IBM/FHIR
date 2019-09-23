/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.model.visitor;

import static com.ibm.fhir.model.util.ModelSupport.getTypeName;

public interface Visitable {
    void accept(java.lang.String elementName, int elementIndex, Visitor visitor);
    default void accept(java.lang.String elementName, Visitor visitor) {
        accept(elementName, -1, visitor);
    }
    default void accept(Visitor visitor) {
        accept(getTypeName(getClass()), -1, visitor);
    }
}
