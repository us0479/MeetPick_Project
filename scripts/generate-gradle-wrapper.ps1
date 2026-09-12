$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$backendPath = Join-Path $projectRoot "backend"
$wrapperJar = Join-Path $backendPath "gradle\wrapper\gradle-wrapper.jar"
$wrapperProperties = Join-Path $backendPath "gradle\wrapper\gradle-wrapper.properties"

$gradleVersion = "9.7.1"
$distributionSha256 = "acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a"
$wrapperJarSha256 = "7a9ce74cff467ca1bf60a4fcd9f05185acceda4d0f382434d393e17864262c5d"

Write-Host "[MeetPick] Gradle Wrapper $gradleVersion 생성"
Write-Host "Backend: $backendPath"

docker version | Out-Null

docker run --rm `
  -v "${backendPath}:/home/gradle/project" `
  -w /home/gradle/project `
  "gradle:${gradleVersion}-jdk21-ubi9" `
  gradle wrapper --gradle-version $gradleVersion --distribution-type bin

if (-not (Test-Path (Join-Path $backendPath "gradlew.bat"))) {
    throw "Gradle Wrapper 생성 실패: gradlew.bat 없음"
}
if (-not (Test-Path $wrapperJar)) {
    throw "Gradle Wrapper 생성 실패: gradle-wrapper.jar 없음"
}

$actualJarSha = (Get-FileHash $wrapperJar -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualJarSha -ne $wrapperJarSha256) {
    throw "Gradle Wrapper JAR checksum 불일치: $actualJarSha"
}

$content = Get-Content $wrapperProperties
$content = $content | Where-Object { $_ -notmatch '^distributionSha256Sum=' }
$content += "distributionSha256Sum=$distributionSha256"
[System.IO.File]::WriteAllLines($wrapperProperties, $content, [System.Text.UTF8Encoding]::new($false))

Write-Host "[OK] Gradle Wrapper 생성 및 checksum 검증 완료"
