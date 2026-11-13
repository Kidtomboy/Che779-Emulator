# Architecture

## Tổng quan

Project 1 là kiến trúc đa tiến trình. Remoter không nhúng emulator; nó tạo và quản lý các tiến trình Java riêng. Mỗi tiến trình Emulator chạy một game/JVM và mở một IPC server chỉ bind vào `127.0.0.1`.

```text
Windows desktop
┌──────────────────────────────────────────────┐
│ Che779 Remoter (WinForms)                    │
│  MainForm / process orchestration            │
│  IpcClient ───────────────┐                   │
│  MouseSync (Win32 hook)   │                   │
└───────────────────────────┼───────────────────┘
                            │ TCP loopback
                 ┌──────────┴───────────┐
                 ▼                      ▼
        Emulator JVM #1         Emulator JVM #2 ...
        IpcServer               IpcServer
        Swing / MIDlet          Swing / MIDlet
        Game A                  Game B
```

## Emulator

- Entry point: `apps/emulator/source/src/com/che779/emu/Che779Main.java`.
- IPC server: `apps/emulator/source/src/com/che779/emu/IpcServer.java`.
- Emulator dựa trên MicroEmulator 3.0.0; nhiều lớp upstream được chỉnh trực tiếp.
- Cấu hình và dữ liệu người dùng lưu trong `%APPDATA%\Che779\Che779-Emulator` theo tài liệu baseline.
- `ProxyManager` quản lý proxy HTTP/SOCKS5; `CleanRam`/`JnaClean` thực hiện các chức năng dọn RAM.

## Remoter

- Entry point: `apps/remoter/source/Program.cs`.
- UI và điều phối: `MainForm.cs`, `MainForm.Logic.cs`, `MainForm.Extras.cs`, `MainForm.Sync.cs`.
- `EmuTab` giữ trạng thái process và cổng IPC của một phiên.
- `IpcClient` gửi JSON line tới `127.0.0.1`.
- `MouseSync` dùng Windows low-level mouse hook; luồng đồng bộ chuột khác với IPC.
- Cấu hình lưu trong `%APPDATA%\Che779\Che779-Remoter`.

## Liên kết giữa hai app

- Remoter chọn đường dẫn Java, JAR emulator và JAR game.
- Remoter dò cổng, khởi chạy Emulator với tham số và đọc thông tin `CHE779_PORT` từ stdout để biết cổng thực tế.
- Emulator mở server trên loopback; Remoter tạo `IpcClient` tới cổng đó.
- Hai phía phải tương thích về action, tham số, phản hồi và version. Xem `IPC_PROTOCOL.md`.

## Ranh giới trách nhiệm

- Emulator sở hữu trạng thái game, cửa sổ Swing, proxy áp dụng cho phiên, thao tác phím/chuột và screenshot.
- Remoter sở hữu vòng đời process, bố trí cửa sổ, giao diện quản lý nhiều phiên, macro và phát lệnh.
- MouseSync hiện mô phỏng tọa độ theo tỷ lệ cửa sổ; không dùng IPC để truyền sự kiện chuột giữa các cửa sổ.

## Nợ kiến trúc đã biết

- `MainForm.cs` và `MainForm.Logic.cs` lớn; chưa refactor trong revision cấu trúc này.
- IPC parser là parser regex tự viết, không phải JSON parser đầy đủ.
- Server tạo thread cho từng client, chưa giới hạn số client/độ dài dòng và không đặt read timeout.
- `MainForm.Sync` có đường gọi đọc WinForms controls từ thread hook; cần kiểm tra và chuyển snapshot về UI thread.
