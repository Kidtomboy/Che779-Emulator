# Build Che779_Remoter.exe (C#, Windows) bang csc.exe co san cua .NET Framework 4.x, hoac dotnet neu co.
# Chay: powershell -ExecutionPolicy Bypass -File build_remoter.ps1
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework64\v4.0.30319\csc.exe'
if (-not (Test-Path $csc)) { $csc = Join-Path $env:WINDIR 'Microsoft.NET\Framework\v4.0.30319\csc.exe' }
if (-not (Test-Path $csc)) { throw 'Khong tim thay csc.exe. Cai .NET Framework 4.x hoac dung: dotnet build Che779Remoter.csproj -c Release' }
$files = Get-ChildItem *.cs | ForEach-Object { $_.Name }
& $csc /nologo /target:winexe /codepage:65001 /optimize+ /out:Che779_Remoter.exe /win32icon:che779.ico `
  /r:System.dll /r:System.Core.dll /r:System.Drawing.dll /r:System.Windows.Forms.dll $files
if ($LASTEXITCODE -ne 0) { throw 'BUILD LOI' }
Write-Host 'Xong: Che779_Remoter.exe'
