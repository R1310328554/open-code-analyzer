#!/usr/bin/env bash
# 可选：集成外部 AI/图谱工具（存在才调用，不存在则提示安装）
# 参考:
#   - https://github.com/safishamsi/graphify
#   - https://github.com/Egonex-AI/Understand-Anything
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT="${1:-}"
VERSION="${2:-}"

if [[ -z "$PROJECT" || -z "$VERSION" ]]; then
  echo "用法: $0 <project> <version>"
  echo "示例: $0 springframework 7.0.8"
  exit 1
fi

SRC="$ROOT/$PROJECT/$VERSION/original"
OUT="$ROOT/$PROJECT/$VERSION/_reports/external-tools"
mkdir -p "$OUT"

if [[ ! -d "$SRC" ]]; then
  echo "[oca] 找不到 original: $SRC （请先 ./bin/oca fetch ...）"
  exit 1
fi

echo "[oca] 外部工具集成目录: $OUT"

# -------- graphify --------
if command -v graphify >/dev/null 2>&1; then
  echo "[oca] 检测到 graphify，尝试生成知识图谱..."
  (
    cd "$SRC"
    # graphify 通常作为 Claude Code skill；CLI 若可用则导出到 reports
    graphify . --svg >/dev/null 2>&1 || true
    if [[ -d graphify-out ]]; then
      rm -rf "$OUT/graphify"
      mv graphify-out "$OUT/graphify"
      echo "[oca] graphify 输出 -> $OUT/graphify"
    fi
  )
else
  cat >"$OUT/graphify.README.md" <<'EOF'
# graphify 未安装

项目: https://github.com/safishamsi/graphify

可选安装（需要 Claude Code 环境时更完整）:
```bash
pip install graphifyy && graphify install
```

然后在 original 目录对核心模块执行 `/graphify`，把 `graphify-out/` 拷到 `_reports/external-tools/graphify/`。
EOF
  echo "[oca] 未检测到 graphify，已写入安装说明"
fi

# -------- Understand-Anything --------
if [[ -d "$HOME/.understand-anything" ]] || command -v understand-anything-viewer >/dev/null 2>&1; then
  echo "[oca] 检测到 Understand-Anything 相关安装，请在 AI Coding 环境中对 original 执行 /understand --language zh"
  cat >"$OUT/understand-anything.README.md" <<EOF
# Understand-Anything

仓库: https://github.com/Egonex-AI/Understand-Anything

建议:
1. 在 Cursor/Claude Code 中安装插件
2. 打开 \`$SRC\`
3. 执行: /understand --language zh
4. 将生成的 \`.ua/\` 复制到:
   \`$OUT/understand-anything/\`
EOF
else
  cat >"$OUT/understand-anything.README.md" <<EOF
# Understand-Anything 未安装

仓库: https://github.com/Egonex-AI/Understand-Anything

一键安装（多平台）:
\`\`\`bash
curl -fsSL https://raw.githubusercontent.com/Egonex-AI/Understand-Anything/main/install.sh | bash
\`\`\`

安装后对源码目录执行 \`/understand --language zh\`，把 \`.ua/\` 结果落到:
\`$OUT/understand-anything/\`
EOF
  echo "[oca] 未检测到 Understand-Anything，已写入安装说明"
fi

echo "[oca] 完成。oca 主分析不依赖这些工具；它们用于增强图谱/交互探索。"
