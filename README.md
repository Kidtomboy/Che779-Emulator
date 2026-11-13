# Che779 Project 1 — Emulator V1 + Remoter

**Phiên bản ứng dụng nguồn:** `0.0.0.2 custom`  
**Phiên bản đóng gói repository:** `P1-restructure-2026.11.12` (không thay đổi version ứng dụng)

Project 1 là một bộ công cụ Windows/Java gồm hai ứng dụng phối hợp chặt chẽ:

1. **Che779 Emulator V1** — giả lập J2ME dựa trên MicroEmulator; mỗi phiên game chạy trong một tiến trình JVM riêng và cung cấp IPC server trên loopback.
2. **Che779 Remoter** — ứng dụng Windows Forms quản lý các tiến trình Emulator, gửi lệnh IPC, quản lý tab/cửa sổ, proxy và đồng bộ chuột.

Đây là **một repository duy nhất** vì hai ứng dụng tạo thành một sản phẩm thống nhất. Source và quy trình build vẫn được tách riêng để dễ debug và kiểm soát.

## Kiến trúc

```text
Che779 Project 1
├── Che779 Remoter (Windows Forms)
│   ├── quản lý nhiều tiến trình Emulator
│   ├── IPC client ───────────────┐
│   └── đồng bộ chuột Windows    │ TCP 127.0.0.1 / JSON Lines
│                                ▼
└── Che779 Emulator V1 (Java) ─ IPC server
    └── mỗi game = một JVM / cửa sổ riêng
```

Giao thức chung được mô tả tại [`docs/IPC_PROTOCOL.md`](docs/IPC_PROTOCOL.md). Không sửa một phía của IPC mà bỏ qua phía còn lại.

## Bắt đầu nhanh

### Yêu cầu

- Windows để chạy Remoter.
- JDK 9 trở lên để chạy build script hiện tại (script dùng `--release 8`, bytecode mục tiêu Java 8).
- .NET Framework 4.8 Developer Pack / MSBuild hoặc `csc.exe` .NET Framework để build Remoter.
- JNA là tùy chọn cho một số chức năng Clean RAM; xem [`docs/BUILD_AND_RUN.md`](docs/BUILD_AND_RUN.md).

### Build Emulator

Linux/macOS/Git Bash:

```bash
cd apps/emulator/source
./build.sh
```

Windows:

```bat
cd apps\emulator\source
build_emulator.bat
```

JAR được tạo tại `apps/emulator/source/Che779_Emulator.jar`.

### Build Remoter

Windows, dùng .NET SDK/MSBuild:

```powershell
dotnet build apps/remoter/source/Che779Remoter.csproj -c Release
```

Hoặc chạy `build_remoter.bat` trong `apps/remoter/source` nếu máy có `csc.exe` tương thích.

### Chạy cùng nhau

1. Build Emulator và Remoter.
2. Đặt `Che779_Emulator.jar` cạnh `Che779_Remoter.exe` trong thư mục chạy, hoặc chọn đường dẫn JAR trong Remoter.
3. Chọn Java (`javaw.exe`/`java.exe`) và file game JAR trong Remoter.
4. Mở phiên game từ Remoter.

Remoter có chức năng tự dò `Che779_Emulator.jar` cạnh file EXE. Cách đóng gói release chính thức sẽ giữ hai file cạnh nhau để giảm thao tác cấu hình.

## Cấu trúc repository

| Đường dẫn | Vai trò |
|---|---|
| `apps/emulator/source/` | Source Java, tài nguyên và build scripts của Emulator |
| `apps/emulator/prebuilt/` | JAR baseline có trong gói đầu vào; không phải output build mới |
| `apps/remoter/source/` | Source C#, project file và build script của Remoter |
| `docs/` | Tài liệu kỹ thuật và bộ nhớ dự án có thể bàn giao |
| `tests/` | Kiểm tra cấu trúc, manifest và các điều kiện tĩnh |
| `scripts/` | Script đóng gói hai ứng dụng thành một bộ chạy |
| `.github/workflows/` | CI build từng app và đóng gói artifact tích hợp |

## Tài liệu quan trọng

- [Trạng thái dự án](docs/PROJECT_STATE.md)
- [AI handoff / bộ nhớ bàn giao](docs/AI_HANDOFF.md)
- [Kiểm kê source và hash baseline](docs/SOURCE_INVENTORY.md)
- [Kiến trúc](docs/ARCHITECTURE.md)
- [Giao thức IPC](docs/IPC_PROTOCOL.md)
- [Build và chạy](docs/BUILD_AND_RUN.md)
- [Kiểm thử](docs/TESTING.md)
- [Debug log / vấn đề đã biết](docs/DEBUG_LOG.md)
- [Quyết định kỹ thuật](docs/DECISIONS.md)
- [Clone và variant](docs/VARIANTS_AND_CLONES.md)
- [Roadmap](docs/ROADMAP.md)
- [Third-party notices và trạng thái license](LICENSES/README.md)

## Trạng thái xác minh hiện tại

- Emulator: đã build thành công trong môi trường rà soát bằng OpenJDK 21, với target bytecode Java 8.
- Remoter: chưa được build trong môi trường rà soát vì không có Windows/.NET Framework compiler.
- Chưa chạy kiểm thử runtime trên Windows và chưa xác minh đồng bộ chuột/proxy bằng game thực tế.

Xem [`docs/PROJECT_STATE.md`](docs/PROJECT_STATE.md) để biết rõ phần nào đã xác minh và phần nào chưa.

## Đóng góp và fork

Repository được tổ chức để người khác có thể đọc source, fork và tự build. Bản build do workflow chính thức của repository tạo ra sẽ được phân biệt bằng release/tag và checksum. Việc lựa chọn giấy phép áp dụng cho toàn bộ phần mã Che779 vẫn cần chủ sở hữu xác nhận; xem [`LICENSES/README.md`](LICENSES/README.md).

## CHE779 maintenance follow-up — 2026-11-13

This archive includes a follow-up maintenance pass:
- `apps/emulator/source/src/com/che779/emu/IpcServer.java`: replaced regex field scanning with a strict flat-JSON object parser.
- `apps/emulator/source/test/com/che779/emu/IpcServerJsonTest.java` and `apps/emulator/source/run-ipc-tests.sh`: parser regression tests.
- `apps/remoter/source/MainForm.Sync.cs`: marshal low-level mouse-hook callbacks to the WinForms UI thread before reading tab/UI state.

Run `cd apps/emulator/source && ./run-ipc-tests.sh` for the Java IPC parser regression test. The C# Remoter project was not compiled in this environment because no .NET compiler/runtime was available.
