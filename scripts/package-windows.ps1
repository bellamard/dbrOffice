$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$Version = if ($env:DBR_VERSION) { $env:DBR_VERSION } else { "1.0.0" }
$InputDir = Join-Path $Root "target/package-input-windows"
$OutputDir = Join-Path $Root "dist/windows"
$AppJar = Join-Path $Root "target/dbrOffice-1.0-SNAPSHOT.jar"

Push-Location $Root
try {
    .\mvnw.cmd -B -DskipTests package org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy-dependencies `
        -DincludeScope=runtime "-DoutputDirectory=$InputDir"
    if ($LASTEXITCODE -ne 0) { throw "La préparation Maven a échoué." }

    New-Item -ItemType Directory -Force -Path $InputDir, $OutputDir | Out-Null
    Copy-Item -Force $AppJar $InputDir

    jpackage `
        --type exe `
        --name "DBR Office" `
        --app-version $Version `
        --vendor "DBR" `
        --description "DBR Office - gestion administrative" `
        --input $InputDir `
        --module-path $InputDir `
        --module "com.b2la.dbroffice/com.b2la.dbroffice.HelloApplication" `
        --dest $OutputDir `
        --win-menu `
        --win-shortcut `
        --win-dir-chooser `
        --win-per-user-install
    if ($LASTEXITCODE -ne 0) { throw "La création de l'installateur Windows a échoué." }
} finally {
    Pop-Location
}
