#!/bin/bash

# Have script stop if there is an error
set -e

REPO=broadinstitute
PROJECT=gatk-protected
REPO_PRJ=${REPO}/${PROJECT}

#################################################
# Parsing arguments
#################################################
while getopts "e:psl" option; do
	case "$option" in
		e) GITHUB_TAG="$OPTARG" ;;
		p) IS_PUSH=true ;;
		s) IS_HASH=true ;;
		l) IS_NOT_LATEST=true ;;
	esac
done

if [ -z "$GITHUB_TAG" ]; then
	printf "Option -e requires an argument.\n \
Usage: %s: -e <GITHUB_TAG> [-psl] \n \
where <GITHUB_TAG> is the github tag (or hash when -s is used) to use in building the docker image\n \
(e.g. bash build_docker.sh -e 1.0.0.0-alpha1.2.1)\n \
Optional arguments:  \n \
-p \t (DEV) push image to docker hub once complete.  This will use the GITHUB_TAG in dockerhub as well. \n \
\t\t Unless -l is specified, this will also push this image to the 'latest' tag. \n \
-s \t The GITHUB_TAG (-e parameter) is actually a github hash, not tag.  git hashes cannot be pushed as latest, so -l is implied.  \n \
-l \t Do not also push the image to the 'latest' tag. \n " $0
	exit 1
fi

# Make sure sudo or root was used.
if [ "$(whoami)" != "root" ]; then
	echo "You must have superuser privileges (through sudo or root user) to run this script"
	exit 1
fi

# Login to dockerhub
if [ IS_PUSH ]; then
	docker login
fi

# Build
echo "Building image to tag ${REPO_PRJ}:${GITHUB_TAG}..."
if [ IS_HASH ]; then
	docker build -t ${REPO_PRJ}:${GITHUB_TAG} --build-arg GITHUB_TAG=${GITHUB_TAG} --build-arg GITHUB_DIR=\  .
else
	docker build -t ${REPO_PRJ}:${GITHUB_TAG} --build-arg GITHUB_TAG=${GITHUB_TAG} .
fi


# Run unit tests
# TODO: Run unit tests

## Push
if [ IS_PUSH ]; then

	docker push ${REPO_PRJ}:${GITHUB_TAG}

	if [ ! IS_NOT_LATEST ] && [ ! IS_HASH ] ; then
		docker push ${REPO_PRJ}:latest
	fi

	if [ ! IS_NOT_LATEST ] && [ IS_HASH ] ; then
		echo " Will not push a hash as latest to dockerhub. "
	fi

else
	echo "Not pushing to dockerhub"
fi