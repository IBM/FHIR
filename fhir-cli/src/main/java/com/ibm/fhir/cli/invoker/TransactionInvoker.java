/*
 * (C) Copyright IBM Corp. 2016,2017,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.cli.invoker;

import com.ibm.fhir.model.resource.Bundle;

/**
 * This class is the OperationInvoker implementation for the 'transaction' operation.
 * 
 * @author padams
 */
public class TransactionInvoker extends OperationInvoker {

    /* (non-Javadoc)
     * @see com.ibm.fhir.cli.OperationInvoker#invoke(com.ibm.fhir.cli.InvocationContext)
     */
    @Override
    public void doInvoke(InvocationContext ic) throws Exception {
        Object resource = ic.getRequestResourceWithExcp();
        if (resource instanceof Bundle) {
            response = client.transaction((Bundle)resource, requestHeaders);
        } else {
            throw new IllegalArgumentException("Input resource must be a Bundle.");
        }
    }
}
