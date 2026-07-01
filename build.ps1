$ErrorActionPreference = "Stop"

$legacyBuildDirectories = @(
    "out",
    "out8",
    "out-rebuild",
    "target-check"
)

Get-ChildItem -Path $PSScriptRoot -Directory -Force | Where-Object {
    $legacyBuildDirectories -contains $_.Name -or $_.Name -like "out-check*"
} | ForEach-Object {
    Remove-Item -LiteralPath $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
}

$buildRoot = Join-Path ([System.IO.Path]::GetTempPath()) "MoonLightBuild"
$buildId = [System.Guid]::NewGuid().ToString("N")
$output = Join-Path $buildRoot $buildId
$tempJar = Join-Path $buildRoot ("foxshelf-toolbox-" + $buildId + ".jar")
New-Item -ItemType Directory -Force -Path $output | Out-Null

try {
    $sources = Get-ChildItem -Path (Join-Path $PSScriptRoot "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
    if (-not $sources) {
        throw "No Java sources found."
    }

    javac -encoding UTF-8 -d $output $sources
    if ($LASTEXITCODE -ne 0) {
        throw "javac failed with exit code $LASTEXITCODE."
    }

    $manifest = Join-Path $output "manifest.txt"
    "Main-Class: toolbox.Main`r`n" | Set-Content -Path $manifest -Encoding ASCII

    $jarPath = Join-Path $PSScriptRoot "foxshelf-toolbox.jar"
    if (Test-Path $tempJar) {
        Remove-Item -LiteralPath $tempJar -Force -ErrorAction SilentlyContinue
    }

    jar cfm $tempJar $manifest -C $output .
    if ($LASTEXITCODE -ne 0) {
        throw "jar failed with exit code $LASTEXITCODE."
    }

    if (-not (Test-Path $tempJar)) {
        throw "jar output was not created."
    }

    if (Test-Path $jarPath) {
        try {
            Remove-Item -LiteralPath $jarPath -Force
        } catch {
            throw "Cannot replace foxshelf-toolbox.jar because it is in use. Close the running application and rebuild."
        }
    }
    try {
        Move-Item -LiteralPath $tempJar -Destination $jarPath -Force
    } catch {
        throw "Cannot move rebuilt jar into place. Close the running application and rebuild."
    }

    $jarInfo = Get-Item $jarPath
    Write-Output ("Built jar: " + $jarInfo.FullName)
    Write-Output ("Jar size: " + $jarInfo.Length + " bytes")
    Write-Output ("Jar time: " + $jarInfo.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss"))
}
finally {
    if (Test-Path $output) {
        Remove-Item -LiteralPath $output -Recurse -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $tempJar) {
        Remove-Item -LiteralPath $tempJar -Force -ErrorAction SilentlyContinue
    }
}
