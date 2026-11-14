#!/usr/bin/env bash
# Chay Che779 Remoter. Tu build neu chua co jar. Dung: ./run.sh [game.jar ...]
cd "$(dirname "$0")"
[ -f Che779_Remoter.jar ] || ./build.sh || exit 1
exec java -jar Che779_Remoter.jar "$@"
