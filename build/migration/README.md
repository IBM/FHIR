# Integration Test Framework for the Database Migration

This document outlines the end-to-end migration automation framework that exercises the `fhir-persistence-schema` and the migration between versions.

## Steps

The automation runs with these steps: 

1. **Checkout source code for main** - Checks out the git code and populates the `github` environment variables.
2. **Fetch the right branch** - Fetches the right branch from the repository
3. **Setup Java** - Downloads and setup for Java 11
4. **Gather the environment details** - Gathers some rudimentary debugging details and dumps to the standard output
5. **Determine parameters for environment variables** - Figures out the Build specific parameters:
   - `env.migration_skip` - indicates if the migration should be skipped, e.g. no changes in the update-schema from prior release
   - `env.migration_cache` - indicates if the workflow should cache the database instead of recreating it
   - `env.migration_branch` - indicates the branch or release used to checkout previous
6. **Restore the cache for the previous version** - if and only if the cache exists, we restore it.
7. **Checks if the cache actually exists** - if the cache doesn't exist, we reset the environment variable
8. **Checkout source code for previous** - if and only if the cache does not exist, we checkout the previous to `prev`
9. **Setup docker-compose and database** - if and only if the cache does not exist, we setup the prior environment
10. **Run previous release's Integration Tests** - if and only if the cache does not exist, we run the prior IT
11. **Setup previous release's cached database** - if and only if the cache exists, we setup the database and start it
12. **Migrate to the current release** - runs the update-schema
13. **Run LATEST Integration Tests** - Run the reindex and current integration tests and check verison history are updated
14. **Teardown and cleanup** - Shutsdown the Docker images and prunes the system completely
15. **Cache the Database** - Cache the Database iff migration_cache is true
16. **Gather error logs** - This step only runs upon a failure condition. 
17. **Upload logs** - The step uploads the results of the integration tests and the operational logs are posted to the job. 

Note, step 5 and step 14 cache the database using the [GitHub Cache Action](https://github.com/actions/cache).

## Parameters

The GitHub Action is parameterized with a matrix for each migration based on the matrix parameters.

``` yaml
strategy:
  matrix:
    datastore: [ 'db2', 'postgres' ]
      target: ['previous', 'last']
```

### Parameter: **datastore**

The datastore that is being tested. 

Each datastore layer that is tested as part of the framework uses the default build files and the files that match the `matrix.datastore` name added to the `migration.yml`.

### Parameter: **target** 

The target indicates the starting point of a release number or `previous` or last `release`. To keep this a low number of jobs, we should keep to the minimum numbers.

Each release is specified by MAJOR.MINOR.PATCH. The framework tests using the previous MINOR release, and the previous release to the previous MINOR release. 

Consider the following examples to see how the selection occurs:

- **last** - The build looks back to the last tag.
- **previous** - The build looks back to the tag PREVIOUS to the `last` tag's MINOR release. The previous tag is determined based on a lexigraphical sort, so any patch-build does not reset the order.
- **MAJOR.MINOR.PATCH** - The build looks at the specific tag. e.g. 4.8.1

Deisgn note, original implementation had a special parameter `Parameter: **type**`. The type indicates how the schema is reached... `step` by step or `direct` to the current `main` schema cli.

## Files

The following files are the files that orchestrate datastore specific tasks as well as common tasks.

|Filename|Purpose|
|----------|----------------|
|`fhir/build/common/gather-environment-details.sh`|Gathers the environment details|
|`fhir/build/migration/bin/0_determine.sh`|Determines if the build should run|
|`fhir/build/migration/bin/1_check-cache.sh`|Checks if the file is cached and copied down|
|`fhir/build/migration/bin/1_previous-setup.sh`|Runs the previous setup|
|`fhir/build/migration/bin/2_compose.sh`|Setup docker compose and database used in previous release|
|`fhir/build/migration/bin/2_previous-integration-test.sh`|Runs the previous integration tests|
|`fhir/build/migration/bin/3_previous-teardown.sh`|Shuts down the previous server|
|`fhir/build/migration/bin/3_previous-cache-startup.sh`|Alternatively, starts the previous cache|
|`fhir/build/migration/bin/4_current-migrate.sh`|Migrates the database|
|`fhir/build/migration/bin/5_current-pre-integration-test.sh`|Starts the server with the current settings|
|`fhir/build/migration/bin/6_current-reindex.sh`|Runs a reindex|
|`fhir/build/migration/bin/7_current-integration-test.sh`|Starts the current integration tests|
|`fhir/build/migration/bin/8_teardown.sh`|Shuts down the docker images and prunes the db|
|`fhir/build/common/gather-logs.sh migration`|gathers the log files|

`fhir/` is the directory the code is the destination where `main` is checked out.
`prev/` is the directory the previous release is checked out to, which is generally a tag.

The logs are in the workarea under each datastore specific set of scripts.

Transaction Timeout is 300 seconds.

Consult the reference implementation (`db2`) to start a new migration job.

## Testing the Automation

To test the build, be sure to pre-set the environment variable `WORKSPACE` with `export WORKSPACE=$(pwd)`.
You must also start Docker, so the image is built that supports the IBM FHIR Server.

If you have any questions, please reach out on Zulip.