#!/bin/bash

set -e

APP_NAME="PRM_Advisory"
MAIN_CLASS="Main"
VERSION="1.0"
DIST_DIR="dist"
TEMPLATE_DIR="templates"
JAR_NAME="$APP_NAME.jar"
FAT_JAR="$APP_NAME-fat.jar"
RUNTIME_DIR="$DIST_DIR/runtime"
DMG_NAME="$APP_NAME-$VERSION.dmg"

JAVA_HOME=$(/usr/libexec/java_home)
JAVAFX_SDK="/Users/Dell/Desktop/javafx-sdk-24.0.2"
JAVAFX_LIB="$JAVAFX_SDK/lib"

echo "🧹 Cleaning old builds..."
rm -rf $DIST_DIR
mkdir -p $DIST_DIR/$TEMPLATE_DIR

echo "🔨 Compiling Java files..."
javac \
  --module-path "$JAVAFX_LIB" \
  --add-modules javafx.controls,javafx.fxml \
  -cp "lib/*" \
  -d out \
  src/*.java

echo "📦 Creating FAT JAR with dependencies..."
jar --create --file=$DIST_DIR/$FAT_JAR \
    --main-class=$MAIN_CLASS \
    -C out . \
    -C lib .

echo "⚙️ Creating custom Java runtime image with jlink..."
jlink \
  --module-path "$JAVA_HOME/jmods:$JAVAFX_LIB" \
  --add-modules java.base,java.desktop,java.logging,java.xml,javafx.controls,javafx.fxml \
  --output $RUNTIME_DIR \
  --strip-debug \
  --compress=2 \
  --no-header-files \
  --no-man-pages

echo "🧩 Copying JavaFX native .dylib files into runtime..."
cp $JAVAFX_LIB/libprism_*.dylib $RUNTIME_DIR/lib/
cp $JAVAFX_LIB/libjavafx_*.dylib $RUNTIME_DIR/lib/

echo "📂 Copying field mapping file into templates/"
cp amc_field_mapping.json $DIST_DIR/$TEMPLATE_DIR/

echo "📦 Creating .dmg with jpackage..."
jpackage \
  --type dmg \
  --name "$APP_NAME" \
  --app-version "$VERSION" \
  --input "$DIST_DIR" \
  --main-jar "$FAT_JAR" \
  --main-class "$MAIN_CLASS" \
  --java-options "-Dfile.encoding=UTF-8" \
  --runtime-image "$RUNTIME_DIR" \
  --icon "icon.icns" \
  --dest "$DIST_DIR"

echo "✅ Done! Check dist/ for .dmg and mount to test the app"
