#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -ex
set -o pipefail

###############################################################################
# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the migration directory
cd "${WORKSPACE}/fhir/build/migration/${1}/"

echo "Details for the db.tgz"
ls -al ../workarea/db.tgz

echo "Restore the database cache"
tar xzf ../workarea/db.tgz workarea/volumes/dist/db

# Reset to Original Directory
popd > /dev/null

# EOF
###############################################################################