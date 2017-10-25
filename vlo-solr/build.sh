#!/bin/sh
SOLR_DIST_URL="http://ftp.tudelft.nl/apache/lucene/solr/6.6.2/solr-6.6.2.tgz"
SOLR_TARGET_DIR="target/solr"
SOLR_COLLECTION_NAME="collection1"
SOLR_CONFIGURATION_SOURCE_DIR="`pwd`/../vlo-web-app/src/test/resources/solr/collection1"

set -e

if [ "clean" = "$1" ]
then
	# Clean build output
	if [ -d "${SOLR_TARGET_DIR}" ]
	then
		echo "Removing ${SOLR_TARGET_DIR}"
		rm -rf "${SOLR_TARGET_DIR}"
	else
		echo "Nothing to do"
	fi
	exit 0
fi

# Check if configuration sources are available
if [ ! -d "${SOLR_CONFIGURATION_SOURCE_DIR}" ]
then
	echo "Error: solr configuration source directory ${SOLR_CONFIGURATION_SOURCE_DIR} does not exist."
	exit 1
fi

# Make a build output directory, or fail if it already exists
if [ -d "${SOLR_TARGET_DIR}" ]
then
	echo "Solr target directory already exists. Run '$0 clean' to clean up."
	exit 1
else
	mkdir -p "${SOLR_TARGET_DIR}"
fi

(
	cd "${SOLR_TARGET_DIR}"
	echo "====\nDownloading and extracting Solr...\n====\n"
	curl -\# -L "${SOLR_DIST_URL}" | tar zxf - --strip-components 1

	#create collection
	echo "\n====\nCreating Solr collection '${SOLR_COLLECTION_NAME}'... \n====\n"
	bin/solr start
	bin/solr create -c "${SOLR_COLLECTION_NAME}"
	bin/solr stop

	echo "\n====\nApply VLO Solr configuration... \n====\n"
	(
		cd "server/solr/${SOLR_COLLECTION_NAME}"
		#remove default configuration
		rm -f \
			conf/managed-schema \
			conf/solrconfig.xml \
			conf/*.txt
		#copy in custom configuration
		cp -r ${SOLR_CONFIGURATION_SOURCE_DIR}/* .
	)	
)

echo "Completed building pre-configured VLO Solr instance in ${SOLR_TARGET_DIR}

You can start it by running '${SOLR_TARGET_DIR}/bin/solr start'"
