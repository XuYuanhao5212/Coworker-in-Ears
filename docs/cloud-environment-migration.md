# Cloud Environment Migration Checklist

## Scope

This document prepares the repository for the online environment page the user referenced:

[Codex environment settings](https://chatgpt.com/codex/settings/environment/69bab4f144ec8191b5f5b2db7a47b97e/edit)

I did **not** modify that online environment directly here.
This workspace can prepare the configuration, but authenticated UI changes must still be applied in the online environment if direct browser control is unavailable.

## What The Cloud Environment Must Provide

### Required runtime

- Java 21 compatible runtime
- Android SDK root with:
  - `platform-tools`
  - `build-tools;34.0.0`
  - `platforms;android-34`
  - command-line tools new enough to understand current SDK package metadata

### Required environment variables

- `JAVA_HOME`
- `ANDROID_SDK_ROOT`
- `ANDROID_HOME`
- `GRADLE_USER_HOME`

### Recommended values in cloud

Do **not** use the local `F:\...` paths in cloud.
Use cloud-owned paths instead, for example:

```text
JAVA_HOME=/opt/java/openjdk-21
ANDROID_SDK_ROOT=/workspace/android-sdk
ANDROID_HOME=/workspace/android-sdk
GRADLE_USER_HOME=/workspace/.gradle-user
```

## Repository Expectations

The project currently assumes:

- Gradle 8.4 wrapper
- Android Gradle Plugin 8.3.2
- Kotlin 1.9.24
- compileSdk 34
- minSdk 26
- targetSdk 34

## Cloud Bootstrap Order

1. Ensure Java 21 is available.
2. Ensure Android SDK 34 packages are installed.
3. Set the four environment variables above.
4. Restore or create the Gradle cache directory.
5. Run:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
```


## Repo-Local Cloud Assets

The repository now includes cloud-ready helper files that can be pointed to directly from Codex settings:

- `scripts/cloud-setup.sh`
- `scripts/cloud-maintenance.sh`
- `docs/codex-cloud-environment-template.md`

These files keep the cloud migration plan inside the repo and avoid touching the user's local IDE or SDK installation.

## Cache Directories Worth Persisting

- `.gradle-user/caches`
- `.gradle-user/wrapper`

Persisting those two paths will reduce repeated bootstrap time significantly.

## Known Limitation

The standard wrapper uses:

```text
https://services.gradle.org/distributions/gradle-8.4-bin.zip
```

So cloud execution still needs outbound network access at least once, unless the wrapper distribution and Gradle caches are pre-seeded.

## Suggested Online Environment Fields

If the online environment UI supports free-form setup or env vars, configure:

- Working directory: repository root
- Env vars:
  - `JAVA_HOME`
  - `ANDROID_SDK_ROOT`
  - `ANDROID_HOME`
  - `GRADLE_USER_HOME`
- Optional bootstrap commands:
  - `./gradlew testDebugUnitTest`
  - `./gradlew assembleDebug`

## Migration Recommendation

Move work to cloud only after these are true:

- `testDebugUnitTest` is green locally
- `assembleDebug` is green locally
- the cloud environment can provide Android SDK 34
- cache persistence for Gradle is available

Without Android SDK provisioning in cloud, the repository can still support logic and unit-test work, but full APK packaging will remain local-only.

