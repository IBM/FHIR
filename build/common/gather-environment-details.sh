#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -ex

###############################################################################
# output the environment details
###############################################################################

echo "Reporting on the disk space:"
df -h

echo "Reporting on the docker setup:"
docker info

echo "Reporting on the Docker system setup:"
docker system info

# EOF
###############################################################################