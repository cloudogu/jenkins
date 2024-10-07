#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail

echo "                                     ./////,                    "
echo "                                 ./////==//////*                "
echo "                                ////.  ___   ////.              "
echo "                         ,**,. ////  ,////A,  */// ,**,.        "
echo "                    ,/////////////*  */////*  *////////////A    "
echo "                   ////'        \VA.   '|'   .///'       '///*  "
echo "                  *///  .*///*,         |         .*//*,   ///* "
echo "                  (///  (//////)**--_./////_----*//////)   ///) "
echo "                   V///   '°°°°      (/////)      °°°°'   ////  "
echo "                    V/////(////////\. '°°°' ./////////(///(/'   "
echo "                       'V/(/////////////////////////////V'      "

JENKINS_HOME_DIR="/var/lib/jenkins"

run_main() {
  INIT_SCRIPT_FOLDER="${JENKINS_HOME_DIR}/init.groovy.d"
  # TODO rename resources to jenkins
  MAIN_INIT_SCRIPTS_FOLDER="/var/tmp/resources/init.groovy.d"
  CUSTOM_INIT_SCRIPTS_FOLDER="/var/lib/custom.init.groovy.d"

  # set state to installing
  doguctl state 'installing'

  # create truststore for java processes
  TRUSTSTORE="${JENKINS_HOME_DIR}/truststore.jks"
  create_truststore.sh "${TRUSTSTORE}" > /dev/null

  # create ca store for git, mercurial and subversion
  create-ca-certificates.sh "${JENKINS_HOME_DIR}/ca-certificates.crt"
  createCurlCertificates "${JENKINS_HOME_DIR}"
  createSubversionCertificates "${JENKINS_HOME_DIR}"

  # copy init scripts

  # remove old folder to be sure,
  # that it contains no script which is already removed from custom init script folder
  if [ -d "${INIT_SCRIPT_FOLDER}" ]; then
    rm -rf "${INIT_SCRIPT_FOLDER}"
  fi

  # copy fresh main init scripts
  cp -rf "${MAIN_INIT_SCRIPTS_FOLDER}" "${INIT_SCRIPT_FOLDER}"

  # merge custom init scripts, if the volume is not empty
  if [ "$(ls -A ${CUSTOM_INIT_SCRIPTS_FOLDER})" ]; then
    cp "${CUSTOM_INIT_SCRIPTS_FOLDER}"/* "${INIT_SCRIPT_FOLDER}"/scripts
  fi

  # set initial setting for slave-to-master-security
  # see https://wiki.jenkins-ci.org/display/JENKINS/Slave+To+Master+Access+Control
  SLAVE_TO_MASTER_SECURITY="${JENKINS_HOME_DIR}/secrets/slave-to-master-security-kill-switch"
  if [ ! -f "${SLAVE_TO_MASTER_SECURITY}" ]; then
    SECRETS_DIRECTORY=$(dirname "${SLAVE_TO_MASTER_SECURITY}")
    if [ ! -d "${SECRETS_DIRECTORY}" ]; then
      mkdir -p "${SECRETS_DIRECTORY}"
    fi
    echo 'false' > "${SLAVE_TO_MASTER_SECURITY}"
  fi

  # Disable CLI over Remoting as advised with Jenkins LTS 2.46.2
  # see https://jenkins.io/blog/2017/04/26/security-advisory/
  CLI_CONFIG_FILE="${JENKINS_HOME_DIR}/jenkins.CLI.xml"
  if [ ! -f "${CLI_CONFIG_FILE}" ]; then
    cp /var/tmp/resources/jenkins.CLI.xml "${CLI_CONFIG_FILE}"
    chmod 0644 "${CLI_CONFIG_FILE}"
  fi

  # Set maven truststore options in .mavenrc file so they won't get copied to slave machines
  if [[ ! -e /var/lib/jenkins/.mavenrc ]]; then
    echo "MAVEN_OPTS=\"\$MAVEN_OPTS -Djavax.net.ssl.trustStore=${TRUSTSTORE}\"" > "${JENKINS_HOME_DIR}/.mavenrc"
    echo "MAVEN_OPTS=\"\$MAVEN_OPTS -Djavax.net.ssl.trustStorePassword=changeit\"" >> "${JENKINS_HOME_DIR}/.mavenrc"
  fi

  start_jenkins
}

function start_jenkins() {
  JENKINS_ARGS=("-Djava.awt.headless=true" \
    "-Djavax.net.ssl.trustStore=${TRUSTSTORE}"\
    "-Djavax.net.ssl.trustStorePassword=changeit"\
    "-Djenkins.install.runSetupWizard=false")

  # Backslashes should not be escaped automatically
  # shellcheck disable=SC2162
  read -a ADDITIONAL_JENKINS_ARGS <<< "$(doguctl config additional_java_args)"

  if [[ "${ADDITIONAL_JENKINS_ARGS[0]}" != "UNSET" ]];
  then
    for i in "${ADDITIONAL_JENKINS_ARGS[@]}"
    do
      JENKINS_ARGS+=("${i}")
    done
  fi

  # starting jenkins
  if [[ "$(doguctl config "container_config/memory_limit" -d "empty")" == "empty" ]];  then
    echo "Starting Jenkins without memory limits..."
  else
    # Retrieve configurable java limits, valid default values exist
    MEMORY_LIMIT_MAX_PERCENTAGE=$(doguctl config "container_config/java_max_ram_percentage")
    MEMORY_LIMIT_MIN_PERCENTAGE=$(doguctl config "container_config/java_min_ram_percentage")
    echo "Starting Jenkins with memory limits: MaxRAMPercentage=${MEMORY_LIMIT_MAX_PERCENTAGE}, MinRAMPercentage=${MEMORY_LIMIT_MIN_PERCENTAGE} ..."
    JENKINS_ARGS+=("-XX:MaxRAMPercentage=${MEMORY_LIMIT_MAX_PERCENTAGE}")
    JENKINS_ARGS+=("-XX:MinRAMPercentage=${MEMORY_LIMIT_MIN_PERCENTAGE}")
  fi

  # disable shellcheck her. This behaviour is intended
  # shellcheck disable=SC2086
  java "${JENKINS_ARGS[@]}" -jar /jenkins.war --prefix=/jenkins
}

function createCurlCertificates() {
  local homeDir="${1}"
  echo "cacert = ${homeDir}/ca-certificates.crt" > "${homeDir}/.curlrc"
}

function createSubversionCertificates() {
  local homeDir="${1}"
  local subversionConfigDir="${homeDir}/.subversion"
  local subversionConfigFile="${subversionConfigDir}/servers"

  BASE_CREATE_CERT_SKRIPT="/usr/bin/create-ca-certificates.sh"

  sourcingExitCode=0
  # shellcheck disable=SC1090
  source "${BASE_CREATE_CERT_SKRIPT}" || sourcingExitCode=$?
  if [[ ${sourcingExitCode} -ne 0 ]]; then
    echo "ERROR: An error occurred while sourcing ${BASE_CREATE_CERT_SKRIPT}."
  fi

  if ! existAdditionalCertificates ; then
    return 0
  fi

  local additionalCertTOC
  additionalCertTOC="$(doguctl config --global "${ADDITIONAL_CERTIFICATES_TOC}")"

  initializeSubversionConfig

  resetSubversionSSLConfig "${subversionConfigFile}"

  # note the deliberate leaving out of surrounding quotes because space is supposed to be the delimiter within the
  # Table of Content entries.
  for certAlias in ${additionalCertTOC} ; do
    local certCount
    local cert
    cert="$(doguctl config --global "${ADDITIONAL_CERTIFICATES_DIR_KEY}/${certAlias}")"

    if ! checkCertCount "${cert}"; then
      echo "ERROR: Skip adding invalid additional certificate for key ${ADDITIONAL_CERTIFICATES_DIR_KEY}/${certAlias} to subversion store: Unequal number of begin and end lines."
      continue
    fi

    echo "Adding additional certificate for key ${certAlias} to subversion store..."

    splitCertificates "${subversionConfigDir}" "${certAlias}" "${cert}"

    certCount="$(countCertString BEGIN "${cert}")"
    addCertificatePathToSubversionSSLConfig "${subversionConfigFile}" "${subversionConfigDir}" "${certAlias}" "${certCount}"
  done
}

function initializeSubversionConfig() {
  # Have Subversion create a valid configuration. It does not matter if it fails.
  svn status || true
}

function checkCertCount() {
  local content="${1}"
  certCount="$(countCertString BEGIN "${content}")"
  endCount="$(countCertString END "${content}")"

  if [[ "${certCount}" != "${endCount}" ]]; then
    return 1
  fi

  return 0
}

function countCertString() {
  local pattern="${1}"
  local content="${2}"
  local count=0

  count="$(echo "${content}" | tr "-" "\n" | grep -c "${pattern}")"
  echo "${count}"
}

function splitCertificates() {
  local subversionConfigDir="${1}"
  local certAlias="${2}"
  local certs="${3}"

  # based on https://serverfault.com/questions/391396/how-to-split-a-pem-file
  echo "${certs}" | csplit -z -f "${subversionConfigDir}/cert-${certAlias}-" - '/-----BEGIN CERTIFICATE-----/' '{*}'
}

function resetSubversionSSLConfig() {
  subversionServerConfigFile="${1}"

  echo "Resetting subversion SSL entry..."
  assertSubversionSSLEntry "${subversionServerConfigFile}"
  
  sed -i 's|\(# \)\?ssl-authority-files.\+|ssl-authority-files = |' "${subversionServerConfigFile}"
}

function assertSubversionSSLEntry() {
  echo "assertSubversionSSLEntry"

  local subversionServerConfigFile="${1}"
  local configEntryFound=0
  cat "${subversionServerConfigFile}"

  grep -q 'ssl-authority-files' "${subversionServerConfigFile}" || configEntryFound=$?
  if [[ ${configEntryFound} -ne 0 ]]; then
    echo "ERROR: Could not find entry 'ssl-authority-files' in ${subversionServerConfigFile}"
    exit 1
  fi
}

function addCertificatePathToSubversionSSLConfig() {
  local subversionServerConfigFile="${1}"
  local subversionConfigDir="${2}"
  local certificateAlias="${3}"
  local certificateCount="${4}"

  for zeroBasedCount in $(seq 0 "${certificateCount}"); do
    if [[ ${zeroBasedCount} -eq ${certificateCount} ]]; then
      return 0
    fi

    counter=$(printf "%02d" "${zeroBasedCount}")
    local certificatePath="${subversionConfigDir}/cert-${certificateAlias}-${counter}"
    sed -i "s|\(ssl-authority-files =.\+\)|\1${certificatePath};|" "${subversionServerConfigFile}"
  done
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  run_main
fi
