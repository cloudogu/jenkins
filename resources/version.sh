#!/bin/bash
set -o errexit
set -o nounset


function compareVersion() {
  local sourceVersion="${1}"
  local targetVersion="${2}"

  if [[ "${sourceVersion}" == "${targetVersion}" ]]; then
    echo 0
    return 0
  fi

  declare -r semVerRegex='([0-9]+)\.([0-9]+)\.([0-9]+)-([0-9]+)'

  sourceMajor=0
  sourceMinor=0
  sourceBugfix=0
  sourceDogu=0
  targetMajor=0
  targetMinor=0
  targetBugfix=0
  targetDogu=0

  if [[ ${sourceVersion} =~ ${semVerRegex} ]]; then
    sourceMajor=${BASH_REMATCH[1]}
    sourceMinor="${BASH_REMATCH[2]}"
    sourceBugfix="${BASH_REMATCH[3]}"
    sourceDogu="${BASH_REMATCH[4]}"
  else
    echo "ERROR: source dogu version ${sourceVersion} does not seem to be a semantic version"
    exit 1
  fi

  if [[ ${targetVersion} =~ ${semVerRegex} ]]; then
    targetMajor=${BASH_REMATCH[1]}
    targetMinor="${BASH_REMATCH[2]}"
    targetBugfix="${BASH_REMATCH[3]}"
    targetDogu="${BASH_REMATCH[4]}"
  else
    echo "ERROR: target dogu version ${targetVersion} does not seem to be a semantic version"
    exit 1
  fi

  if [[ $((sourceMajor)) -lt $((targetMajor)) ]] ; then
     echo -1
  elif [[ $((sourceMajor)) -gt $((targetMajor)) ]] ; then
    echo 1
  else
    if [[ $((sourceMinor)) -lt $((targetMinor)) ]] ; then
       echo -1
    elif [[ $((sourceMinor)) -gt $((targetMinor)) ]] ; then
      echo 1
    else
      if [[ $((sourceBugfix)) -lt $((targetBugfix)) ]] ; then
         echo -1
      elif [[ $((sourceBugfix)) -gt $((targetBugfix)) ]] ; then
        echo 1
      else
        if [[ $((sourceDogu)) -lt $((targetDogu)) ]] ; then
           echo -1
        elif [[ $((sourceBugfix)) -gt $((targetDogu)) ]] ; then
          echo 1
        fi
      fi
    fi
  fi
}

function isGreaterVersionThen() {
  local F_VERSION="${1}"
  local T_VERSION="${2}"
  if [[ "$(compareVersion $F_VERSION $T_VERSION)" == "1" ]]; then
    true; return
  fi
  false; return
}

function isLesserVersionThen() {
  local F_VERSION="${1}"
  local T_VERSION="${2}"
  if [[ "$(compareVersion $F_VERSION $T_VERSION)" == "-1" ]]; then
    true; return
  fi
  false; return
}