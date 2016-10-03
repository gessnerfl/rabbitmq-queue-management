#!/bin/bash

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo -e "Assemble Pull Request #$TRAVIS_PULL_REQUEST => Branch [$TRAVIS_BRANCH]"
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_TAG" == "" ]; then
  echo -e 'Assemble Branch with Snapshot => Branch ['$TRAVIS_BRANCH']'
elif [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_TAG" != "" ]; then
  echo -e 'Assemble Branch for Release => Branch ['$TRAVIS_BRANCH']  Tag ['$TRAVIS_TAG']'
else
  echo -e 'WARN: Should not be here => Branch ['$TRAVIS_BRANCH']  Tag ['$TRAVIS_TAG']  Pull Request ['$TRAVIS_PULL_REQUEST']'
fi
./gradlew assemble