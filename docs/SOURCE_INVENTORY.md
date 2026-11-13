# Source Inventory and Baseline Integrity

## Input archives

Các SHA-256 này được lấy từ manifest kiểm tra của gói đánh giá độc lập:

| Input | SHA-256 |
|---|---|
| `Che779_Emulator_0.0.0.2_custom.zip` | `a1a8cfe2d7b31bf75961e6aff2d3c63d8960d704e768f1bcd51cc518e1a9eaf3` |
| `Che779_Remoter_0.0.0.2_custom.zip` | `e840403c31649f9580d6202f6531074b8b3b58fea1ae58b07392ace68979c943` |

## Inventory

- Emulator: 314 Java source files.
- Remoter: 15 C# source files.
- Remoter source archive contains 20 files total.
- Emulator source archive contains the Java source tree, build scripts, resources, ASM 3.2 dependency and one supplied JAR.

## Prebuilt JAR

`apps/emulator/prebuilt/Che779_Emulator.jar` is the JAR supplied in the original Emulator archive.

SHA-256: `449cff51259336a693167ce6d8752519273313c7edd8fd0ec0f6fe65d0349be0`

## Build comparison

A fresh build was performed with OpenJDK 21 using the repository's `build.sh` (`javac --release 8`). The generated JAR and supplied baseline JAR contained:

- 565 entries each.
- 0 entries present only in either JAR.
- 0 entry-content differences across all shared entries.

The overall JAR file hashes differ because ZIP/JAR packaging metadata can differ. This comparison confirms that the generated archive's entry contents matched the supplied baseline in this build run; it does not prove runtime behavior on Windows.
