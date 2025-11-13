# NDK Verification and Installation Guide
# Run this in PowerShell

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Android NDK Checker" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check for Android SDK
$androidSdkPath = "$env:LOCALAPPDATA\Android\Sdk"
Write-Host "[1/3] Checking Android SDK..." -ForegroundColor Yellow

if (Test-Path $androidSdkPath) {
    Write-Host "  Done: Android SDK found at $androidSdkPath" -ForegroundColor Green
} else {
    Write-Host "  Error: Android SDK not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please install Android Studio first:" -ForegroundColor Yellow
    Write-Host "https://developer.android.com/studio" -ForegroundColor Cyan
    exit 1
}

# Check for NDK
Write-Host "[2/3] Checking Android NDK..." -ForegroundColor Yellow
$ndkPath = "$androidSdkPath\ndk"

if (Test-Path $ndkPath) {
    $ndkVersions = Get-ChildItem $ndkPath -Directory
    if ($ndkVersions.Count -gt 0) {
        Write-Host "  Done: NDK found!" -ForegroundColor Green
        Write-Host "  Versions installed:" -ForegroundColor Gray
        foreach ($version in $ndkVersions) {
            Write-Host "    - $($version.Name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  Warning: NDK folder exists but no versions installed" -ForegroundColor Yellow
        $ndkInstalled = $false
    }
} else {
    Write-Host "  Warning: NDK not found" -ForegroundColor Yellow
    $ndkInstalled = $false
}

# Check for CMake
Write-Host "[3/3] Checking CMake..." -ForegroundColor Yellow
$cmakePath = "$androidSdkPath\cmake"

if (Test-Path $cmakePath) {
    Write-Host "  Done: CMake found!" -ForegroundColor Green
} else {
    Write-Host "  Warning: CMake not found" -ForegroundColor Yellow
}

# Final report
Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

if ($ndkInstalled -eq $false) {
    Write-Host "ACTION REQUIRED: Install NDK" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Steps to install NDK:" -ForegroundColor White
    Write-Host "1. Open Android Studio" -ForegroundColor Gray
    Write-Host "2. Tools > SDK Manager" -ForegroundColor Gray
    Write-Host "3. Click 'SDK Tools' tab" -ForegroundColor Gray
    Write-Host "4. Check these items:" -ForegroundColor Gray
    Write-Host "   [ ] NDK (Side by side)" -ForegroundColor Gray
    Write-Host "   [ ] CMake" -ForegroundColor Gray
    Write-Host "5. Click 'Apply' and wait for download" -ForegroundColor Gray
    Write-Host "6. Run this script again to verify" -ForegroundColor Gray
} else {
    Write-Host "All components installed!" -ForegroundColor Green
    Write-Host ""
    Write-Host "You are ready to build!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Open Android Studio" -ForegroundColor White
    Write-Host "2. File > Open > C:\Users\Dell\project" -ForegroundColor White
    Write-Host "3. Let Gradle sync" -ForegroundColor White
    Write-Host "4. Build > Make Project" -ForegroundColor White
    Write-Host "5. Run on device" -ForegroundColor White
}

Write-Host ""
