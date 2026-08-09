#!/usr/bin/env bash
# 处理 CURRENT.json 指向的一个类。
# 本脚本只做「准备 + 调用精读改写器」；改写器必须基于对源码的理解生成中文注释。
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT="${1:?project}"
VERSION="${2:?version}"
export PYTHONPATH="${ROOT}/framework${PYTHONPATH:+:$PYTHONPATH}"
export OCA_CLASSWORK_ROOT="$ROOT/$PROJECT/$VERSION"
exec python3 -m oca.classwork.process_one
