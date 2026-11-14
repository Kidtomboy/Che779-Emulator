# Bản fork, bản tự build và nhận diện bản chính thức

## Chính sách

- Mọi người được fork và tự build.
- Không cần tạo bản sao mã nguồn chỉ để đổi tên, biểu tượng hay cấu hình build.
- Emulator và Remoter là hai ứng dụng thành phần của một sản phẩm, không phải hai bản sao của nhau.
- Chỉ tách thành bản riêng khi có thay đổi logic đủ độc lập và cần bảo trì riêng.

## Nhận diện bản chính thức

Bản chính thức chỉ được phát hành qua workflow **Phát hành** của repo này khi gắn thẻ `vX.Y.Z`, có số phiên bản (file `VERSION`) và `SHA256SUMS.txt`. Bản fork hoặc tự build cần ghi rõ là bản tự build để không bị nhầm với bản chính thức.

## Không khóa người dùng

Dự án không có cơ chế chống fork, chống sao chép hay khóa thiết bị. Độ tin cậy của bản chính thức dựa trên nơi phát hành và mã SHA-256.
