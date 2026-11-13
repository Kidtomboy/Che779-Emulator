@echo off
chcp 65001 >nul
rem Build Che779_Emulator.jar (can JDK 8+ trong PATH)
cd /d "%~dp0"
if exist out rmdir /s /q out
if exist stubout rmdir /s /q stubout
if exist jarroot rmdir /s /q jarroot
mkdir out & mkdir stubout & mkdir jarroot
dir /s /b stubs\*.java > stubs.txt
javac --release 8 -nowarn -d stubout @stubs.txt || goto :err
dir /s /b src\*.java > sources.txt
javac --release 8 -nowarn -proc:none -encoding UTF-8 -d out -cp lib\asm-3.2.jar;stubout @sources.txt || goto :err
xcopy /s /e /y /q out\* jarroot\ >nul
xcopy /s /e /y /q res\* jarroot\ >nul
cd jarroot & jar xf ..\lib\asm-3.2.jar & rmdir /s /q META-INF & cd ..
(echo Manifest-Version: 1.0& echo Implementation-Title: Che779 Emulator& echo Implementation-Version: 0.0.0.2 custom& echo Main-Class: com.che779.emu.Che779Main& echo Class-Path: lib/jna.jar) > manifest.txt
jar cfm Che779_Emulator.jar manifest.txt -C jarroot .
echo OK: %cd%\Che779_Emulator.jar
goto :eof
:err
echo LOI BUILD
exit /b 1
