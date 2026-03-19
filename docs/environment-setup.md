# Environment Setup

## Current Build Status

The repository already has successful local build evidence:

- Unit tests passed and the HTML report exists at `app/build/reports/tests/testDebugUnitTest/index.html`.
- A debug APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

That means the project is not blocked on Kotlin or Android compilation right now.

## What Actually Happened During The Earlier Timeout

The earlier long-running `testDebugUnitTest assembleDebug` task did not fail because of app code.
The real instability came from environment orchestration:

1. The standard Gradle wrapper points to `https://services.gradle.org/distributions/gradle-8.4-bin.zip`.
2. In restricted or sandboxed runs, wrapper bootstrap may fail before Gradle even starts because it cannot fetch the distribution.
3. During first-time escalated runs, Gradle also had to warm caches and install missing Android SDK components for API 34.
4. By the time the long command timed out at the orchestration layer, useful outputs had already been produced.

So the timeout was primarily an environment/bootstrap issue, not a code regression.

## Local Tool Paths

These are the user-provided local tool locations this repository expects by default:

- Android Studio JBR: `F:\JetBrains\Tools\Android Studio\jbr`
- Android SDK: `F:\JetBrains\Tools\Android\Sdk`

The repository does not write to those directories.
All repository-owned writable state is kept inside the repo:

- Gradle caches: `.gradle-user/`
- Optional downloaded Gradle distribution: `.tools/`
- Local SDK pointer file: `local.properties`

## Recommended Local Commands

Use the repo entrypoints instead of relying on IDE state:

```powershell
.\scripts\env-doctor.cmd
.\scripts\run-android-build.cmd
```

If you prefer PowerShell directly, the `.cmd` wrappers already run the matching `.ps1` files with `-ExecutionPolicy Bypass`.

If your caches are already warm and you want to avoid network access:

```powershell
.\scripts\run-android-build.cmd -Offline
```

If you want to force the standard wrapper path:

```powershell
.\scripts\run-android-build.cmd -UseWrapper
```

## Why The Repository Keeps Both Wrapper And Bundled Local Fallback

- `gradlew` is the standard, portable entry point for CI and cloud environments.
- `.tools/gradle-8.4/` is a repo-local fallback for local or sandboxed runs where wrapper bootstrap may be blocked by network policy.

The wrapper remains the canonical project configuration.
The local bundled Gradle path exists only to make local execution more reliable.
