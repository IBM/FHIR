-- ##############################################################################
-- (C) Copyright IBM Corp. 2021
--
-- SPDX-License-Identifier: Apache-2.0
-- ###############################################################################

-- Create the users
CREATE USER fhirbatch  WITH LOGIN encrypted password 'change-password';
CREATE USER fhirserver WITH LOGIN encrypted password 'change-password';

GRANT CONNECT ON DATABASE fhirdb TO fhirserver;
GRANT CONNECT ON DATABASE fhirdb TO fhirbatch;

CREATE SCHEMA IF NOT EXISTS fhirdata;
CREATE SCHEMA IF NOT EXISTS fhir_admin;
CREATE SCHEMA IF NOT EXISTS fhir_oauth;
CREATE SCHEMA IF NOT EXISTS fhir_jbatch;

-- only for the fhiradmin user
GRANT ALL ON schema fhirdata TO fhiradmin WITH GRANT OPTION;
GRANT ALL ON schema fhir_admin TO fhiradmin WITH GRANT OPTION;
GRANT ALL ON schema fhir_oauth TO fhiradmin WITH GRANT OPTION;
GRANT ALL ON schema fhir_jbatch TO fhiradmin WITH GRANT OPTION;

GRANT USAGE ON SCHEMA fhirdata to fhiradmin;
GRANT USAGE ON SCHEMA fhir_oauth to fhiradmin;
GRANT USAGE ON SCHEMA fhir_jbatch to fhiradmin;

GRANT USAGE ON SCHEMA fhirdata to fhirserver;
GRANT USAGE ON SCHEMA fhir_oauth to fhirserver;
GRANT USAGE ON SCHEMA fhir_jbatch to fhirserver;

GRANT USAGE ON SCHEMA fhirdata to fhirbatch;
GRANT USAGE ON SCHEMA fhir_oauth to fhirbatch;
GRANT USAGE ON SCHEMA fhir_jbatch to fhirbatch;
-- EOF
