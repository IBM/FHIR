#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -ex
set -o pipefail

# previous_teardown
previous_teardown(){
    migration="${1}"
    if [ -f "${WORKSPACE}/fhir/build/migration/${migration}/3_previous-teardown.sh" ]
    then
        echo "Running [${migration}] setting setup prerequisites"
        bash ${WORKSPACE}/fhir/build/migration/${migration}/3_previous-teardown.sh "${1}"
    fi
}

###############################################################################
# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the migration/bin directory
cd "${WORKSPACE}/fhir/build/migration/${1}/"

previous_teardown "${1}"

# Reset to Original Directory
popd > /dev/null

# EOF
###############################################################################