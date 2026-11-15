# Trạng thái dự án — Che779 Emulator + Remoter

**Phiên bản:** `1.3.0` (file `VERSION`)

## Thành phần

| Thành phần | Công nghệ | Thư mục | Trạng thái |
|---|---|---|---|
| Emulator | Java / Swing / MicroEmulator 3.0.0 | `apps/emulator` | Chủ dự án xác nhận chạy tốt trên Windows; Claude đã chạy trên Linux |
| Remoter Windows | C# / Windows Forms / .NET Framework 4.8 | `apps/remoter-windows` | Chủ dự án xác nhận chạy tốt trên Windows; Claude chưa build được (không có trình biên dịch C#) |
| IPC | TCP loopback + JSON một dòng | — | 17 lệnh, xem `IPC_PROTOCOL.md` |

## Đã có

- Phát hành tự động: gắn thẻ `vX.Y.Z` → zip Windows + `SHA256SUMS.txt` (build trên Windows).
- CI kiểm tra cấu trúc và build cả 3 thành phần.
- Bộ file build Windows: `.bat`/`.cmd` và `.ps1`. Repo này **chỉ Windows**; bản Linux ở Che779-Linux.

## Chưa kiểm chứng

Xem danh sách đầy đủ ở [TESTING](TESTING.md). Tóm tắt: workflow Phát hành chưa chạy thật, các script Windows mới chưa chạy trên Windows.

## Điều kiện để coi là hoàn tất

1. Workflow Phát hành chạy xanh và zip chạy được trên máy sạch.
3. Rủi ro bảo mật mức cao (IPC không xác thực, mật khẩu proxy) được xử lý hoặc chủ dự án chấp nhận có ghi lại.

## Nguồn sự thật

Mã nguồn và commit là bằng chứng về cách làm; log CI là bằng chứng về build. File này chỉ tóm tắt — khi mâu thuẫn, tin mã nguồn và CI.
