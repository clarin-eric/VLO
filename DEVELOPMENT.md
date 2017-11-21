# VLO development information 

Some general development notes.

## Setting up 

Follow the instructions in DEPLOY-README to set up a development environment.
You may deploy the *web app* to a Tomcat instance from your IDE. You can run (and
optionally install) *Solr* using the [script in `vlo-solr`](vlo-solr/build-solr.sh). For
instruction on the latter, see the [`vlo-solr` documentation](vlo-solr/README.md).

### Docker based workflow

You can also use the Docker images available for the VLO and Solr in your development
workflow:

- [docker-vlo](https://gitlab.com/CLARIN-ERIC/docker-vlo-beta)
- A Solr image:
  - [hub.docker.com/_/solr](https://hub.docker.com/_/solr/): the official Solr image
  - [CLARIN-ERIC/docker-solr](https://gitlab.com/CLARIN-ERIC/docker-solr): CLARIN's
  adaptation of the Solr image that better works in the CLARIN infrastructure

For development you can choose either of the two Solr images, as long as you configure
it right. See their respective documentation files for details. You can also use the
configurations provided in [compose_vlo](https://gitlab.com/CLARIN-ERIC/compose_vlo).

#### Example

```sh
# Make sure to set these right!
VLO_VERSION="4.3-SNAPSHOT"
SOLR_IMAGE_VERSION="1.0.0-beta3"
VLO_IMAGE_VERSION="1.3.0-beta2"
VLO_GIT_CHECKOUT="${HOME}/git/VLO"

# Start the Solr server
docker run --rm -d --name vlo_dev_solr \
	-v ${VLO_GIT_CHECKOUT}/vlo-solr/solr-home:/docker-entrypoint-initsolr.d/solr_home:ro \
	-v ${HOME}/vlo-dev-solr-data:/solr-data \
	-e SOLR_DATA_HOME=/solr-data \
	-p 8983:8983 \
	registry.gitlab.com/clarin-eric/docker-solr:${SOLR_IMAGE_VERSION}

# Start the web app
docker run --rm -d --name vlo_dev_web \
	--link vlo_dev_solr \
	-e VLO_DOCKER_SOLR_URL=http://vlo_dev_solr:8983/solr/vlo-index/ \
	-v ${VLO_GIT_CHECKOUT}/vlo-web-app/target/vlo-web-app-${VLO_VERSION}:/opt/vlo/war/vlo \
	-p 8080:8080 \
	registry.gitlab.com/clarin-eric/docker-vlo-beta:${VLO_IMAGE_VERSION}
```

See the [docker-solr registry](https://gitlab.com/CLARIN-ERIC/docker-solr/container_registry)
and [docker-vlo registry](https://gitlab.com/CLARIN-ERIC/docker-vlo-beta/container_registry)
to find out what versions of the images are available.
Make sure that `VLO_VERSION` matches the version of the VLO checked out in your
`VLO_GIT_CHECKOUT` directory.

The mounts and environment variables in `vlo_dev_solr` ensure that the right Solr
configuration is provisioned (from the VLO source tree) and that the Solr data is
persisted (in the user's home directory in this example). Note that you can also mount
the `solr-home` directory directly onto a location to serve as `SOLR_HOME` (e.g.
`-v ${VLO_GIT_CHECKOUT}/vlo-solr/solr-home:/my-solr-home -e SOLR_HOME=/my-solr-home`)
but only do this if you want to capture any changes made by Solr to apply them to the
configuration in the sources. See the 
[docker-solr](https://gitlab.com/CLARIN-ERIC/docker-solr) project for more information.

The mount in `vlo_dev_web` causes an override of the web application directory by the
build output of the `vlo-web-app` project, which means that the effects of a change to the
web app can be seen inside the container by simply doing a (partial) rebuild of the 
project.

After this you will should able to visit `http://localhost:8080` (VLO web app) and 
`http://localhost:8983/solr` (Solr's administration interface).

The first time you start the Solr container it would not contain data. You can simply
run an importer against `http://localhost:8983/solr/vlo-index` with some local data or
copy some existing sample data (TODO: link to sample data project) into the Solr data
directory on your host.

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
- `docker` for use in the [docker-vlo](https://gitlab.com/CLARIN-ERIC/docker-vlo-beta) project
- `dev-vm` for the development host (alpha-vlo.clarin.eu)
- `production` for production (vlo.clarin.eu)

To build using a profile, use e.g. `mvn clean install -Pproduction`. Please do this
when making a deployment package for beta (`beta`) or production (`production`)!

It's good practice to turn your tag into a "release" on GitHub and attach the deployment
package for the target environment (docker, production). Share this link with the 
administrators or, if you want to be friendly, make a pull request for the applicable 
docker project if available - see [CLARIN on GitLab](https://gitlab.com/CLARIN-ERIC). Your
admin can tell you more :)
