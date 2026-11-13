# IPC Protocol — hiện trạng 0.0.0.2 custom

## Transport

- TCP, chỉ bind `127.0.0.1`.
- Một thông điệp JSON trên một dòng UTF-8; một dòng phản hồi JSON tương ứng.
- Client: `apps/remoter/source/IpcClient.cs`.
- Server: `apps/emulator/source/src/com/che779/emu/IpcServer.java`.
- Client timeout connect 800 ms, send/receive 1500 ms và thử tối đa hai lần theo source hiện tại.
- Server dò từ `basePort` đến `basePort + 199`.

## Action server hiện hỗ trợ

| Action | Tham số chính | Mục đích |
|---|---|---|
| `keypress` | `key`, `hold` | Nhấn và nhả phím |
| `keydown` | `key` | Giữ phím xuống |
| `keyup` | `key` | Nhả phím |
| `text` | `text` hoặc `value` | Nhập văn bản |
| `clean_ram` | — | Chạy tác vụ Clean RAM bất đồng bộ |
| `sleep` | `state` | Bật/tắt trạng thái sleep |
| `hide` | — | Ẩn cửa sổ |
| `show` | — | Hiện cửa sổ |
| `mouse` | `type`, `fx`, `fy` | Gửi sự kiện chuột theo tọa độ tỷ lệ |
| `proxy` | `mode`, `host`, `port`, `user`, `pass`, `type` | Đặt proxy override hoặc none |
| `proxy_get` | — | Lấy mô tả proxy đang hoạt động |
| `ping` | — | Kiểm tra phản hồi IPC |
| `status` | — | Trạng thái PID, port, sleep và version |
| `screenshot` | `path` | Ghi screenshot tới đường dẫn được cung cấp |
| `version` | — | Lấy version ứng dụng |
| `quit` / `close` | — | Kết thúc tiến trình Emulator |

## Ví dụ trao đổi

Request:

```json
{"action":"ping"}
```

Response:

```json
{"ok":true}
```

## Hành vi parser hiện tại

`IpcServer.parse()` dùng regex để trích key/value. `unescape()` xử lý một phần escape nhưng không tương đương parser JSON chuẩn. Không gửi JSON lồng nhau hoặc chuỗi có cấu trúc phức tạp; schema thực tế hiện tại là object phẳng với giá trị scalar.

## Rủi ro và yêu cầu khi mở rộng

1. Chưa có authentication/token. Loopback không phơi IPC trực tiếp ra LAN, nhưng tiến trình cục bộ khác có thể thử kết nối nếu biết cổng.
2. `screenshot.path` là đường dẫn do client cung cấp và có thể tạo/ghi đè file theo quyền của tiến trình.
3. Server không đặt read timeout (`setSoTimeout(0)`), không giới hạn độ dài dòng hoặc số client.
4. Cần thống nhất protocol version riêng với app version trước khi mở rộng giao thức.
5. Thay đổi action phải cập nhật cả server, client, tài liệu và test tương ứng.
6. Không thêm `ping` lần nữa; action này đã tồn tại.

## Quy tắc tương thích

- Không xóa/đổi tên action hiện có mà không có phương án tương thích.
- Mỗi thay đổi protocol cần ghi `DECISIONS.md`, cập nhật bảng action và test.
- Remoter phải xử lý timeout, response null và lỗi `ok:false` rõ ràng.
