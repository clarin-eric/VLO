#!/usr/bin/env bash
SCRIPT_DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"

#shellcheck disable=SC2154
cd "${SCRIPT_DIR}" && java -Xmx2G -jar "${SCRIPT_DIR}/vlo-statistics-${project.version}.jar" "$@"
