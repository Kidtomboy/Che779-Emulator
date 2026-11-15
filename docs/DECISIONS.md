# Technical Decisions

## D-001 — Một repository cho hai ứng dụng Project 1

- **Quyết định:** Emulator V1 và Remoter nằm trong cùng repository.
- **Lý do:** hai app tạo thành một sản phẩm; Remoter phụ thuộc giao thức và cách khởi chạy Emulator.
- **Cách thực hiện:** `apps/emulator/` và `apps/remoter-windows/` tách source/build riêng; tài liệu IPC ở cấp repo.

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

## Quyết định 15/11/2026: chia lại 4 repo theo nền tảng (thực hiện ở 1.3.0)
Do Cherry quyết, Claude ghi lại. Áp dụng cho cả 4 repo; Claude và Darling không làm ngược lại khi chưa hỏi Cherry.

**Câu nhớ:** *1 và 2 = Windows · 3 = Linux · 4 = Android (kèm Termux).*

| Repo | Nền tảng | Chứa | Phát hành (GitHub Actions tự build) |
|---|---|---|---|
| P1 Che779-Emulator | Windows | Mã V1 + Remoter Windows (C#), file build `.bat/.cmd/.ps1`, docs, test | zip Windows |
| P2 Che779-EmulatorV2 | Windows | Mã V2, file build `.bat/.cmd/.ps1`, docs, test | zip Windows |
| P3 Che779-Linux | Linux (bỏ Termux) | Script/gói Linux, **Remoter Linux** (chuyển từ P1), docs, test; bản Linux của V1 và V2 | `.deb`, `.tar.gz` (giữ `che779.sh` tạm đến khi hai cái này ổn); `.rpm` để sau |
| P4 Che779-Android | Android + Termux | Mã APK (Gradle) + bản clone; thư mục `termux/` (script Termux chuyển từ P3, **chỉ V2**) | APK, APK clone, `che779-termux.sh` |

**Quy tắc kèm theo**
1. **Mã Java Emulator chỉ có một chỗ:** V1 ở P1, V2 ở P2. P3 và P4 **không chép mã**, chỉ **ghim commit** (file `EMULATOR_V1_REF` / `EMULATOR_V2_REF`) và Actions kéo về build.
2. Termux chỉ chạy **V2** (Cherry thấy Termux khó thao tác). **V1 cho Termux** để trong thư mục `test/` của P4 cho ai muốn trải nghiệm, không cam kết.
3. Gói Linux dùng **gói chính thức của distro** cho Java; `.deb` khai báo phụ thuộc Java thay vì tải tay. Không làm AppImage/Flatpak/Snap (quá phức tạp).
4. Mọi README mở đầu bằng dòng "Nền tảng: …" và cùng một bảng 4 repo.
5. **Giữ nguyên tên repo** (đổi tên làm hỏng link cũ).
6. **Thứ tự:** (a) tạo tag `v1.2.0` cho cả 4 repo như hiện có (Cherry tạo tay vì Claude không đẩy được tag); (b) hoàn thiện Remoter Linux ở P1 (Xem lưới; Cherry/Darling thử bản Linux thật); (c) bản **1.3.0** mới chia lại: chuyển Remoter Linux sang P3, thêm `.deb`/`.tar.gz`, tách Termux sang P4 (`termux/`), dọn P1/P3, sửa README/docs/workflow, cập nhật `che779-projects`.
7. Phiên bản và mọi quy ước 1.2.0 (tiếng Việt, CHANGELOG chỉ ghi bản hiện tại, SHA256SUMS tính sau cùng trên file cuối, ghi rõ đã/chưa thử) vẫn áp dụng.

## Quyết định của Cherry (15/11/2026): chia repo theo nền tảng — áp dụng từ bản 1.3.0

**Câu để nhớ: P1 và P2 = Windows · P3 = Linux · P4 = Android (kèm Termux).**

| Repo | Nền tảng | Chứa | Cách ra file cho người dùng |
|---|---|---|---|
| P1 Che779-Emulator | Windows | Mã nguồn V1 + Remoter Windows, file build `.bat/.cmd/.ps1` | GitHub Actions build, đóng zip + `SHA256SUMS.txt` |
| P2 Che779-EmulatorV2 | Windows | Mã nguồn V2, file build `.bat/.cmd/.ps1` | Như trên |
| P3 Che779-Linux | Linux (bỏ Termux) | Mã riêng: script, Remoter Linux; V1/V2 build từ P1/P2 | Actions tạo `.deb` + `.tar.gz` (+ `.sh` giữ tạm) + `SHA256SUMS.txt` |
| P4 Che779-Android | Android | Gradle/APK, bản clone, thư mục `termux/` (chỉ V2) | Actions tạo APK, APK clone, `che779-termux.sh` + `SHA256SUMS.txt` |

1. **Không chép mã Emulator V1/V2 sang repo khác.** P3 và P4 ghim commit của P1/P2 (như `apps/linux/EMULATOR_V2_REF`) rồi build trong Actions.
2. **Remoter Linux chuyển từ P1 sang P3** (ghim V1 từ P1). P1 chỉ còn Windows.
3. **Termux chỉ chạy V2**, nằm ở P4 (`termux/`). V1 cho ai muốn thử đặt ở thư mục `test/` (không hỗ trợ chính thức). P3 bỏ Termux.
4. **Linux đóng gói:** `.deb` (Ubuntu/Mint/Debian, phụ thuộc gói Java chính thức) + `.tar.gz` chạy ngay; `.rpm` để sau; **không** AppImage/Flatpak/Snap. `.sh` một file giữ đến khi hai cách trên ổn.
5. **`SHA256SUMS.txt` có ở mọi bản phát hành của cả 4 repo**, tính SAU CÙNG trên đúng các file sẽ đăng.
6. **Thứ tự:** (a) Cherry tạo tag `v1.2.0` cho cả 4 repo như hiện có; (b) hoàn thiện Remoter Linux (Xem lưới) và nhờ chạy thử Linux thật; (c) 1.3.0 mới chia lại. Không đổi tên repo.
7. Mỗi README bắt đầu bằng dòng "Nền tảng: …" và bảng 4 repo giống nhau; mô tả repo trên GitHub ghi dạng "[Windows] …", "[Linux] …", "[Android] …".

- **Việc của repo này ở 1.3.0:** bỏ `apps/remoter-linux` (chuyển sang P3); giữ `apps/emulator`, `apps/remoter-windows`; cập nhật README/docs/workflow, `tests/verify_project1.py`.
