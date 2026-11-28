#!/usr/bin/env bash
# Nhật ký build: gom log của các job ĐÃ XONG trong lần chạy hiện tại, rồi đăng lên Release tiền-phát-hành <thẻ> (không dời thẻ).
# Dùng (trong job chạy cuối, có GH_TOKEN, quyền contents: write + actions: read):
#   bash .github/scripts/publish-buildlog.sh <thẻ> "<tiêu đề>" [tệp-ghi-chú-thêm]
set -euo pipefail
TAG="${1:?Thiếu thẻ}"; TITLE="${2:?Thiếu tiêu đề}"; EXTRA="${3:-}"
R="$GITHUB_REPOSITORY"; RUN="$GITHUB_RUN_ID"; URL="$GITHUB_SERVER_URL/$R/actions/runs/$RUN"
: > build.log; : > jobs.tsv
gh api --paginate "repos/$R/actions/runs/$RUN/jobs" --jq '.jobs[] | select(.conclusion != null) | [.id, .name, .conclusion] | @tsv' > jobs.tsv || true
if [[ -s jobs.tsv ]]; then
  while IFS=$'\t' read -r id name concl; do
    { echo "######## JOB: $name ($concl)"
      gh api "repos/$R/actions/jobs/$id/logs" 2>&1 | sed -E 's/^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:.]+Z //' || echo "(không tải được log của job này)"
    } >> build.log
  done < jobs.tsv
fi
{
  echo "Commit: $GITHUB_SHA"
  echo "Nhánh/thẻ: $GITHUB_REF_NAME  ·  Sự kiện: $GITHUB_EVENT_NAME"
  echo "Lần chạy: $URL"
  echo
  echo "==== KẾT QUẢ TỪNG JOB"
  if [[ -s jobs.tsv ]]; then awk -F'\t' '{printf "- %s: %s\n",$2,$3}' jobs.tsv; else echo "(chưa đọc được danh sách job)"; fi
  if [[ -n "$EXTRA" && -s "$EXTRA" ]]; then echo; cat "$EXTRA"; fi
  echo
  echo "==== LỖI (nếu có)"
  grep -nE "error:|Error:|THẤT BẠI|##\[error\]|Caused by|Exception|FAILED|Thiếu |không khớp" build.log | head -150 || echo "(không thấy dòng lỗi)"
  echo
  echo "==== ĐUÔI LOG (10000 dòng cuối)"
  tail -n 10000 build.log
} > build-tail.log
NOTES="Nhật ký build gần nhất. Commit: $GITHUB_SHA. Run: $URL"
if gh release view "$TAG" >/dev/null 2>&1; then
  gh release edit "$TAG" --title "$TITLE" --notes "$NOTES" --prerelease --latest=false
else
  gh release create "$TAG" --prerelease --latest=false --target "$GITHUB_SHA" --title "$TITLE" --notes "$NOTES"
fi
gh release upload "$TAG" build-tail.log --clobber
mkdir -p verify && gh release download "$TAG" --pattern build-tail.log --dir verify --clobber
cmp build-tail.log verify/build-tail.log
echo "Đã đăng nhật ký build lên Release '$TAG' ($(wc -l < build-tail.log) dòng)."
