The goal of this project is to make it easy to create an installation of a solr/server.

As of version 6, Solr is intended to be run as a stand-alone service rather than deployed
to a servlet container. This project only contains the VLO specific Solr configuration.

# Building

To obtain a pre-configured, runnable instance of Solr, run the script

	./build-solr.sh

in this directory. It will retrieve Solr, create and configure a collection and prepare
a distribution package `vlo-solr.tar.gz` than you can deploy on a server. 

# Running

There are various ways of running the Solr instance. For instructions on how to best 
deploy the Solr instance, see the following page:

	https://lucene.apache.org/solr/guide/7_0/taking-solr-to-production.html#taking-solr-to-production

The output of this build script can also be used in the build process of a dedicated 
Docker image.

Make sure to configure the Solr home directory via the `solr.solr.home` system property
and the Solr data directory via the `solr.data.dir` property.
For example, if starting the Solr instance using the "solr start" command, do as follows:

	${SOLR_BIN_DIR}/solr start -s /srv/vlo/config/solr -t /var/solr-data

Or add the setting of the property to `SOLR_OPTS` variable in the `solr.in.sh` file (see 
the official Solr documentation instructions for details).
