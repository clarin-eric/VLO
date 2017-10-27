# Deploying the VLO

The instructions below describe the installation of the applications necessary
for browsing: the VLO web application, the Solr server, and the meta data
importer.

- `$VLO`:  installation folder for VLO e.g.: /opt/vlo/
- `$CATALINA_HOME`:  tomcat’s installation folder
- `$SOLR_DATA`:  SOLR location for storing indexed data 
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

- `service tomcat6 stop` 
	- (or `$CATALINA_HOME/bin/shutdown.sh` if tomcat is 
not installed as a service)
- `mkdir $VLO`
- `tar -C $VLO --strip-components=1 -zxvf vlo-xxx-Distribution.tar.gz`

In the tree starting in $VLO, the configuration of the application is stored under subfolder config.

## Solr

Note: you may find some useful information in the documentation of the
[vlo-solr subproject](./vlo-solr/README.md).

### Installation

Follow the instructions of your operating system and/or the official 
[Solr documentation](https://lucene.apache.org/solr/resources.html) on installing or
upgrading Solr on your system. 

If you have a **docker-based setup**, you can also use the
[official docker image](https://github.com/docker-solr) or use the
[version maintained by CLARIN](https://gitlab.com/CLARIN-ERIC/docker-solr) if working in
CLARIN's central infrastructure. 

In any case, make sure to **always use a matching version of Solr**. Find out which version
to use by looking for the `solr.version` property in the project's [POM file](pom.xml), or
checking the [upgrade instructions](UPGRADE.txt).

### Configuration

`TODO: setting Solr home and Solr data`

## Web-app

### Deployment

Deploy the war:

	unzip -d $CATALINA_HOME/webapps/vlo  $VLO/war/vlo-web-app-xxx.war

OR, if your Tomcat application's docBase is pointed to `$VLO/war/vlo`:

	(cd $VLO/war && ./unpack-wars.sh)

### Configuration

Modify (if needed):
- `$CATALINA_HOME/webapps/vlo/META-INF/context.xml`
	- parameter `eu.carlin.cmdi.vlo.config.location`
		- should point to VloConfig.xml, e.g. `$VLO/config/VloConfig.xml`
	- parameter `eu.carlin.cmdi.vlo.solr.serverUrl`
		- should be set to Solr server base URL (see [above](#solr)), e.g. `http://localhost:8983/vlo_solr/core0/`
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

## Importer configuration

Modify `DataRoot` for importer directly in `$VLO/config/VloConfig.xml` or in
the file that is included into that file via XInclude

```xml
<DataRoot>
	<originName>MPI self harvest</originName>
	<rootFile>path to the metada root folder</rootFile>           
	<prefix>http://m12404423/vlomd/</prefix>
	<tostrip>/var/www/vlomd/</tostrip>
	<deleteFirst>false</deleteFirst>
</DataRoot>
```

A dataRoot element describes the meta data files. The toStrip part of
the description is left out of the rootFile part to create a http link
to the metadata; the links starts with the prefix.
	
## Mapping configuration

Review the following configuration properties in VloConfig.xml:
- facetConceptsFile, nationalProjectMapping, organisationNamesUrl, 
languageNameVariantsUrl, licenseAvailabilityMapUrl, resourceClassMapUrl,
licenseURIMapUrl, licenseTypeMapUrl

These should either be set to the defaults to use bundled resources or configured
to be a (file) URL pointing to the location of the corresponding
mapping/normalisation definition from a version of the VLO-mapping definition. 

Both the web application and the importer make use of the files at the configured
locations, so make sure that the content is available when starting the front end
or importer!

See <https://github.com/clarin-eric/VLO-mapping> and the comments in VloConfig.xml 
for more information.

## Importing data

The importer can be found in the `$VLO/bin/` folder.

- Before starting data import, first start the Tomcat server:
	- `service tomcat6 start`
		- or `$CATALINA_HOME/bin/startup.sh` if tomcat is not installed as a service
- Run the importer:
	- `$VLO/bin/vlo_solr_importer.sh `
		- optionally pass the path to a custom `VloConfig.xml` using the `-c` option (the default is in `$VLO/config`)

The importer logs information in `$VLO/log`

Because metadata is not static, it is recommended to run the importer a
couple of times a week.

