# Build và chạy

## Yêu cầu

- **JDK 9 trở lên** để build Java (`javac --release 8` tạo bytecode Java 8). Đã kiểm chứng với OpenJDK 21.
- Chạy: **Java 8 trở lên**.
- Remoter Windows: Windows có .NET Framework 4.x (`csc.exe` có sẵn) hoặc .NET SDK.

Cài JDK từ [Adoptium](https://adoptium.net). Các script build **không cần `unzip`**.

## Build (Windows)

| Thành phần | Lệnh |
|---|---|
| Emulator | `apps/emulator/source/build_emulator.bat` hoặc `build_emulator.ps1` |
| Remoter Windows | `apps/remoter-windows/source/build_remoter.bat` / `.ps1`, hoặc `dotnet build apps/remoter-windows/source/Che779Remoter.csproj -c Release` |

Kết quả: `Che779_Emulator.jar`, `Che779_Remoter.exe` ngay trong thư mục nguồn. Đóng gói zip tại máy mình: `scripts/package-windows.ps1` (tự tạo `SHA256SUMS.txt` sau khi zip xong).

`.github/scripts/build-emulator.sh` chỉ phục vụ CI và repo Che779-Linux (build jar trên Linux); người dùng Windows không cần.

## Chạy

**Windows:** để `Che779_Emulator.jar` và `Che779_Remoter.exe` cùng một thư mục, bấm đúp Remoter, chọn Java, chọn file game `.jar`, bấm Bắt đầu.

Bản Linux (Remoter Linux, `.deb`, `.tar.gz`) nằm ở repo **Che779-Linux**.

## Dữ liệu và thư viện tùy chọn

- Dữ liệu: `%APPDATA%\Che779\Che779-Emulator`, `%APPDATA%\Che779\Che779-Remoter` (Windows).
- JNA là tùy chọn: đặt `jna.jar` vào `apps/emulator/source/lib/` thì Clean RAM dùng JNA, không có thì dùng PowerShell. Nếu phân phối kèm JNA, phải kèm đúng giấy phép.

## Bản dựng sẵn

`apps/emulator/prebuilt/Che779_Emulator.jar` là jar gốc từ gói đầu vào, giữ để tham chiếu, **không** phải bản 1.3.0. Bản 1.3.0 lấy ở mục Releases.

## Phát hành

Gắn thẻ `v1.3.1` → workflow **Phát hành** (chạy trên Windows) build Emulator + Remoter, đóng zip và tạo `SHA256SUMS.txt`, rồi đăng lên Releases.
