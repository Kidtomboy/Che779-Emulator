# Trạng thái dự án — Che779 Emulator + Remoter

**Phiên bản:** `1.2.0` (file `VERSION`)

## Thành phần

| Thành phần | Công nghệ | Thư mục | Trạng thái |
|---|---|---|---|
| Emulator | Java / Swing / MicroEmulator 3.0.0 | `apps/emulator` | Chủ dự án xác nhận chạy tốt trên Windows; Claude đã chạy trên Linux |
| Remoter Windows | C# / Windows Forms / .NET Framework 4.8 | `apps/remoter-windows` | Chủ dự án xác nhận chạy tốt trên Windows; Claude chưa build được (không có trình biên dịch C#) |
| Remoter Linux | Java / Swing | `apps/remoter-linux` | Lõi và giao diện đã chạy thử dưới Xvfb với game thật |
| IPC | TCP loopback + JSON một dòng | — | 17 lệnh, xem `IPC_PROTOCOL.md` |

## Đã có

- Phát hành tự động: gắn thẻ `vX.Y.Z` → zip Windows, zip Linux, `SHA256SUMS.txt`.
- CI kiểm tra cấu trúc và build cả 3 thành phần.
- Bộ file build đủ cho cả hai hệ: `.sh` (Linux), `.bat`/`.cmd` và `.ps1` (Windows).

## Chưa kiểm chứng

Xem danh sách đầy đủ ở [TESTING](TESTING.md). Tóm tắt: workflow Phát hành chưa chạy thật, các script Windows mới chưa chạy trên Windows, Remoter Linux chưa thử với trình quản lý cửa sổ thật hoặc nhiều tab.

## Điều kiện để coi là hoàn tất

1. Workflow Phát hành chạy xanh và zip chạy được trên máy sạch.
2. Remoter Linux thử xong trên một máy Linux có màn hình thật.
3. Rủi ro bảo mật mức cao (IPC không xác thực, mật khẩu proxy) được xử lý hoặc chủ dự án chấp nhận có ghi lại.

## Nguồn sự thật

Mã nguồn và commit là bằng chứng về cách làm; log CI là bằng chứng về build. File này chỉ tóm tắt — khi mâu thuẫn, tin mã nguồn và CI.
