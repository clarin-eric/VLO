#!/usr/bin/env bash

set -e

SRC_BASE_DIR="${HOME}/git"
VLO_SRC_PATH="${SRC_BASE_DIR}/vlo"
DOCKER_PROJECT_PATH="${SRC_BASE_DIR}/docker-vlo"
COMPOSE_PROJECT_PATH="${SRC_BASE_DIR}/compose_vlo/compose_vlo"
CI_URL="https://travis-ci.org/clarin-eric/VLO/builds"
DOCKER_CI_URL="https://gitlab.com/CLARIN-ERIC/docker-vlo-beta/pipelines"

VLO_NEW_VERSION=""
NEW_DOCKER_VERSION=""

ask_confirm() {
	YN_ANSWER=""
	while ! ( [ "$YN_ANSWER" = 'y' ] ||  [ "$YN_ANSWER" = 'n' ] ); do
		echo -n "$1 [y/n]?"
		read YN_ANSWER
	done
	
	if [ "$YN_ANSWER" = 'y' ]; then
		return 0
	else
		return 1
	fi
}

ask_confirm_abort() {
	if ! ask_confirm "$1 (n to abort)"; then
		exit 1
	fi
}

press_key_to_continue() {
	echo -n "Press any key to continue..."
	read -n 1
}

#### Compile & release source

(cd "$VLO_SRC_PATH" && ( 

	# show current version in pom
	echo "Checking current branch and version info..."
	CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
	CURRENT_VERSION=$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)
	echo "Current branch: ${CURRENT_BRANCH}"
	echo "Current version: ${CURRENT_VERSION}"
	
	# ask version
	echo -n "Version to release? "
	read TARGET_VERSION

	echo "Switching to release branch"
	RELEASE_BRANCH="release-${TARGET_VERSION}"
	git checkout -b "${RELEASE_BRANCH}"
	
	# set version
	echo "Setting version to ${TARGET_VERSION} in pom files..."
	mvn -q versions:set -DnewVersion=${TARGET_VERSION} versions:commit
	git --no-pager diff
	
	ask_confirm_abort "Continue to commit & push?"
		
	# commit
	echo "Committing..."
	git commit -m "Project version to ${TARGET_VERSION}" pom.xml */pom.xml
	# push
	echo "Pushing..."
	git push -u origin "${RELEASE_BRANCH}"
	
	echo "Check CI output before continuing! (${CI_URL})"
	ask_confirm_abort "Continue?"

	# tag
	echo "Creating and pushing tag..."
	# create & push tag
	git tag -m "VLO ${TARGET_VERSION}" -a "${TARGET_VERSION}"
	git push origin "${TARGET_VERSION}"
	
	echo "Check CI output before continuing! (${CI_URL})"
	ask_confirm_abort "Continue?"
	
	# check if expected file exists (curl)
	
	SUCCESS=-1
	RETRY=0
	while ( [ ${SUCCESS} -ne 0 ] && [ ${RETRY} -eq 0 ] ); do
		REMOTE_RELEASE_URL="https://github.com/clarin-eric/VLO/releases/download/${TARGET_VERSION}/vlo-${TARGET_VERSION}-docker.tar.gz"
		echo "Checking for distribution package at github.com"
		RESPONSE_CODE=$(curl -IL ${REMOTE_RELEASE_URL} -o /dev/null -w '%{http_code}\n' -s)
		if ( [ "${RESPONSE_CODE}" = "200" ] || [ "${RESPONSE_CODE}" = "403" ] ); then
			SUCCESS=0
		else
			SUCCESS=-1
			if ask_confirm "Failed to retrieve from ${REMOTE_RELEASE_URL} (response code ${RESPONSE_CODE}) Retry? "; then
				RETRY=0
			else
				RETRY=1
			fi
		fi
	done
			
	if ask_confirm "Merge into '${CURRENT_BRANCH}' branch?"; then
		git checkout "${CURRENT_BRANCH}"
		git merge "${RELEASE_BRANCH}"
		if ask_confirm "Done. Push branch '${CURRENT_BRANCH}'?"; then
			git push origin "${CURRENT_BRANCH}"
		fi
		if ask_confirm "Delete branch '${RELEASE_BRANCH}'?"; then
			git branch -d "${RELEASE_BRANCH}"
			git push origin ":${RELEASE_BRANCH}"
		fi
	fi
	
	VLO_NEW_VERSION="${TARGET_VERSION}"
))

