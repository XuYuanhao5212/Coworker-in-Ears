# Codex Cloud Environment Template

Use this as the fill-in guide for the Codex settings page.

## Suggested Environment Name

`coworker-in-ears-android-cloud`

## Recommended Description

`Android build and test environment for Coworker in Ears, optimized for Gradle cache resume and APK validation.`

## Working Directory

The checked-out repository root in Codex cloud (the same workspace that contains this repo).

## Setup Script

`scripts/cloud-setup.sh`

## Maintenance Script

`scripts/cloud-maintenance.sh`

## Environment Variables

Set these values in the environment settings page:

```text
JAVA_HOME=/opt/java/openjdk-21
ANDROID_SDK_ROOT=/workspace/android-sdk
ANDROID_HOME=/workspace/android-sdk
GRADLE_USER_HOME=/workspace/.gradle-user
```

If the UI supports path updates, it is also useful to add:

```text
PATH=/opt/java/openjdk-21/bin:/workspace/android-sdk/platform-tools:/workspace/android-sdk/cmdline-tools/latest/bin:$PATH
```

## Internet Access Strategy

Recommended setup:

* Setup phase: internet enabled
* Agent phase: internet off by default

If the environment UI uses presets, start with `Common dependencies`. If Android SDK package downloads are not covered by the preset, add these domains:

* `services.gradle.org`
* `dl.google.com`
* `maven.google.com`
* `repo.maven.apache.org`

Keep agent internet as limited as possible. Only widen it if the task truly needs external downloads during the agent phase.

## Why This Setup

* The project uses Gradle wrapper bootstrap, so setup needs outbound access at least once.
* Android build tooling is the main cloud risk, so the setup script checks `java`, `sdkmanager`, `platform-tools`, `build-tools;34.0.0`, and `platforms;android-34`.
* Gradle cache persistence should focus on `.gradle-user/caches` and `.gradle-user/wrapper`.
* Setup exports are not enough by themselves in Codex, so the environment page should hold the real env vars.

## What The Setup Script Assumes

* A Java 21 runtime is already available in the container image or installed through the environment.
* Android command-line tools are available if the setup script is expected to install SDK packages.
* The cloud environment can write inside the workspace, but not to host tool directories.

## Risk Notes

* If `sdkmanager` is missing, the cloud environment cannot self-install the Android SDK packages and should be treated as misconfigured.
* If the environment cannot provide `platforms;android-34` and `build-tools;34.0.0`, APK packaging may remain local-only.
* If the environment page resets cache settings, the next task will rerun setup from scratch.
