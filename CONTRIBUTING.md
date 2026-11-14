# Đóng góp cho Che779 Emulator + Remoter

Cảm ơn bạn đã quan tâm! Tài liệu này giúp bạn gửi thay đổi nhanh và ít phải sửa lại.

## Trước khi bắt đầu

1. Đọc `docs/PROJECT_STATE.md`, `docs/ARCHITECTURE.md` và `docs/IPC_PROTOCOL.md`.
2. Xác định thay đổi thuộc Emulator (`apps/emulator`), Remoter Windows (`apps/remoter-windows`) hay Remoter Linux (`apps/remoter-linux`).
3. Đổi giao thức IPC thì phải sửa **cả hai phía** (Emulator và Remoter), `docs/IPC_PROTOCOL.md` và test.
4. Không đưa vào repo: file build, dữ liệu người dùng, mật khẩu proxy, khóa ký.

## Cách gửi thay đổi

1. Fork repo, tạo nhánh từ `main`.
2. Sửa, rồi chạy `python tests/verify_project1.py`.
3. Build phần bạn sửa: Java bằng `apps/emulator/source/build.sh`, Remoter Linux bằng `apps/remoter-linux/build.sh`, Remoter Windows bằng `dotnet build` trên Windows.
4. Cập nhật `docs/` và `CHANGELOG.md` nếu hành vi thay đổi.
5. Mở pull request và điền mẫu có sẵn. Ghi rõ **đã chạy gì, trên hệ điều hành nào, và gì chưa chạy**.

## Nguyên tắc

- Không ghi "đã kiểm tra" khi chưa chạy thật.
- Giữ nguyên thông báo bản quyền và giấy phép của MicroEmulator (Apache 2.0).
- Báo lỗi bằng mẫu Issue; thông tin nhạy cảm xem `SECURITY.md`.
