#!/bin/sh
set -e
if [ "$LANE" = "node" ]; then
    yarn install
    npm run test
else
    npm install -g react-native-cli
    react-native -v

    cd example
    yarn install
    rm -rf node_modules/react-native-billing/example

    cd android && ./gradlew assembleRelease
fi