#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

java_home="${JAVA_HOME:-/opt/java/openjdk-21}"
android_sdk_root="${ANDROID_SDK_ROOT:-/workspace/android-sdk}"
android_home="${ANDROID_HOME:-$android_sdk_root}"
gradle_user_home="${GRADLE_USER_HOME:-$repo_root/.gradle-user}"

export JAVA_HOME="$java_home"
export ANDROID_SDK_ROOT="$android_sdk_root"
export ANDROID_HOME="$android_home"
export GRADLE_USER_HOME="$gradle_user_home"

fail() {
  printf 'ERROR: %s\n' "$1" >&2
  exit 1
}

log() {
  printf '[cloud-setup] %s\n' "$1"
}

require_file() {
  local path="$1"
  local message="$2"
  [[ -e "$path" ]] || fail "$message"
}

find_sdkmanager() {
  local candidate
  for candidate in \
    "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" \
    "$ANDROID_SDK_ROOT/cmdline-tools/bin/sdkmanager" \
    "$ANDROID_SDK_ROOT/tools/bin/sdkmanager" \
    "$(command -v sdkmanager 2>/dev/null || true)"; do
    if [[ -n "$candidate" && -x "$candidate" ]]; then
      printf '%s\n' "$candidate"
      return 0
    fi
  done
  return 1
}

ensure_android_package() {
  local path="$1"
  local package="$2"
  local sdkmanager="$3"

  if [[ -e "$path" ]]; then
    log "$package already present"
    return 0
  fi

  if [[ -z "$sdkmanager" ]]; then
    fail "Missing $package at $path and sdkmanager is unavailable. The cloud environment must either preinstall the Android SDK 34 packages or provide Android command-line tools so setup can install them."
  fi

  log "installing $package"
  yes | "$sdkmanager" --licenses >/dev/null
  "$sdkmanager" --sdk_root="$ANDROID_SDK_ROOT" --install "$package"

  [[ -e "$path" ]] || fail "$package was not installed at $path"
}

log "repo_root=$repo_root"
log "JAVA_HOME=$JAVA_HOME"
log "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
log "GRADLE_USER_HOME=$GRADLE_USER_HOME"

require_file "$JAVA_HOME/bin/java" "Java runtime not found at $JAVA_HOME. Configure a Java 21 runtime in the environment settings."
require_file "$repo_root/gradlew" "Gradle wrapper is missing. The repository must include ./gradlew for cloud bootstrap."

if [[ ! -d "$ANDROID_SDK_ROOT" ]]; then
  fail "Android SDK root not found at $ANDROID_SDK_ROOT. The cloud environment needs a preprovisioned Android SDK or a setup image that can install one."
fi

mkdir -p "$GRADLE_USER_HOME"

sdkmanager=""
if sdkmanager_path="$(find_sdkmanager)"; then
  sdkmanager="$sdkmanager_path"
fi

log "sdkmanager=${sdkmanager:-missing}"

ensure_android_package "$ANDROID_SDK_ROOT/platform-tools/adb" "platform-tools" "$sdkmanager"
ensure_android_package "$ANDROID_SDK_ROOT/platforms/android-34" "platforms;android-34" "$sdkmanager"
ensure_android_package "$ANDROID_SDK_ROOT/build-tools/34.0.0" "build-tools;34.0.0" "$sdkmanager"

log "verifying Java and Gradle wrapper"
"$JAVA_HOME/bin/java" -version
"$repo_root/gradlew" --version

log "cloud setup complete"
