#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail

source "$(dirname "$0")/version.sh"


FROM_VERSION="${1}"
TO_VERSION="${2}"

echo "Executing Jenkins post-upgrade from ${FROM_VERSION} to ${TO_VERSION}"

if [ "${FROM_VERSION}" = "${TO_VERSION}" ]; then
  echo "FROM and TO versions are the same; Exiting..."
  exit 0
fi

# from 2.476 and above CAS-Plugin 1.7.0 is needed
if isLesserVersionThan $FROM_VERSION "2.476.0-0" && isGreaterVersionThan $TO_VERSION "2.476.0-0"; then
  CAS_PLUGIN_VERSION="1.7.0"
  CAS_PLUGIN_SHA="763846f16e56ef288a44c8e581e5047b15f7df644e8cb092454502f9026742ef"

  echo "Need to update cas-plugin to ${CAS_PLUGIN_VERSION}"

  JENKINS_PLUGIN_DIR="/var/lib/jenkins/plugins"
  CAS_PLUGIN_FALLBACK="$(dirname "$0")/var/tmp/resources/cas-plugin-${CAS_PLUGIN_VERSION}.hpi"

  echo "Removing old Plugin"
  # Delete unzipped plugin-dir for cas-plugin
  rm -rf "${JENKINS_PLUGIN_DIR}/cas-plugin"
  # Delete cas-plugin-archive
  rm -f "${JENKINS_PLUGIN_DIR}/plugins/cas-plugin.jpi"

  echo "Copy new CAS-Plugin ${CAS_PLUGIN_VERSION}"
  echo "${CAS_PLUGIN_SHA} ${CAS_PLUGIN_FALLBACK}" | sha256sum -c -
  cp "${CAS_PLUGIN_FALLBACK}" "${JENKINS_PLUGIN_DIR}/cas-plugin.jpi"
fi

echo "Jenkins post-upgrade done"