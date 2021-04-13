#!/usr/bin/env bash
##########################################################################################
# Script to create a docker image and deploy that image to a host via ssh.
#
# To use, set an environment variable 'VLO_DOCKER_DIR' to the location of your
# local checkout of <https://gitlab.com/CLARIN-ERIC/docker-vlo-beta/>.
#
# This script does not build the VLO. Make sure to build with the 'docker' profile before
# calling.
#
# You can override the following defaults by setting the environment variables:
# - DATA_ENV_FILE: name of environment variables definitions file for build (place in
# docker directory) - defaults to 'copy_data_dev.env.sh'
#
# Author: Twan Goosen <twan@clarin.eu>
##########################################################################################
set -e
DATA_ENV_FILE="${DATA_ENV_FILE:-copy_data_dev.env.sh}"

if [ -z "${VLO_DOCKER_DIR}" ]; then
	echo "ERROR: VLO_DOCKER_DIR environment variable not set.

To fix this, type e.g.:

	export VLO_DOCKER_DIR=~/git/docker-vlo
	
and try again" > /dev/stderr
	exit 1
fi

(
	cd "${VLO_DOCKER_DIR}"

	TEMP_BUILD_OUT="/tmp/vlo-build.out"
	IMAGE_NAME="docker-vlo"

	export DATA_ENV_FILE
	bash build.sh --local --build | tee "${TEMP_BUILD_OUT}"
)
