#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../apps/emulator/source" && pwd)"
bash "$(dirname "$0")/../.github/scripts/build-emulator.sh"
cd "$ROOT"
mkdir -p test-out
javac --release 8 -encoding UTF-8 -cp "out:stubout:lib/asm-3.2.jar" -d test-out test/com/che779/emu/IpcServerJsonTest.java
java -cp "test-out:out:stubout:lib/asm-3.2.jar" com.che779.emu.IpcServerJsonTest
