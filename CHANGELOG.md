# Nhật ký thay đổi

Chỉ ghi những gì **phiên bản hiện tại** đang làm được. Lịch sử các bản trước không còn được lưu ở đây.

## [1.2.0] — Bản phát hành

### Che779 Emulator (Java)
- Chạy game J2ME `.jar`, mỗi game một tiến trình Java riêng, dựa trên MicroEmulator 3.0.0.
- Menu **Che779**: Clean RAM, Sleep (F12) và tự Sleep, giảm FPS, tốc độ game (bước 0,1), giảm đồ họa, màu.
- Thay IP máy chủ: ưu tiên `server.txt` trong jar game, rồi máy chủ chọn từ `servers.txt`, rồi tự nhập IP/cổng.
- Quản lý Proxy HTTP (có xác thực) và SOCKS5.
- Cổng điều khiển IPC chỉ mở trên `127.0.0.1`, JSON một dòng; 17 lệnh (xem `docs/IPC_PROTOCOL.md`).
- Dữ liệu cấu hình nằm ngoài thư mục chương trình (`%APPDATA%\Che779\Che779-Emulator`).

### Che779 Remoter — Windows (C#)
- Mở nhiều tab game cùng lúc, theo dõi trạng thái (cổng, CPU, RAM, PID), Hiện/Ẩn, Ngủ, dừng và mở lại.
- Tự sắp xếp cửa sổ theo lưới; Hiện lại từng tab về đúng vị trí cũ.
- Đồng bộ phím, đồng bộ chuột, bàn phím ảo đầy đủ.
- Macro phím (ghi rồi phát lặp), chụp màn hình, hẹn giờ đóng, tự mở lại tab khi thoát.
- Lưu và nạp hồ sơ `.che779`; tách dữ liệu từng tab; giao diện tối.

### Che779 Remoter — Linux (Java/Swing, mới)
- Mở, đóng, mở lại tab; Hiện/Ẩn, Ngủ/Thức, dọn RAM, đặt proxy, chụp màn hình.
- Khung xem trước: chuột và phím thao tác ở đây được nhân ra các tab đã tick.
- Chạy được cả trên Windows và macOS có Java.

### Phát hành
- Mỗi lần gắn thẻ `vX.Y.Z`, GitHub tự build và tạo `Che779-Emulator-<phiên bản>-Windows.zip`, `Che779-Remoter-<phiên bản>-Linux.zip` và `SHA256SUMS.txt`.
