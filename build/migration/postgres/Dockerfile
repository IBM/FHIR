###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

FROM postgres:12.6-alpine

# Hard Coded environment values
ENV POSTGRES_DB fhirdb
ENV POSTGRES_USER postgres
ENV PGDATA /db/data
ENV LANG en_US.utf8

# Create a working data directory
VOLUME /db
WORKDIR /db

# Open up the port
EXPOSE 5432

# Load db resources
COPY --chown=postgres:postgres resources/db.sql /docker-entrypoint-initdb.d/db.sql
COPY --chown=postgres:postgres resources/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod 755 /docker-entrypoint.sh

# Add the fhiradmin user
RUN addgroup -S fhir && adduser -S fhiradmin -G fhir
RUN echo "fhiradmin:change-password" | chpasswd

ENTRYPOINT ["/docker-entrypoint.sh"]