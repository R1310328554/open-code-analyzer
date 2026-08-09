#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-6b docs_src [10:20]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][10:20]

PREPEND: dict[str, str] = {
    "docs_src/configure_swagger_ui/__init__.py": (
        '"""FastAPI 文档示例：配置 Swagger UI 参数（swagger_ui_parameters）。"""\n'
    ),
    "docs_src/cookie_param_models/__init__.py": (
        '"""FastAPI 文档示例：用 Pydantic 模型声明 Cookie 参数。"""\n'
    ),
    "docs_src/cookie_params/__init__.py": '"""FastAPI 文档示例：Cookie 参数（Cookie parameters）。"""\n',
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/configure_swagger_ui/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 001：关闭 Swagger UI 的语法高亮（syntaxHighlight）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            'app = FastAPI(swagger_ui_parameters={"syntaxHighlight": False})',
            '# swagger_ui_parameters 传入 Swagger UI 前端配置\n'
            'app = FastAPI(swagger_ui_parameters={"syntaxHighlight": False})',
        ),
        (
            "@app.get(\"/users/{username}\")\nasync def read_user(username: str):",
            '@app.get("/users/{username}")\nasync def read_user(username: str):\n    """示例路由；文档页将不启用代码语法高亮。"""',
        ),
    ],
    "docs_src/configure_swagger_ui/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 002：为 Swagger UI 指定语法高亮主题（obsidian）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            'app = FastAPI(swagger_ui_parameters={"syntaxHighlight": {"theme": "obsidian"}})',
            '# 将 syntaxHighlight 设为对象以选择主题\n'
            'app = FastAPI(swagger_ui_parameters={"syntaxHighlight": {"theme": "obsidian"}})',
        ),
        (
            "@app.get(\"/users/{username}\")\nasync def read_user(username: str):",
            '@app.get("/users/{username}")\nasync def read_user(username: str):\n    """示例路由；Swagger UI 使用 obsidian 高亮主题。"""',
        ),
    ],
    "docs_src/configure_swagger_ui/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程 003：关闭 Swagger UI 深度链接（deepLinking）。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            'app = FastAPI(swagger_ui_parameters={"deepLinking": False})',
            "# deepLinking=False 时 URL 不会随当前操作变化\n"
            'app = FastAPI(swagger_ui_parameters={"deepLinking": False})',
        ),
        (
            "@app.get(\"/users/{username}\")\nasync def read_user(username: str):",
            '@app.get("/users/{username}")\nasync def read_user(username: str):\n    """示例路由；文档页禁用按操作锚定 URL。"""',
        ),
    ],
    "docs_src/cookie_param_models/tutorial001_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 001（Annotated）：用 Pydantic 模型一次性声明多个 Cookie 字段。"""\n\nfrom typing import Annotated',
        ),
        (
            "class Cookies(BaseModel):",
            'class Cookies(BaseModel):\n    """从请求 Cookie 解析出的字段集合。"""',
        ),
        (
            "    session_id: str",
            "    session_id: str  # 必填 Cookie",
        ),
        (
            "    fatebook_tracker: str | None = None",
            "    fatebook_tracker: str | None = None  # 可选追踪 Cookie",
        ),
        (
            "    googall_tracker: str | None = None",
            "    googall_tracker: str | None = None  # 可选追踪 Cookie",
        ),
        (
            "async def read_items(cookies: Annotated[Cookies, Cookie()]):",
            "async def read_items(cookies: Annotated[Cookies, Cookie()]):\n"
            '    """Cookie() 将模型各字段映射为同名 Cookie 并注入。"""',
        ),
    ],
    "docs_src/cookie_param_models/tutorial001_py310.py": [
        (
            "from fastapi import Cookie, FastAPI",
            '"""教程 001：Pydantic Cookie 模型（非 Annotated 默认参数写法）。"""\n\nfrom fastapi import Cookie, FastAPI',
        ),
        (
            "class Cookies(BaseModel):",
            'class Cookies(BaseModel):\n    """Cookie 字段模型；字段名须与 Cookie 键一致。"""',
        ),
        (
            "    session_id: str",
            "    session_id: str  # 必填 Cookie",
        ),
        (
            "async def read_items(cookies: Cookies = Cookie()):",
            "async def read_items(cookies: Cookies = Cookie()):\n"
            '    """`= Cookie()` 声明整个模型来自 Cookie 参数。"""',
        ),
    ],
    "docs_src/cookie_param_models/tutorial002_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程 002（Annotated）：Cookie 模型禁止额外字段（extra=forbid）。"""\n\nfrom typing import Annotated',
        ),
        (
            "class Cookies(BaseModel):",
            'class Cookies(BaseModel):\n    """仅允许声明过的 Cookie；未知 Cookie 将导致校验失败。"""',
        ),
        (
            '    model_config = {"extra": "forbid"}',
            '    model_config = {"extra": "forbid"}  # 拒绝模型未定义的 Cookie',
        ),
        (
            "    session_id: str",
            "    session_id: str  # 必填 Cookie",
        ),
        (
            "async def read_items(cookies: Annotated[Cookies, Cookie()]):",
            "async def read_items(cookies: Annotated[Cookies, Cookie()]):\n"
            '    """返回校验通过的 Cookie 模型；多余 Cookie 会触发 422。"""',
        ),
    ],
    "docs_src/cookie_param_models/tutorial002_py310.py": [
        (
            "from fastapi import Cookie, FastAPI",
            '"""教程 002：Cookie 模型 extra=forbid（非 Annotated 写法）。"""\n\nfrom fastapi import Cookie, FastAPI',
        ),
        (
            "class Cookies(BaseModel):",
            'class Cookies(BaseModel):\n    """声明允许的 Cookie 集合；额外 Cookie 不被接受。"""',
        ),
        (
            '    model_config = {"extra": "forbid"}',
            '    model_config = {"extra": "forbid"}  # Pydantic 拒绝未声明字段',
        ),
        (
            "    session_id: str",
            "    session_id: str  # 必填 Cookie",
        ),
        (
            "async def read_items(cookies: Cookies = Cookie()):",
            "async def read_items(cookies: Cookies = Cookie()):\n"
            '    """解析 Cookie 为模型；未知 Cookie 键会校验失败。"""',
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, rel: str) -> str:
    for old, new in FILE_REPLACEMENTS.get(rel, []):
        if old not in text:
            if has_chinese(text):
                continue
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_queue_done(files: list[str]) -> None:
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
        shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    if rel in PREPEND:
        if text.strip():
            if not text.startswith('"""'):
                text = PREPEND[rel] + text
        else:
            text = PREPEND[rel]
    text = apply_replacements(text, rel)
    if not has_chinese(text):
        raise ValueError(f"No Chinese content after annotation: {rel}")
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
