# Debug Log / Known Issues

Các mục dưới đây là phát hiện từ review source `1.3.0`, chưa phải tất cả đều đã tái hiện runtime. Trạng thái ban đầu: **Open / cần xác minh**.

## P1 — IPC chưa xác thực và screenshot nhận đường dẫn tùy ý

- **Khu vực:** `IpcServer.java`, `Che779Main.screenshot()`.
- **Quan sát:** server bind loopback; chưa có token xác thực; `screenshot` nhận path từ request.
- **Tác động:** tiến trình cục bộ biết cổng có thể gửi lệnh; screenshot có thể ghi file theo quyền người dùng.
- **Đề xuất:** đánh giá token phiên và kiểm tra path/giới hạn output; giữ tương thích Remoter.
- **Trạng thái:** rủi ro bảo mật còn mở; chưa được xử lý trong audit này.

## P1 — IPC parser không phải JSON parser đầy đủ

- **Khu vực:** `IpcServer.parse()` / `unescape()`.
- **Quan sát:** cần xác minh parser hiện tại theo test protocol và schema đang dùng; chưa đủ căn cứ gọi đây là lỗi runtime.
- **Đề xuất:** bổ sung test escape và dữ liệu biên trước khi quyết định thay parser.
- **Trạng thái:** cần xác minh; không tự động thay parser.

## P1 — Server không giới hạn client và thời gian đọc

- **Khu vực:** `IpcServer.serve()`.
- **Quan sát source main:** hiện có timeout 5 giây, giới hạn 16 client và giới hạn 65.536 ký tự mỗi dòng.
- **Trạng thái:** các biện pháp giới hạn đã có trong source; cần giữ test hồi quy. Không giao lại việc triển khai các giới hạn này.

## P1 — Có khả năng truy cập WinForms control từ thread hook

- **Khu vực:** `MainForm.Sync.cs`, `MouseSync.cs`, `Targets()`.
- **Quan sát source main:** `OnMouseEvent` đã marshal callback về UI thread bằng `BeginInvoke` trước khi đọc `tabs` và trạng thái WinForms.
- **Trạng thái:** phần truy cập UI từ hook đã có biện pháp xử lý trong source; cần build/test Windows để xác nhận runtime. Không giao lại việc này nếu chưa phát hiện regression.

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
