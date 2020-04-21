#!/usr/bin/env bash
###############################################################################
# (C) Copyright IBM Corp. 2020
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################
set -ex

# The full path to the directory of this script, no matter where its called from
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd ${DIR}

java -jar schema/fhir-persistence-schema-*-cli.jar \
  --prop-file db2.properties --schema-name FHIRDATA --update-schema
