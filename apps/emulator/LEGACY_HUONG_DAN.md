# Che779 — phiên bản 0.0.0.2 custom

Tất cả thành phần (Emulator, Remoter, IPC) đều mang version **0.0.0.2 custom**: hiện ở tiêu đề Remoter, About của Emulator, thanh Manifest của jar, lệnh IPC `status`/`version` và nút "Phiên bản" trong Remoter → khi báo lỗi hãy kèm version.

## Nơi lưu dữ liệu (không còn ghi vào thư mục giải nén)
`%APPDATA%\Che779\Che779-Emulator` (cấu hình che779.xml, proxies.txt, servers.txt, filesystem, tab1.. nếu tách dữ liệu)
`%APPDATA%\Che779\Che779-Remoter` (che779_remoter.ini). Remoter tự chuyển file ini cũ nếu có.

## Emulator (build từ MicroEmulator 3.0.0 gốc, Apache 2.0)
Menu "Che779": Clean RAM · Sleep (F12) · Tự động Sleep · Giảm FPS · Tốc độ game · Giảm đồ họa · Màu · **Kích hoạt thay IP · Thay Ip Phiên Bản · Quản lý Proxy** (giai đoạn 2 đã xong).
- Thứ tự ưu tiên IP: `server.txt` trong jar game > tích + chọn máy chủ > tự nhập IP/PORT.
- Danh sách máy chủ: file `servers.txt` (mỗi dòng `ip|port|tên`), có thể tải từ URL tự đặt (nút "Tải lại danh sách"). Không gọi về máy chủ nào của bên thứ ba.
- Proxy: HTTP (Basic auth) / SOCKS5. Hãy tự sử dụng proxy bạn đã mua.

## Tự build
Emulator: `emulator-src\build_emulator.bat` (JDK 8+) hoặc `build.sh`. Remoter: `remoter-src\build_remoter.bat`.
Clean RAM qua JNA: đặt `jna.jar` vào `lib/` (xem lib/DOC_TOI_DAY.txt).

## Khác biệt đã biết so với ZMicroPro
Tốc độ game bước 0.1 (0.1–100); skin cảm ứng theo bố cục MicroEmulator gốc.

## Cập nhật (vẫn 0.0.0.2 custom)
- Sửa: Ẩn rồi Hiện nhiều tab → từng cửa sổ về đúng vị trí/kích thước cũ (Remoter ghi nhớ trước khi ẩn, không phụ thuộc "Tự động sắp xếp").
- Remoter: nút "Quản lý Proxy" (thẻ 5) — thêm hàng loạt, kiểm tra, chế độ: không can thiệp / tắt / chung 1 proxy / mỗi tab 1 proxy. Chọn trước khi mở tab (tự áp lúc khởi động) hoặc sau khi mở (nút đồng bộ).
- Remoter: "Đồng bộ Chuột (nhấp / kéo)" — nhấp/kéo trên 1 tab sẽ nhân ra các tab khác (cần các tab cùng kích thước để trùng toạ độ).
- Emulator: thêm lệnh IPC `proxy`, `proxy_get`, `mouse` và tham số `-Dche779.proxy`.

## Thay đổi 0.0.0.2 custom
- Nút **Hiện** ở "Hành động": hiện từng tab một theo thứ tự, mỗi tab được trả về đúng vị trí cũ rồi mới hiện tab kế tiếp (hết lỗi sắp xếp lung tung khi mở nhiều tab).
- **Quản lý Proxy trong Remoter** (thẻ 5/6): kho proxy chung, chọn chế độ (theo emulator / tắt / dùng chung 1 proxy / mỗi tab một proxy xoay vòng), áp dụng trước khi mở tab (tham số `-Dche779.proxy`) hoặc sau khi mở (lệnh IPC `proxy`).
- **Đồng bộ chuột** (thẻ 5): nhấp/kéo trên một tab sẽ lặp lại ở mọi tab còn lại (theo toạ độ tỉ lệ, nên các tab nên cùng kích thước).
- Cả Emulator và Remoter đều được sửa; cần dùng cặp 0.0.0.2 với nhau để đồng bộ proxy/chuột hoạt động.
- Lần chạy đầu (chưa có cấu hình) tự chọn sẵn "Resizable device" làm thiết bị mặc định.
