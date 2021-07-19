#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -x

run_reindex(){
    migration="${1}"

    if [ ! -z "${migration}" ] && [ -f "${WORKSPACE}/fhir/build/migration/${migration}/6_current-reindex.sh" ]
    then 
        echo "Running [${migration}] specific integration tests"
        bash ${WORKSPACE}/fhir/build/migration/${migration}/6_current-reindex.sh
    else
        # Run the $reindex
        i=1
        bash ${WORKSPACE}/fhir/build/common/wait_for_it.sh
        # Date YYYY-MM-DDTHH:MM:SSZ
        DATE_ISO=$(date +%Y-%m-%dT%H:%M:%SZ)
        status=$(curl -k -X POST -o reindex.json -i -w '%{http_code}' -u 'fhiruser:change-password' 'https://localhost:9443/fhir-server/api/v4/$reindex' \
            -H 'Content-Type: application/fhir+json' -H 'X-FHIR-TENANT-ID: default' \
            -d "{\"resourceType\": \"Parameters\",\"parameter\":[{\"name\":\"resourceCount\",\"valueInteger\":100},{\"name\":\"tstamp\",\"valueString\":\"${DATE_ISO}\"}]}")
        echo "Status: ${status}"

        while [ $status -ne 200 ]
        do
            if [ $status -eq 000 ]
            then 
                echo "Bad Response... server is not up"
                exit 30
            fi
            if [ $status -eq 400 ]
            then
                echo "bad request on the client side"
                cat reindex.json
                exit 20
            fi
            if [ $status -eq 500 ]
            then
                echo "bad results on the server side"
                cat reindex.json
                exit 10
            fi
            if [ ! -f reindex.json ]
            then
                echo "File expected, something didn't work right"
            fi
            i=$((i+1))
            if [ $(cat reindex.json | grep -c "Reindex complete") -eq 1 ]
            then
                echo "${i}"
                break
            fi
            status=$(curl -k -X POST -o reindex.json -i -w '%{http_code}' -u 'fhiruser:change-password' 'https://localhost:9443/fhir-server/api/v4/$reindex' \
                -H 'Content-Type: application/fhir+json' -H 'X-FHIR-TENANT-ID: default' \
                -d "{\"resourceType\": \"Parameters\",\"parameter\":[{\"name\":\"resourceCount\",\"valueInteger\":100},{\"name\":\"tstamp\",\"valueString\":\"${DATE_ISO}\"}]}")
            echo "Status: ${status}"
        done
    fi
}

###############################################################################

# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the migration/bin directory
cd "fhir/"

run_reindex "${1}"

# Reset to Original Directory
popd > /dev/null

# EOF
###############################################################################