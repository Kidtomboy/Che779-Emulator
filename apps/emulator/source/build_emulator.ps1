# Build Che779_Emulator.jar (PowerShell). Can JDK 8+. Chay: powershell -ExecutionPolicy Bypass -File build_emulator.ps1
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$javac = 'javac'; $jar = 'jar'
if ($env:JAVA_HOME) { $javac = Join-Path $env:JAVA_HOME 'bin\javac.exe'; $jar = Join-Path $env:JAVA_HOME 'bin\jar.exe' }
if (-not (Get-Command $javac -ErrorAction SilentlyContinue)) { throw 'Khong tim thay javac. Cai JDK 8+ va dat JAVA_HOME hoac PATH.' }
foreach ($d in 'out','stubout','jarroot','asmx') { if (Test-Path $d) { Remove-Item -Recurse -Force $d } }
New-Item -ItemType Directory out,stubout,jarroot,asmx | Out-Null
$stubs = Get-ChildItem -Recurse stubs -Filter *.java | ForEach-Object { $_.FullName }
& $javac --release 8 -nowarn -d stubout $stubs; if ($LASTEXITCODE -ne 0) { throw 'Loi bien dich stubs.' }
$src = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
& $javac --release 8 -nowarn -proc:none -encoding UTF-8 -d out -cp "lib\asm-3.2.jar;stubout" $src; if ($LASTEXITCODE -ne 0) { throw 'Loi bien dich ma nguon.' }
Copy-Item -Recurse -Force out\* jarroot\
Copy-Item -Recurse -Force res\* jarroot\
Push-Location asmx; & $jar xf ..\lib\asm-3.2.jar; Pop-Location
Remove-Item -Recurse -Force asmx\META-INF -ErrorAction SilentlyContinue
Copy-Item -Recurse -Force asmx\* jarroot\
Remove-Item -Recurse -Force asmx
$mf = "Manifest-Version: 1.0`nMain-Class: com.che779.emu.Che779Main`nClass-Path: lib/jna.jar`nImplementation-Title: Che779 Emulator`nImplementation-Version: 1.2.0`nCreated-By: Che779 Team`n"
[IO.File]::WriteAllText((Join-Path $PWD 'manifest.txt'), $mf)
& $jar cfm Che779_Emulator.jar manifest.txt -C jarroot .; if ($LASTEXITCODE -ne 0) { throw 'Dong goi jar that bai.' }
Write-Host "OK: $PWD\Che779_Emulator.jar"
