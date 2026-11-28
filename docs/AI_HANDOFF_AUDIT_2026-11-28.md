# AI Audit Handoff — Project 1 (2026-11-28)

> Snapshot bàn giao cho Claude. Đây là bản tổng hợp bằng chứng đã đọc trong phiên audit; không phải xác nhận toàn bộ hành vi runtime.

## Repository baseline
- Repo: Kidtomboy/Che779-Emulator
- VERSION: 1.3.1
- main HEAD đã đọc: 51b58c5e459aa90d58f791c53fd3de643e005a61
- Audit issue mới: https://github.com/Kidtomboy/Che779-Emulator/issues/3
- Issue cũ được thay đầu mối: https://github.com/Kidtomboy/Che779-Emulator/issues/2
- Issue chuyển Remoter Linux (đóng, giữ lịch sử): https://github.com/Kidtomboy/Che779-Emulator/issues/1

## Bằng chứng trực tiếp
- docs/DEBUG_LOG.md tại blob 569a0ec9259268ae04c6a2d796fbff75084dd832: ghi IPC timeout 5s, max 16 clients, line cap 65,536; MouseSync callback đã marshal bằng BeginInvoke; screenshot path restriction BUILD_VERIFIED, runtime NOT_RUN.
- docs/AI_HANDOFF.md tại blob 97f7fa9e4771509a243a3435d5ec9b6abcf4d85e: xác nhận IPC/authentication risk và Settings exception handling; có quy tắc đồng bộ bốn repo.
- apps/remoter-windows/source/Settings.cs tại blob e04e2c6f3cf460cdb28fc5fc12b3a0fcabd58ff7: LoadFrom/SaveTo swallow exception; SaveTo ghi trực tiếp File.WriteAllLines.
- P1 CI gần nhất được ghi nhận xanh tại HEAD 51b58c5e.

## Trạng thái phân loại
- Không giao lại IPC timeout/client/line cap và BeginInvoke: đã có trong source.
- Settings save/load error visibility + atomic safety: còn vấn đề theo static review.
- IPC auth, parser edge cases, MouseSync canvas mapping, port exhaustion/race, process restart lifecycle: cần test/đánh giá tiếp.
- Windows save/load và runtime integration: NOT_RUN trong handoff này.

## Quy tắc bàn giao cho Claude
1. Fetch main và xác nhận HEAD trước khi hành động.
2. Đọc Issue #3 và đối chiếu lại các file nêu trên.
3. Giữ nguyên protocol INI/IPC cho tới khi có test tương thích và phê duyệt.
4. Phân biệt STATIC_REVIEW, BUILD_VERIFIED, CI_VERIFIED, runtime VERIFIED và NOT_RUN.
5. Không sửa source chỉ từ kết luận static; chuẩn bị test plan và acceptance criteria trước.
6. P3 phải cập nhật EMULATOR_V1_REF khi P1 đổi commit theo quy tắc đồng bộ của dự án.

## Audit scope limitation
Phiên bàn giao này không chạy Windows desktop hoặc thiết bị thật; không tuyên bố runtime pass. Chỉ các nội dung nêu rõ ở trên có bằng chứng trực tiếp trong snapshot này.
