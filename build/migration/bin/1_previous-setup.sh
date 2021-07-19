#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -e
set +x

# required_build - executes for every build
required_build(){
    # Clean up the packages and docker files not on the Mac
    if [[ "$OSTYPE" != "darwin"* ]]
    then
        sudo apt clean
        docker rmi $(docker image ls -aq)
        df -h
    fi

    # build binaries
    mvn -T2C -B install --file fhir-examples --no-transfer-progress
    mvn -T2C -B install --file fhir-parent -DskipTests -P include-fhir-igs,integration --no-transfer-progress

    # create and remove a 1 GB file to make sure we have the room needed later
    df -h
    dd if=/dev/urandom oflag=direct of=balloon.dat bs=1024k count=1000
    rm -f balloon.dat
    sudo apt clean
    docker system prune -f
    df -h

    docker version
}

# migration_build - executes for each migration type.
migration_build(){
    migration="${1}"
    if [ -f "build/migration/${migration}/1_previous-setup.sh" ]
    then
        echo "Running [${migration}] setting up prerequisites"
        bash build/migration/${migration}/1_previous-setup.sh
    fi
}

###############################################################################
# Store the current directory to reset to
pushd $(pwd) > /dev/null

if [ -z "${WORKSPACE}" ]
then 
    echo "The WORKSPACE value is unset"
    exit -1
fi 

# Change to the release directory
cd "${WORKSPACE}/prev"

required_build
migration_build "${1}"

# Reset to Original Directory
popd > /dev/null

# EOF 
###############################################################################