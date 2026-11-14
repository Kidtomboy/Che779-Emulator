#!/bin/bash
# Build Che779_Emulator.jar tu source (can JDK 8+; khong can Maven)
set -e
cd "$(dirname "$0")"
rm -rf out stubout jarroot; mkdir out stubout jarroot
javac --release 8 -nowarn -d stubout $(find stubs -name '*.java')
javac --release 8 -nowarn -proc:none -encoding UTF-8 -d out -cp lib/asm-3.2.jar:stubout $(find src -name '*.java')
cp -r out/. jarroot/; cp -r res/. jarroot/
rm -rf asmx; mkdir asmx; (cd asmx && jar xf ../lib/asm-3.2.jar); rm -rf asmx/META-INF; cp -r asmx/. jarroot/; rm -rf asmx   # dung jar cua JDK, khong can unzip
printf 'Manifest-Version: 1.0\nMain-Class: com.che779.emu.Che779Main\nClass-Path: lib/jna.jar\nImplementation-Title: Che779 Emulator\nImplementation-Version: 0.0.0.2 custom\nCreated-By: Che779 Team\n' > manifest.txt
jar cfm Che779_Emulator.jar manifest.txt -C jarroot .
echo "OK: $(pwd)/Che779_Emulator.jar"
