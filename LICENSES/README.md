# License status

**Chưa gán một license chung cho toàn bộ repository.** Chính sách mong muốn là cho phép fork và tự build, nhưng cần xác nhận quyền cấp phép trước khi phát hành công khai.

Lý do: source Emulator chứa code MicroEmulator với header chỉ rõ dual license LGPL-2.1-or-later hoặc Apache-2.0; repository còn có ASM 3.2 binary và code Che779 tùy biến. Không được suy diễn rằng toàn bộ code Che779/Remoter tự động mang Apache-2.0 chỉ vì một số file upstream có header Apache-compatible.

Trước khi public repo, chủ sở hữu cần quyết định license cho phần code tự viết và rà soát license/notice của dependency. Không xóa header bản quyền upstream. Xem `THIRD_PARTY_NOTICES.md`.
