#!/bin/bash

chmod +x gradlew
VERSION=$(shell git describe --tags --match=v* --always --dirty)

if [ "$TRAVIS_PULL_REQUEST" == "true" ]; then
  echo -e "Build Pull Request #$TRAVIS_PULL_REQUEST => Branch [$TRAVIS_BRANCH]"
  ./gradlew build sonarqube -Dsonar.projectVersion=$VERSION -Dsonar.pullrequest.key=$TRAVIS_PULL_REQUEST -Dsonar.pullrequest.branch=$TRAVIS_PULL_REQUEST_BRANCH -Dsonar.pullrequest.base=$TRAVIS_BRANCH
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_TAG" == "" ] && [ "$TRAVIS_BRANCH" != "master" ] ; then
  echo -e 'Build Feature Branch ['$TRAVIS_BRANCH']'
  ./gradlew build sonarqube -Dsonar.projectVersion=$VERSION -Dsonar.branch.name=$TRAVIS_BRANCH -Dsonar.branch.target=master
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_TAG" == "" ] ; then
  echo -e 'Build Branch ['$TRAVIS_BRANCH']'
  ./gradlew build sonarqube  -Dsonar.projectVersion=$VERSION
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_TAG" != "" ]; then
  echo -e 'Build Tag ['$TRAVIS_TAG']'
  ./gradlew build sonarqube -Dsonar.projectVersion=$VERSION -Dsonar.branch.name=master
else
  echo -e 'WARN: Should not be here => Branch ['$TRAVIS_BRANCH']  Tag ['$TRAVIS_TAG']  Pull Request ['$TRAVIS_PULL_REQUEST']'
  ./gradlew build
fi