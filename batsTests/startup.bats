#!/usr/bin/env bash
# Bind an unbound BATS variable that fails all tests when combined with 'set -o nounset'
export BATS_TEST_START_TIME="0"


load '/workspace/target/bats_libs/bats-support/load.bash'
load '/workspace/target/bats_libs/bats-assert/load.bash'
load '/workspace/target/bats_libs/bats-mock/load.bash'
load '/workspace/target/bats_libs/bats-file/load.bash'

setup() {
  export STARTUP_DIR=/workspace/

  # bats-mock/mock_create needs to be injected into the path so the production code will find the mock
  doguctl="$(mock_create)"
  export doguctl
  ln -s "${doguctl}" "${BATS_TMPDIR}/doguctl"
  svn="$(mock_create)"
  export svn
  ln -s "${svn}" "${BATS_TMPDIR}/svn"
  export PATH="${PATH}:${BATS_TMPDIR}"
  mockCaCertificates="$(mktemp)"
  export mockCaCertificates
  mockHome="$(mktemp -d)"
  export mockHome
  BATSLIB_FILE_PATH_REM="#${TEST_TEMP_DIR}"
  BATSLIB_FILE_PATH_ADD='<temp>'
}

teardown() {
  rm "${BATS_TMPDIR}/doguctl"
  rm "${BATS_TMPDIR}/svn"
  rm "${mockCaCertificates}"
  rm -rf "${mockHome}"
}

assert_file_not_contains() {
  local file="${1}"
  local expectedPattern="${2}"

  if ! grep -v "${expectedPattern}" "${file}"; then
    local -r rem="$BATSLIB_FILE_PATH_REM"
    local -r add="$BATSLIB_FILE_PATH_ADD"
    batslib_print_kv_single 4 'path' "${file/$rem/$add}" \
      | batslib_decorate "Expected to not find '${expectedPattern}'" \
      | fail
  fi
}

@test "createCurlCertificates() should create a curl config file if not existing" {
  assert_file_not_exist "${mockHome}/.curlrc"
  source /workspace/resources/startup.sh

  run createCurlCertificates "${mockHome}"

  assert_success
  assert_file_exist "${mockHome}/.curlrc"
  assert_file_contains "${mockHome}/.curlrc" "cacert = ${mockHome}/ca-certificates.crt"
}

@test "createCurlCertificates() should overwrite an existing curl config file" {
  testString="Hello world."
  echo "${testString}" > "${mockHome}/.curlrc"
  assert_file_exist "${mockHome}/.curlrc"
  source /workspace/resources/startup.sh

  run createCurlCertificates "${mockHome}"

  assert_success
  assert_file_exist "${mockHome}/.curlrc"
  assert_file_contains "${mockHome}/.curlrc" "cacert = ${mockHome}/ca-certificates.crt"
  grep -v "${testString}" "${mockHome}/.curlrc" || fail "Expected to not find 'Hello World'"
}

@test "createSubversionCertificates() should do nothing when no additional certificates exist" {
  assert_not_exist "${mockHome}/.subversion"
  mock_set_status "${doguctl}" 0
  mock_set_output "${doguctl}" "NV" 1
  source /workspace/batsTests/usr/bin/create-ca-certificates.sh
  source /workspace/resources/startup.sh

  run createSubversionCertificates "${mockHome}"

  assert_success
  assert_not_exist "${mockHome}/.subversion"
  assert_equal "$(mock_get_call_num "${doguctl}")" "1"
  assert_equal "$(mock_get_call_args "${doguctl}" "1")" "config --default NV --global certificate/additional/toc"
}

