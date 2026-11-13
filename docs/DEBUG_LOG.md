# Debug Log / Known Issues

Các mục dưới đây là phát hiện từ review source `0.0.0.2 custom`, chưa phải tất cả đều đã tái hiện runtime. Trạng thái ban đầu: **Open / cần xác minh**.

## P1 — IPC chưa xác thực và screenshot nhận đường dẫn tùy ý

- **Khu vực:** `IpcServer.java`, `Che779Main.screenshot()`.
- **Quan sát:** server bind loopback nhưng không có token; action `quit/close` kết thúc tiến trình; `screenshot` nhận path từ request.
- **Tác động:** tiến trình cục bộ biết cổng có thể gửi lệnh; screenshot có thể ghi file theo quyền người dùng.
- **Đề xuất:** token phiên ngẫu nhiên; kiểm tra path/giới hạn output; ghi log từ chối; không mở server ra LAN.
- **Trạng thái:** chưa sửa, cần thiết kế tương thích Remoter.

## P1 — IPC parser không phải JSON parser đầy đủ

- **Khu vực:** `IpcServer.parse()` / `unescape()`.
- **Quan sát:** regex parser chỉ hỗ trợ schema phẳng và escape một phần.
- **Tác động:** chuỗi đặc biệt hoặc mở rộng protocol có thể parse sai.
- **Đề xuất:** dùng JSON parser đã vendored hoặc thay parser có test; không thêm dependency không rõ license.
- **Trạng thái:** chưa sửa.

## P1 — Server không giới hạn client và thời gian đọc

- **Khu vực:** `IpcServer.serve()`.
- **Quan sát:** `setSoTimeout(0)`, một thread cho mỗi client, không giới hạn độ dài dòng.
- **Tác động:** client cục bộ có thể giữ socket/thread hoặc gửi payload lớn.
- **Đề xuất:** read timeout, max line length, giới hạn concurrent clients và đóng socket lỗi.
- **Trạng thái:** chưa sửa.

## P1 — Có khả năng truy cập WinForms control từ thread hook

- **Khu vực:** `MainForm.Sync.cs`, `MouseSync.cs`, `Targets()`.
- **Quan sát:** callback hook có đường gọi đọc checkbox/grid/collection UI.
- **Tác động:** race condition hoặc cross-thread exception.
- **Đề xuất:** snapshot target trên UI thread; hook chỉ phát event chuẩn hóa; đồng bộ lifetime của hook.
- **Trạng thái:** chưa sửa; Remoter chưa được build/test trong môi trường hiện tại.

## P2 — MouseSync ánh xạ theo cửa sổ ngoài

- **Khu vực:** `MouseSync.cs`.
- **Quan sát:** xử lý `WM_LBUTTONDOWN`, `WM_LBUTTONUP`, `WM_MOUSEMOVE`; chưa thấy wheel/right/middle; tọa độ tỷ lệ dựa trên cửa sổ ngoài, không phải canvas.
- **Tác động:** có thể lệch khi khác viền, kích thước hoặc aspect ratio; không nên mô tả là bắt buộc cùng kích thước.
- **Đề xuất:** xác định vùng client/canvas, kiểm thử resize; bổ sung nút chuột chỉ khi có nhu cầu.
- **Trạng thái:** chưa sửa.

## P2 — Cạn kiệt dải port

- **Khu vực:** `MainForm.Logic.cs` `PickPort()`, `IpcServer.start()`.
- **Quan sát:** Remoter dò 300 port; nếu không tìm thấy có nhánh trả về `BasePort`; Emulator tự dò 200 port.
- **Tác động:** trường hợp dải port bận có thể lỗi khởi động hoặc lệch kỳ vọng.
- **Đề xuất:** trả lỗi rõ ràng khi hết port; để server bind port 0 hoặc có cơ chế cấp phát an toàn và thông báo cổng thật.
- **Trạng thái:** chưa sửa.

## P2 — Settings nuốt exception

- **Khu vực:** `Settings.cs`.
- **Quan sát:** lỗi load/save được catch nhưng không thông báo đầy đủ.
- **Tác động:** người dùng có thể tưởng cấu hình đã lưu.
- **Đề xuất:** log chẩn đoán, cảnh báo UI khi save thất bại, giữ an toàn dữ liệu cũ.
- **Trạng thái:** chưa sửa.

## P2 — Vòng đời process / auto-restart có thể race

- **Khu vực:** Remoter process callbacks, ThreadPool, `EmuTab`.
- **Quan sát:** nhiều callback có thể cập nhật trạng thái cùng lúc.
- **Tác động:** trạng thái UI/process có thể không nhất quán khi stop trong lúc restart.
- **Đề xuất:** mô hình state transition rõ ràng, cancellation token hoặc generation id, test start/stop/restart.
- **Trạng thái:** chưa sửa.

## P3 — File logic lớn và thiếu test tự động

- **Khu vực:** `MainForm.cs`, `MainForm.Logic.cs`.
- **Quan sát:** file UI/logic lớn; archive không có test suite.
- **Đề xuất:** tách service theo process lifecycle, proxy, macro, layout; thêm test IPC và lifecycle sau khi thiết lập build Windows.
- **Trạng thái:** backlog.

## Ghi chú review đã đính chính

- Port collision detection đã tồn tại ở cả Remoter và Emulator; vấn đề còn lại là nhánh cạn kiệt và race.
- `ping` đã tồn tại; không cần thêm lại.
- MouseSync dùng tọa độ tỷ lệ; không khẳng định tất cả cửa sổ bắt buộc cùng kích thước.
