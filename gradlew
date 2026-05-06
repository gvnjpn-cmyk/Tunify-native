#!/bin/sh
GRADLE_VERSION="8.4"
GRADLE_DIR="${HOME}/.gradle-dist/gradle-${GRADLE_VERSION}"
GRADLE_ZIP="/tmp/gradle-${GRADLE_VERSION}.zip"
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
if [ ! -f "${GRADLE_DIR}/bin/gradle" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -fsSL "${GRADLE_URL}" -o "${GRADLE_ZIP}" || wget -q "${GRADLE_URL}" -O "${GRADLE_ZIP}"
    mkdir -p "${HOME}/.gradle-dist"
    unzip -q "${GRADLE_ZIP}" -d "${HOME}/.gradle-dist"
fi
exec "${GRADLE_DIR}/bin/gradle" "$@"