@test "createSubversionCertificates() should split double-PEM keys and save them in .subversion" {
  mockSubversionServersConfig="${mockHome}/.subversion/servers"
  mock_set_status "${doguctl}" 0
  mock_set_output "${doguctl}" "alias1\n" 1
  mock_set_output "${doguctl}" "alias1\n" 2
  mock_set_output "${doguctl}" "-----BEGIN CERTIFICATE-----\nCERT FOR CONTENT1\n-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----\nCA-CERT FOR CONTENT1\n-----END CERTIFICATE-----\n" 3
  mock_set_side_effect "${svn}" "mkdir -p ${mockHome}/.subversion ; echo '[global]' > ${mockSubversionServersConfig}; echo 'ssl-authority-files =' >> ${mockSubversionServersConfig}"

  csplit (){
    echo "CERT FOR CONTENT1" > "${mockHome}/.subversion/cert-alias1-00"
    echo "CA-CERT FOR CONTENT1" > "${mockHome}/.subversion/cert-alias1-01"
  }

  source /workspace/batsTests/usr/bin/create-ca-certificates.sh
  source /workspace/resources/startup.sh

  run createSubversionCertificates "${mockHome}"

  assert_success
  assert_exist "${mockSubversionServersConfig}"
  assert_line --partial "Adding additional certificate for key alias1 to subversion store"
  assert_exist "${mockHome}/.subversion/cert-alias1-00"
  assert_exist "${mockHome}/.subversion/cert-alias1-01"
  assert_file_contains "${mockHome}/.subversion/cert-alias1-00" "CERT FOR CONTENT1"
  assert_file_contains "${mockHome}/.subversion/cert-alias1-01" "CA-CERT FOR CONTENT1"
  assert_file_contains "${mockSubversionServersConfig}" "[global]"
  assert_file_contains "${mockSubversionServersConfig}" "${mockHome}/.subversion/cert-alias1-00;${mockHome}/.subversion/cert-alias1-01;"
  assert_equal "$(mock_get_call_num "${doguctl}")" "3"
  assert_equal "$(mock_get_call_args "${doguctl}" "1")" "config --default NV --global certificate/additional/toc"
  assert_equal "$(mock_get_call_args "${doguctl}" "2")" "config --global certificate/additional/toc"
  assert_equal "$(mock_get_call_args "${doguctl}" "3")" "config --global certificate/additional/alias1"
  assert_equal "$(mock_get_call_num "${svn}")" "1"
}

@test "createSubversionCertificates() should skip defect certificate and split double-PEM keys and save them in .subversion" {
  mock_set_status "${doguctl}" 0
  mock_set_output "${doguctl}" "alias1 alias2\n" 1
  mock_set_output "${doguctl}" "alias1 alias2\n" 2
  mock_set_output "${doguctl}" "-----BEGIN CERTIFICATE-----\nCERT FOR CONTENT1\n-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----\nCA-CERT FOR CONTENT1\n---DEFECT\n" 3
  mock_set_output "${doguctl}" "-----BEGIN CERTIFICATE-----\nCERT FOR CONTENT2\n-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----\nCA-CERT FOR CONTENT2\n-----END CERTIFICATE-----\n" 4
  mock_set_side_effect "${svn}" "mkdir -p ${mockHome}/.subversion ; echo '[global]' > ${mockHome}/.subversion/servers ; echo 'ssl-authority-files =' >> ${mockHome}/.subversion/servers"

  csplit (){
    echo "CERT FOR CONTENT2" > "${mockHome}/.subversion/cert-alias2-00"
    echo "CA-CERT FOR CONTENT2" > "${mockHome}/.subversion/cert-alias2-01"
  }

  source /workspace/batsTests/usr/bin/create-ca-certificates.sh
  source /workspace/resources/startup.sh

  run createSubversionCertificates "${mockHome}"

  assert_success
  assert_exist "${mockHome}/.subversion/servers"
  refute_line --partial "Adding additional certificate for key alias1 to subversion store"
  assert_line --partial "ERROR: Skip adding invalid additional certificate for key certificate/additional/alias1"
  assert_line --partial "Adding additional certificate for key alias2 to subversion store"
  assert_not_exist "${mockHome}/.subversion/cert-alias1-00"
  assert_not_exist "${mockHome}/.subversion/cert-alias1-01"
  assert_exist "${mockHome}/.subversion/cert-alias2-00"
  assert_exist "${mockHome}/.subversion/cert-alias2-01"
  assert_file_contains "${mockHome}/.subversion/cert-alias2-00" "CERT FOR CONTENT2"
  assert_file_contains "${mockHome}/.subversion/cert-alias2-01" "CA-CERT FOR CONTENT2"
  assert_equal "$(mock_get_call_num "${doguctl}")" "4"
  assert_equal "$(mock_get_call_args "${doguctl}" "1")" "config --default NV --global certificate/additional/toc"
  assert_equal "$(mock_get_call_args "${doguctl}" "2")" "config --global certificate/additional/toc"
  assert_equal "$(mock_get_call_args "${doguctl}" "3")" "config --global certificate/additional/alias1"
  assert_equal "$(mock_get_call_args "${doguctl}" "4")" "config --global certificate/additional/alias2"
  assert_equal "$(mock_get_call_num "${svn}")" "1"
}

