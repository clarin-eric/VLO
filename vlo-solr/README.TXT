The goal of this project is to make it easy to create an installation of a solr/server.
The projects is basically a stripped down version of the solr example download with a maven pom for installation.

Changes from the original solr example:
- The end result of a mvn package is a zip in the target dir with the solr war, solr conf and tomcat context file.
- The solr war differs that it is not using jetty and has log4j.properties added so we can direct the logging. 
  See the log4j.properties in WEB-INF/classes to take a look at the location of the log file. 
- Added a maven pom with some properties to set the solr home and solr log dir in an easy way.
- Adjusted the solr/conf/schema.xml file to be used for our facets.

Installation:
- Check the pom.xml for properties that can be set. 
- run: mvn package 
- unzip: target/vlo_solr-1.4.1-vlo_solr_conf.zip to your favorite location (e.g. production server).
- check the log4j.properties (inside the war) and vlo_solr.xml for proper solr_home/log directories
- copy example tomcat context file: vlo_solr.xml to: <Tomcat-Home>/conf/Catalina/localhost
- Go to e.g: http://localhost:8080/vlo_solr to see if the instance is working properly.

If you see the Welcome to Solr! message you have the solr server up and running and ready to be used.