<div align="center">

# 🎮 Che779 Emulator + Remoter

**Giả lập J2ME cho Windows: mỗi tab là một cửa sổ game riêng, điều khiển tập trung bằng Remoter.**

> **Nền tảng: Windows.** Quy ước 4 repo: **P1 và P2 = Windows · P3 = Linux + Termux · P4 = Android**. Bản Linux (kể cả Remoter Linux) và Termux nằm ở [Che779-Linux](https://github.com/Kidtomboy/Che779-Linux).

Emulator (Java) chạy từng game trong một tiến trình riêng; Remoter mở, sắp xếp và đồng bộ tất cả các cửa sổ đó.
Remoter viết bằng C# (WinForms) cho Windows.

[![Phiên bản](https://img.shields.io/github/v/release/Kidtomboy/Che779-Emulator?label=phi%C3%AAn%20b%E1%BA%A3n&color=blue)](../../releases/latest)
[![Lượt tải](https://img.shields.io/github/downloads/Kidtomboy/Che779-Emulator/total?label=l%C6%B0%E1%BB%A3t%20t%E1%BA%A3i&color=brightgreen)](../../releases)
[![CI](https://img.shields.io/github/actions/workflow/status/Kidtomboy/Che779-Emulator/ci.yml?branch=main&label=CI)](../../actions)
[![Windows](https://img.shields.io/badge/Windows-0078D6?logo=windows&logoColor=white)](#-tải-về)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)](#-yêu-cầu-hệ-thống)
[![Giấy phép](https://img.shields.io/badge/giấy%20phép-Apache%202.0-orange)](LICENSE)

[Tải về](#-tải-về) · [Tính năng](#-tính-năng) · [Bắt đầu nhanh](#-bắt-đầu-nhanh) · [Hướng dẫn](#-hướng-dẫn-sử-dụng) · [Hỏi đáp](#-hỏi-đáp-và-xử-lý-lỗi) · [Build](#-build-từ-mã-nguồn) · [Tài liệu](#-tài-liệu) · [Ghi công](#-ghi-công-và-giấy-phép)

</div>

---

## 📥 Tải về

Vào mục **[Releases](../../releases/latest)** và tải file Windows:

| Hệ điều hành | File cần tải | Bên trong có gì |
|---|---|---|
| 🪟 **Windows** | `Che779-Emulator-<phiên bản>-Windows.zip` | `Che779_Emulator.jar` + `Che779_Remoter.exe` |

Giải nén ra **một thư mục**, giữ hai file cạnh nhau. Mỗi bản phát hành có kèm `SHA256SUMS.txt` để bạn kiểm tra file tải về có nguyên vẹn không:

```bash
Get-FileHash <file>.zip -Algorithm SHA256   # Windows PowerShell
```

> Chưa thấy file nào ở Releases? Bản phát hành được tạo tự động khi chủ dự án gắn thẻ phiên bản. Trong lúc đó bạn có thể [tự build](#-build-từ-mã-nguồn).

---

## 🌐 Hệ sinh thái Che779

Che779 gồm 4 dự án, mỗi dự án một repo. Bạn đang xem **Project 1**.

| # | Dự án | Chạy trên | Dùng khi nào |
|:-:|---|---|---|
| 1 | **Che779-Emulator** ← *bạn đang ở đây* | Windows | Mỗi tab một cửa sổ, điều khiển bằng Remoter |
| 2 | [**Che779-EmulatorV2**](https://github.com/Kidtomboy/Che779-EmulatorV2) | Windows | Nhiều tab trong một cửa sổ, một tiến trình |
| 3 | [**Che779-Linux**](https://github.com/Kidtomboy/Che779-Linux) | Linux + Termux | Gói `.deb` / `.tar.gz` cho Linux, gồm V1 + V2 + Remoter Linux; script Termux (Android) |
| 4 | [**Che779-Android**](https://github.com/Kidtomboy/Che779-Android) | Android | Chạy trực tiếp trên điện thoại bằng APK |

> 📌 Tên repo này từng là tên của bản Android. Bản Android nay nằm ở **Che779-Android**; các link cũ vẫn tự chuyển hướng.

**Nên chọn Project 1 hay Project 2?** Project 1 cho mỗi game một tiến trình riêng nên một game treo không kéo sập game khác, đổi lại tốn RAM hơn. Project 2 gom mọi tab vào một cửa sổ nên nhẹ và gọn hơn.

---

## ✨ Tính năng

### Emulator (Java)

| | Tính năng | Mô tả |
|:-:|---|---|
| 🧹 | **Clean RAM** | Dọn bộ nhớ qua JNA; không có JNA thì dùng PowerShell (Windows) |
| 😴 | **Sleep (F12)** | Ngủ thủ công hoặc tự ngủ khi rảnh |
| 🎞️ | **Giảm FPS, giảm đồ họa** | Tiết kiệm CPU khi treo nhiều game |
| ⏩ | **Tốc độ game** | Chỉnh theo bước 0,1 |
| 🔀 | **Thay IP máy chủ** | `server.txt` trong jar game → máy chủ chọn từ `servers.txt` (`ip\|port\|tên`, có thể tải từ URL) → tự nhập |
| 🌐 | **Quản lý Proxy** | HTTP (có xác thực) và SOCKS5 |
| 🔌 | **IPC** | Cho chương trình khác điều khiển qua TCP `127.0.0.1` |

### Remoter Windows (C#)

| | Tính năng | Mô tả |
|:-:|---|---|
| 🗂️ | **Quản lý nhiều tab** | Mở, dừng, mở lại; bảng theo dõi cổng, CPU, RAM, trạng thái ngủ, PID |
| 🔲 | **Tự sắp xếp** | Xếp cửa sổ theo lưới (chọn số cột, hàng); Hiện lại từng tab về đúng vị trí cũ |
| ⌨️ | **Đồng bộ phím + bàn phím ảo** | Một phím nhấn được nhân ra các tab |
| 🖱️ | **Đồng bộ chuột** | Nhấp và kéo ở một tab được nhân ra các tab khác, theo tọa độ tỉ lệ |
| ⏺️ | **Macro phím** | Ghi rồi phát lặp |
| 📸 | **Chụp màn hình** | Chụp từng tab |
| ⏱️ | **Hẹn giờ, tự mở lại** | Hẹn giờ đóng tab; tự mở lại tab khi thoát |
| 💾 | **Hồ sơ `.che779`** | Lưu và nạp toàn bộ cấu hình; tách dữ liệu từng tab; giao diện tối |

---

## 🖥️ Yêu cầu hệ thống

| | Windows |
|---|---|
| Hệ điều hành | Windows có .NET Framework 4.8 |
| Java | **Java 8 trở lên** (tải từ [Adoptium](https://adoptium.net)) |
| RAM | Tùy game; mỗi tab là một JVM nên cần nhiều RAM hơn Project 2 (đặt RAM tối đa mỗi tab trong Remoter) |

Dùng Linux? Xem [Che779-Linux](https://github.com/Kidtomboy/Che779-Linux).

---

## 🚀 Bắt đầu nhanh

1. Tải `…-Windows.zip`, giải nén ra một thư mục.
2. Bấm đúp **`Che779_Remoter.exe`**.
3. Chọn file game `.jar`, đặt số tab, bấm **Bắt đầu**.

---

## 📖 Hướng dẫn sử dụng

### Remoter Windows
1. **Chọn Java và game:** nút **Duyệt** chọn `javaw.exe`/`java.exe` và file game `.jar`; **Thêm game (.jar)** để chạy nhiều game khác nhau.
2. **Bắt đầu:** đặt số tab và RAM tối đa mỗi tab. Bảng danh sách hiện trạng thái từng tab.
3. **Hành động:** Hiện/Ẩn, Ngủ, dừng, mở lại từng tab hoặc cả nhóm.
4. **Đồng bộ:** bật Đồng bộ Phím hoặc Đồng bộ Chuột để thao tác một tab lặp lại ở các tab khác. Phím thật dùng được: mũi tên, Enter = OK, F1/F2 = phím mềm, 0–9, A–Z, Space, Backspace.
5. **Hồ sơ:** lưu cấu hình thành file `.che779` để nạp lại sau (phải dừng hết tab trước khi nạp).

---

## ⚙️ Cấu hình và nơi lưu dữ liệu

| | Vị trí (Windows) |
|---|---|
| Dữ liệu Emulator (cấu hình, proxy, máy chủ, RMS) | `%APPDATA%\Che779\Che779-Emulator` |
| Cấu hình Remoter | `%APPDATA%\Che779\Che779-Remoter` |

Dữ liệu **không** ghi vào thư mục bạn giải nén, nên cập nhật bản mới không mất dữ liệu. Cần JNA cho Clean RAM sâu hơn? Đặt `jna.jar` vào `lib/` cạnh `Che779_Emulator.jar` (tùy chọn).

---

## 🧩 Cách hoạt động

```mermaid
flowchart LR
    R["🎛️ Remoter<br/>Windows (C#)"] -- "IPC · TCP 127.0.0.1<br/>JSON một dòng" --> E1["Emulator<br/>game 1 · một JVM"]
    R --> E2["Emulator<br/>game 2 · một JVM"]
    R --> E3["Emulator<br/>game N · một JVM"]
```

Mỗi game chạy trong một tiến trình Java riêng, nên một game treo không ảnh hưởng game khác. Remoter nói chuyện với từng Emulator qua cổng TCP chỉ mở trên `127.0.0.1` (mặc định từ **7790**). Mỗi lệnh là một dòng JSON và nhận lại một dòng trả lời. Các lệnh: `ping`, `status`, `version`, `keypress`, `keydown`, `keyup`, `text`, `mouse`, `hide`, `show`, `sleep`, `clean_ram`, `proxy`, `proxy_get`, `screenshot`, `quit`, `close`. Chi tiết: [`docs/IPC_PROTOCOL.md`](docs/IPC_PROTOCOL.md).

Vì IPC là TCP + JSON thuần, bạn có thể tự viết công cụ điều khiển bằng ngôn ngữ khác.

---

## 🔧 Build từ mã nguồn

Cần JDK 9 trở lên (build ra bytecode Java 8).

| Thành phần | Windows |
|---|---|
| Emulator | `apps/emulator/source/build_emulator.bat` hoặc `build_emulator.ps1` |
| Remoter Windows | `apps/remoter-windows/source/build_remoter.bat` / `.ps1`, hoặc `dotnet build apps/remoter-windows/source/Che779Remoter.csproj -c Release` |

(`.github/scripts/build-emulator.sh` chỉ để CI và repo Che779-Linux build jar, không dành cho người dùng Windows.)

Đóng gói bản Windows ngay trên máy bạn: `scripts/package-windows.ps1`. Kiểm tra cấu trúc repo: `python tests/verify_project1.py`. Chi tiết: [`docs/BUILD_AND_RUN.md`](docs/BUILD_AND_RUN.md).

Chủ dự án phát hành bằng cách gắn thẻ `vX.Y.Z`: workflow **Phát hành** tự build, đóng gói zip và tạo `SHA256SUMS.txt`.

---

## ❓ Hỏi đáp và xử lý lỗi

| Bạn gặp | Làm gì |
|---|---|
| Remoter báo *"Chưa chọn đúng file Che779_Emulator.jar"* | Để `Che779_Emulator.jar` cùng thư mục với Remoter, hoặc chọn bằng nút **Duyệt**. |
| Không mở được game | Kiểm tra đã cài Java 8+ và đã chọn đúng `javaw.exe`/`java` trong Remoter. |
| Tab báo "Không phản hồi" | Thử **Mở lại**. |
| Game không vào được mạng | Thử đổi máy chủ hoặc đặt proxy trong menu **Che779** của Emulator. Lỗi mạng còn có thể do chính máy chủ game. |
| Muốn báo lỗi | Mở [Issue](../../issues/new/choose), ghi phiên bản (nút **Phiên bản** trong Remoter), hệ điều hành và Java. Xóa mật khẩu proxy khỏi log. |

---

## ⚠️ Giới hạn và vấn đề đã biết

- Remoter Windows chỉ chạy trên Windows (dùng WinForms và API Windows).
- Mỗi tab là một JVM nên **tốn RAM hơn** Project 2.
- **IPC chưa có xác thực:** tiến trình khác trên cùng máy biết cổng thì gửi được lệnh. Không mở cổng này ra mạng. Xem [`SECURITY.md`](SECURITY.md).
- Mật khẩu proxy được lưu và truyền dạng chữ thường.
- Clean RAM trên Windows chỉ thu nhỏ vùng nhớ hiển thị của tiến trình, không giải phóng bộ nhớ Java thật.
- Đồng bộ chuột của Remoter Windows theo tọa độ cả cửa sổ; các tab nên cùng kích thước để trùng vị trí.

Phần nào đã kiểm chứng, phần nào chưa: [`docs/TESTING.md`](docs/TESTING.md).

---

## 🗂️ Cấu trúc repo

```
Che779-Emulator/
├─ README.md  CONTRIBUTING.md  SECURITY.md  LICENSE  CHANGELOG.md  VERSION
├─ apps/
│  ├─ emulator/          Emulator (Java) — source/ và prebuilt/
│  └─ remoter-windows/   Remoter Windows (C#) — source/
├─ docs/                 Tài liệu kỹ thuật (có mục lục)
├─ scripts/              Đóng gói bản Windows (package-windows.ps1)
├─ tests/                Kiểm tra cấu trúc repo, kiểm thử IPC
└─ .github/              Workflow CI + Phát hành, mẫu Issue và Pull Request
```

---

## 📚 Tài liệu

[Mục lục](docs/README.md) · [Trạng thái](docs/PROJECT_STATE.md) · [Kiến trúc](docs/ARCHITECTURE.md) · [IPC](docs/IPC_PROTOCOL.md) · [Build và chạy](docs/BUILD_AND_RUN.md) · [Kiểm thử](docs/TESTING.md) · [Lỗi đã biết](docs/DEBUG_LOG.md) · [Quyết định](docs/DECISIONS.md) · [Lộ trình](docs/ROADMAP.md) · [Fork và bản tự build](docs/VARIANTS_AND_CLONES.md) · [Nguồn gốc mã](docs/SOURCE_INVENTORY.md) · [Bàn giao](docs/AI_HANDOFF.md)

---

## 🤝 Đóng góp và bảo mật

Muốn góp sức? Đọc [`CONTRIBUTING.md`](CONTRIBUTING.md). Tìm thấy lỗ hổng bảo mật? Xem [`SECURITY.md`](SECURITY.md) và báo riêng, đừng đăng công khai.

---

## 🙏 Ghi công và giấy phép

Emulator dựa trên **[MicroEmulator](https://github.com/barteo/microemu)** 3.0.0 (Apache License 2.0). Giữ nguyên thông báo bản quyền khi phân phối lại; xem [`LICENSE`](LICENSE).

<div align="center">

Làm bởi **Cherry** · [Báo lỗi](../../issues/new/choose) · [Đóng góp](CONTRIBUTING.md) · [Bảo mật](SECURITY.md)

</div>
