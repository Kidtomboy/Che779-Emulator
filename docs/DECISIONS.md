# Technical Decisions

## D-001 — Một repository cho hai ứng dụng Project 1

- **Quyết định:** Emulator V1 và Remoter nằm trong cùng repository.
- **Lý do:** hai app tạo thành một sản phẩm; Remoter phụ thuộc giao thức và cách khởi chạy Emulator.
- **Cách thực hiện:** `apps/emulator/` và `apps/remoter/` tách source/build riêng; tài liệu IPC ở cấp repo.

## D-002 — Không dùng README cũ làm nguồn sự thật

- **Quyết định:** tài liệu mới được lập từ source archive và đánh giá độc lập; README cũ chỉ được giữ làm tài liệu lịch sử tham khảo.
- **Lý do:** README cũ được tạo trước một phần quá trình phát triển và có các nhận định đã bị source review đính chính.

## D-003 — Giữ baseline JAR riêng

- **Quyết định:** JAR có sẵn từ archive được lưu tại `apps/emulator/prebuilt/`; build mới tạo output trong `apps/emulator/source/`.
- **Lý do:** giữ bằng chứng binary đầu vào và không nhầm binary cũ với output build mới.

## D-004 — Không refactor logic runtime trong lần tái cấu trúc đầu tiên

- **Quyết định:** giữ nguyên source app; tập trung tổ chức repo, tài liệu, kiểm tra tĩnh, CI và package.
- **Lý do:** Remoter chưa thể build trong môi trường hiện tại; thay đổi logic mà không có Windows test sẽ tăng rủi ro.
- **Điều kiện xem xét lại:** sau khi Cherry duyệt cấu trúc và có Windows CI/runtime test.

## D-005 — Open source/fork, phân biệt official release

- **Quyết định:** thiết kế repo để người khác có thể fork và tự build; workflow chính thức tạo artifact có thể truy vết.
- **Lưu ý:** chưa tự gán license chung cho code Che779. Cần Cherry xác nhận license sau khi rà soát quyền đối với code tùy biến và third-party.

## D-006 — Không thêm ping lần nữa

- **Quyết định:** giữ action `ping` hiện có.
- **Lý do:** source `IpcServer.java` đã hỗ trợ action này; đánh giá cũ đề xuất thêm ping là không cần thiết.
