#!/bin/bash
set -o errexit
set -o nounset
set -o pipefail


CURRENT_KEY="${1}"
NEW_KEY="${2}"

if doguctl config "${CURRENT_KEY}"; then
  echo "Migrating key ${CURRENT_KEY} to ${NEW_KEY}"
  val=$(doguctl config "${CURRENT_KEY}")
  doguctl config "${NEW_KEY}" "${val}"
  if [[ $? -eq 0 ]]; then
     doguctl config --remove "${CURRENT_KEY}"
     echo "Keymigration finished successfully"
  fi
else
  echo "No Keymigration required"
fi

