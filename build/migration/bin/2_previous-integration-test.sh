#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -ex

run_tests(){
    # The integration tests may be overriden completely, or fall through to the default. 
    migration="${1}"

    if [ ! -z "${migration}" ] && [ -f "build/migration/${migration}/2_previous-integration-test.sh" ]
    then 
        echo "Running [${migration}] previouos specific integration tests"
        bash ${WORKSPACE}/fhir/build/migration/${migration}/2_previous-integration-test.sh
    else
        # Runs the migration tests
        echo "Running containers are:"
        docker container ls -a
        echo ""

        echo "fhir container are:"
        docker container inspect $(docker container ls | grep fhir_1 | awk '{print $NF}' ) | jq -r '.[]'
        echo ""

        echo "Running Integration tests: "
        mvn -B test -f fhir-server-test -DskipWebSocketTest=true --no-transfer-progress \
            -DskipTests=false | tee fhir/build/migration/integration-test-results/prev-integration-tests.log
        # Add || docker container logs "$(docker container ls | grep fhir_1 | awk '{print $NF}' )"
        echo "Done Running Tests"
        echo ""

        # docker container logs "db2_fhir_1" 
    fi
}

###############################################################################
export BASE="$(pwd)"
# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the migration/bin directory
cd "prev/"

bash ${WORKSPACE}/fhir/build/common/wait_for_it.sh
run_tests "${1}"

# Reset to Original Directory
popd > /dev/null

# EOF
###############################################################################