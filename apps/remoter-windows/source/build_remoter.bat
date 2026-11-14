@echo off
chcp 65001 >nul
setlocal
rem ------------------------------------------------------------------
rem  Build Che779_Remoter.exe KHONG CAN cai SDK: dung csc.exe co san cua Windows (.NET Framework 4.x)
rem ------------------------------------------------------------------
set CSC=%WINDIR%\Microsoft.NET\Framework64\v4.0.30319\csc.exe
if not exist "%CSC%" set CSC=%WINDIR%\Microsoft.NET\Framework\v4.0.30319\csc.exe
if not exist "%CSC%" (
  echo Khong tim thay csc.exe - hay cai .NET Framework 4.x hoac dung: dotnet build -c Release
  pause & exit /b 1
)
"%CSC%" /nologo /target:winexe /codepage:65001 /optimize+ /out:Che779_Remoter.exe /win32icon:che779.ico ^
  /r:System.dll /r:System.Core.dll /r:System.Drawing.dll /r:System.Windows.Forms.dll *.cs
if errorlevel 1 ( echo BUILD LOI & pause & exit /b 1 )
echo Xong: Che779_Remoter.exe
pause
