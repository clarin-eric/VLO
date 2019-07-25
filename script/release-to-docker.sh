#!/usr/bin/env bash

set -e

SRC_BASE_DIR="${HOME}/git"
VLO_SRC_PATH="${SRC_BASE_DIR}/vlo"
DOCKER_PROJECT_PATH="${SRC_BASE_DIR}/docker-vlo"
COMPOSE_PROJECT_PATH="${SRC_BASE_DIR}/compose_vlo"

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
	echo "Checking current version..."
	CURRENT_VERSION=$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout)
	echo "Current version: ${CURRENT_VERSION}"
	
	# ask version
	echo -n "Version to release? "
	read TARGET_VERSION
	
	# set version
	echo "Setting version to ${TARGET_VERSION} in pom files..."
	mvn -q versions:set -DnewVersion=${TARGET_VERSION} versions:commit
	git --no-pager diff
	
	ask_confirm_abort "Continue to commit & push?"
		
	# commit
	echo "Committing..."
	# push
	echo "Pushing..."
	
	echo "Check build output before continuing!"
	press_key_to_continue

	# tag
	echo "Creating and pushing tag..."
	# push tag
	# check if expected file exists (curl)
))

### Release docker
(cd "$DOCKER_PROJECT_PATH" &&
	(pwd)
)

### Update & tag compose
(cd "$COMPOSE_PROJECT_PATH" &&
	(pwd)
)

