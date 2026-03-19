#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

java_home="${JAVA_HOME:-/opt/java/openjdk-21}"
android_sdk_root="${ANDROID_SDK_ROOT:-/workspace/android-sdk}"
gradle_user_home="${GRADLE_USER_HOME:-$repo_root/.gradle-user}"

export JAVA_HOME="$java_home"
export ANDROID_SDK_ROOT="$android_sdk_root"
export ANDROID_HOME="${ANDROID_HOME:-$android_sdk_root}"
export GRADLE_USER_HOME="$gradle_user_home"

log() {
  printf '[cloud-maintenance] %s\n' "$1"
}

check_path() {
  local label="$1"
  local path="$2"
  if [[ -e "$path" ]]; then
    log "$label: present"
  else
    log "$label: missing"
  fi
}

log "repo_root=$repo_root"
check_path "java" "$JAVA_HOME/bin/java"
check_path "gradlew" "$repo_root/gradlew"
check_path "android-sdk" "$ANDROID_SDK_ROOT"
check_path "gradle-user-home" "$GRADLE_USER_HOME"
check_path "platform-tools" "$ANDROID_SDK_ROOT/platform-tools"
check_path "platforms;android-34" "$ANDROID_SDK_ROOT/platforms/android-34"
check_path "build-tools;34.0.0" "$ANDROID_SDK_ROOT/build-tools/34.0.0"

log "maintenance is intentionally lightweight; it only validates cache-resume readiness"
