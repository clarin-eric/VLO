#!/bin/sh

# This means it the script only works started from the bin directory, adjust the
# workdir if you installed this somewhere and want to run it from anywhere.

# Filter of the Error stream from which to prevent the admins from getting daily
#  "can't find java" emails
JAVA=`which java 2>/dev/null`
if [  -z $JAVA ]; then
#set java for mpi servers
JAVA=/lat/java/bin/java
fi

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

$JAVA -Xmx1024M -cp .:vlo_importer-2.18-importer.jar eu.clarin.cmdi.vlo.importer.MetadataImporter 
