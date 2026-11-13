# AI Handoff — Che779 Project 1

> Đọc file này trước khi tiếp tục công việc ở Project 1. Đây là bản tóm tắt chuyển giao; source code, workflow và các tài liệu kỹ thuật liên kết bên dưới mới là nguồn kiểm chứng.

## Chủ sở hữu và cách làm việc

- Chủ sở hữu/direction owner: Cherry.
- Project 1 phải được Cherry duyệt trước khi bắt đầu Project 2.
- Không tự ý thay đổi tính năng hoặc hành vi người dùng khi chỉ đang tái cấu trúc tài liệu.
- Không lấy README cũ làm nguồn sự thật; đối chiếu source, build và test.
- Ghi lại mọi quyết định ảnh hưởng kiến trúc vào `DECISIONS.md`.
- Sau mỗi thay đổi, cập nhật `CHANGELOG.md`, `PROJECT_STATE.md` và các tài liệu bị ảnh hưởng.
- Phân biệt rõ: đã đọc source, đã build, đã test runtime, đã được Cherry duyệt. Không đánh đồng các trạng thái này.

## Nhận diện Project 1

- Một repository gồm hai app phối hợp: Che779 Emulator V1 (Java) và Che779 Remoter (C# WinForms).
- Emulator mỗi game một JVM riêng; Remoter quản lý process và điều khiển qua TCP loopback JSON Lines.
- Baseline app version: `0.0.0.2 custom`.
- Emulator source: `apps/emulator/source/`.
- Remoter source: `apps/remoter/source/`.
- IPC contract: `docs/IPC_PROTOCOL.md`.

## Đầu vào đã dùng

- `Che779_Emulator_0.0.0.2_custom.zip`.
- `Che779_Remoter_0.0.0.2_custom.zip`.
- Independent review: `Che779_Danh_Gia_Doc_Lap_va_So_Sanh.zip`, mục Project 1.
- README/guide cũ được giữ tại `apps/emulator/LEGACY_HUONG_DAN.md`, chỉ dùng tham khảo lịch sử.

## Trạng thái đã xác minh trong revision đầu

- Emulator build thành công bằng OpenJDK 21; build script nhắm bytecode Java 8 (`--release 8`).
- JAR build mới có cùng 565 entry và nội dung entry giống JAR baseline; khác biệt hash file ZIP do metadata/đóng gói.
- Kiểm tra tĩnh `python tests/verify_project1.py --jar ...` đã pass.
- Remoter chưa build được trong môi trường Linux hiện tại vì không có .NET/Windows compiler. CI Windows được thêm nhưng chưa chạy trong GitHub của Cherry.
- Chưa có runtime integration test trên Windows.

## Những điều không được quên

- `ping` đã tồn tại; không đề xuất thêm lần nữa.
- Port collision detection đã tồn tại ở cả hai phía; cần xử lý nhánh cạn kiệt/race, không tuyên bố không có dò port.
- MouseSync dùng tọa độ tỷ lệ; không nói cứng là bắt buộc cùng kích thước. Hạn chế nằm ở mapping theo cửa sổ ngoài và khác biệt canvas/viền/aspect ratio.
- IPC hiện không có authentication; parser JSON là regex; server chưa giới hạn read timeout/client/line length.
- Remoter hook có nguy cơ đọc WinForms controls từ thread hook.
- Settings có thể nuốt lỗi load/save.
- Không khẳng định các mục này đã sửa; chúng vẫn là backlog ở `docs/DEBUG_LOG.md`.

## Việc tiếp theo

1. Cherry xem và duyệt cấu trúc/ZIP.
2. Khi có GitHub Windows Actions hoặc máy Windows, xác minh build Remoter.
3. Chạy package tích hợp, test Remoter mở Emulator và gửi lệnh IPC.
4. Chỉ sau khi có kết quả mới quyết định sửa các lỗi P1/P2; thay đổi protocol phải cập nhật cả hai app và test.
5. Chờ Cherry duyệt Project 1 trước khi bắt đầu Project 2.

## Lệnh kiểm tra

```bash
python tests/verify_project1.py
cd apps/emulator/source && ./build.sh
cd ../../.. && python tests/verify_project1.py --jar apps/emulator/source/Che779_Emulator.jar
```

Trên Windows:

```powershell
dotnet build apps/remoter/source/Che779Remoter.csproj -c Release
```
