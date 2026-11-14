# Chính sách bảo mật

## Báo lỗi bảo mật

Nếu bạn tìm thấy lỗ hổng, **đừng đăng công khai** trong Issue. Hãy dùng mục *Security → Report a vulnerability* của repo trên GitHub để báo riêng. Khi báo, ghi rõ phiên bản, hệ điều hành, các bước tái hiện, và xóa mật khẩu proxy hoặc thông tin cá nhân khỏi log.

## Những điều cần biết về bảo mật hiện tại

| Vấn đề | Mức độ | Ghi chú |
|---|---|---|
| IPC chưa có xác thực | Trung bình | Cổng chỉ mở trên `127.0.0.1`, nhưng tiến trình khác trên cùng máy biết cổng thì gửi được lệnh (`quit`, `text`, `proxy`, `screenshot`...). |
| Lệnh `screenshot` ghi file | Trung bình | Ghi ảnh ra đường dẫn do bên gọi chỉ định, theo quyền của người dùng. |
| Mật khẩu proxy lưu và truyền dạng chữ thường | Trung bình | Lưu trong `proxies.txt`, tham số `-Dche779.proxy`, và lệnh IPC `proxy`. |

Khuyến nghị: không mở cổng IPC ra mạng, không dùng proxy chứa mật khẩu quan trọng trên máy dùng chung. Chi tiết và kế hoạch xử lý nằm ở `docs/DEBUG_LOG.md` và `docs/ROADMAP.md`.

## Tải bản chính thức

Chỉ tải file từ mục **Releases** của repo chính thức và kiểm tra bằng `SHA256SUMS.txt`.
