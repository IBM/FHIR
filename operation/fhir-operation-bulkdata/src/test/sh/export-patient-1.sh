#!/usr/bin/env bash

# ----------------------------------------------------------------------------
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
# ----------------------------------------------------------------------------

# export patient

# 0 - Set the tenant id and password
export TENANT_ID=''
export PASS=''

# 1 - Create an Export Request
curl -k -u "fhiruser:${PASS}" -H "Content-Type: application/fhir+json" -X GET \
    'https://localhost:9443/fhir-server/api/v4/Patient/$export?_outputFormat=application/fhir%2Bndjson' \
    -v --header "X-FHIR-TENANT-ID: ${TENANT_ID}"

# 2 - Get the Content-Location and the Job
# < HTTP/2 202
# < content-location: https://localhost:9443/fhir-server/api/v4/$bulkdata-status?job=eikWD%2BJszJJ_DkN6HWMLYQ

# 3 - Set the Job Id and execute the request
# Repeat until 200(OK)
curl --location --request GET 'https://localhost:9443/fhir-server/api/v4/$bulkdata-status?job=Jn5OP%2BC3loz783i5kaxeaw' \
    --header 'Content-Type: application/fhir+json' -k \
    -u "fhiruser:${PASS}" -v --header "X-FHIR-TENANT-ID: ${TENANT_ID}"

# 4 - Check the file that is output.