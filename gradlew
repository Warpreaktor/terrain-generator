#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR="$SCRIPT_DIR"

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

GRADLE_VERSION="8.14.3"
GRADLE_HOME="$PROJECT_DIR/.gradle/wrapper/dists/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"
GRADLE_ZIP="$PROJECT_DIR/.gradle/wrapper/dists/gradle-$GRADLE_VERSION-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

mkdir -p "$PROJECT_DIR/.gradle/wrapper/dists"

if [ ! -x "$GRADLE_BIN" ]; then
  if [ ! -f "$GRADLE_ZIP" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    if command -v curl >/dev/null 2>&1; then
      curl -L "$GRADLE_URL" -o "$GRADLE_ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget "$GRADLE_URL" -O "$GRADLE_ZIP"
    else
      echo "Neither curl nor wget was found. Install Gradle manually or install one of these tools." >&2
      exit 1
    fi
  fi

  echo "Unpacking Gradle $GRADLE_VERSION..."
  rm -rf "$GRADLE_HOME"
  unzip -q "$GRADLE_ZIP" -d "$PROJECT_DIR/.gradle/wrapper/dists"
fi

exec "$GRADLE_BIN" "$@"
