# Variants and Clones

## Chính sách Project 1

- Cho phép fork và tự build.
- Không cần clone source chỉ để thay đổi tên, icon hoặc cấu hình build.
- Emulator V1 và Remoter là hai app thành phần, không phải hai clone của nhau.
- Một clone source chỉ nên được tạo khi có thay đổi logic/vòng đời đủ độc lập và cần bảo trì riêng.
- Các cấu hình build/branding có thể được thêm thành variant khi có nhu cầu cụ thể.

## Nhận diện artifact

Official artifact cần được phát hành qua workflow/tag của repository chính thức, có version, commit SHA và checksum. Fork/custom build cần được nhận diện là bản tự build hoặc bản tùy biến, không gây nhầm với release chính thức.

## Không khóa người dùng

Không thêm cơ chế chống fork/copy source hoặc khóa thiết bị. Tính xác thực của release chính thức dựa trên nguồn phát hành và checksum, không dựa trên việc ngăn người dùng tự build.
