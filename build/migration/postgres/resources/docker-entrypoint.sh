#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set -o errexit
set -o nounset
set -o pipefail

mkdir -p /db/data

echo "user id: $(su - postgres -c "id -u && id -g")"

find /db

if [ ! -f /db/data/pg_hba.conf ]
then
    mkdir -p /db/data
    chown -R postgres:postgres /db/data
    su - postgres -c "/usr/local/bin/initdb -D /db/data"

    # Let it listen on all ports
    sed -ri "s!^#?(listen_addresses)\s*=\s*\S+.*!\1 = '*'!" /db/data/postgresql.conf

    # Configuration
    sed -i -e "s/^max_connections = .*$/max_connections = 100/" \
           -e "s/^#max_prepared_transactions = .*$/max_prepared_transactions = 100/" \
           -e "s/^#max_locks_per_transaction = .*$/max_locks_per_transaction = 128/" \
           -e "s/^shared_buffers =.*$/shared_buffers = 4GB/" \
           -e "s/^#effective_cache_size = 128MB.*$/effective_cache_size = 2GB/" \
           -e "s/^#work_mem = 1MB.*$/work_mem = 1MB/" \
           -e "s/^#maintenance_work_mem = 16MB.*$/maintenance_work_mem = 512MB/" \
           -e "s/^#checkpoint_segments = .*$/checkpoint_segments = 32/" \
           -e "s/^#checkpoint_completion_target = 0.5.*$/checkpoint_completion_target = 0.7/" \
           -e "s/^#wal_buffers =.*$/wal_buffers = 16MB/" \
           -e "s/^#default_statistics_target = 100.*$/default_statistics_target = 100/" /db/data/postgresql.conf

    cat << EOF >> /db/data/pg_hba.conf

host    all             all     0.0.0.0/0               md5
EOF

    su - postgres -c '/usr/local/bin/pg_ctl -D "/db/data" --wait --timeout=120 start'
    # Create the FHIRADMIN user
    su - postgres -c "/usr/local/bin/psql -c \"CREATE USER fhiradmin WITH LOGIN encrypted password 'change-password';\""
    # Create the Database
    su - postgres -c "/usr/local/bin/psql -c \"CREATE DATABASE fhirdb OWNER 'fhiradmin';\""
    su - postgres -c '/usr/local/bin/psql --dbname=fhirdb -v ON_ERROR_STOP=1 < /docker-entrypoint-initdb.d/db.sql'
else
    su - postgres -c '/usr/local/bin/pg_ctl -D "/db/data" --wait --timeout=120 start'
fi

if [ -d /db/data ]
then
    su - postgres -c "id -u && id -g"
    chown -R postgres:postgres /db/data
fi

exec "$@"
# EOF
