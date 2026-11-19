#!/bin/bash
set -e
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}
export ANDROID_HOME=~/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

echo "=== Java ==="
java -version
echo "=== SDK Manager licenses ==="
yes | sdkmanager --licenses || true
echo "=== Installing SDK packages ==="
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "=== Generating Gradle wrapper ==="
cd ~/workspace/meandme-terminal
~/gradle-8.7/bin/gradle wrapper --gradle-version 8.7

echo "=== Building APK ==="
./gradlew assembleDebug --no-daemon

echo "=== APK ==="
ls -lh app/build/outputs/apk/debug/app-debug.apk
mkdir -p ~/workspace/your_files/meandme-terminal
cp app/build/outputs/apk/debug/app-debug.apk ~/workspace/your_files/meandme-terminal/MeandMe-Terminal.apk
ls -lh ~/workspace/your_files/meandme-terminal/MeandMe-Terminal.apk
echo "DONE"
