# Nhật ký thay đổi

Chỉ ghi những gì **phiên bản hiện tại** đang làm được. Lịch sử các bản trước không còn được lưu ở đây.

## [1.3.1] — Bản phát hành (chỉ Windows)

So với 1.3.0: lệnh chụp màn hình của cổng IPC chỉ ghi tệp `.png` và từ chối đích không phải tệp thường (giảm rủi ro ghi đè tệp tùy ý; token IPC vẫn chưa có). Chưa chạy thử trên Windows (`NOT_RUN`).

> Từ 1.3.0 repo này chỉ dành cho Windows. Bản Linux (kể cả Remoter Linux) nằm ở Che779-Linux.

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

### Phát hành
- Gắn thẻ `vX.Y.Z` → GitHub Actions build **trên Windows** (`build_emulator.ps1` + `dotnet build`) và tạo `Che779-Emulator-<phiên bản>-Windows.zip` cùng `SHA256SUMS.txt` (mã băm tính sau cùng trên đúng file zip).
