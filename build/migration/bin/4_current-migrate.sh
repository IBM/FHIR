#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -ex
set -o pipefail

run_migrate(){
    migration="${1}"

    echo "Building the current docker image and the current java artifacts"
    pushd $(pwd) > /dev/null
    cd "${WORKSPACE}/fhir"
    mvn -T2C -B install --file fhir-examples --no-transfer-progress
    mvn -T2C -B install --file fhir-parent -DskipTests -P include-fhir-igs,integration --no-transfer-progress

    cd fhir-install
    docker build -t ibmcom/ibm-fhir-server:snapshot .
    cd ..
    popd > /dev/null

    if [ ! -z "${migration}" ] && [ -f "${WORKSPACE}/fhir/build/migration/${migration}/4_current-migrate.sh" ]
    then 
        echo "Running [${migration}] migration"
        bash ${WORKSPACE}/fhir/build/migration/${migration}/4_current-migrate.sh
    fi
}

###############################################################################

# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the migration/bin directory
cd "fhir/"

run_migrate "${1}"

# Reset to Original Directory
popd > /dev/null

# EOF
###############################################################################