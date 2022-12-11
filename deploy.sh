#!/bin/bash

####################################################################################################
# A script to build and deploy the release build of the APK file to the connected emulator or device
# First ensure that ~/Library/Android/sdk/platform-tools is in your PATH environment variable
####################################################################################################

cd "$(dirname "${BASH_SOURCE[0]}")"

#
# Point to the Android Java location
#
JAVA_HOME='/Applications/Android Studio.app/Contents/jre/Contents/Home'
APP_PACKAGE_ID='com.authsamples.basicmobileapp'

#
# Do a clean
#
./gradlew clean
if [ $? -ne 0 ]; then
  echo 'Problem encountered cleaning the Android build system'
  exit
fi

#
# Do a release build
#
./gradlew assembleRelease
if [ $? -ne 0 ]; then
  echo 'Problem encountered building the Android app'
  exit
fi

#
# Uninstall if required
#
FOUND=$(adb shell pm list packages | grep $APP_PACKAGE_ID)
if [ "$FOUND" != '' ]; then

    adb uninstall $APP_PACKAGE_ID
    if [ $? -ne 0 ]; then
        echo 'Problem encountered uninstalling the existing Android app'
        exit
    fi
fi

#
# Deploy to the connected device
#
APK_FILE='./app/build/outputs/apk/release/app-release.apk'
adb install $APK_FILE
if [ $? -ne 0 ]; then
  echo 'Problem encountered deploying the Android app to the device'
  exit
fi

#
# Then run the app
#
adb shell am start -n "$APP_PACKAGE_ID/$APP_PACKAGE_ID.app.MainActivity"
if [ $? -ne 0 ]; then
  echo 'Problem encountered deploying the Android app to the device'
  exit
fi