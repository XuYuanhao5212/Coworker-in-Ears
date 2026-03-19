param(
    [string]$JavaHome = "F:\JetBrains\Tools\Android Studio\jbr",
    [string]$AndroidSdkRoot = "F:\JetBrains\Tools\Android\Sdk"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$gradleUserHome = Join-Path $repoRoot ".gradle-user"
$bundledGradle = Join-Path $repoRoot ".tools\gradle-8.4\bin\gradle.bat"
$wrapper = Join-Path $repoRoot "gradlew.bat"
$localProperties = Join-Path $repoRoot "local.properties"
$apkPath = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
$testReport = Join-Path $repoRoot "app\build\reports\tests\testDebugUnitTest\index.html"

function Show-Status($label, $value) {
    "{0,-24} {1}" -f $label, $value
}

Write-Host "Coworker in Ears environment doctor" -ForegroundColor Cyan
Write-Host ("Repository: {0}" -f $repoRoot)
Write-Host ""

Show-Status "JAVA_HOME" $JavaHome
Show-Status "ANDROID_SDK_ROOT" $AndroidSdkRoot
Show-Status "GRADLE_USER_HOME" $gradleUserHome
Show-Status "Bundled Gradle" $bundledGradle
Show-Status "Gradle Wrapper" $wrapper
Show-Status "local.properties" $localProperties
Show-Status "Debug APK" $apkPath
Show-Status "Unit Test Report" $testReport
Write-Host ""

$checks = @(
    @{ Name = "JDK exists"; Value = (Test-Path $JavaHome) },
    @{ Name = "java.exe exists"; Value = (Test-Path (Join-Path $JavaHome "bin\java.exe")) },
    @{ Name = "SDK exists"; Value = (Test-Path $AndroidSdkRoot) },
    @{ Name = "platform-tools exists"; Value = (Test-Path (Join-Path $AndroidSdkRoot "platform-tools")) },
    @{ Name = "Bundled Gradle exists"; Value = (Test-Path $bundledGradle) },
    @{ Name = "Gradle wrapper exists"; Value = (Test-Path $wrapper) },
    @{ Name = "local.properties exists"; Value = (Test-Path $localProperties) },
    @{ Name = "Debug APK exists"; Value = (Test-Path $apkPath) },
    @{ Name = "Unit test report exists"; Value = (Test-Path $testReport) }
)

foreach ($check in $checks) {
    $status = if ($check.Value) { "OK" } else { "MISSING" }
    $color = if ($check.Value) { "Green" } else { "Yellow" }
    Write-Host ("[{0}] {1}" -f $status, $check.Name) -ForegroundColor $color
}
