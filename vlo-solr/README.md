The goal of this project is to make it easy to create an installation of a solr/server.

As of version 6, Solr is intended to be run as a stand-alone service rather than deployed
to a servlet container. This project only contains the VLO specific Solr configuration.

# Building

To obtain a pre-configured, runnable instance of Solr, run the script

```
./build-solr.sh
```

in this directory. It will retrieve Solr, create and configure a collection and prepare
a distribution package `vlo-solr.tar.gz` than you can deploy on a server.  Note
that the distribution contains two directories:

- `solr`: a solr distribution that you can use to run or install a solr server
- `solr-home-vlo`: a directory prepared to serve as the 'solr home' directory, containing
a pre-configured core that the VLO importer and web app can work with

You will need to use both in order to get the VLO up and running! The former can be
substituted with an instance of the correct version of Solr provided via the package
manager of your OS or the official Solr Docker images (also see below).

# Running

There are various ways of running the Solr instance. For instructions on how to best 
deploy the Solr instance, see the the section 
["Taking Solr to Production"](https://lucene.apache.org/solr/guide/7_0/taking-solr-to-production.html#taking-solr-to-production)
of the official Solr guide.

Make sure to configure the Solr home directory via the `solr.solr.home` system property
or `-s` parameter, and the Solr data directory via the `solr.data.dir` property or the
`-t` parameter. For example, if starting the Solr instance using the "solr start" command,
do as follows:

```
${SOLR_BIN_DIR}/solr start -s ${SOLR_VLO_HOME_DIR} -t ${SOLR_VLO_DATA_DIR}
```

Or set the properties `SOLR_HOME` and `SOLR_DATA_HOME` in the `solr.in.sh` file (see 
the official Solr documentation instructions for details).

## Docker

Official Docker images for Solr, and usage instructions, can be found on
[DockerHub](https://hub.docker.com/_/solr/).
	
Provided you have a `solr-home-vlo` directory and want to use a persistent data directory,
and that all permisions are set correctly, one can run a VLO Solr instance using the 
following command:

```
docker run -it \
	-v ${HOST_SOLR_HOME}:/solr-home-vlo \
	-v ${HOST_SOLR_DATA_HOME}:/solr-home-vlo/collection1/data \
	-e SOLR_HOME=/solr-home-vlo \
	-p 8983:8983 \
	solr:7.1-alpine`
```
