#!/usr/bin/env python3
"""Static checks for the Che779 Project 1 repository. Uses Python stdlib only."""
from __future__ import annotations
import argparse
import pathlib
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
REQUIRED = [
    "README.md", "CONTRIBUTING.md", "SECURITY.md", "LICENSE", "CHANGELOG.md", "VERSION",
    "docs/README.md", "docs/AI_HANDOFF.md", "docs/ARCHITECTURE.md", "docs/BUILD_AND_RUN.md",
    "docs/DEBUG_LOG.md", "docs/DECISIONS.md", "docs/IPC_PROTOCOL.md", "docs/PROJECT_STATE.md",
    "docs/ROADMAP.md", "docs/SOURCE_INVENTORY.md", "docs/TESTING.md", "docs/VARIANTS_AND_CLONES.md",
    "apps/emulator/source/build.sh",
    "apps/emulator/source/src/com/che779/emu/Che779Main.java",
    "apps/emulator/source/src/com/che779/emu/IpcServer.java",
    "apps/remoter-windows/source/Che779Remoter.csproj",
    "apps/remoter-windows/source/Program.cs",
    "apps/remoter-windows/source/IpcClient.cs",
    "apps/remoter-linux/build.sh",
    "apps/remoter-linux/src/com/che779/remoter/Main.java",
    ".github/workflows/ci.yml", ".github/workflows/release.yml",
]
ACTIONS = [
    "keypress", "keydown", "keyup", "text", "clean_ram", "sleep",
    "hide", "show", "mouse", "proxy", "proxy_get", "ping",
    "status", "screenshot", "version", "quit", "close",
]

def fail(message: str) -> None:
    print(f"FAIL: {message}")
    raise SystemExit(1)

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", help="Optional built Emulator JAR to inspect")
    args = parser.parse_args()

    missing = [p for p in REQUIRED if not (ROOT / p).is_file()]
    if missing:
        fail("missing required files: " + ", ".join(missing))

    server = (ROOT / "apps/emulator/source/src/com/che779/emu/IpcServer.java").read_text(encoding="utf-8")
    for action in ACTIONS:
        if f'"{action}"' not in server:
            fail(f"IPC server no longer contains expected action: {action}")

    if not (ROOT / "apps/emulator/prebuilt/Che779_Emulator.jar").is_file():
        fail("baseline prebuilt JAR missing")

    if args.jar:
        jar_path = pathlib.Path(args.jar)
        if not jar_path.is_absolute():
            jar_path = (pathlib.Path.cwd() / jar_path).resolve()
        if not jar_path.is_file():
            fail(f"JAR not found: {jar_path}")
        try:
            with zipfile.ZipFile(jar_path) as jar:
                names = set(jar.namelist())
                if "com/che779/emu/Che779Main.class" not in names:
                    fail("JAR missing Emulator entry point class")
                manifest = jar.read("META-INF/MANIFEST.MF").decode("utf-8", "replace")
                if "Main-Class: com.che779.emu.Che779Main" not in manifest:
                    fail("JAR manifest has unexpected Main-Class")
                ver = (ROOT / "VERSION").read_text().strip()
                if f"Implementation-Version: {ver}" not in manifest:
                    fail(f"JAR manifest version differs from VERSION ({ver})")
        except zipfile.BadZipFile:
            fail("provided JAR is not a valid ZIP/JAR")

    print("PASS: Project 1 repository structure and IPC action inventory")
    if args.jar:
        print("PASS: Emulator JAR entry point and manifest")
    return 0

if __name__ == "__main__":
    sys.exit(main())
