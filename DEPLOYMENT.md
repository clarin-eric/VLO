# Deploying the VLO

The instructions below describe the installation of the applications necessary
for browsing: the VLO web application, the Solr server, and the meta data
importer.

- `$VLO`:  installation folder for VLO e.g.: /opt/vlo/
- `$CATALINA_HOME`:  Tomcat’s installation folder (for the web app)
- `$SOLR_HOME`:  SOLR location for configuration and definitions
- `$SOLR_DATA_HOME`:  SOLR location for storing indexed data 
- `xxx`:  VLO version number

## The archive

The VLO importer and web application are contained in an archive:

	vlo-xxx-Distribution.tar.gz

Deploying the VLO application means:
- ensuring that Tomcat and a compatible version of Solr are installed
- unpacking the archive in a suitable location
- unpacking the war files contained in the archive
- adapt the application's context files and
- adapt the VloConfig.xml main configuration file and/or the dataroot config
 included in that file
- optionally executing upgrade steps described in the UPGRADE file
   
## Archive unpacking

- `service tomcat stop` 
- `service solr stop`
- `mkdir ${VLO} # you may have to move an old VLO version first` 
- `tar -C ${VLO} --strip-components=1 -zxvf vlo-xxx-Distribution.tar.gz`

In the tree starting in `$VLO`, the configuration of the application is stored under subfolder config.

## Solr

Note: you may find some useful information in the documentation of the
[vlo-solr subproject](./vlo-solr/README.md).

### Installation

