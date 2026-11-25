#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${DBR_VERSION:-1.0.0}"
INPUT_DIR="$ROOT/target/package-input-linux"
OUTPUT_DIR="$ROOT/dist/linux"
APP_JAR="$ROOT/target/dbrOffice-1.0-SNAPSHOT.jar"

cd "$ROOT"
bash ./mvnw -B -DskipTests package org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy-dependencies \
  -DincludeScope=runtime "-DoutputDirectory=$INPUT_DIR"
mkdir -p "$INPUT_DIR" "$OUTPUT_DIR"
cp -f "$APP_JAR" "$INPUT_DIR/"

jpackage \
  --type deb \
  --name "DBR Office" \
  --linux-package-name dbr-office \
  --app-version "$VERSION" \
  --vendor "DBR" \
  --description "DBR Office - gestion administrative" \
  --input "$INPUT_DIR" \
  --module-path "$INPUT_DIR" \
  --module com.b2la.dbroffice/com.b2la.dbroffice.HelloApplication \
  --dest "$OUTPUT_DIR" \
  --linux-shortcut \
  --linux-package-deps "libasound2, libfontconfig1, libfreetype6, libglib2.0-0, libgtk-3-0, libx11-6, libxext6, libxi6, libxrender1, libxtst6"
