# VLO development information 

Some general development notes.

## Setting up 

Follow the instructions in DEPLOY-README to set up a development environment.
You may deploy the Solr instance and the web app from your IDE. Make sure to
set the required Java system property for the Solr data location (solr.data.dir)!

## Preparing for release 

These instructions apply to any kind of release, whether it's a stable
version, or for beta or alpha deployment. Always increase the version number
and keep the development in -SNAPSHOT but not the tags so that a deployed version
can always be traced back to its sources easily!

* Make sure to use stable dependencies. In particular, check the CLARIN base style
and VLO-mapping versions as these are often developed in parallel to the VLO.

* Make a release branch

	```
	git checkout development
	git checkbout -b release-vlo-4.a.b
	```

* Change the version number in the poms to match the release
  (should match the branch name and be non-snapshot!)

	```
	mvn versions:set -DnewVersion=4.a.b
	```

  This will update the version numbers of the parent pom and all VLO
  modules in one go! Alpha and beta releases should be named accordingly,
  for example `4.a.b-beta1`.

* Build the tag and inspect the output of vlo-distribution

	```
	mvn clean install 		#do not skip unit tests ;)
	```
	
  Unpack the tarball in vlo-distribution/target somehwere and check its
  contents on version numbers, config files etc.

  You may also want to do a `git diff` to check the change of the version
  numbers.

* Clean up and commit

	```
	mvn versions:commit 		#cleans up POM backups
	mvn clean			#cleans up build output
	git commit -m "Created tag for VLO version 4.a.b"
	```
* Merge into master and tag to finalise the release 

	```
	git checkout master
	git merge release-vlo-4.a.b	#usually no conflicts need to be resolved after this
	git push
	#tag
	git tag -a 4.a.b
	git push --tags
	```
	
* Merge changes into development

	```
	git checkout development
	git merge release-vlo-4.a.b		#maybe some conflicts needs to be resolved after this
	mvn version:set -DnewVersion 4.c-SNAPSHOT
	mvn versions:commit
	git commit
	git push
	```
* Done!

After building the entire project, a deployment package will be present in the
`target` directory of `vlo-distribution`. This includes a WAR for
the web app front end, a prepared configuraiton for Solr, the importer script, and default
configuration files.

Be aware of the following build profiles that pre-configure the deployment packages
for different environments:
- `local-testing` for local development and testing purposes
- `dev-vm` for the development host (alpha-vlo.clarin.eu)
- `beta` for the staging host (beta-vlo.clarin.eu)
- `production` for production (vlo.clarin.eu)

To build using a profile, use e.g. `mvn clean install -Pproduction`. Please do this
when making a deployment package for beta (`beta`) or production (`production`)!

It's good practice to turn your tag into a "release" on GitHub and attach the deployment
package for the target environment (beta, production). Share this link with the 
administrators or, if you want to be friendly, make a pull request for the applicable 
docker project if available - see [CLARIN on GitLab](https://gitlab.com/CLARIN-ERIC). Your
admin can tell you more :)
