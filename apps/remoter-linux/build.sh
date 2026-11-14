#!/usr/bin/env bash
# Build Che779_Remoter.jar (Java/Swing, chay duoc tren Linux/Windows/macOS). Can JDK 8+.
set -euo pipefail
cd "$(dirname "$0")"
command -v javac >/dev/null 2>&1 || { echo "[LOI] Khong thay javac. Cai JDK (Debian/Ubuntu/Mint: sudo apt install default-jdk)." >&2; exit 1; }
command -v jar >/dev/null 2>&1 || { echo "[LOI] Khong thay jar. Cai JDK day du." >&2; exit 1; }
rm -rf out; mkdir out
javac --release 8 -nowarn -encoding UTF-8 -d out $(find src -name '*.java') 2>&1 | grep -v '^Picked up' || true
[ -f out/com/che779/remoter/Main.class ] || { echo "[LOI] Bien dich that bai." >&2; exit 1; }
printf 'Manifest-Version: 1.0\nMain-Class: com.che779.remoter.Main\nImplementation-Title: Che779 Remoter (Java)\nImplementation-Version: 0.0.0.2 linux\n' > out/manifest.txt
jar cfm Che779_Remoter.jar out/manifest.txt -C out com
echo "OK: $(pwd)/Che779_Remoter.jar"
