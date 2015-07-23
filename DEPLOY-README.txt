VLO DEPLOY-README.txt 

The instructions below describe the installation of the applications necessary
for browsing: the VLO web application, the Solr server, and the meta data
importer.

$VLO 		– installation folder for VLO e.g.: /opt/vlo/
$CATALINA_HOME 	– tomcat’s installation folder
$SOLR_DATA 	- SOLR location for storing indexed data 
xxx             - VLO version number

1. The archive

   The VLO importer and web application are contained in an archive:

        vlo-xxx-Distribution.tar.gz

   Deploying the VLO application means:
   - unpacking the archive in a suitable location
   - unpacking the war files contained in the archive
   - adapt the application's context files and
   - adapt the VloConfig.xml main configuration file and/or the dataroot config
     included in that file
   - optionally executing upgrade steps described in the UPGRADE file
   
2. Archive unpacking

	» service tomcat6 stop (or $CATALINA_HOME/bin/shutdown.sh if tomcat is
          not installed as a service)
	» mkdir $VLO
	» tar -C $VLO --strip-components=1 -zxvf vlo-xxx-Distribution.tar.gz
	
	In the tree starting in $VLO, the configuration of the application is
        stored under subfolder config.

3. Solr server & web-app installation

	» unzip -d $CATALINA_HOME/webapps/vlo  $VLO/war/vlo-web-app-xxx.war
	» unzip -d $CATALINA_HOME/webapps/vlo-solr $VLO/war/vlo-solr-xxx.war 

	Modify (if needed):
		$CATALINA_HOME/webapps/vlo/META-INF/context.xml
                - parameter "eu.carlin.cmdi.vlo.config.location" 
                    should point to VloConfig.xml,
                     e.g. value="$VLO/config/VloConfig.xml"
                - parameter "eu.carlin.cmdi.vlo.solr.serverUrl" 
                    is for custom SOLR server, leave commented for the default
		
		$CATALINA_HOME/webapps/vlo-solr/META-INF/context.xml
                - parameter "solr/home" 
                    should point to the folder with config files for SOLR, 
                    e.g. value="$VLO/config/solr"/>
		
	and copy them to  $CATALINA_HOME/conf/Catalina/localhost/ as vlo.xml and
        vlo-solr.xml respectively. 
		» cp $CATALINA_HOME /webapps/vlo/META-INF/context.xml\
                        $CATALINA_HOME/conf/Catalina/localhost/vlo.xml
		» cp $CATALINA_HOME /webapps/vlo-solr/META-INF/context.xml\
                        $CATALINA_HOME/conf/Catalina/localhost/vlo-solr.xml
		
	Add following line to the $CATALINA_HOME/bin/setenv.sh:
		» echo 'export JAVA_OPTS="$JAVA_OPTS -Dsolr.data.dir=$SOLR_DATA"'\
                        >> $CATALINA_HOME/bin/setenv.sh
		
	Optional:
		
		Modify log4j settings in:
                - $CATALINA_HOME/webapps/vlo/WEB-INF/classes/log4j.properties
                - $CATALINA_HOME/webapps/vlo-solr/WEB-INF/classes/log4j.properties
		
		Wicket runs in development mode by default, to change it add 
                following parameter to the JAVA_OPTS in 
                $CATALINA_HOME/bin/setenv.sh:
			-Dwicket.configuration=deployment
		or set a context parameter 'configuration' to the value 
                'deployment' in the application's context fragment.
		
4. Importer configuration
	
	Modify DataRoot for importer directly in $VLO/config/VloConfig.xml or in
        the file that is included into that file via XInclude

                <originName>MPI self harvest</originName>
                        <rootFile>path to the metada root folder</rootFile>           
                        <prefix>http://m12404423/vlomd/</prefix>
                        <tostrip>/var/www/vlomd/</tostrip>
                        <deleteFirst>false</deleteFirst>
                </DataRoot>
	
	A dataRoot element describes the meta data files. The toStrip part of
	the description is left out of the rootFile part to create a http link
	to the metadata; the links starts with the prefix.
	
	
5. Importing data
	
	importer can be found in $VLO/bin/ folder

	Before starting data import, first start the Tomcat server:

	» service tomcat6 start 

        (or $CATALINA_HOME/bin/startup.sh if tomcat is not installed as a 
        service)
	
	run importer:

	 » $VLO/bin/vlo_solr_importer.sh 
	
        (optionally pass the path to custom VloConfig.xml, default is in 
        $VLO/config)
	
	The importer logs information in  $VLO/log
	
	Because meta data is not static, it is recommended to run the importer a
	couple of times a week. Please note that, for the tar file with metadata
        of 270 MB run approximately takes 2h.
