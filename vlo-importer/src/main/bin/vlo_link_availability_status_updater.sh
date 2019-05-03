#!/usr/bin/env bash

if [ -z "$IMPORTER_JAVA_OPTS" ]; then
	IMPORTER_JAVA_OPTS="-Xmx4G"
fi

# Get the script's source directory
DIR="$(dirname "$0")"
cd "${DIR}"

# Filter of the Error stream from which to prevent the admins from getting daily
#  "can't find java" emails
JAVA=`which java 2>/dev/null`
if [  -z $JAVA ]; then
#set java for mpi servers
JAVA=/lat/java/bin/java
fi

#default configuration file
DFLT_CONFIG=${DIR}/"../config/VloConfig.xml"

LOGDIR=${DIR}/../log/
echo Logging in ${LOGDIR}

# Please specify the configuration to the importer via a command line parameter

$JAVA ${IMPORTER_JAVA_OPTS} \
    -cp "${DIR}:${DIR}/vlo-importer-${project.version}-importer.jar" \
	-DconfigFile=${DFLT_CONFIG} \
    -DIMPORTER_LOG_DIR=${LOGDIR} \
    -DIMPORTER_LOG_LEVEL=${IMPORTER_LOG_LEVEL:-INFO} \
    eu.clarin.cmdi.vlo.importer.linkcheck.AvailabilityStatusUpdater "$@"

