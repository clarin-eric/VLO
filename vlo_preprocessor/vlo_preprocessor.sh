#!/bin/sh
# script to start up the vlo preprocessing xsl.

INPUT_DIR=$1

if [ -z $INPUT_DIR ] 
then
    echo "Usage params missing: <path_to_inputdir>"
    echo "For example: '/lat/tools/vlo_importer/data/imdi/imdi-20110106/corpora'"
    echo "ERROR"
    exit 0
fi

#assuming work dir has these files 
SAXONJAR=saxon8.jar
XSLTEMPLATE=ImdiCmdi_preprocess.xsl
ORGANISATIONS_INPUT_FILE=OrganisationControlledVocabulary.xml

JAVA=java

echo "Starting preprocessing..."
$JAVA -Xmx1024M -jar $SAXONJAR -it main $XSLTEMPLATE inputDir=$INPUT_DIR organisationsFile=$ORGANISATIONS_INPUT_FILE
echo "...done."