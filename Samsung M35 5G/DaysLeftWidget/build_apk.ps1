# Quick Build Script for DaysLeftWidget APK

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Building Days Left Anti-Procrastination Widget" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Check for Android SDK
if (-not $env:ANDROID_HOME -and -not $env:ANDROID_SDK_ROOT) {
    $commonSdk = "$env:LOCALAPPDATA\Android\Sdk"
    if (Test-Path $commonSdk) {
        $env:ANDROID_HOME = $commonSdk
        Write-Host "Found Android SDK at: $commonSdk" -ForegroundColor Green
    }
}

Write-Host "Opening project in Android Studio or running build..." -ForegroundColor Yellow
Write-Host "You can also open this folder in Android Studio and press 'Run' or 'Build APK'." -ForegroundColor White
