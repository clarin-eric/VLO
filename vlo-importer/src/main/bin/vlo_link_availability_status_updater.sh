#!/usr/bin/env bash

if [ -z "$IMPORTER_JAVA_OPTS" ]; then
	IMPORTER_JAVA_OPTS="-Xmx4G"
fi

# Get the script's source directory
SCRIPT_DIR="$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
cd "${SCRIPT_DIR}" || exit 1

if [ -z "${JAVA}" ]; then
	JAVA="$(which java 2>/dev/null)"
fi

if ! [ -x "${JAVA}" ]; then
	echo "Error: java not found"
	exit 1
fi

#default configuration file
DFLT_CONFIG="${SCRIPT_DIR}/../config/VloConfig.xml"

LOGDIR="${SCRIPT_DIR}/../log/"
echo "Logging in ${LOGDIR}"

# Please specify the configuration to the importer via a command line parameter

$JAVA "${IMPORTER_JAVA_OPTS}" \
    -cp "${SCRIPT_DIR}/lib/*" \
	-DconfigFile="${DFLT_CONFIG}" \
    -DIMPORTER_LOG_DIR="${LOGDIR}" \
    -DIMPORTER_LOG_LEVEL="${IMPORTER_LOG_LEVEL:-INFO}" \
    eu.clarin.cmdi.vlo.importer.linkcheck.AvailabilityStatusUpdater "$@"

