# Che779 Remoter (Java) — bản chạy trên Linux

Remoter viết bằng **Java/Swing** để điều khiển nhiều **Emulator V1** (`Che779_Emulator.jar`, mỗi game một tiến trình) trên **Linux** (cũng chạy được trên Windows/macOS có Java). Nó nói chuyện với Emulator qua cùng giao thức IPC như Remoter C# trên Windows ([`docs/IPC_PROTOCOL.md`](../../docs/IPC_PROTOCOL.md)).

## Chạy

```bash
sudo apt install default-jdk      # Debian/Ubuntu/Mint (gói chính thức); distro khác: gói JDK tương ứng
cd apps/remoter-linux
./run.sh                          # tự build nếu chưa có Che779_Remoter.jar
./run.sh /duong/dan/game.jar      # mở sẵn game
```

Cần `Che779_Emulator.jar`: Remoter tự dò cạnh nó, ở `../emulator/source/` hoặc `../emulator/prebuilt/`; không thấy thì bấm **Cài đặt** để chọn. Máy không có màn hình (SSH/server) có thể dùng `xvfb-run -a ./run.sh`.

## Dùng

| Nút | Việc |
|---|---|
| Mở game… / Mở lại / Đóng / Đóng hết | Quản lý tab (mỗi game một cửa sổ Emulator) |
| Hiện / Ẩn / Ngủ / Thức / Dọn RAM | Gửi tới tab đang chọn (+ các tab đã tick nếu bật *Đồng bộ*) |
| Proxy… | `host:port` hoặc `host:port:user:pass`; để trống = tắt |
| Chụp… | Lưu ảnh màn hình tab đang chọn |
| Khung xem trước + bàn phím | Nhấp/kéo chuột và gõ phím ở đây → nhân ra mọi tab đã tick (tọa độ theo tỉ lệ) |

Cấu hình lưu ở `~/.che779/remoter.properties`; log từng tab ở thư mục tạm (`che779-tab<N>.log`).

## Khác với Remoter Windows

- Đồng bộ chuột/phím làm trên **khung xem trước** của Remoter, không hook toàn cục vào cửa sổ Emulator (X11/Wayland không cho phép dễ dàng).
- Chưa có: xếp cửa sổ, xem lưới, quản lý proxy hàng loạt, hẹn giờ đóng, tự mở lại tab văng.

## Build

| Hệ | Lệnh |
|---|---|
| Linux/macOS | `./build.sh` |
| Windows CMD | `build.cmd` |
| Windows PowerShell | `powershell -ExecutionPolicy Bypass -File build.ps1` |

Cần JDK 8+ (biên dịch ra bytecode Java 8). Kết quả: `Che779_Remoter.jar`.

## Trạng thái kiểm thử

- ✅ Build bằng JDK 21 trên Linux; phần lõi (mở tab, ping, status, phím, chuột, ẩn/hiện, ngủ, dọn RAM, chụp, đóng) đã chạy với Emulator V1 và game thật dưới màn hình ảo Xvfb.
- ✅ Giao diện hiện đúng, khung xem trước hiển thị game (chụp từ Xvfb).
- ❌ Chưa thử trên màn hình/cửa sổ thật có trình quản lý cửa sổ, nhiều tab cùng lúc, Wayland, Termux; `build.cmd`/`build.ps1` chưa chạy trên Windows.
