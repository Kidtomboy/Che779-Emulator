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
- Baseline app version: `1.3.0`.
- Emulator source: `apps/emulator/source/`.
- Remoter source: `apps/remoter-windows/source/`.
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
- IPC chưa có authentication; đây vẫn là rủi ro bảo mật. `IpcServer` hiện đã có timeout 5 giây, giới hạn 16 client và 65.536 ký tự/dòng.
- `MainForm.Sync.cs` hiện marshal hook callback về UI thread bằng `BeginInvoke`; cần xác nhận bằng build/runtime Windows.
- `Settings.LoadFrom` / `SaveTo` vẫn catch rồi bỏ qua exception; đây là mục cần xử lý trong Issue #2.
- Không khẳng định mục nào đã sửa runtime nếu chưa có kết quả build/test Windows; đối chiếu `docs/DEBUG_LOG.md`.

## Việc tiếp theo

1. Cherry xem và duyệt cấu trúc/ZIP.
2. Khi có GitHub Windows Actions hoặc máy Windows, xác minh build Remoter.
3. Chạy package tích hợp, test Remoter mở Emulator và gửi lệnh IPC.
4. Chỉ sau khi có kết quả mới quyết định sửa các lỗi P1/P2; thay đổi protocol phải cập nhật cả hai app và test.
5. Chờ Cherry duyệt Project 1 trước khi bắt đầu Project 2.

## Lệnh kiểm tra

```bash
python tests/verify_project1.py
bash .github/scripts/build-emulator.sh   # hoặc apps/emulator/source/build_emulator.ps1 trên Windows
cd ../../.. && python tests/verify_project1.py --jar apps/emulator/source/Che779_Emulator.jar
```

Trên Windows:

```powershell
dotnet build apps/remoter-windows/source/Che779Remoter.csproj -c Release
```


## Audit độc lập 1.3.0 — 2026-11-16

Darling đã rà soát source và tài liệu ở revision hiện tại. Danh sách phát hiện, phân loại mức độ xác minh và phạm vi được phép làm nằm trong [AI_CHANNEL.md](AI_CHANNEL.md) (ghi chú mới nhất ở cuối) và GitHub Issue: https://github.com/Kidtomboy/Che779-Emulator/issues/2.

**Quy tắc bàn giao:** audit/Issue không phải quyền tự động sửa source. Claude phải chờ Cherry duyệt từng giải pháp/phạm vi; trước khi code cần kiểm tra lại main vì source có thể thay đổi. Sau khi sửa, cập nhật DEBUG_LOG, TESTING, PROJECT_STATE và AI_CHANNEL với bằng chứng build/runtime.

## Đối chiếu version — 2026-11-23 (đồng bộ theo yêu cầu Cherry)
| Nguồn | Giá trị |
|---|---|
| `VERSION` / `PROJECT_STATE.md` | 1.3.0 |
| HEAD `main` | `7dbe80855b8a507e295ff7c7ae68f3ac7e5ef3ca` |
| Tag | `v1.3.0` → `d0c8ba5`; `v1.2.0` → `ac7a81d` |
| Release | Release `v1.3.0`, `v1.2.0` (target `main`) |
- Chưa cài/chạy artifact thực tế: `NOT_RUN`. Mức kiểm tra ở đây chỉ là đối chiếu metadata Git/GitHub (`STATIC_REVIEW`).
- Mọi chênh lệch (nếu có) giữ nguyên là version inconsistency cho đến khi Cherry/Darling xác định nguồn chuẩn; Claude không tự sửa version hay dời tag.