### Release docker
(cd "$DOCKER_PROJECT_PATH" && (
	DOCKER_CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
	echo "Updating docker project. Current branch: ${DOCKER_CURRENT_BRANCH}"
	
	if [ "${VLO_NEW_VERSION}" = "" ]; then
		echo "New VLO version not set. Something has gone wrong, aborting!"
		exit 1
	fi

	echo "Existing tags for VLO ${VLO_NEW_VERSION}:"
	git --no-pager tag --list 'vlo-'${VLO_NEW_VERSION}'*'


	DOCKER_TARGET_VERSION_DEFAULT="vlo-${VLO_NEW_VERSION}-1"
	echo -n "Docker image version to release? [${DOCKER_TARGET_VERSION_DEFAULT}]"
	read DOCKER_TARGET_VERSION
	if [ "${DOCKER_TARGET_VERSION}" = "" ]; then
		DOCKER_TARGET_VERSION="${DOCKER_TARGET_VERSION_DEFAULT}"
	fi
	#TODO: check if a tag already exists - loop if necessary

	DOCKER_RELEASE_BRANCH="release-${DOCKER_TARGET_VERSION}"
	git checkout -b "${DOCKER_RELEASE_BRANCH}"

	# change VLO version for build process
	DATA_ENV_FILE="copy_data.env.sh"
	echo "Setting new version in ${DATA_ENV_FILE}"
	sed -i -e 's/VLO_VERSION=\".*\"/VLO_VERSION=\"'${VLO_NEW_VERSION}'\"/' "${DATA_ENV_FILE}"
	
	# user confirmation for changes...
	git --no-pager diff "${DATA_ENV_FILE}"
	ask_confirm_abort "Continue to commit and push?"
	git commit -m "VLO version to ${VLO_NEW_VERSION}" "${DATA_ENV_FILE}"
	git push origin "${DOCKER_RELEASE_BRANCH}"
	
	echo "Check CI output before continuing! (${DOCKER_CI_URL})"
	ask_confirm_abort "Continue to tag?"
	
	# tag and push
	git tag -a -m "VLO image ${DOCKER_TARGET_VERSION}" -a "${DOCKER_TARGET_VERSION}"
	git push origin "${DOCKER_TARGET_VERSION}"
	
	# merge if user wants to
	if ask_confirm "Merge into '${DOCKER_CURRENT_BRANCH}' branch?"; then
		git checkout "${DOCKER_CURRENT_BRANCH}"
		git merge "${DOCKER_RELEASE_BRANCH}"
		if ask_confirm "Done. Push branch '${DOCKER_CURRENT_BRANCH}'?"; then
			git push origin "${DOCKER_CURRENT_BRANCH}"
		fi
		if ask_confirm "Delete branch '${DOCKER_RELEASE_BRANCH}'?"; then
			git branch -d "${DOCKER_RELEASE_BRANCH}"
			git push origin ":${DOCKER_RELEASE_BRANCH}"
		fi
	fi
	
	NEW_DOCKER_VERSION="${DOCKER_TARGET_VERSION}"
))

### Update & tag compose
(cd "$COMPOSE_PROJECT_PATH" && (

	if [ "${NEW_DOCKER_VERSION}" = "" ]; then
		echo "New VLO version not set. Something has gone wrong, aborting!"
		exit 1
	fi

	COMPOSE_CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
	echo "Updating compose project. Current branch: ${COMPOSE_CURRENT_BRANCH}"

	echo "Existing tags for VLO ${VLO_NEW_VERSION}:"
	git --no-pager tag --list 'vlo-'${VLO_NEW_VERSION}'*'

	COMPOSE_TARGET_VERSION_DEFAULT="vlo-${VLO_NEW_VERSION}-1"
	echo -n "Docker image version to release? [${COMPOSE_TARGET_VERSION_DEFAULT}]"
	read COMPOSE_TARGET_VERSION
	if [ "${COMPOSE_TARGET_VERSION}" = "" ]; then
		COMPOSE_TARGET_VERSION="${COMPOSE_TARGET_VERSION_DEFAULT}"
	fi
	#TODO: check if a tag already exists - loop if necessary

	COMPOSE_RELEASE_BRANCH="release-${COMPOSE_TARGET_VERSION}"
	git checkout -b "${COMPOSE_RELEASE_BRANCH}"
	
	# change VLO image version for build process
	COMPOSE_FILE="clarin/docker-compose.yml"
	echo "Setting new image version in ${COMPOSE_FILE}"
	#image: &vlo_web_image registry.gitlab.com/clarin-eric/docker-vlo-beta:vlo-4.7.1-alpha3d-1
	sed -e 's/\(.*docker-vlo-beta:\).*/\1'${NEW_DOCKER_VERSION}'/' "${COMPOSE_FILE}"

	# user confirmation for changes...
	git --no-pager diff "${COMPOSE_FILE}"
	ask_confirm_abort "Continue to commit and push?"
	git commit -m "VLO image version to ${NEW_DOCKER_VERSION}" "${COMPOSE_FILE}"
	git push origin "${COMPOSE_RELEASE_BRANCH}"

	ask_confirm_abort "Continue to tag?"
	
	# tag and push
	git tag -a -m "VLO compose project ${COMPOSE_TARGET_VERSION}" -a "${COMPOSE_TARGET_VERSION}"
	git push origin "${COMPOSE_TARGET_VERSION}"
	
	# merge if user wants to
	if ask_confirm "Merge into '${COMPOSE_CURRENT_BRANCH}' branch?"; then
		git checkout "${COMPOSE_CURRENT_BRANCH}"
		git merge "${COMPOSE_RELEASE_BRANCH}"
		if ask_confirm "Done. Push branch '${COMPOSE_CURRENT_BRANCH}'?"; then
			git push origin "${COMPOSE_CURRENT_BRANCH}"
		fi
		if ask_confirm "Delete branch '${COMPOSE_RELEASE_BRANCH}'?"; then
			git branch -d "${COMPOSE_RELEASE_BRANCH}"
			git push origin ":${COMPOSE_RELEASE_BRANCH}"
		fi
	fi
))
