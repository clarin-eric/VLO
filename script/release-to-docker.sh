#!/usr/bin/env bash

set -e

SRC_BASE_DIR="${HOME}/git"
VLO_SRC_PATH="${SRC_BASE_DIR}/vlo"
DOCKER_PROJECT_PATH="${SRC_BASE_DIR}/docker-vlo"
COMPOSE_PROJECT_PATH="${SRC_BASE_DIR}/compose_vlo"
CI_URL="https://travis-ci.org/clarin-eric/VLO/builds"

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
	press_key_to_continue

	# tag
	echo "Creating and pushing tag..."
	# create & push tag
	git tag -m "VLO ${TARGET_VERSION}" -a "${TARGET_VERSION}"
	git push origin "${TARGET_VERSION}"
	
	echo "Check CI output before continuing! (${CI_URL})"
	press_key_to_continue
	
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
			
	if ask_confirm "Merge into ${CURRENT_BRANCH} branch?"; then
		git checkout "${CURRENT_BRANCH}"
		git merge "${RELEASE_BRANCH}"
		if ask_confirm "Done. Push branch?"; then
			git push origin "${CURRENT_BRANCH}"
		fi
	fi
))

### Release docker
(cd "$DOCKER_PROJECT_PATH" &&
	(pwd)
)

### Update & tag compose
(cd "$COMPOSE_PROJECT_PATH" &&
	(pwd)
)
