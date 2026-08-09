#!/usr/bin/env bash
# 用法:
#   ./bin/analyze-project.sh <project|url|org/repo> [version|latest] [--max-files N]
#
# 示例:
#   ./bin/analyze-project.sh springframework latest
#   ./bin/analyze-project.sh https://github.com/spring-projects/spring-framework 7.0.8
#   ./bin/analyze-project.sh spring-projects/spring-boot latest --max-files 200
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ $# -lt 1 ]]; then
  cat <<'EOF'
用法: ./bin/analyze-project.sh <project|url|org/repo> [version|latest] [额外 oca analyze 参数...]

步骤（自动执行）:
  1) resolve  解析仓库与最新 Release / 指定版本
  2) fetch    depth=1 浅克隆到 <project>/<version>/original
  3) scan     模块/语言静态扫描
  4) complexity 复杂度热点 + 意图候选
  5) arch     生成 架构说明/ 与 项目模块说明/
  6) annotate 同步 analyzed/ 并写入中文意图注释（跳过无营养 getter/setter）

说明:
  - original/ 只读快照，不做改动
  - analyzed/ 在复杂类/方法处添加中文解析；原有注释保留，OCA 注释插在其前
EOF
  exit 1
fi

exec "$ROOT/bin/oca" analyze "$@"
