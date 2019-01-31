#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail

FROM_VERSION="${1}"
TO_VERSION="${2}"

# show backup warning if upgrading from version < 2.138.4-1
if [ "${FROM_VERSION}" != "2.138.4-1" ] && [ "${FROM_VERSION}" == "`printf "${FROM_VERSION}\n2.138.4-1" | sort | head -n1`" ]; then
    RED='\033[0;31m'
    COLOR_OFF='\033[0m'
    printf "${RED}~~~~WARNING~~~~\n\n"
    printf "${COLOR_OFF}After applying this upgrade, Jenkins will migrate the user records to a slightly different storage format that uses a central index to map user IDs to directory names. This is a backwards incompatible change, so older releases of Jenkins will not be able to deal with the new storage format. We strongly recommend taking a full backup of at least the \$JENKINS_HOME/users/ directory before upgrading.\n\n"
    printf "For more information see %s \n\n" "https://jenkins.io/doc/upgrade-guide/2.138/#upgrading%20to%20jenkins%20lts%202.138.4"
fi