$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $projectRoot

Write-Host "=== MeetPick Backbone Verify ==="

function Require-Command([string]$name) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        throw "필수 명령어를 찾을 수 없습니다: $name"
    }
}

function Import-DotEnv([string]$path) {
    if (-not (Test-Path $path)) {
        return
    }

    foreach ($line in Get-Content $path) {
        $trimmed = $line.Trim()
        if ($trimmed -eq "" -or $trimmed.StartsWith("#")) {
            continue
        }

        $parts = $trimmed.Split("=", 2)
        if ($parts.Count -ne 2) {
            continue
        }

        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

function Wait-HttpOk([string]$uri, [int]$timeoutSeconds = 60) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    do {
        try {
            $response = Invoke-RestMethod -Uri $uri -Method Get -TimeoutSec 3
            if ($response.status -eq "UP") {
                return $response
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    } while ((Get-Date) -lt $deadline)

    throw "Health check timeout: $uri"
}

function Stop-ProcessTree($process) {
    if ($null -eq $process) {
        return
    }

    try {
        if (-not $process.HasExited) {
            & taskkill.exe /PID $process.Id /T /F 2>$null | Out-Null
        }
    } catch {
        # 검증 본체 성공/실패를 cleanup 오류가 가리지 않게 한다.
    }
}

Require-Command "docker"
Require-Command "java"
Require-Command "node"
Require-Command "npm"

$javaVersion = & java -version 2>&1 | Select-Object -First 1
if ($javaVersion -notmatch '\b21(?:\.|\b)') {
    throw "Java 21이 필요합니다. 현재: $javaVersion"
}

$nodeRaw = (node --version).TrimStart('v')
$nodeVersion = [version]$nodeRaw
if ($nodeVersion.Major -ne 22 -or $nodeVersion -lt [version]'22.12.0') {
    throw "Node.js 22.12+ (22.x)가 필요합니다. 현재: v$nodeRaw"
}

Write-Host "[INFO] $javaVersion"
Write-Host "[INFO] Node v$nodeRaw / npm $(npm --version)"

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "[INFO] .env 생성"
}
Import-DotEnv ".env"

if (-not (Test-Path "frontend/.env.local")) {
    Copy-Item "frontend/.env.example" "frontend/.env.local"
    Write-Host "[INFO] frontend/.env.local 생성"
}

Write-Host "[0/7] Docker Compose 설정 검증"
docker version | Out-Null
docker compose version | Out-Null
docker compose config --quiet

if (-not (Test-Path "backend/gradlew.bat")) {
    & "$PSScriptRoot/generate-gradle-wrapper.ps1"
}

Write-Host "[1/7] MySQL / Redis 시작"
docker compose up -d --wait

Write-Host "[2/7] Backend clean test"
Push-Location backend
try {
    .\gradlew.bat clean test
    .\gradlew.bat bootJar
} finally {
    Pop-Location
}

Write-Host "[3/7] Frontend install / build"
Push-Location frontend
try {
    if (Test-Path "package-lock.json") {
        npm ci
    } else {
        npm install
    }
    npm run build
} finally {
    Pop-Location
}

Write-Host "[4/7] 실제 local profile Backend health 검증"
$backendProcess = $null
$viteProcess = $null
try {
    $bootJar = Get-ChildItem "backend/build/libs/*.jar" |
        Where-Object { $_.Name -notlike "*-plain.jar" } |
        Select-Object -First 1

    if ($null -eq $bootJar) {
        throw "실행 가능한 Spring Boot JAR를 찾을 수 없습니다."
    }

    $javaCommand = (Get-Command java).Source
    $quotedBootJar = '"' + $bootJar.FullName + '"'
    $backendProcess = Start-Process `
        -FilePath $javaCommand `
        -ArgumentList @("-jar", $quotedBootJar, "--spring.profiles.active=local") `
        -WorkingDirectory $projectRoot `
        -PassThru

    $health = Wait-HttpOk "http://127.0.0.1:8080/actuator/health" 90
    Write-Host "[OK] Backend health: $($health.status)"

    Write-Host "[5/7] Vite dev proxy → Backend health 검증"
    $npmCmd = Get-Command "npm.cmd" -ErrorAction SilentlyContinue
    $npmCommand = if ($null -ne $npmCmd) { $npmCmd.Source } else { (Get-Command npm).Source }
    $viteProcess = Start-Process `
        -FilePath $npmCommand `
        -ArgumentList @("run", "dev", "--", "--host", "127.0.0.1") `
        -WorkingDirectory (Join-Path $projectRoot "frontend") `
        -PassThru

    $proxyHealth = Wait-HttpOk "http://127.0.0.1:5173/actuator/health" 60
    Write-Host "[OK] Vite proxy health: $($proxyHealth.status)"
} finally {
    Stop-ProcessTree $viteProcess
    Stop-ProcessTree $backendProcess
}

Write-Host "[6/7] Docker 상태"
docker compose ps

Write-Host "[7/7] 재현성 파일 확인"
if (-not (Test-Path "backend/gradlew")) {
    throw "backend/gradlew가 없습니다."
}
if (-not (Test-Path "backend/gradlew.bat")) {
    throw "backend/gradlew.bat가 없습니다."
}
if (-not (Test-Path "backend/gradle/wrapper/gradle-wrapper.jar")) {
    throw "Gradle Wrapper JAR가 없습니다."
}
if (-not (Test-Path "frontend/package-lock.json")) {
    throw "frontend/package-lock.json 생성에 실패했습니다."
}

Write-Host "=== BACKBONE VERIFY SUCCESS ==="
Write-Host "[NEXT] Gradle Wrapper 전체와 frontend/package-lock.json을 Git에 커밋하세요."
