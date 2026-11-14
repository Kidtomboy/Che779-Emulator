@echo off
chcp 65001 >nul
rem Build Che779_Remoter.jar (Java/Swing) - can JDK 8+ trong PATH hoac JAVA_HOME
cd /d "%~dp0"
set "JAVAC=javac" & set "JAR=jar"
if defined JAVA_HOME set "JAVAC=%JAVA_HOME%\bin\javac.exe" & set "JAR=%JAVA_HOME%\bin\jar.exe"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > sources.txt
"%JAVAC%" --release 8 -nowarn -encoding UTF-8 -d out @sources.txt || goto :err
(echo Manifest-Version: 1.0& echo Main-Class: com.che779.remoter.Main& echo Implementation-Title: Che779 Remoter (Java)& echo Implementation-Version: 0.0.0.2 linux) > out\manifest.txt
"%JAR%" cfm Che779_Remoter.jar out\manifest.txt -C out com || goto :err
del sources.txt
echo OK: %cd%\Che779_Remoter.jar
goto :eof
:err
echo LOI BUILD
exit /b 1