Follow the instructions of your operating system and/or the official 
[Solr documentation](https://lucene.apache.org/solr/resources.html) on installing or
upgrading Solr on your system, in particular the section
["Taking Solr to Production"](https://lucene.apache.org/solr/guide/7_1/taking-solr-to-production.html).

A script to obtain a runnable/installable version of Solr can be found inside
`${VLO}/solr/vlo-xxx-solr.tar.gz`. Unpack this, then run the `build-solr.sh` script
inside the unpaked `solr` directory and the script will retrieve and prepare everything 
you need to run or install Solr.  For example:

```sh
$ cd ${VLO}/solr
$ tar zxvf vlo-xxx-solr.tar.gz
$ cd solr
$ ./build-solr.sh
[Build script output omitted]
$ target/solr/bin/solr start -s ${VLO}/solr/vlo-solr-home # This starts a Solr instance in the foreground right away (port 8983)
[Output omitted]
$ curl -L http://localhost:8983/solr/vlo-index/select?rows=10
[Solr response omitted]
$ target/solr/bin/solr stop 
[Output omitted]
$ # Note: the next step will (attempt to) properly install Solr on your system!
$ sudo target/solr/bin/install_solr_service.sh target/solr-?.?.?.tgz
```

If you have a **docker-based setup**, you can also use the
[official docker image](https://github.com/docker-solr), or use the
[version maintained by CLARIN](https://gitlab.com/CLARIN-ERIC/docker-solr/container_registry) if working in
CLARIN's central infrastructure. For configuration hints, see the next section.

In any case, make sure to **always use a matching version of Solr**. Find out which version
to use by looking for the `solr.version` property in the project's [POM file](pom.xml), or
checking the [upgrade instructions](UPGRADE.txt).

### Configuration

Solr allows you to configure (i.e. override the default settings for) the location of
both the [Solr home directory](https://lucene.apache.org/solr/guide/7_1/solr-configuration-files.html#solr-home)
and the location where the index data is stored (by default the latter is a subdirectory
of the former). The way to configure these is by setting the properties `solr.solr.home`
and/or `solr.data.home`. When running Solr as a service in the host, these properties
are best configured by setting the matching variables in the `solr.in.sh` file.

To make the Solr instance work with the VLO, set the `solr.solr.home` property
(`SOLR_HOME` variable) to:
```sh
${VLO}/solr/vlo-solr-home
```

It is a good idea to configure a **separate** directory for Solr to store its **data**
(index and transaction log), as the above location will be replaced when updating the VLO,
and the contents of this directory can grow rather large.
To do this, create a `solr-data` directory somewhere outside the `${VLO}` 
directory (e.g. `/srv/solr-data`) and set `solr.data.home` (`SOLR_DATA_HOME` variable)
to its absolute path. The VLO specific data will then be stored by Solr in
`${solr.data.home}/${CORE_NAME}/data`. Note: as of version 4.3 of the VLO,
`CORE_NAME=vlo-index`.

### Security

The bundled Solr configuration has been extended with a security configuration (see
`solr/vlo-solr-home/security.json` in the deployment package) which enables basic 
authentication for all HTTP access. This file contains hashed passwords for a number of
users with different roles. The VLO has to be configured with credentials for both a user
with only read access, and a user with read/write access. For this purpose, a
number of settings have been introduced to VloConfig.xml:

- `solrUserReadOnly`
- `solrUserReadOnlyPass`
- `solrUserReadWrite`
- `solrUserReadWritePass`

The docker compose setup for the VLO provides shared environment variables for securing
and accessing the Solr instance. See its [documentation](https://gitlab.com/CLARIN-ERIC/compose_vlo/). 
**IMPORTANT**: the Solr home provisioning volume must (and can safely) be removed before
starting the services after upgrading!!

For some technical notes, see [issue #126](https://github.com/clarin-eric/VLO/issues/126) and
the relevant [Solr documentation section](https://lucene.apache.org/solr/guide/7_3/basic-authentication-plugin.html).

#### Docker

In a container based setup (i.e. using Docker (Compose)), you may want to use 
**volumes and/or mounts** to make sure that the right Solr configuration is loaded, and to
ensure the persistence of the Solr index. 

When using the official Solr image for Docker, or CLARIN's derivation of it, the Solr 
*home directory* can be configured when starting the container by setting the environment 
variable `SOLR_HOME`. The *data directory* can be set using the environment variable
`SOLR_DATA_HOME`. For a usage example, see the 
[documentation of the vlo-solr subproject](vlo-solr/README.md). 

Note: making use of the default configuration in which a core's data directory is found
in `${solr.data.home}/${CORE_NAME}/data`, one *could ommit* setting `solr.data.home`
and just mount a persistent (core specific!) storage directory in that location. This is
**not recommended** for production.

The [VLO Docker Compose configuration](https://gitlab.com/CLARIN-ERIC/compose_vlo) may
be helpful to get started. Notice that there are various compose configuration overlays
(`.yml` files) for different environments and different optional features/extensions.

## Web-app

### Deployment

Deploy the war:

	unzip -d "$CATALINA_HOME/webapps/vlo"  "${VLO}/war/vlo-web-app-xxx.war"

OR, if your Tomcat application's docBase is pointed to `${VLO}/war/vlo`:

	(cd "${VLO}/war" && ./unpack-wars.sh)

### Configuration

Modify (if needed):
- `$CATALINA_HOME/webapps/vlo/META-INF/context.xml`
	- parameter `eu.carlin.cmdi.vlo.config.location`
		- should point to `VloConfig.xml`, e.g. `${VLO}/config/VloConfig.xml`
	- parameter `eu.carlin.cmdi.vlo.solr.serverUrl`
		- to override the URL defined in `VloConfig.xml`, should be set to Solr server 
		base URL (see [above](#solr)), e.g. `http://localhost:8983/solr/vlo-index/`
			- leave commented out for the default
	
and copy it to `$CATALINA_HOME/conf/Catalina/localhost/` as `vlo.xml`

	cp /webapps/vlo/META-INF/context.xml $CATALINA_HOME/conf/Catalina/localhost/vlo.xml

Optional:
	
- Modify log4j settings in `$CATALINA_HOME/webapps/vlo/WEB-INF/classes/log4j.properties`
- Wicket runs in development mode by default, to change it add 
following parameter to the `JAVA_OPTS` in `$CATALINA_HOME/bin/setenv.sh`: 
	- `-Dwicket.configuration=deployment`
- or set a context parameter 'configuration' to the value 
		'deployment' in the application's context fragment.

Piwik access statistics can be configured by setting the  following context parameters:
- `eu.clarin.cmdi.vlo.piwik.enableTracker` (set true to enable)
- `eu.clarin.cmdi.vlo.piwik.siteId` (defaults to production value)
- `eu.clarin.cmdi.vlo.piwik.host` (defaults to production value)
- `eu.clarin.cmdi.vlo.piwik.domains` (defaults to production value)
See packaged context.xml for details and examples.

User satisfaction score elicitation and storage in a CouchDB (or equivalent) database
can be configured by setting the following context parameters:
- `eu.clarin.cmdi.vlo.rating.enabled` (set true to enable)
- `eu.clarin.cmdi.vlo.rating.couchdb.url` (has to be the url of the target parent resource)
- `eu.clarin.cmdi.vlo.rating.couchdb.user`
- `eu.clarin.cmdi.vlo.rating.couchdb.password`
- `eu.clarin.cmdi.vlo.rating.panel.showPanelDelay` (defaults to 120)
- `eu.clarin.cmdi.vlo.rating.panel.dismissTimeout` (defaults to 604800)
- `eu.clarin.cmdi.vlo.rating.panel.submitTimeout` (defaults to 2592000)
See packaged context.xml for details and examples.

## Importerer

### Data roots configuration

Modify `DataRoot` for importer directly in `${VLO}/config/VloConfig.xml` or in
the file that is included into that file via XInclude

```xml
<DataRoot>
	<originName>Descriptive name of data origin</originName>
	<rootFile>path to the metada root folder</rootFile>           
	<prefix>http://vlo.clarin.eu/cmdi/</prefix>
	<tostrip>/var/www/cmdi/</tostrip>
	<deleteFirst>false</deleteFirst>
</DataRoot>
```

A `DataRoot` element describes sets of metadata files. The `toStrip` part of
the description is left out of the `rootFile` part to create an absolute, public URL
to the metadata; the links starts with the `prefix`. Set `deleteFirst` to `true` to ensure
that all existing records from this set are removed from the index before importing.
	
### Mapping configuration

Review the following configuration properties in `VloConfig.xml`:
- `facetConceptsFile`, `valueMappingsFile`, `organisationNamesUrl`, 
`languageNameVariantsUrl`, `licenseAvailabilityMapUrl`, `resourceClassMapUrl`,
`licenseURIMapUrl`, `licenseTypeMapUrl`

These should either be set to the defaults to use bundled resources or configured
to be a (file) URL pointing to the location of the corresponding
mapping/normalisation definition from a version of the VLO-mapping definition. 

Both the web application and the importer make use of the files at the configured
locations, so make sure that the content is available when starting the front end
or importer!

See <https://github.com/clarin-eric/VLO-mapping> and the comments in VloConfig.xml 
for more information.

### Importing data

The importer can be found in the `${VLO}/bin/` folder.

- Before starting data import, first start the Tomcat server:
	- `service tomcat6 start`
		- or `$CATALINA_HOME/bin/startup.sh` if tomcat is not installed as a service
- Run the importer:
	- `${VLO}/bin/vlo_solr_importer.sh `
		- optionally pass the path to a custom `VloConfig.xml` using the `-c` option (the default is in `${VLO}/config`)

The importer logs information in `${VLO}/log`

Because metadata is not static, it is recommended to run the importer a
couple of times a week.

