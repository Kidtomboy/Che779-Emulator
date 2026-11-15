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

## Giao diện (6 khu vực, bám Remoter Windows)

Giao diện tối, màu hồng, bố cục 6 khu vực như bản Windows (có thể chuyển sáng/tối ở mục 5):

1. **Đường dẫn** Emulator · Game · Java (có nút Duyệt).
2. **Cấu hình tab & lưới**: RAM tối đa, số tab, chờ (ms), kích thước ô tab, lưới cột × hàng, *Tự tính*, tự sắp xếp, tách dữ liệu từng tab; lưới nút số tab 1…N (bấm để chọn và hiện tab).
3. **Hành động**: Bắt đầu, Dừng, Ẩn, Hiện, Ngủ, Thức, Clean RAM, Sắp xếp tab, Thêm game…, Thêm N tab, Xóa dòng, Đóng hết.
4. **Danh sách tab & trạng thái hệ thống**: Game, Trạng thái, Cổng, CPU, RAM, Ngủ, PID; CPU/RAM hệ thống và tổng các tab (đọc từ `/proc`, chỉ Linux).
5. **Đồng bộ & bàn phím**: Đồng bộ Tab / Phím / Chuột, giao diện tối, *Mở bàn phím đầy đủ / xem trước* (khung xem trước + bàn phím ảo).
6. **Tiện ích**: Lưu/Nạp hồ sơ, Thư mục dữ liệu, Xóa dữ liệu; tự mở lại tab khi tắt/crash (tối đa 5 lần), tự bắt đầu khi mở Remoter, tự Clean RAM mỗi N phút, tự cho ngủ khi RAM ≥ N%, hẹn giờ đóng hết, luôn nằm trên cùng; cùng nút Proxy…, Chụp…, Mở lại.

Cấu hình: `~/.che779/Che779-Remoter/che779_remoter.ini` (cùng tên khóa với bản Windows); dữ liệu Emulator: `~/.che779/Che779-Emulator` (hoặc `tab<N>` khi bật tách dữ liệu); log từng tab: thư mục tạm (`che779-tab<N>.log`).

## Đối chiếu tính năng Windows ↔ Linux

| Tính năng Windows | Linux | Ghi chú |
|---|---|---|
| Đường dẫn Emulator/Game/Java + Duyệt | Implemented | |
| RAM, số tab, chờ, ô tab, lưới, Tự tính, tách dữ liệu | Implemented | Tự tính dựa theo bề ngang màn hình |
| Bắt đầu / Dừng / Đóng hết / Thêm game / Thêm N tab / Xóa dòng | Implemented | |
| Ẩn / Hiện / Ngủ / Thức / Clean RAM | Implemented | Qua IPC; Ẩn/Hiện không trả đúng vị trí cửa sổ như Windows |
| Sắp xếp tab | Equivalent | Dùng `xdotool` (X11). Không có `xdotool` hoặc Wayland thuần: báo rõ, không làm giả |
| Cột CPU/RAM/PID, thống kê hệ thống | Implemented | Đọc `/proc`; không có trên Windows/macOS |
| Đồng bộ Tab / Phím | Implemented | Phím thật chỉ khi cửa sổ Remoter (hoặc cửa sổ xem trước) đang focus |
| Đồng bộ Chuột | Equivalent | Chỉ qua **khung xem trước** của Remoter (tọa độ tỉ lệ), không hook toàn cục |
| Bàn phím đầy đủ | Equivalent | Nằm trong cửa sổ xem trước + bàn phím ảo |
| Lưu/Nạp hồ sơ, Thư mục dữ liệu, Xóa dữ liệu | Implemented | |
| Tự mở lại, tự bắt đầu, tự Clean RAM, canh RAM, hẹn giờ, luôn trên cùng | Implemented | Luôn-trên-cùng phụ thuộc window manager |
| Xem lưới (GridPreview) | **Chưa làm** | Giai đoạn sau |
| Macro (ghi/phát) | **Chưa làm** | Chưa quyết định có cần ở Linux không |
| Giao diện sáng/tối | Implemented | |

## Build

| Hệ | Lệnh |
|---|---|
| Linux/macOS | `./build.sh` |
| Windows CMD | `build.cmd` |
| Windows PowerShell | `powershell -ExecutionPolicy Bypass -File build.ps1` |

Cần JDK 8+ (biên dịch ra bytecode Java 8). Kết quả: `Che779_Remoter.jar`.

## Trạng thái kiểm thử

- ✅ Build bằng JDK 21; chạy dưới Xvfb với Emulator V1 + game thật: 3 tab tự mở từ cấu hình, bảng hiện đúng trạng thái/cổng/CPU/RAM/PID, thống kê hệ thống (ảnh chụp Xvfb).
- ✅ `tests/RemoterCoreTest.java` (lõi): thêm dòng, chạy, IPC sẵn sàng, PID, RAM tiến trình, tách dữ liệu theo tab, Clean RAM, Ngủ/Thức, Dừng giữ dòng, chạy lại — đạt toàn bộ.
- ⚠️ **Chưa bấm thử từng nút bằng chuột**: máy thử không có công cụ giả lập chuột/phím. Nhóm nút Ẩn/Hiện, Thêm game/Thêm N tab, Xóa dòng, hồ sơ, các tùy chọn tự động chỉ mới được kiểm gián tiếp (đọc mã + lõi).
- ❌ Sắp xếp tab chưa thử (không có `xdotool` trên máy thử); chưa thử màn hình thật có window manager, Wayland, Termux; `build.cmd`/`build.ps1` chưa chạy trên Windows.
