# Build Che779_Remoter.jar (Java/Swing). Can JDK 8+. Chay: powershell -ExecutionPolicy Bypass -File build.ps1
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$javac = 'javac'; $jar = 'jar'
if ($env:JAVA_HOME) { $javac = Join-Path $env:JAVA_HOME 'bin\javac.exe'; $jar = Join-Path $env:JAVA_HOME 'bin\jar.exe' }
if (-not (Get-Command $javac -ErrorAction SilentlyContinue)) { throw 'Khong tim thay javac. Cai JDK 8+ va dat JAVA_HOME hoac PATH.' }
if (Test-Path out) { Remove-Item -Recurse -Force out }
New-Item -ItemType Directory out | Out-Null
$src = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
& $javac --release 8 -nowarn -encoding UTF-8 -d out $src
if ($LASTEXITCODE -ne 0) { throw 'Bien dich that bai.' }
$mf = "Manifest-Version: 1.0`nMain-Class: com.che779.remoter.Main`nImplementation-Title: Che779 Remoter (Java)`nImplementation-Version: 1.2.0`n"
[IO.File]::WriteAllText((Join-Path $PWD 'out\manifest.txt'), $mf)
& $jar cfm Che779_Remoter.jar out\manifest.txt -C out com
if ($LASTEXITCODE -ne 0) { throw 'Dong goi jar that bai.' }
Write-Host "OK: $PWD\Che779_Remoter.jar"
