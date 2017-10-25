#!/bin/sh
SOLR_DIST_URL="http://ftp.tudelft.nl/apache/lucene/solr/6.6.2/solr-6.6.2.tgz"

set -e

if [ -d "target/solr" ]
then
	echo "Solr target directory already exists"
	exit 1
else
	mkdir -p "target/solr"
fi

(
	cd "target/solr"
	echo Downloading and extracting Solr...
	curl -\# -L "${SOLR_DIST_URL}" | tar zxf - --strip-components 1

	#create "collection1"
	echo Creating Solr collection... 
	bin/solr start
	bin/solr create -c "collection1"
	bin/solr stop

	#TODO: copy in config
)

