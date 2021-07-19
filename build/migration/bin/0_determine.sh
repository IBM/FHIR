#!/usr/bin/env bash

###############################################################################
# (C) Copyright IBM Corp. 2021
#
# SPDX-License-Identifier: Apache-2.0
###############################################################################

set +x

# generate_between_tag_changelog - generates the details of the changes between tags
generate_between_tag_changelog() {
    echo "Changes to 'fhir-persistence-schema' between tags"
    PREVIOUS_TAG=HEAD
    for TAG in $(git tag --sort=-v:refname)
    do
        COUNT=$(git diff --name-only ${TAG}..${PREVIOUS_TAG} fhir-persistence-schema/src/main | sort -u | wc -l)
        echo "${PREVIOUS_TAG}..${TAG}|${COUNT}"
        PREVIOUS_TAG=${TAG}
    done


    echo "Changes to 'fhir-database-utils' and 'fhir-persistence-schema' between tags"
    PREVIOUS_TAG=HEAD
    for TAG in $(git tag --sort=-v:refname)
    do
        DB_UTIL_COUNT=$(git log ${TAG}..${PREVIOUS_TAG} --oneline --name-only fhir-database-utils/src/main | grep -i fhir-database-utils | sort -u | wc -l)
        FPS_COUNT=$(git diff --name-only ${TAG}..${PREVIOUS_TAG} fhir-persistence-schema/src/main | sort -u | wc -l)
        echo "${PREVIOUS_TAG}..${TAG}|${DB_UTIL_COUNT}|${FPS_COUNT}"
        PREVIOUS_TAG=${TAG}
    done

    echo "Changes to 'build/migration' between tags"
    PREVIOUS_TAG=HEAD
    for TAG in $(git tag --sort=-v:refname)
    do
        COUNT=$(git log ${TAG}..${PREVIOUS_TAG} --oneline --name-only build/migration | grep -i build/migration | sort -u | wc -l)
        echo "${PREVIOUS_TAG}..${TAG}|${COUNT}"
        PREVIOUS_TAG=${TAG}
    done
}

# should_run - determine if the migration should run
should_run() {
    TAG="$(git tag --sort=-v:refname | head -n 1)"
    PREVIOUS_TAG="HEAD"

    i=0

    # Schema Changes
    COUNT=$(git diff --name-only ${TAG}..${PREVIOUS_TAG} fhir-persistence-schema/src/main | sort -u | wc -l)
    i=$((i + ${COUNT}))
    echo "# fhir-persistence-schema = ${COUNT}"
    git diff --name-only ${TAG}..${PREVIOUS_TAG} fhir-persistence-schema/src/main | sort -u
    echo

    # Structural Changes to Database Support
    COUNT=$(git diff --name-only ${TAG}..${PREVIOUS_TAG} fhir-database-utils/src/main | sort -u | wc -l)
    i=$((i + ${COUNT}))
    echo "# fhir-database-utils = ${COUNT}"
    git diff --name-only ${TAG}..${PREVIOUS_TAG} fhir-database-utils/src/main | sort -u
    echo

    # Automation Changes
    echo "# build/migration = ${COUNT}"
    i=$((i + ${COUNT}))
    COUNT=$(git diff --name-only ${TAG}..${PREVIOUS_TAG} build/migration | sort -u | wc -l)
    git diff --name-only ${TAG}..${PREVIOUS_TAG} build/migration | sort -u
    echo

    # Set the migration_skip flag
    if [ $i != 0 ]
    then
        echo "env.migration_skip=false"
        echo "migration_skip=false" >> $GITHUB_ENV
    else
        echo "env.migration_skip=true"
        echo "migration_skip=true" >> $GITHUB_ENV
    fi
    
    # JDBC Changes (e.g. how we extract or retrieve the data)
    # Not included in the count
    # echo "# fhir-persistence-jdbc"
    # git diff --name-only ${TAG}..${PREVIOUS_TAG} fhir-persistence-jdbc/src/main | sort -u

}

# pick_version - picks the version based on the minor. 
# From the Matrix, at least one is passed in: release: ['latest', 'previous', 'MAJOR.MINOR.PATCH']
pick_version() {
    INPUT="${1}"
    if [ "last" == "${INPUT}" ]
    then
        VERSION=$(git tag --sort=-v:refname | grep -v '-' | head -1)
    elif [ "previous" == "${INPUT}" ]
    then
        MAJOR_VERSION=$(for TAG in  $(git tag --sort=-v:refname | grep -v '-')
        do
            MAJOR=$(echo $TAG | sed 's|\.| |g' | awk '{print $1}')
            MINOR=$(echo $TAG | sed 's|\.| |g' | awk '{print $2}')
            SEP='.'
            if [ -z "${MINOR}" ]
            then
                SEP=''
            fi
            echo $MAJOR$SEP$MINOR
        done | sort -ur | head -2 | tail -1)
        VERSION=$(git tag --sort=-v:refname | grep -v '-' |  grep  -i ${MAJOR_VERSION} | head -n 1)
    else
        VERSION="${INPUT}"
    fi
    echo "env.migration_branch=${VERSION}"
    echo "migration_branch=${VERSION}" >> $GITHUB_ENV
}

###############################################################################
cd fhir/

generate_between_tag_changelog
should_run
pick_version "${1}"

echo "migration_cache=false" >> $GITHUB_ENV

# EOF
###############################################################################
