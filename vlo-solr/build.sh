#!/bin/sh
SOLR_DIST_URL="http://ftp.tudelft.nl/apache/lucene/solr/6.6.2/solr-6.6.2.tgz"
SOLR_TARGET_DIR="target/solr"

set -e

if [ -d "${SOLR_TARGET_DIR}" ]
then
	echo "Solr target directory already exists"
	exit 1
else
	mkdir -p "${SOLR_TARGET_DIR}"
fi

(
	cd "${SOLR_TARGET_DIR}"
	echo "====\nDownloading and extracting Solr...\n====\n"
	curl -\# -L "${SOLR_DIST_URL}" | tar zxf - --strip-components 1

	#create "collection1"
	echo "\n====\nCreating Solr collection... \n====\n"
	bin/solr start
	bin/solr create -c "collection1"
	bin/solr stop

	#TODO: copy in config
)

echo "Completed building pre-configured VLO Solr instance in ${SOLR_TARGET_DIR}

You can start it by running '${SOLR_TARGET_DIR}/bin/solr start'"
