= Table of Contents =
- What is the VLO? 
- What should I read? 
- Setting up
	- Preparing for release
- Using the VLO
	- Running an import
	
= What is the VLO? =

Using the VLO faceted browser, you can browse metadata by facet. It consists of three
software components: a Solr server with VLO specific configuration, an importer and a
web application front end.

For more information, see the wiki page:

	https://trac.clarin.eu/wiki/CmdiVirtualLanguageObservatory

= What should I read? =

- README
	This file; a general introduction and development information
- DEPLOY-README
	Instructions on how to deploy a fresh VLO installation
- UPGRADE
	Instructions on how to upgrade an existing VLO installation
- CHANGES
	A list of changes per release
- COPYING
	Licensing information

= Development information =

Some general development notes.

= Setting up =

Follow the instructions in DEPLOY-README to set up a development environment.
You may deploy the Solr instance and the web app from your IDE. Make sure to
set the required Java system property for the Solr data location (solr.data.dir)!

== Preparing for release ==

These instructions apply to any kind of release, whether it's a stable
version, or for beta or alpha deployment. Always increase the version number
and keep the trunk in -SNAPSHOT but not the tags so that a deployed version
can always be traced back to its sources!

* Make a tag of the version to release:

	svn cp . ../tags/vlo-3.x	#executed from trunk

* Change the version number in the poms to match the release
  (should match the directory name and be non-snapshot!!):

	cd ../tags/vlo-3.x
	mvn versions:set -DnewVersion=3.x

  This will update the version numbers of the parent pom and all VLO
  modules in one go!

* Build the tag and inspect the output of vlo-distribution

	mvn clean install 		#do not skip unit tests ;)

  Unpack the tarball in vlo-distribution/target somehwere and check its
  contents on version numbers, config files etc.

  You may also want to do a 'svn diff' to check the change of the version
  numbers.

* Clean up and commit

	mvn versions:commit 		#cleans up POM backups
	mvn clean			#cleans up build outpit
	svn commit -m "Created tag for VLO version 3.x"

* Update the version number of the trunk if the release was a stable

	cd ../../trunk
	mvn version:set -DnewVersion 3.y-SNAPSHOT
	mvn versions:commit
	svn commit -m "Bumped trunk version to 3.y-SNAPSHOT"

* Done!

After building the entire project, a deployment package will be present in the
'target' directory of vlo-distribution. This includes WARs for both the Solr
and the web app front end as well as the importer script and default configuration
files.

= Using the VLO =

After a successful deployment and configuration, you should be able to browse to
the VLO web app and browse the imported records.

If the VLO is empty, you will need to run an import first. 

== Running an import ==

To run an import, go the 'bin' child directory of the VLO application directory 
and run

	./vlo_solr_importer.sh
	
as the appropriate user (e.g. 'vlouser'). 

It's advisable to run this in a detached background process (for example using 
"screen") because an import can take quite a long time depending on the amount
of records to be imported.

Some progress information is logged to a file 'log/vlo-importer.log'. It gets
rotated automatically by the import process.

For a fresh import, even when the VLO is not configured to delete all documents
from the index first, you can simply remove the contents of the Solr data 
directory (check server configuration documentation for the exact location).