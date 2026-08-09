#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-2b docs_src [15:30]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][15:30]

PREPEND: dict[str, str] = {
    "docs_src/additional_status_codes/__init__.py": '"""FastAPI 文档示例：附加 HTTP 状态码（additional status codes）。"""\n',
    "docs_src/advanced_middleware/__init__.py": '"""FastAPI 文档示例：高级中间件（advanced middleware）。"""\n',
    "docs_src/app_testing/__init__.py": '"""FastAPI 文档示例：应用测试（app testing）。"""\n',
    "docs_src/app_testing/app_a_py310/__init__.py": '"""简单 FastAPI 应用及 TestClient 测试示例（app_a）。"""\n',
    "docs_src/app_testing/app_b_an_py310/__init__.py": '"""带请求头认证与 CRUD 的 FastAPI 应用及测试示例（app_b，Annotated 语法）。"""\n',
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/additional_responses/tutorial004_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程：在 responses 中声明多种状态码及 image/png 等非 JSON 内容类型。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 响应模型。"""',
        ),
        (
            "responses = {",
            "# 共享的附加响应描述，供 OpenAPI 文档展示\nresponses = {",
        ),
        (
            "    responses={**responses, 200: {\"content\": {\"image/png\": {}}}}",
            "    # 合并共享 responses，并为 200 声明 image/png 媒体类型\n    responses={**responses, 200: {\"content\": {\"image/png\": {}}}}",
        ),
        (
            "    if img:\n        return FileResponse",
            "    if img:\n        # img=true 时返回 PNG 文件而非 JSON\n        return FileResponse",
        ),
    ],
    "docs_src/additional_status_codes/tutorial001_py310.py": [
        (
            "from fastapi import Body, FastAPI, status",
            '"""教程：更新返回 200，新建时通过 JSONResponse 返回 201 Created。"""\n\nfrom fastapi import Body, FastAPI, status',
        ),
        (
            "items = {\"foo\": {\"name\": \"Fighters\", \"size\": 6}, \"bar\": {\"name\": \"Tenders\", \"size\": 3}}",
            '# 模拟内存数据库\nitems = {"foo": {"name": "Fighters", "size": 6}, "bar": {"name": "Tenders", "size": 3}}',
        ),
        (
            "    if item_id in items:\n        item = items[item_id]",
            "    if item_id in items:\n        # 已存在：更新字段，默认返回 200 OK\n        item = items[item_id]",
        ),
        (
            "    else:\n        item = {\"name\": name, \"size\": size}",
            "    else:\n        # 新建条目：显式设置 201 Created 状态码\n        item = {\"name\": name, \"size\": size}",
        ),
    ],
    "docs_src/additional_status_codes/tutorial001_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程：使用 Annotated 语法声明请求体，创建时返回 201。"""\n\nfrom typing import Annotated',
        ),
        (
            "items = {\"foo\": {\"name\": \"Fighters\", \"size\": 6}, \"bar\": {\"name\": \"Tenders\", \"size\": 3}}",
            '# 模拟内存数据库\nitems = {"foo": {"name": "Fighters", "size": 6}, "bar": {"name": "Tenders", "size": 3}}',
        ),
        (
            "    name: Annotated[str | None, Body()] = None,\n    size: Annotated[int | None, Body()] = None,",
            "    # Annotated + Body() 声明可选请求体字段\n    name: Annotated[str | None, Body()] = None,\n    size: Annotated[int | None, Body()] = None,",
        ),
        (
            "    if item_id in items:\n        item = items[item_id]",
            "    if item_id in items:\n        # 已存在：更新并返回 200\n        item = items[item_id]",
        ),
        (
            "    else:\n        item = {\"name\": name, \"size\": size}",
            "    else:\n        # 新建：返回 201 Created\n        item = {\"name\": name, \"size\": size}",
        ),
    ],
    "docs_src/advanced_middleware/tutorial001_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程：HTTPSRedirectMiddleware 将所有 HTTP 请求重定向到 HTTPS。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app.add_middleware(HTTPSRedirectMiddleware)",
            "# 非 HTTPS 请求将被 307 重定向到 HTTPS\napp.add_middleware(HTTPSRedirectMiddleware)",
        ),
    ],
    "docs_src/advanced_middleware/tutorial002_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程：TrustedHostMiddleware 限制允许访问的主机名。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app.add_middleware(\n    TrustedHostMiddleware, allowed_hosts=[\"example.com\", \"*.example.com\"]\n)",
            "# 仅允许 example.com 及其子域名的 Host 头\napp.add_middleware(\n    TrustedHostMiddleware, allowed_hosts=[\"example.com\", \"*.example.com\"]\n)",
        ),
    ],
    "docs_src/advanced_middleware/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程：GZipMiddleware 对超过阈值的响应体进行 GZip 压缩。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "app.add_middleware(GZipMiddleware, minimum_size=1000, compresslevel=5)",
            "# 响应体 >= 1000 字节时使用压缩级别 5 进行 GZip\napp.add_middleware(GZipMiddleware, minimum_size=1000, compresslevel=5)",
        ),
        (
            '    return "somebigcontent"',
            '    # 返回较大内容以触发 GZip 压缩\n    return "somebigcontent"',
        ),
    ],
    "docs_src/app_testing/app_a_py310/main.py": [
        (
            "from fastapi import FastAPI",
            '"""被测应用：最小 FastAPI 示例，提供根路径 GET 接口。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "@app.get(\"/\")\nasync def read_main():",
            '@app.get("/")\nasync def read_main():\n    """返回 Hello World JSON。"""',
        ),
    ],
    "docs_src/app_testing/app_a_py310/test_main.py": [
        (
            "from fastapi.testclient import TestClient",
            '"""使用 TestClient 对 app_a 进行同步 HTTP 测试。"""\n\nfrom fastapi.testclient import TestClient',
        ),
        (
            "client = TestClient(app)",
            "# TestClient 在 ASGI 层模拟请求，无需启动真实服务器\nclient = TestClient(app)",
        ),
        (
            "def test_read_main():",
            "def test_read_main():\n    \"\"\"验证根路径返回 200 与预期 JSON。\"\"\"",
        ),
    ],
    "docs_src/app_testing/app_b_an_py310/main.py": [
        (
            "from typing import Annotated",
            '"""被测应用：带 X-Token 请求头认证的 Items CRUD API。"""\n\nfrom typing import Annotated',
        ),
        (
            "fake_secret_token = \"coneofsilence\"",
            '# 模拟密钥令牌，用于校验 X-Token 请求头\nfake_secret_token = "coneofsilence"',
        ),
        (
            "fake_db = {",
            "# 模拟内存数据库\nfake_db = {",
        ),
        (
            "class Item(BaseModel):",
            'class Item(BaseModel):\n    """Item 资源的数据模型。"""',
        ),
        (
            "async def read_main(item_id: str, x_token: Annotated[str, Header()]):",
            "async def read_main(item_id: str, x_token: Annotated[str, Header()]):\n    \"\"\"按 ID 读取 Item，需有效 X-Token。\"\"\"",
        ),
        (
            "    if x_token != fake_secret_token:",
            "    # 校验请求头令牌\n    if x_token != fake_secret_token:",
        ),
        (
            "async def create_item(item: Item, x_token: Annotated[str, Header()]) -> Item:",
            "async def create_item(item: Item, x_token: Annotated[str, Header()]) -> Item:\n    \"\"\"创建 Item，ID 冲突时返回 409。\"\"\"",
        ),
    ],
    "docs_src/app_testing/app_b_an_py310/test_main.py": [
        (
            "from fastapi.testclient import TestClient",
            '"""app_b 的集成测试：覆盖认证、404 与冲突等场景。"""\n\nfrom fastapi.testclient import TestClient',
        ),
        (
            "client = TestClient(app)",
            "# 针对同一 app 实例创建测试客户端\nclient = TestClient(app)",
        ),
        (
            "def test_read_item():",
            "def test_read_item():\n    \"\"\"有效令牌时应成功读取已有 Item。\"\"\"",
        ),
        (
            "def test_read_item_bad_token():",
            "def test_read_item_bad_token():\n    \"\"\"无效 X-Token 应返回 400。\"\"\"",
        ),
        (
            "def test_read_nonexistent_item():",
            "def test_read_nonexistent_item():\n    \"\"\"不存在的 Item ID 应返回 404。\"\"\"",
        ),
        (
            "def test_create_item():",
            "def test_create_item():\n    \"\"\"有效请求应成功创建新 Item。\"\"\"",
        ),
        (
            "def test_create_item_bad_token():",
            "def test_create_item_bad_token():\n    \"\"\"创建时无效令牌应返回 400。\"\"\"",
        ),
        (
            "def test_create_existing_item():",
            "def test_create_existing_item():\n    \"\"\"重复 ID 创建应返回 409 Conflict。\"\"\"",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    dst = ANALYZED / rel
    if not dst.exists():
        raise FileNotFoundError(f"Missing analyzed file: {rel}")
    text = dst.read_text(encoding="utf-8")
    if has_chinese(text):
        return
    if rel in PREPEND:
        if text.strip():
            if not text.startswith('"""'):
                text = PREPEND[rel] + text
        else:
            text = PREPEND[rel]
    for old, new in FILE_REPLACEMENTS.get(rel, []):
        if old not in text:
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise ValueError(f"No Chinese content after annotation: {rel}")
    dst.write_text(text, encoding="utf-8")


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
        print("Marked 15 files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
