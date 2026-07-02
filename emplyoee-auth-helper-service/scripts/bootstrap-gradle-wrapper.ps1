# Downloads the Gradle distribution zip and extracts gradle-wrapper.jar into gradle/wrapper
# Run this in PowerShell on your machine from the project root.

$distUrl = 'https://services.gradle.org/distributions/gradle-8.6-bin.zip'
$zipPath = Join-Path $PSScriptRoot 'gradle-8.6-bin.zip'
$destDir = Join-Path $PSScriptRoot '..\gradle\wrapper' | Resolve-Path -Relative
$destDirFull = Join-Path (Get-Location) $destDir

Write-Host "Downloading $distUrl to $zipPath ..."
Invoke-WebRequest -Uri $distUrl -OutFile $zipPath

Write-Host "Extracting gradle-wrapper.jar to $destDirFull ..."
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($zipPath, $PSScriptRoot)

# The wrapper jar is under gradle-8.6/lib/gradle-wrapper.jar inside the zip
$sourceJar = Join-Path $PSScriptRoot 'gradle-8.6\lib\gradle-wrapper.jar'
if (-not (Test-Path $sourceJar)) {
    Write-Error "Expected wrapper jar not found at $sourceJar"
    exit 1
}

if (-not (Test-Path $destDirFull)) { New-Item -ItemType Directory -Path $destDirFull -Force | Out-Null }
Copy-Item -Path $sourceJar -Destination $destDirFull -Force

Write-Host "Cleaning up zip and extracted gradle directories..."
Remove-Item -Recurse -Force (Join-Path $PSScriptRoot 'gradle-8.6')
Remove-Item -Force $zipPath

Write-Host "gradle-wrapper.jar installed to $destDirFull"
Write-Host "You can now run .\gradlew <task> on Windows or ./gradlew on Unix"
