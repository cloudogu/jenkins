#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail

DIRECTORY="/etc/ssl"
STORE=""
STOREPASS="changeit"
CERTALIAS="ces"
BASE_CREATE_CERT_SKRIPT="/usr/bin/create-ca-certificates.sh"

sourcingExitCode=0
# shellcheck disable=SC1090
source "${BASE_CREATE_CERT_SKRIPT}" || sourcingExitCode=$?
if [[ ${sourcingExitCode} -ne 0 ]]; then
  echo "ERROR: An error occurred while sourcing ${BASE_CREATE_CERT_SKRIPT}."
fi

function create(){
  # create ssl directory
  if [[ ! -d "$DIRECTORY" ]]; then
    mkdir "$DIRECTORY"
  fi

  CERTIFICATE="$(mktemp)"

  # read certificate from etcd
  doguctl config --global certificate/server.crt > "${CERTIFICATE}"

  prepareJavaKeystore

  importInstanceCertificate
  importAdditionalCertificates

  # cleanup temp files
  rm -f "${CERTIFICATE}"
}

function prepareJavaKeystore() {
  cp "${DIRECTORY}/certs/java/cacerts" "${STORE}"
  # cacerts keystore is readonly in alpine package
  chmod 644 "${STORE}"
}

function importInstanceCertificate() {
  importCertificate "${CERTALIAS}" "${CERTIFICATE}"
}

function importAdditionalCertificates() {
  if ! existAdditionalCertificates ; then
    return 0
  fi

  local additionalCertTOC
  additionalCertTOC="$(doguctl config --global "${ADDITIONAL_CERTIFICATES_TOC}")"

  # note the deliberate leaving out of surrounding quotes because space is supposed to be the delimiter within the
  # Table of Content entries.
  for certAlias in ${additionalCertTOC} ; do
    local cert
    cert="$(doguctl config --global "${ADDITIONAL_CERTIFICATES_DIR_KEY}/${certAlias}")"
    certFile=$(mktemp)
    echo "${cert}" > "${certFile}"

    importCertificate "${certAlias}" "${certFile}"

    rm "${certFile}"
  done
}

function importCertificate() {
  certAlias="${1}"
  certFile="${2}"

  keytool -keystore "${STORE}" -storepass "${STOREPASS}" -alias "${certAlias}" \
      -import -file "${certFile}" -noprompt
}


function run_main() {
  STORE="${1:-$DIRECTORY/truststore.jks}"
  create 2> /dev/null
  echo "-Djavax.net.ssl.trustStore=${STORE} -Djavax.net.ssl.trustStorePassword=${STOREPASS}"
}

# make the script only run when executed, not when sourced from bats tests)
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  run_main "$@"
fi
