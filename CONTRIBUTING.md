# Contributing

Cảm ơn bạn quan tâm tới Che779 Project 1.

## Trước khi thay đổi

1. Đọc `docs/PROJECT_STATE.md`, `docs/ARCHITECTURE.md` và `docs/IPC_PROTOCOL.md`.
2. Xác định thay đổi thuộc Emulator, Remoter hay cả hai.
3. Nếu thay đổi IPC, cập nhật cả client/server, protocol docs và tests.
4. Không commit build output, user data, proxy credentials hoặc signing secrets.

## Trước khi gửi pull request

- Chạy `python tests/verify_project1.py`.
- Build Emulator nếu có thay đổi Java.
- Build Remoter trên Windows nếu có thay đổi C#.
- Ghi thay đổi vào `CHANGELOG.md` và cập nhật tài liệu bị ảnh hưởng.
- Nêu rõ test nào đã chạy và test nào chưa chạy.

## Nguyên tắc

Không tuyên bố test thành công nếu chưa thực sự chạy. Không xóa attribution/license header của upstream.
