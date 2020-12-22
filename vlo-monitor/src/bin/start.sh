#!/bin/sh

# Get the script's source directory
DIR="$(dirname "$0")"
JAVA=`which java 2>/dev/null`

(cd "${DIR}" && $JAVA -Xmx4G -jar "${DIR}/vlo-monitor-${project.version}.jar")
