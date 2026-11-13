# Build and Run

## Emulator V1

### Yêu cầu

- JDK 9+ để chạy script hiện tại vì `javac --release` được hỗ trợ từ JDK 9. Output vẫn nhắm bytecode Java 8.
- Thư viện `asm-3.2.jar` đã được cung cấp tại `apps/emulator/source/lib/`.
- JNA là tùy chọn; xem `apps/emulator/source/lib/DOC_TOI_DAY.txt`.

### Linux/macOS/Git Bash

```bash
cd apps/emulator/source
./build.sh
```

### Windows

```bat
cd apps\emulator\source
build_emulator.bat
```

Output: `apps/emulator/source/Che779_Emulator.jar`.

Lưu ý: `build.sh` hiện dùng `--release 8`; câu mô tả cũ “JDK 8+” chưa chính xác cho mọi JDK 8 compiler. Đã xác minh bằng OpenJDK 21. Cần kiểm tra lại cách build với JDK 8 trước khi ghi hỗ trợ JDK 8 là đã xác nhận.

## Remoter

### MSBuild / .NET SDK

Windows:

```powershell
dotnet build apps/remoter/source/Che779Remoter.csproj -c Release
```

Hoặc mở `Che779Remoter.csproj` bằng Visual Studio. Project target `net48`.

### Compiler csc.exe

Trong `apps/remoter/source`:

```bat
build_remoter.bat
```

Script cũ tìm `csc.exe` trong .NET Framework 4.x. Đây là đường build truyền thống; cần xác minh trên Windows thực tế.

## Chạy hai ứng dụng

- Đặt `Che779_Remoter.exe` và `Che779_Emulator.jar` cạnh nhau để Remoter có thể tự dò JAR.
- Cần cài Java runtime tương thích và chọn `javaw.exe`/`java.exe` trong Remoter.
- Chọn file game `.jar`.
- Cấu hình người dùng nằm trong `%APPDATA%\Che779\Che779-Emulator` và `%APPDATA%\Che779\Che779-Remoter`.

## JNA

Build manifest tham chiếu `lib/jna.jar`. Archive hiện không kèm `jna.jar`; tài liệu nguồn ghi JNA tùy chọn cho Clean RAM và có fallback PowerShell phía Remoter. Nếu phân phối JNA, phải bổ sung đúng thư viện, version và license notice vào package.

## Package tích hợp

Dùng `scripts/package-windows.ps1` sau khi có JAR và EXE build. Script tạo một thư mục staging với cả hai file cạnh nhau và ZIP. Không commit thư mục build output vào source repository.
