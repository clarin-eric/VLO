#!/bin/sh

# Get the script's source directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Filter of the Error stream from which to prevent the admins from getting daily
#  "can't find java" emails
JAVA=`which java 2>/dev/null`
if [  -z $JAVA ]; then
#set java for mpi servers
JAVA=/lat/java/bin/java
fi

# try to get the configuration file name from the command line options
if [ 1 -eq $# ]; then
CONFIG=$1
else
# fall back to default location
echo No configuration location specified, using default
CONFIG=${DIR}/"../config/VloConfig.xml"
fi

LOGDIR=${DIR}/../log/
echo Logging in ${LOGDIR}

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

$JAVA -Xmx2024M \
    -cp "${DIR}:${DIR}/vlo-importer-3.2-SNAPSHOT-importer.jar" \
    -DIMPORTER_LOG_DIR=${LOGDIR} \
    eu.clarin.cmdi.vlo.importer.MetadataImporter -c "$CONFIG"

