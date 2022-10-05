#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail

# this is a mock script being sourced from a production script.
# the actual script origins from the base image

ADDITIONAL_CERTIFICATES_DIR_KEY="certificate/additional"
ADDITIONAL_CERTIFICATES_TOC="${ADDITIONAL_CERTIFICATES_DIR_KEY}/toc"

function existAdditionalCertificates() {
  additionalCertTOC="$(doguctl config --default NV --global "${ADDITIONAL_CERTIFICATES_TOC}")"

  if [[ -z "${additionalCertTOC// }" ]] || [[ "${additionalCertTOC}" == "NV" ]]; then
    return 1
  else
    return 0
  fi
}