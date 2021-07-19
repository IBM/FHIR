#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -e
set -x
set -o pipefail

###############################################################################

# Store the current directory to reset to
pushd $(pwd) > /dev/null

# Change to the migration/bin directory
cd "${WORKSPACE}/fhir/build/migration/postgres"

# In order not to hit this after packaging everything up,w e want to run this before we start up the db.
# waiting for server to start....2021-07-16 20:42:03.136 UTC [9] FATAL:  data directory "/db/data" has invalid permissions
# 2021-07-16 20:42:03.136 UTC [9] DETAIL:  Permissions should be u=rwx (0700) or u=rwx,g=rx (0750).
sudo chown -R 70:70 ${WORKSPACE}/fhir/build/migration/postgres/workarea/volumes/dist/db
sudo chmod -R 0750 ${WORKSPACE}/fhir/build/migration/postgres/workarea/volumes/dist/db

# Startup db
docker-compose up --remove-orphans -d db

echo "Debug Details >>> "
docker container inspect postgres_db_1 | jq -r '.[] | select (.Config.Hostname == "postgres_postgres_1").State.Status'

echo "Debug All Details >>> "
sleep 15
docker container inspect postgres_db_1 | jq -r '.[]'
docker container logs postgres_db_1
cx=0
while [ $(docker container inspect postgres_db_1 | jq -r '.[] | select (.Config.Hostname == "postgres_postgres_1").State.Status' | wc -l) -gt 0 ] && [ $(docker container inspect postgres_db_1 | jq -r '.[] | select (.Config.Hostname == "postgres_postgres_1").State.Health.Status' | grep starting | wc -l) -eq 1 ]
do
    echo "Waiting on startup of db ${cx}"
    cx=$((cx + 1))
    if [ ${cx} -ge 300 ]
    then
        echo "Failed to start"
        break
    fi
    sleep 10
done

echo ">>> Persistence >>> current is being run"

echo "- Update Schema"
java -jar ${WORKSPACE}/fhir/fhir-persistence-schema/target/fhir-persistence-schema-*-cli.jar \
    --db-type postgresql --prop db.host=localhost --prop db.port=5432 --prop db.database=fhirdb \
    --prop user=fhiradmin --prop password=change-password \
    --schema-name FHIRDATA --update-schema --pool-size 1

echo "- Grants to FHIRSERVER"
java -jar ${WORKSPACE}/fhir/fhir-persistence-schema/target/fhir-persistence-schema-*-cli.jar \
    --db-type postgresql --prop db.host=localhost --prop db.port=5432 --prop db.database=fhirdb \
    --prop user=fhiradmin --prop password=change-password \
    --schema-name FHIRDATA --grant-to FHIRSERVER --pool-size 1

echo ">>> Set up the derby databases for multidatastore scenarios"
DB_LOC="${WORKSPACE}/fhir/build/migration/postgres/workarea/volumes/dist/derby"
rm -rf ${WORKSPACE}/fhir/build/migration/postgres/workarea/volumes/dist/derby
mkdir -p ${DB_LOC}
java -jar ${WORKSPACE}/fhir/fhir-persistence-schema/target/fhir-persistence-schema-*-cli.jar \
    --db-type derby --prop db.database=${DB_LOC}/fhirDB --prop db.create=Y \
    --update-schema
java -jar ${WORKSPACE}/fhir/fhir-persistence-schema/target/fhir-persistence-schema-*-cli.jar \
    --db-type derby --prop db.database=${DB_LOC}/profile --prop db.create=Y \
    --prop resourceTypes=Patient,Group,Practitioner,PractitionerRole,Person,RelatedPerson,Organization,Location,Observation,MedicationAdministration,StructureDefinition,ElementDefinition,CodeSystem,ValueSet,Resource \
    --update-schema
java -jar ${WORKSPACE}/fhir/fhir-persistence-schema/target/fhir-persistence-schema-*-cli.jar \
    --db-type derby --prop db.database=${DB_LOC}/reference --prop db.create=Y \
    --prop resourceTypes=Patient,Group,Practitioner,PractitionerRole,Device,Organization,Location,Medication,Observation,MedicationAdministration,StructureDefinition,ElementDefinition,CodeSystem,ValueSet,Resource \
    --update-schema
java -jar ${WORKSPACE}/fhir/fhir-persistence-schema/target/fhir-persistence-schema-*-cli.jar \
    --db-type derby --prop db.database=${DB_LOC}/study1 --prop db.create=Y \
    --prop resourceTypes=Patient,Group,Practitioner,PractitionerRole,Device,Organization,Location,Encounter,AllergyIntolerance,Observation,Condition,CarePlan,Provenance,Medication,MedicationAdministration,StructureDefinition,ElementDefinition,CodeSystem,ValueSet,Resource \
    --update-schema

# Reset to Original Directory
popd > /dev/null

# EOF
###############################################################################