# OpenCV Android SDK Installation Script
# Run this in PowerShell

Write-Host "================================" -ForegroundColor Cyan
Write-Host "OpenCV Android SDK Installer" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Create directory
$opencvDir = "C:\opencv"
Write-Host "[1/5] Creating directory: $opencvDir" -ForegroundColor Yellow
if (!(Test-Path $opencvDir)) {
    New-Item -ItemType Directory -Path $opencvDir -Force | Out-Null
    Write-Host "  Done: Directory created" -ForegroundColor Green
} else {
    Write-Host "  Done: Directory already exists" -ForegroundColor Green
}

# Step 2: Download OpenCV
$downloadUrl = "https://github.com/opencv/opencv/releases/download/4.8.0/opencv-4.8.0-android-sdk.zip"
$zipFile = "$env:TEMP\opencv-android-sdk.zip"

Write-Host "[2/5] Downloading OpenCV 4.8.0 (300MB)..." -ForegroundColor Yellow
Write-Host "  URL: $downloadUrl" -ForegroundColor Gray
Write-Host "  This may take 5-10 minutes..." -ForegroundColor Gray

if (Test-Path $zipFile) {
    Write-Host "  Done: File already downloaded" -ForegroundColor Green
} else {
    try {
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipFile -UseBasicParsing
        $ProgressPreference = 'Continue'
        Write-Host "  Done: Download complete!" -ForegroundColor Green
    } catch {
        Write-Host "  Error: Download failed" -ForegroundColor Red
        Write-Host "  Please download manually from: https://opencv.org/releases/" -ForegroundColor Yellow
        exit 1
    }
}

# Step 3: Extract
Write-Host "[3/5] Extracting files..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $zipFile -DestinationPath $opencvDir -Force
    Write-Host "  Done: Extraction complete!" -ForegroundColor Green
} catch {
    Write-Host "  Error: Extraction failed" -ForegroundColor Red
    exit 1
}

# Step 4: Verify installation
Write-Host "[4/5] Verifying installation..." -ForegroundColor Yellow
$configFile = "$opencvDir\OpenCV-android-sdk\sdk\native\jni\OpenCVConfig.cmake"
if (Test-Path $configFile) {
    Write-Host "  Done: OpenCV installed correctly!" -ForegroundColor Green
} else {
    Write-Host "  Error: Verification failed" -ForegroundColor Red
    exit 1
}

# Step 5: Configure project
Write-Host "[5/5] Configuring project..." -ForegroundColor Yellow
$opencvPath = "C:/opencv/OpenCV-android-sdk/sdk/native/jni"

$appBuildGradle = "C:\Users\Dell\project\app\build.gradle"
$content = Get-Content $appBuildGradle -Raw

if ($content -notmatch "DOpenCV_DIR") {
    $newContent = $content -replace "(externalNativeBuild \{[\s\S]*?cmake \{[\s\S]*?cppFlags)", "`$1`n                arguments '-DOpenCV_DIR=$opencvPath'"
    Set-Content -Path $appBuildGradle -Value $newContent
    Write-Host "  Done: Project configured!" -ForegroundColor Green
} else {
    Write-Host "  Done: Already configured" -ForegroundColor Green
}

# Cleanup
Write-Host ""
Write-Host "Cleaning up..." -ForegroundColor Yellow
Remove-Item $zipFile -Force -ErrorAction SilentlyContinue
Write-Host "  Done!" -ForegroundColor Green

# Success
Write-Host ""
Write-Host "================================" -ForegroundColor Green
Write-Host "INSTALLATION SUCCESSFUL!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""
Write-Host "OpenCV Path: $opencvPath" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Open Android Studio" -ForegroundColor White
Write-Host "2. Open project: C:\Users\Dell\project" -ForegroundColor White
Write-Host "3. Sync Gradle" -ForegroundColor White
Write-Host "4. Build Project" -ForegroundColor White
Write-Host ""
