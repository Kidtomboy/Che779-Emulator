# Project State — Che779 Project 1

**Baseline ứng dụng:** `0.0.0.2 custom`  
**Ngày lập hồ sơ:** 2026-11-12  
**Nguồn đầu vào:** `Che779_Emulator_0.0.0.2_custom.zip`, `Che779_Remoter_0.0.0.2_custom.zip` và đánh giá độc lập Project 1.

## Mục tiêu

Duy trì Che779 Emulator V1 và Che779 Remoter như hai ứng dụng riêng nhưng phối hợp thành một sản phẩm trong cùng repository.

## Thành phần

| Thành phần | Công nghệ | Trạng thái source |
|---|---|---|
| Emulator V1 | Java / Swing / MicroEmulator 3.0.0 | Đã nhập source baseline |
| Remoter | C# / Windows Forms / .NET Framework 4.8 | Đã nhập source baseline |
| IPC | TCP loopback + JSON Lines | Đã ghi tài liệu hợp đồng hiện tại |

## Đã thực hiện trong revision tái cấu trúc

- [x] Gom hai app vào cùng repository.
- [x] Tách source Emulator và Remoter.
- [x] Giữ JAR đầu vào làm binary baseline riêng.
- [x] Viết lại README theo cấu trúc và source hiện có, không lấy README cũ làm sự thật tuyệt đối.
- [x] Ghi tài liệu kiến trúc, IPC, build, test, debug, quyết định và roadmap.
- [x] Thêm kiểm tra tĩnh repository.
- [x] Thêm workflow build riêng hai ứng dụng và đóng gói artifact chung.
- [x] Lập AI handoff và kiểm kê hash baseline để chuyển giao ngữ cảnh.
- [x] Build Emulator bằng JDK 21 với `--release 8` thành công.

## Chưa xác minh

- [ ] Build Remoter bằng MSBuild/.NET Framework trên Windows.
- [ ] Chạy Remoter và mở Emulator từ Remoter trên Windows.
- [ ] Test vòng đời start/stop/crash/restart của nhiều JVM.
- [ ] Test IPC thực tế cho tất cả action.
- [ ] Test proxy, mouse sync và screenshot với game thật.
- [ ] Test package ZIP trên máy Windows sạch.

## Điều kiện để đánh dấu Project 1 hoàn tất

1. CI build thành công cả Emulator và Remoter.
2. Gói phát hành chứa `Che779_Remoter.exe` và `Che779_Emulator.jar` cạnh nhau.
3. Remoter mở được ít nhất một phiên Emulator và điều khiển được qua IPC.
4. Các kiểm thử tối thiểu về IPC, đóng tiến trình và đường dẫn game/JAR đạt.
5. Các rủi ro bảo mật và thread-safety mức cao được xử lý hoặc Cherry chấp thuận bằng quyết định có ghi nhận.
6. README, changelog và project state phản ánh đúng release được xác minh.

## Nguồn sự thật

- Source code và commit là bằng chứng về implementation.
- CI logs và test reports là bằng chứng về build/test.
- File này chỉ tóm tắt trạng thái; khi mâu thuẫn phải kiểm tra source và workflow thực tế.
