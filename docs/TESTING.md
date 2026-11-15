# Kiểm thử

## Tự động (CI, mỗi lần push)

| Việc | Chạy ở đâu |
|---|---|
| `python tests/verify_project1.py`: cấu trúc repo, 17 lệnh IPC còn đủ, kiểm tra manifest jar | Linux |
| Build Emulator (JDK 21, bytecode Java 8) | Linux |
| Build Remoter Windows (`dotnet build`, .NET Framework 4.8) | Windows |

Chạy tay: `python tests/verify_project1.py --jar apps/emulator/source/Che779_Emulator.jar`.

## Đã kiểm chứng

| Hạng mục | Cách kiểm | Kết quả |
|---|---|---|
| Emulator + Remoter Windows chạy trên Windows | Chủ dự án tự chạy (đã sửa file build của mình) | Chạy tốt |
| Emulator trên Linux (màn hình ảo Xvfb) với game thật | Claude chạy | Mở được game |
| IPC: `ping`, `status`, `version`, `keypress`, `hide/show`, `screenshot`, `proxy_get` | Claude gửi lệnh tới Emulator đang chạy | Phản hồi đúng |

## Chưa kiểm chứng

- Các file `build.cmd`, `build.ps1` và `package-windows.ps1` chưa chạy trên Windows.
- Workflow **Phát hành** chưa chạy thật lần nào (sẽ biết khi gắn thẻ đầu tiên).
- Proxy, đồng bộ chuột với nhiều tab khác kích thước, nhiều JVM bị thoát đột ngột.

Không đánh dấu một mục là đã kiểm chứng khi chưa có kết quả chạy thật.
