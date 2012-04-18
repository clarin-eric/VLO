#!/bin/sh

# This means it the script only works started from the bin directory,
# adjust the workdir if you installed this somewhere and want to run it from anywhere.

# Filter of the Error stream from which to prevent the admins from getting daily "can't find java" emails
JAVA=`which java 2>/dev/null`
if [  -z $JAVA ]; then
#set java for mpi servers
JAVA=/lat/java/bin/java
fi


$JAVA -Xmx1024M -cp . -jar *.jar eu.clarin.cmdi.vlo.importer.MetadataImporter
