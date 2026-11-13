# Changelog

Mọi thay đổi đáng chú ý sẽ được ghi tại đây. Version ứng dụng và revision đóng gói repository là hai khái niệm riêng.

## [Repository restructure — 2026-11-12]

- Gom Che779 Emulator V1 và Che779 Remoter vào một repository Project 1.
- Giữ source hai ứng dụng trong các thư mục riêng.
- Giữ JAR có sẵn từ gói đầu vào tại `apps/emulator/prebuilt/` làm baseline; không ghi đè nó bằng output build.
- Bổ sung tài liệu kiến trúc, IPC, build, test, debug, quyết định và roadmap.
- Bổ sung kiểm tra tĩnh, workflow CI và script đóng gói hai ứng dụng cạnh nhau.
- Không thay đổi logic runtime của hai ứng dụng trong lần tái cấu trúc này.

## [0.0.0.2 custom — baseline]

- Phiên bản nguồn nhận từ các archive Che779 Emulator V1 và Che779 Remoter.
- Tính năng và hành vi ứng dụng được giữ nguyên; xem `docs/PROJECT_STATE.md` và `docs/DEBUG_LOG.md`.
