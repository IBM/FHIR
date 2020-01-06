@echo off

###############################################################################
# (C) Copyright IBM Corp. 2019, 2020
# 
# SPDX-License-Identifier: Apache-2.0
###############################################################################

<# 
 This script will install the fhir server on the local machine and then
 start it up so that we can run server integration tests
 $WORKSPACE - top level directory of the Jenkins workspace
 $WORKSPACE/SIT - holds everything related to server integration tests
 $WORKSPACE/SIT/fhir-server-dist - installer contents (after unzipping)
 $WORKSPACE/SIT/wlp - fhir server installation 
#>

# Initial wait time after the "server start" command returns
$Env:SERVER_WAITTIME="30"

# Sleep interval after each "metadata" invocation
$Env:SLEEP_INTERVAL="10"

# Max number of "metadata" tries to detect server is running
$Env:MAX_TRIES=10

echo "Preparing environment for fhir-server integration tests..."
if ([string]::isNullOrWhitespace($Env:WORKSPACE)){
    echo "ERROR: WORKSPACE environment variable not set!"
    exit 2
}

# Collect the installers and config files in a common place (same as the docker process)
cd ${WORKSPACE}/fhir-install/docker
& copy-dependencies.ps1

# Remove the entire SIT file tree if it exists
set SIT=${WORKSPACE}/SIT
if [ -d "%SIT%" ]; then
    echo "Removing %SIT%"
    rm -fr %SIT%
fi

if NOT EXISTS %SIT%
mkdir -p %SIT%

# Install a fresh copy of the fhir server
echo "Unzipping fhir-server installer..."
unzip ${WORKSPACE}/fhir-install/docker/volumes/dist/fhir-server-distribution.zip -d %SIT%

echo "Installing fhir server in %SIT%"
%SIT%/fhir-server-dist/install.sh %SIT%

echo "Copying configuration to install location..."
rm -fr %SIT%/wlp/usr/servers/fhir-server/config/*
cp -pr ${WORKSPACE}/fhir-install/docker/volumes/dist/config/* %SIT%/wlp/usr/servers/fhir-server/config/

echo "Copying test artifacts to install location..."
cp -pr ${WORKSPACE}/fhir-operation/target/fhir-operation-*-tests.jar %SIT%/wlp/usr/servers/fhir-server/userlib/

# Start up the fhir server
echo "
>>> Current time: " $(date)
echo "Starting fhir server..."
%SIT%/wlp/bin/server start fhir-server
Powershell.exe -c "[System.TimeZoneInfo]::ConvertTimeBySystemTimeZoneId((Get-Date), 'Line Islands Standard Time').ToString('t')" > d.txt
set DATE_PS=<d.txt
echo ">>> Current time: " %DATE_PS%

# Sleep for a bit to let the server startup
echo "Sleeping ${SERVER_WAITTIME} to let the server start..."
sleep ${SERVER_WAITTIME}

# Next, we'll invoke the metadata API to detect when the
# server is ready to accept requests.
echo "Waiting for fhir-server to complete initialization..."
set metadata_url="https://localhost:9443/fhir-server/api/v4/metadata"
set tries=0
set status=0
while [ $status -ne 200 -a $tries -lt ${MAX_TRIES} ]; do
    tries=$((tries + 1))
    cmd="curl -sS -k -o ${WORKSPACE}/metadata.json -I -w %{http_code} -u fhiruser:change-password $metadata_url "
    echo "Executing[$tries]: $cmd"
    status=$($cmd)
    echo "Status code: $status"
    if [ $status -ne 200 ]
    then
       echo "Sleeping for ${SLEEP_INTERVAL} secs..."
       sleep ${SLEEP_INTERVAL}
    fi
done

# Gather server logs in case there was a problem starting up the server
echo "Collecting pre-test server logs..."
set pre_it_logs=%SIT%/pre-it-logs
set zip_file=${WORKSPACE}/pre-it-logs.zip
rm -fr ${pre_it_logs} 2>/dev/null
mkdir -p ${pre_it_logs}
rm -f ${zip_file} 2>/dev/null
cp -pr %SIT%/wlp/usr/servers/fhir-server/logs ${pre_it_logs}
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

# End of Script 