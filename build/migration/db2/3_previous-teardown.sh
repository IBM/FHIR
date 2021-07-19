#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -e
set +x
set -o pipefail

###############################################################################
# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the migration directory
cd "${WORKSPACE}/fhir/build/migration/${1}/"

docker-compose down

cx=0
while [ $(docker container ls -q | wc -l) -gt 0 ]
do
    echo "Waiting on shutdown of db ${cx}"
    cx=$((cx + 1))
    if [ ${cx} -ge 300 ]
    then
        echo "Failed to start"
        break
    fi
    sleep 10
done

echo "Creating the database cache"
sudo chmod -R 755 ${WORKSPACE}/fhir/build/migration/${1}/workarea/volumes/dist/db
tar czf ${WORKSPACE}/fhir/build/migration/${1}/workarea/db.tgz ${WORKSPACE}/fhir/build/migration/${1}/workarea/volumes/dist/db

echo "Details for the db.tgz"
ls -al ${WORKSPACE}/fhir/build/migration/${1}/workarea/db.tgz

# Reset to Original Directory
popd > /dev/null

# EOF
###############################################################################