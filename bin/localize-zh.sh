#!/usr/bin/env bash
# 将 analyzed 中英文注释译为中文，并补齐字段/方法中文注释
# 用法:
#   ./bin/localize-zh.sh springframework 7.0.8
#   ./bin/localize-zh.sh springframework 7.0.8 --modules spring-jdbc,spring-beans
#   ./bin/localize-zh.sh springframework 7.0.8 --force-sync --modules spring-jdbc
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
exec "$ROOT/bin/oca" annotate "$@" --mode zh
