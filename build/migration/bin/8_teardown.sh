#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -ex

# migration_post - executes for each migration post integration steps
migration_post(){
    migration="${1}"
    if [ ! -z "${migration}" ] && [ -f ${WORKSPACE}/fhir/build/migration/${migration}/8_teardown.sh ]
    then 
        echo "Running [${migration}] teardown"
        bash ${WORKSPACE}/fhir/build/migration/${migration}/8_teardown.sh
    else
        cd ${WORKSPACE}/fhir/build/migration/${migration}/
        docker-compose down --remove-orphans --rmi local -v --timeout 30
        docker system df
        docker system prune -f
    fi
}

###############################################################################
# Check if the workspace is set.
if [ -z "${WORKSPACE}" ]
then 
    echo "The WORKSPACE value is unset"
    exit -1
fi 

# Store the current directory to reset to
pushd $(pwd) > /dev/null

cd "${WORKSPACE}"

migration_post ${1}

# Reset to Original Directory
popd > /dev/null

# EOF 
###############################################################################