#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2020
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -o errexit
set -o nounset
set -o pipefail

DIST="${WORKSPACE}/build/persistence/postgres/workarea/volumes/dist"
SCHEMA="${WORKSPACE}/build/persistence/postgres/workarea/schema"

# pre_integration - 
pre_integration(){
    cleanup_prior
    cleanup_existing_docker
    bring_up_database
    copy_schema_jar
    deploy_schema
    copy_server_config
    bringup_fhir
}

# cleanup_prior - Cleans up the prior files
cleanup_prior(){
    echo "Removing old dependencies..."
    if [ -d $DIST ]
    then
        rm -rf $DIST/* 2> /dev/null
    fi

    if [ -d $SCHEMA ]
    then
        rm -rf $SCHEMA/* 2> /dev/null
    fi
    mkdir -p $DIST
    mkdir -p $SCHEMA
}

# copy_server_config - Copy assembled files
copy_server_config(){
    echo "Copying installation zip files..."
    cp -p ${WORKSPACE}/fhir-install/target/fhir-server-distribution.zip $DIST

    echo "Copying fhir configuration files..."
    cp -pr ${WORKSPACE}/fhir-server/liberty-config/config $DIST
    cp -pr ${WORKSPACE}/fhir-server/liberty-config-tenants/config/* $DIST/config
    cp -pr ${WORKSPACE}/fhir-server/liberty-config/config/default/fhir-server-config-postgresql.json $DIST/config/default/fhir-server-config.json

    USERLIB="${DIST}/userlib"
    mkdir -p $USERLIB

    echo "Copying test artifacts to install location..."
    cp -pr ${WORKSPACE}/operation/fhir-operation-test/target/fhir-operation-*-tests.jar ${USERLIB}
    echo "Finished copying fhir-server dependencies..."
}

# cleanup_existing_docker - cleanup existing docker
cleanup_existing_docker(){
    # Stand up a docker container running the fhir server configured for integration tests
    echo "Bringing down any containers that might already be running as a precaution"
    docker-compose kill
    docker-compose rm -f
}

# bring_up_database - brings up database
# - remove the existing db directory
# - build postgres and wait
bring_up_database(){
    if [ -d db ]
    then 
        rm -rf db/
    fi
    mkdir -p db
    echo "Bringing up postgres... be patient, this will take a minute"
    docker-compose build --pull postgres
    docker-compose up --remove-orphans -d postgres
    echo ">>> Current time: " $(date)

    # Waiting to startup
    count=0
    echo "Waiting while starting up..."
    while [ `docker-compose logs --timestamps postgres | grep -c 'database system is ready to accept connections'` -ne 1 ] && [ "${count}" -ne 120 ]
    do
        echo "... Waiting ... - ${count}"
        sleep 5
        count=$((count+1)) 
    done
}

# copy_schema_jar
copy_schema_jar(){
    echo "Copying fhir-persistence-schema tool..."
    cp -pr ${WORKSPACE}/fhir-persistence-schema/target/fhir-persistence-schema-*-cli.jar $SCHEMA
}

# setup_schema - sets up the schema (in concert with the db)
deploy_schema(){
    cat << EOF > ${WORKSPACE}/build/persistence/postgres/workarea/postgres.properties
db.host=localhost
db.port=5432
db.database=fhirdb
user=fhiradmin
password=change-password
EOF

    java -jar ${SCHEMA}/fhir-persistence-schema-*-cli.jar --db-type postgresql \
        --prop-file workarea/postgres.properties --schema-name FHIRDATA --create-schemas --pool-size 2

    java -jar ${SCHEMA}/fhir-persistence-schema-*-cli.jar --db-type postgresql \
        --prop-file workarea/postgres.properties --schema-name FHIRDATA --update-schema --pool-size 2

    java -jar ${SCHEMA}/fhir-persistence-schema-*-cli.jar --db-type postgresql \
       --prop-file workarea/postgres.properties --schema-name FHIRDATA --grant-to FHIRSERVER --pool-size 2
}

# bringup_fhir 
bringup_fhir(){
    echo "Bringing up the FHIR server... be patient, this will take a minute"
    docker-compose up --remove-orphans -d fhir-server
    echo ">>> Current time: " $(date)

    # TODO wait for it to be healthy instead of just Sleeping
    (docker-compose logs --timestamps --follow fhir-server & P=$! && sleep 60 && kill $P)

    # Gather up all the server logs so we can trouble-shoot any problems during startup
    cd -
    pre_it_logs=${WORKSPACE}/pre-it-logs
    zip_file=${WORKSPACE}/pre-it-logs.zip
    rm -rf ${pre_it_logs} 2>/dev/null
    mkdir -p ${pre_it_logs}
    rm -f ${zip_file}

    echo "
    Docker container status:"
    docker ps -a

    containerId=$(docker ps -a | grep fhir | cut -d ' ' -f 1)
    if [[ -z "${containerId}" ]]; then
        echo "Warning: Could not find the fhir container!!!"
    else
        echo "fhir container id: $containerId"

        # Grab the container's console log
        docker logs $containerId  >& ${pre_it_logs}/docker-console.txt

        echo "Gathering pre-test server logs from docker container: $containerId"
        docker cp -L $containerId:/opt/ol/wlp/usr/servers/fhir-server/logs ${pre_it_logs}

        echo "Zipping up pre-test server logs"
        zip -r ${zip_file} ${pre_it_logs}
    fi

    # Wait until the fhir server is up and running...
    echo "Waiting for fhir-server to complete initialization..."
    healthcheck_url='https://localhost:9443/fhir-server/api/v4/$healthcheck'
    tries=0
    status=0
    while [ $status -ne 200 -a $tries -lt 3 ]; do
        tries=$((tries + 1))
        cmd="curl -k -o ${WORKSPACE}/health.json -I -w "%{http_code}" -u fhiruser:change-password $healthcheck_url"
        echo "Executing[$tries]: $cmd"
        status=$($cmd)
        echo "Status code: $status"
        if [ $status -ne 200 ]
        then
        echo "Sleeping 10 secs..."
        sleep 10
        fi
    done

    if [ $status -ne 200 ]
    then
        echo "Could not establish a connection to the fhir-server within $tries REST API invocations!"
        exit 1
    fi

    echo "The fhir-server appears to be running..."
    exit 0
}

# is_ready_to_run - is this ready to run? 
is_ready_to_run(){
    echo "Preparing environment for fhir-server integration tests..."
    if [ -z "${WORKSPACE}" ]
    then
        echo "ERROR: WORKSPACE environment variable not set!"
        exit 1
    fi
}

###############################################################################
is_ready_to_run

cd build/persistence/postgres
pre_integration

# EOF 
###############################################################################