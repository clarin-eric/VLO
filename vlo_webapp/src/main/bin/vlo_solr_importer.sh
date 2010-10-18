#!/bin/sh

LIB=../share
CLASSPATH=.:./log4j.properties:./importerConfig.xml
JAVA=java

for f in `ls $LIB`
do
	CLASSPATH=$CLASSPATH:$LIB/$f
done

#echo $CLASSPATH

$JAVA -Xmx1024M -cp $CLASSPATH eu.clarin.cmdi.vlo.importer.MetadataImporter "$@"