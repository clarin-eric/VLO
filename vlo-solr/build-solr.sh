#!/bin/sh
SOLR_VERSION="7.1.0"
#SOLR_DIST_URL="http://ftp.tudelft.nl/apache/lucene/solr/${SOLR_VERSION}/solr-${SOLR_VERSION}.tgz"
SOLR_DIST_URL="file:/Users/twagoo/Desktop/solr-${SOLR_VERSION}.tgz"
SOLR_TARGET_DIR="target/solr"
SOLR_HOME_TARGET_DIR="target/solr-home-vlo"
SOLR_TARGET_DIST="target/vlo-solr.tar.gz"
SOLR_COLLECTION_NAME="collection1"
SOLR_CONFIGURATION_SOURCE_DIR="`pwd`/../vlo-web-app/src/test/resources/solr/collection1"

set -e

if [ "clean" = "$1" ]
then
	# Clean build output
	if [ -d "${SOLR_TARGET_DIR}" ] || [ -d "${SOLR_TARGET_DIST}" ] || [ -d "${SOLR_HOME_TARGET_DIR}" ]
	then
		echo "Removing ${SOLR_TARGET_DIR}, ${SOLR_TARGET_DIST} and ${SOLR_HOME_TARGET_DIR}"
		rm -rf "${SOLR_TARGET_DIR}" "${SOLR_TARGET_DIST}" "${SOLR_HOME_TARGET_DIR}"
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

# Make a build output directory, or fail if it already exists
if [ -d "${SOLR_HOME_TARGET_DIR}" ]
then
	echo "Solr home target directory already exists. Run '$0 clean' to clean up."
	exit 1
fi

(
	cd "${SOLR_TARGET_DIR}"
	echo "====\nDownloading and extracting Solr...\n====\n"
	curl -\# -L "${SOLR_DIST_URL}" | tar zxf - --strip-components 1

	#create collection
	echo "\n====\nCreating Solr collection '${SOLR_COLLECTION_NAME}'... \n====\n"
	bin/solr start -p 8989
	bin/solr create -c "${SOLR_COLLECTION_NAME}" -p 8989
	bin/solr stop -p 8989
)

# Make solr home and apply configuration	
echo cp -r "${SOLR_TARGET_DIR}/server/solr" "${SOLR_HOME_TARGET_DIR}"
cp -r "${SOLR_TARGET_DIR}/server/solr" "${SOLR_HOME_TARGET_DIR}"

echo "\n====\nApply VLO Solr configuration... \n====\n"
(
	cd "${SOLR_HOME_TARGET_DIR}/${SOLR_COLLECTION_NAME}"
	#remove default configuration
	rm -f \
		conf/managed-schema \
		conf/solrconfig.xml \
		conf/*.txt
	#copy in custom configuration
	cp -r ${SOLR_CONFIGURATION_SOURCE_DIR}/* .
)

# Clean up server
(
	cd "${SOLR_TARGET_DIR}"
	#delete collection
	echo "\n====\nDeleting Solr collection '${SOLR_COLLECTION_NAME}' from server configuration... \n====\n"
	bin/solr start -p 8989
	bin/solr delete -c "${SOLR_COLLECTION_NAME}" -p 8989
	bin/solr stop -p 8989
)

# Make tarball
echo "\n====\nPackaging VLO Solr instance... \n====\n"
(cd target && tar zc solr solr-home-vlo) > ${SOLR_TARGET_DIST}

echo "Completed building VLO Solr instance in ${SOLR_TARGET_DIR} with configured
Solr home in ${SOLR_HOME_TARGET_DIR}

Created '${SOLR_TARGET_DIST}' for distribution

You can also start the server by running '${SOLR_TARGET_DIR}/bin/solr start -s ${SOLR_HOME_TARGET_DIR} [-t /my/solr/data/directory]'"
