# Roadmap

## Phase 1 — Repository foundation

- [x] Gom Emulator V1 + Remoter.
- [x] Tài liệu hóa kiến trúc và IPC hiện tại.
- [x] Kiểm tra tĩnh và CI build tách app.
- [x] Package tích hợp hai app.
- [ ] Cherry duyệt cấu trúc và nội dung bàn giao.

## Phase 2 — Build/runtime verification

- [ ] Xác minh MSBuild Remoter trên Windows.
- [ ] Chạy package trên máy Windows sạch.
- [ ] Integration test Remoter ↔ Emulator.
- [ ] Test process lifecycle, proxy, mouse sync và screenshot.

## Phase 3 — Reliability and security

- [ ] Thiết kế IPC token/session authentication.
- [ ] Thay parser regex bằng parser JSON có kiểm thử.
- [ ] Giới hạn timeout, kích thước dòng và concurrent clients.
- [ ] Sửa thread affinity trong MouseSync.
- [ ] Xử lý hết port và race port allocation.
- [ ] Báo lỗi Settings load/save.

## Phase 4 — Maintainability

- [ ] Tách process lifecycle khỏi MainForm.
- [ ] Tách proxy/macro/layout services khi có test bảo vệ.
- [ ] Thêm test tự động cho protocol và lifecycle.
- [ ] Chọn và xác nhận license chính thức cho phần code Che779.

## Đã làm trong 1.2.0

- Remoter Linux (Java/Swing); workflow Phát hành; bộ script build đủ Windows và Linux.

## Tiếp theo

- Remoter Linux: xếp cửa sổ, xem lưới, quản lý proxy hàng loạt, hẹn giờ đóng, tự mở lại tab.
- IPC: thêm token xác thực và giới hạn đường dẫn `screenshot`.
