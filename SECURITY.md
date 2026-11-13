# Security Policy

## Báo cáo vấn đề

Vui lòng không đăng proxy credentials, dữ liệu cá nhân hoặc file cấu hình chứa bí mật trong issue công khai. Mô tả phiên bản, môi trường, bước tái hiện và log đã loại bỏ thông tin nhạy cảm.

## Lưu ý bảo mật hiện tại

IPC hiện chỉ bind `127.0.0.1`, nhưng chưa có authentication token. Các action `quit/close` và `screenshot` có tác động tới tiến trình/file theo quyền người dùng. Không expose IPC port ra mạng công cộng. Các hạn chế còn lại được ghi trong `docs/DEBUG_LOG.md`.
