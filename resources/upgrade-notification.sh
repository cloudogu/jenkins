#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail

FROM_VERSION="${1}"
TO_VERSION="${2}"

# show backup warning if upgrading from version < 2.138.4-1
if [ "${FROM_VERSION}" != "2.138.4-1" ] && [ "${FROM_VERSION}" == "$(printf "%s\\n2.138.4-1" "${FROM_VERSION}" | sort | head -n1)" ]; then
    RED='\033[0;31m'
    COLOR_OFF='\033[0m'
    printf "%s ~~~~WARNING~~~~\n\n" "${RED}"
    printf "%s After applying this upgrade, Jenkins will migrate the user records to a slightly different storage format that uses a central index to map user IDs to directory names. This is a backwards incompatible change, so older releases of Jenkins will not be able to deal with the new storage format. We strongly recommend taking a full backup of at least the \$JENKINS_HOME/users/ directory before upgrading.\n\n" "${COLOR_OFF}"
    printf "For more information see %s \n\n" "https://jenkins.io/doc/upgrade-guide/2.138/#upgrading%20to%20jenkins%20lts%202.138.4"
fi

# print upgrade notice for Jenkins 2.176.1 if TO_VERSION is equal or higher than 2.176.1 and FROM_VERSION is lower than 2.176.1
if [ "2.176.1-1" == "$(printf "%s\\n2.176.1-1" "${TO_VERSION}" | sort | head -n1)" ] && [ "2.176.1-1" != "$(printf "%s\\n2.176.1-1" "${FROM_VERSION}" | sort | head -n1)" ]; then
    printf "You are upgrading the Jenkins dogu to version 2.176.1 or higher. There have been changes, including:\\n- Remoting-mode of CLI removed\\n- Symbol nonStoredPasswordParam removed\\n- Mailer plugin resources removed\\n- New security warning related to build authorization\\n- Built-in support for CCtray (cc.xml) files removed\\n- Chinese translations removed\\n\\nHave a look at the upgrade guide: %s\\n" "https://jenkins.io/doc/upgrade-guide/2.176/#upgrading%20to%20jenkins%20lts%202.176.1"
fi

# print upgrade notice for jenkins based on java 11
if [ "2.222.1-2" == "$(printf "%s\\n2.222.1-2" "${TO_VERSION}" | sort | head -n1)" ] && [ "2.222.1-2" != "$(printf "%s\\n2.222.1-2" "${FROM_VERSION}" | sort | head -n1)" ]; then
   printf "\nYou are upgrading the Jenkins dogu to version 2.222.1-2 or higher. There have been changes to the installed jdk version. \\nSince 2.222.1-2, Jenkins is based on OpenJDK version 11. \nLegacy builds using the internal jdk of Jenkins must switch to the configured jdk \"OpenJDK-8\"."
   printf "\nFor more information see %s \n\n" "https://github.com/cloudogu/jenkins/blob/develop/README.md#building-with-openjdk-8"

fi

# print upgrade notice for Jenkins 2.249.3-1 concerning admin group change
if [ "2.249.3-1" == "$(printf "%s\\n2.249.3-1" "${TO_VERSION}" | sort | head -n1)" ] && [ "2.249.3-1" != "$(printf "%s\\n2.249.3-1" "${FROM_VERSION}" | sort | head -n1)" ]; then
   printf "\nYou are upgrading the Jenkins dogu to version 2.249.3-1 or higher. DO NOT change the CES global admin group at the same time!"
   printf "\nIf you have changed the global admin group (via /config/_global/admin_group etcd key), restart the Jenkins dogu before upgrading it!"
fi
