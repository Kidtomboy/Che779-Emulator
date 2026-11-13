# Testing

## Kiểm tra tĩnh repository

Từ root:

```bash
python tests/verify_project1.py
```

Nếu muốn kiểm tra JAR vừa build:

```bash
python tests/verify_project1.py --jar apps/emulator/source/Che779_Emulator.jar
```

## Kiểm thử cần có trên Windows

- Build Remoter bằng MSBuild và `csc.exe` (hai đường build nếu còn hỗ trợ cả hai).
- Khởi chạy Remoter, chọn Java/JAR/game và mở một phiên.
- Mở nhiều phiên; kiểm tra port assignment và thông tin `CHE779_PORT`.
- Gửi `ping`, `status`, `version`, keypress, text, sleep, show/hide và quit.
- Dừng/crash một JVM và xác minh Remoter cập nhật trạng thái đúng.
- Kiểm tra proxy trước và sau khi mở phiên.
- Kiểm tra mouse sync với cửa sổ khác kích thước/aspect ratio.
- Kiểm tra screenshot path và lỗi quyền ghi.
- Chạy thử trên máy Windows sạch, không có source tree.

## Giới hạn

Kiểm tra tĩnh không thay thế runtime integration test. Không đánh dấu test Windows là passed nếu chưa có log hoặc kết quả thực tế.
