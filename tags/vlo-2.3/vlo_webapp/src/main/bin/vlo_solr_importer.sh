#!/bin/sh

# This means it the script only works started from the bin directory, 
# adjust the workdir if you installed this somewhere and want to run it from anywhere.
WORKDIR=.
LIB=../share
CLASSPATH=.
JAVA=`which java`
if [  -z $JAVA ]; then
#set java for mpi servers
JAVA=/lat/java/bin/java
fi

for f in `ls $WORKDIR/*.properties`
do
    CLASSPATH=$CLASSPATH:$WORKDIR/$f
done

for f in `ls $WORKDIR/*.xml`
do
    CLASSPATH=$CLASSPATH:$WORKDIR/$f
done

for f in `ls $LIB`
do
	CLASSPATH=$CLASSPATH:$LIB/$f
done

#echo $CLASSPATH

$JAVA -Xmx1024M -cp $CLASSPATH eu.clarin.cmdi.vlo.importer.MetadataImporter "$@"