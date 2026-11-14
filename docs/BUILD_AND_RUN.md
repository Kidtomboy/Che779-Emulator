# Build và chạy

## Yêu cầu

- **JDK 9 trở lên** để build Java (`javac --release 8` tạo bytecode Java 8). Đã kiểm chứng với OpenJDK 21.
- Chạy: **Java 8 trở lên**.
- Remoter Windows: Windows có .NET Framework 4.x (`csc.exe` có sẵn) hoặc .NET SDK.

Cài JDK bằng gói chính thức của hệ điều hành, ví dụ Debian/Ubuntu/Mint: `sudo apt install default-jdk`. Các script build **không cần `unzip`**.

## Build

| Thành phần | Linux/macOS | Windows |
|---|---|---|
| Emulator | `apps/emulator/source/build.sh` | `build_emulator.bat` hoặc `build_emulator.ps1` |
| Remoter Windows | — | `apps/remoter-windows/source/build_remoter.bat` / `.ps1`, hoặc `dotnet build apps/remoter-windows/source/Che779Remoter.csproj -c Release` |
| Remoter Linux (Java) | `apps/remoter-linux/build.sh` | `build.cmd` hoặc `build.ps1` |

Kết quả: `Che779_Emulator.jar`, `Che779_Remoter.exe` (hoặc `Che779_Remoter.jar`) ngay trong thư mục nguồn của từng thành phần. Đóng gói Windows tại máy mình: `scripts/package-windows.ps1`.

## Chạy

**Windows:** để `Che779_Emulator.jar` và `Che779_Remoter.exe` cùng một thư mục, bấm đúp Remoter, chọn Java, chọn file game `.jar`, bấm Bắt đầu.

**Linux:** `cd apps/remoter-linux && ./run.sh` (tự build nếu chưa có jar). Remoter tự dò `Che779_Emulator.jar` cạnh nó hoặc trong `apps/emulator/`; không thấy thì bấm **Cài đặt**. Máy không có màn hình: `xvfb-run -a ./run.sh`.

## Dữ liệu và thư viện tùy chọn

- Dữ liệu: `%APPDATA%\Che779\Che779-Emulator`, `%APPDATA%\Che779\Che779-Remoter` (Windows); `~/.che779` (Linux).
- JNA là tùy chọn: đặt `jna.jar` vào `apps/emulator/source/lib/` thì Clean RAM dùng JNA, không có thì dùng PowerShell. Nếu phân phối kèm JNA, phải kèm đúng giấy phép.

## Bản dựng sẵn

`apps/emulator/prebuilt/Che779_Emulator.jar` là jar gốc từ gói đầu vào, giữ để tham chiếu, **không** phải bản 1.2.0. Bản 1.2.0 lấy ở mục Releases.