@test "checkCertCount() should return true for equal count of BEGIN and END CERTIFICATE lines" {
  source /workspace/resources/startup.sh

  run checkCertCount "-----BEGIN CERTIFICATE-----\nCERT FOR CONTENT1\n-----END CERTIFICATE-----\n----BEGIN CERTIFICATE-----\nCA-CERT FOR CONTENT1\n-----END CERTIFICATE-----\n"

  assert_success
}

@test "checkCertCount() should return false for unequal count of BEGIN and END CERTIFICATE lines" {
  source /workspace/resources/startup.sh

  run checkCertCount "-----BEGIN CERTIFICATE-----\nCERT FOR CONTENT1\n-----END CERTIFICATE-----\n----BEGIN CERTIFICATE"

  assert_failure
}

@test "countCertString() should return 2" {
  source /workspace/resources/startup.sh

  run countCertString BEGIN "-----BEGIN CERTIFICATE-----\nCERT FOR CONTENT1\n-----END CERTIFICATE-----\n----BEGIN CERTIFICATE-----\nCA-CERT FOR CONTENT1\n-----END CERTIFICATE-----\n"

  assert_success
  assert_output "2"

  run countCertString BEGIN "-----BEGIN CERTIFICATE-----\nCERT FOR CONTENT1\n-----END CERTIFICATE-----\n"

  assert_success
  assert_output "1"
}

@test "resetSubversionSSLConfig() should empty the key's existing values" {
  subversionServersConfig=$(mktemp)
  echo "[global]" > "${subversionServersConfig}"
  {
    echo "# commented line 1"
    echo "# ssl-authority-files = /path/to/cert1.pem;/path/to/cert2.pem"
    echo "# commented line 2"
  } >> "${subversionServersConfig}"
  source /workspace/resources/startup.sh

  run resetSubversionSSLConfig "${subversionServersConfig}"

  assert_success
  assert_file_contains "${subversionServersConfig}" "[global]"
  assert_file_contains "${subversionServersConfig}" "# commented line 1"
  assert_file_contains "${subversionServersConfig}" "# commented line 2"
  assert_file_contains "${subversionServersConfig}" "ssl-authority-files ="
  assert_file_not_contains "${subversionServersConfig}" "# ssl-authority-files"
}

@test "addCertificatePathToSubversionSSLConfig() should replace commented subversion cert line" {
  subversionServersConfig=$(mktemp)
  echo "[global]" > "${subversionServersConfig}"
  {
    echo "# commented line 1"
    echo "ssl-authority-files = /path/to/cert1.pem;/path/to/cert2.pem;"
    echo "# commented line 2"
  } >> "${subversionServersConfig}"

  source /workspace/resources/startup.sh

  run addCertificatePathToSubversionSSLConfig "${subversionServersConfig}" "/path/to/.subversion" "alias1" "2"

  assert_success
  assert_file_contains "${subversionServersConfig}" "[global]"
  assert_file_contains "${subversionServersConfig}" "# commented line 1"
  assert_file_contains "${subversionServersConfig}" "# commented line 2"
  assert_file_contains "${subversionServersConfig}" "ssl-authority-files = /path/to/cert1.pem;/path/to/cert2.pem;/path/to/.subversion/cert-alias1-00;/path/to/.subversion/cert-alias1-01;"
}