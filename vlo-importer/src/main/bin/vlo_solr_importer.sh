#!/usr/bin/env bash

if [ -z "$IMPORTER_JAVA_OPTS" ]; then
	IMPORTER_JAVA_OPTS="-Xmx4G"
fi

# Get the script's source directory
DIR="$(dirname "$0")"
cd "${DIR}"

if [ -z "${JAVA}" ]; then
	JAVA="$(which java 2>/dev/null)"
fi

if ! [ -x "${JAVA}" ]; then
	echo "Error: java not found"
	exit 1
fi

#default configuration file
DFLT_CONFIG=${DIR}/"../config/VloConfig.xml"

LOGDIR="${DIR}/../log/"
echo "Logging in ${LOGDIR}"

# Please specify the configuration to the importer via a system property or via
# the command line. In case of a property add 
#
# -DconfigFile=<pathToConfigFile>
#
# and in case of the command line, add
#
# -c <pathToConfigFile>
#
# to the JAVA command. Please note the a specification on the command line will
# take preference over a specification as a property.
# 
# to process only subset of data roots from configuration file pass the list via command line:
# -l path OR -l "path1 path2 ..."
#

$JAVA ${IMPORTER_JAVA_OPTS} \
    -cp "${DIR}/lib/*" \
	-DconfigFile="${DFLT_CONFIG}" \
    -DIMPORTER_LOG_DIR="${LOGDIR}" \
    -DIMPORTER_LOG_LEVEL="${IMPORTER_LOG_LEVEL:-INFO}" \
    eu.clarin.cmdi.vlo.importer.MetadataImporterRunner "$@"

