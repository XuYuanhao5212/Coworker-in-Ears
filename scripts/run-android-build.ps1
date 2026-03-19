param(
    [string]$JavaHome = "F:\JetBrains\Tools\Android Studio\jbr",
    [string]$AndroidSdkRoot = "F:\JetBrains\Tools\Android\Sdk",
    [string[]]$Tasks = @("testDebugUnitTest", "assembleDebug"),
    [switch]$UseWrapper,
    [switch]$Offline
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$gradleUserHome = Join-Path $repoRoot ".gradle-user"
$bundledGradle = Join-Path $repoRoot ".tools\gradle-8.4\bin\gradle.bat"
$wrapper = Join-Path $repoRoot "gradlew.bat"
$localProperties = Join-Path $repoRoot "local.properties"

if (-not (Test-Path (Join-Path $JavaHome "bin\java.exe"))) {
    throw "java.exe was not found under $JavaHome"
}

if (-not (Test-Path $AndroidSdkRoot)) {
    throw "Android SDK root was not found at $AndroidSdkRoot"
}

if (-not (Test-Path $gradleUserHome)) {
    New-Item -ItemType Directory -Force $gradleUserHome | Out-Null
}

$sdkDirValue = $AndroidSdkRoot -replace "\\", "\\"
$sdkDirValue = $sdkDirValue -replace ":", "\:"
Set-Content -Path $localProperties -Value "sdk.dir=$sdkDirValue" -Encoding ASCII

$env:JAVA_HOME = $JavaHome
$env:ANDROID_SDK_ROOT = $AndroidSdkRoot
$env:ANDROID_HOME = $AndroidSdkRoot
$env:GRADLE_USER_HOME = $gradleUserHome

$gradleCommand = if ($UseWrapper) { $wrapper } elseif (Test-Path $bundledGradle) { $bundledGradle } else { $wrapper }
$arguments = @()
if ($Offline) {
    $arguments += "--offline"
}
$arguments += $Tasks

Write-Host ("Gradle command: {0}" -f $gradleCommand) -ForegroundColor Cyan
Write-Host ("Tasks: {0}" -f ($Tasks -join ", ")) -ForegroundColor Cyan
Write-Host ("GRADLE_USER_HOME: {0}" -f $gradleUserHome) -ForegroundColor Cyan

Push-Location $repoRoot
try {
    & $gradleCommand @arguments
}
finally {
    Pop-Location
}
