#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail

FROM_VERSION="${1}"
TO_VERSION="${2}"

echo "Executing Jenkins pre-upgrade from ${FROM_VERSION} to ${TO_VERSION}"

if [ "${FROM_VERSION}" = "${TO_VERSION}" ]; then
  echo "FROM and TO versions are the same; Exiting..."
  exit 0
fi

ADMIN_GROUP=$(doguctl config --global admin_group)
echo "Setting /config/jenkins/admin_group_last key to \"$ADMIN_GROUP\""
doguctl config admin_group_last "$ADMIN_GROUP"


if

echo "Jenkins pre-upgrade done"