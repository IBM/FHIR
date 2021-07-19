# ----------------------------------------------------------------------------
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
# ----------------------------------------------------------------------------

FROM ibmcom/db2:11.5.5.1

# Create a non-admin user for the FHIR server to use to access db2
RUN groupadd -g 1002 fhir && \
    useradd -u 1002 -g fhir -M -d /database/config/fhirserver fhirserver && \
    echo "change-password" | passwd --stdin fhirserver

# Indicate that we expect to connect to the Db2 service on port 50000
EXPOSE 50000

# Set up the database configuration and build to be run by the main Db2 container configuration
# per https://hub.docker.com/r/ibmcom/db2
RUN mkdir /var/custom
COPY create-database.sh /var/custom
RUN chmod +x /var/custom/create-database.sh
