#!/bin/bash
SOLR_VERSION="7.3.0"
SOLR_DIST_FILE="solr-${SOLR_VERSION}.tgz"
SOLR_DIST_URL="https://archive.apache.org/dist/lucene/solr/${SOLR_VERSION}/${SOLR_DIST_FILE}"
SOLR_DIST_SIGNATURE_URL="https://archive.apache.org/dist/lucene/solr/${SOLR_VERSION}/${SOLR_DIST_FILE}.asc"
SOLR_DIST_KEYS_URL="https://archive.apache.org/dist/lucene/solr/${SOLR_VERSION}/KEYS"

SOLR_TARGET_DIR="target/solr"

SOLR_HOME_TEMPLATE_DIR="`pwd`/solr-home"
SOLR_HOME_TARGET_DIR="target/solr-home-vlo"

set -e

if [ "clean" = "$1" ]
then
	# Clean build output
	if [ -d "${SOLR_TARGET_DIR}" ] || [ -d "${SOLR_HOME_TARGET_DIR}" ] || [ -e "target/${SOLR_DIST_FILE}" ]
	then
		echo "Removing ${SOLR_TARGET_DIR}, ${SOLR_HOME_TARGET_DIR} and ${SOLR_DIST_FILE}"
		rm -rf "${SOLR_TARGET_DIR}" "${SOLR_HOME_TARGET_DIR}" "target/${SOLR_DIST_FILE}"
	else
		echo "Nothing to do"
	fi
	exit 0
fi

# Check if configuration sources are available
if [ ! -d "${SOLR_HOME_TEMPLATE_DIR}" ]
then
	echo "Error: solr home template directory ${SOLR_HOME_TEMPLATE_DIR} does not exist."
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

echo "====\nDownloading and extracting Solr...\n====\n"
(
	cd "${SOLR_TARGET_DIR}"
	mkdir tmp
	curl -\# -L "${SOLR_DIST_URL}" > "../${SOLR_DIST_FILE}"
	
	echo "====== Verifying integrity ======"
	curl -L "${SOLR_DIST_KEYS_URL}" > "tmp/KEYS" 2>/dev/null
	curl -L "${SOLR_DIST_SIGNATURE_URL}" > "tmp/${SOLR_DIST_FILE}.asc" 2>/dev/null
	
	gpg --import "tmp/KEYS" 2> /dev/null
	gpg --verify "tmp/${SOLR_DIST_FILE}.asc" "../${SOLR_DIST_FILE}"
	
	tar -zxf "../${SOLR_DIST_FILE}" --strip-components 1
	
	rm -rf tmp
)

# Make solr home and apply configuration	
cp -r "${SOLR_HOME_TEMPLATE_DIR}" "${SOLR_HOME_TARGET_DIR}"

echo "\nCompleted building VLO Solr instance in ${SOLR_TARGET_DIR} with configured
Solr home in ${SOLR_HOME_TARGET_DIR}

You can start the server by running '${SOLR_TARGET_DIR}/bin/solr start -s ${SOLR_HOME_TARGET_DIR} [-t /my/solr/data/directory]'

OR install Solr on your os by using the installation script: target/solr/bin/install_solr_service.sh target/${SOLR_DIST_FILE}"
