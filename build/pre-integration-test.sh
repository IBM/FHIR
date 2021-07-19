#!/usr/bin/env bash
###############################################################################
# (C) Copyright IBM Corp. 2016, 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################
set -ex

echo "Preparing environment for fhir-server integration tests..."

# The full path to the directory of this script, no matter where its called from
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
export WORKSPACE="$( dirname "${DIR}" )"

# This script will install the fhir server on the local machine and then
# start it up so that we can run server integration tests
# $WORKSPACE - top-level directory for the build
# $WORKSPACE/SIT - holds everything related to server integration tests
# $WORKSPACE/SIT/fhir-server-dist - installer contents (after unzipping)
# $WORKSPACE/SIT/wlp - fhir server installation

# Initial wait time after the "server start" command returns
SERVER_WAITTIME="60"

# Sleep interval after each "$healthcheck" invocation
SLEEP_INTERVAL="30"

# Max number of "metadata" tries to detect server is running
MAX_TRIES=30

# Remove the entire SIT file tree if it exists
export SIT=${WORKSPACE}/SIT
if [ -d "${SIT}" ]; then
    echo "Removing ${SIT}"
    rm -rf ${SIT}
fi
mkdir -p ${SIT}

# Install a fresh copy of the fhir server
echo "Unzipping fhir-server installer..."
unzip ${WORKSPACE}/fhir-install/target/fhir-server-distribution.zip -d ${SIT}

echo "Installing fhir server in ${SIT}"
${SIT}/fhir-server-dist/install.sh ${SIT}

echo "Creating datastores for tenant1"
DB_LOC=${SIT}/wlp/usr/servers/fhir-server/derby
java -jar ${SIT}/fhir-server-dist/tools/fhir-persistence-schema-*-cli.jar \
  --db-type derby --prop db.database=${DB_LOC}/fhirDB --prop db.create=Y \
  --update-schema
java -jar ${SIT}/fhir-server-dist/tools/fhir-persistence-schema-*-cli.jar \
  --db-type derby --prop db.database=${DB_LOC}/profile --prop db.create=Y \
  --prop resourceTypes=Patient,Group,Practitioner,PractitionerRole,Person,RelatedPerson,Organization,Location,Observation,MedicationAdministration,StructureDefinition,ElementDefinition,CodeSystem,ValueSet,Resource \
  --update-schema
java -jar ${SIT}/fhir-server-dist/tools/fhir-persistence-schema-*-cli.jar \
  --db-type derby --prop db.database=${DB_LOC}/reference --prop db.create=Y \
  --prop resourceTypes=Patient,Group,Practitioner,PractitionerRole,Device,Organization,Location,Medication,Observation,MedicationAdministration,StructureDefinition,ElementDefinition,CodeSystem,ValueSet,Resource \
  --update-schema
java -jar ${SIT}/fhir-server-dist/tools/fhir-persistence-schema-*-cli.jar \
  --db-type derby --prop db.database=${DB_LOC}/study1 --prop db.create=Y \
  --prop resourceTypes=Patient,Group,Practitioner,PractitionerRole,Device,Organization,Location,Encounter,AllergyIntolerance,Observation,Condition,CarePlan,Provenance,Medication,MedicationAdministration,StructureDefinition,ElementDefinition,CodeSystem,ValueSet,Resource \
  --update-schema

echo "Copying configuration to install location..."
rm -rf ${SIT}/wlp/usr/servers/fhir-server/config/*
cp -pr ${WORKSPACE}/fhir-server/liberty-config/config/* ${SIT}/wlp/usr/servers/fhir-server/config/
cp -pr ${WORKSPACE}/fhir-server/liberty-config-tenants/config/* ${SIT}/wlp/usr/servers/fhir-server/config/

# Only copy over the Derby datasource definition for this instance
rm -f ${SIT}/wlp/usr/servers/fhir-server/configDropins/overrides/datasource-*.xml
mkdir -p ${SIT}/wlp/usr/servers/fhir-server/configDropins/overrides
cp -p ${WORKSPACE}/fhir-server/liberty-config/configDropins/disabled/datasource-derby.xml ${SIT}/wlp/usr/servers/fhir-server/configDropins/overrides/datasource.xml

echo "Copying test artifacts to install location..."
USERLIB=${SIT}/wlp/usr/servers/fhir-server/userlib
rm -rf ${USERLIB}/fhir-operation-*-tests.jar
cp -pr ${WORKSPACE}/operation/fhir-operation-test/target/fhir-operation-*-tests.jar ${USERLIB}/
cp -pr ${WORKSPACE}/operation/fhir-operation-term-cache/target/fhir-operation-*.jar ${USERLIB}/
find ${WORKSPACE}/conformance -iname 'fhir-ig*.jar' -not -iname 'fhir*-tests.jar' -not -iname 'fhir*-test-*.jar' -exec cp -f {} ${USERLIB} \;

# Start up the fhir server
echo "
>>> Current time: " $(date)
echo "Starting fhir server..."
${SIT}/wlp/bin/server start fhir-server
echo ">>> Current time: " $(date)


# Sleep for a bit to let the server startup
echo "Sleeping ${SERVER_WAITTIME} seconds to let the server start..."
sleep ${SERVER_WAITTIME}

# Next, we'll invoke the $healthcheck API to detect when the
# server is ready to accept requests.
echo "Waiting for fhir-server to complete initialization..."
healthcheck_url='https://localhost:9443/fhir-server/api/v4/$healthcheck'
tries=0
status=0
while [ $status -ne 200 -a $tries -lt ${MAX_TRIES} ]; do
    tries=$((tries + 1))
    set +o errexit
    cmd="curl --max-time 60 -sS -k -o ${WORKSPACE}/health.json -I -w %{http_code} -u fhiruser:change-password $healthcheck_url"
    echo "Executing[$tries]: $cmd"
    status=$($cmd)
    set -o errexit
    echo "Status code: $status"
    if [ $status -ne 200 ]
    then
       echo "Sleeping for ${SLEEP_INTERVAL} secs..."
       sleep ${SLEEP_INTERVAL}
    fi
done

# Gather server logs in case there was a problem starting up the server
echo "Collecting pre-test server logs..."
pre_it_logs=${SIT}/pre-it-logs
zip_file=${WORKSPACE}/pre-it-logs.zip
rm -rf ${pre_it_logs} 2>/dev/null
mkdir -p ${pre_it_logs}
rm -f ${zip_file} 2>/dev/null
cp -pr ${SIT}/wlp/usr/servers/fhir-server/logs ${pre_it_logs}
zip -r ${zip_file} ${pre_it_logs}

# If we weren't able to detect the fhir server ready within the allotted timeframe,
# then exit now...
if [ $status -ne 200 ]
then
    echo "Could not establish a connection to the fhir-server within $tries REST API invocations!"
    exit 1
fi

echo "The fhir-server appears to be running..."
exit 0
