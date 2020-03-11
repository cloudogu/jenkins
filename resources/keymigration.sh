#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail


CURRENT_KEY="${1}"
NEW_KEY="${2}"

if doguctl config "${CURRENT_KEY}"; then
  echo "WARNING: The key ${CURRENT_KEY} was found but is deprecated. To ensure a proper configuration  \
  the key will be migrated to ${NEW_KEY}. Please consider using the new key ${NEW_KEY} because ${CURRENT_KEY} will\
  be deleted in future releases."
  val=$(doguctl config "${CURRENT_KEY}")
  doguctl config "${NEW_KEY}" "${val}"
  if [[ $? -eq 0 ]]; then
     doguctl config --remove "${CURRENT_KEY}"
     echo "Keymigration finished successfully"
  fi
else
  echo "No Keymigration required"
fi

