#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-3b docs_src [10:20]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][10:20]

PREPEND: dict[str, str] = {
    "docs_src/async_tests/__init__.py": '"""FastAPI 文档示例：异步测试（async tests）。"""\n',
    "docs_src/async_tests/app_a_py310/__init__.py": (
        '"""简单 FastAPI 应用及 httpx AsyncClient 异步测试示例（app_a）。"""\n'
    ),
    "docs_src/authentication_error_status_code/__init__.py": (
        '"""FastAPI 文档示例：自定义认证失败 HTTP 状态码。"""\n'
    ),
    "docs_src/background_tasks/__init__.py": '"""FastAPI 文档示例：后台任务（background tasks）。"""\n',
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "docs_src/app_testing/tutorial003_py310.py": [
        (
            "from fastapi import FastAPI",
            '"""教程：使用 TestClient 上下文管理器触发 startup 事件后再测试。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "items = {}",
            "# 模拟内存数据，在 startup 时填充\nitems = {}",
        ),
        (
            "@app.on_event(\"startup\")\nasync def startup_event():",
            '@app.on_event("startup")\nasync def startup_event():\n    """应用启动时预填充 items 数据。"""',
        ),
        (
            "@app.get(\"/items/{item_id}\")\nasync def read_items(item_id: str):",
            '@app.get("/items/{item_id}")\nasync def read_items(item_id: str):\n    """按 ID 返回 startup 阶段写入的 item。"""',
        ),
        (
            "def test_read_items():",
            "def test_read_items():\n    \"\"\"with TestClient 会运行 startup，再断言 GET 响应。\"\"\"",
        ),
        (
            "    with TestClient(app) as client:",
            "    # 进入 with 块时执行 startup 事件\n    with TestClient(app) as client:",
        ),
    ],
    "docs_src/app_testing/tutorial004_py310.py": [
        (
            "from contextlib import asynccontextmanager",
            '"""教程：测试 lifespan 上下文管理器——启动填充、退出清理。"""\n\nfrom contextlib import asynccontextmanager',
        ),
        (
            "items = {}",
            "# 模块级共享状态，由 lifespan 管理生命周期\nitems = {}",
        ),
        (
            "@asynccontextmanager\nasync def lifespan(app: FastAPI):",
            "@asynccontextmanager\nasync def lifespan(app: FastAPI):\n    \"\"\"应用 lifespan：启动时填充 items，关闭时清空。\"\"\"",
        ),
        (
            "    yield\n    # clean up items\n    items.clear()",
            "    yield\n    # lifespan 结束时清理 items\n    items.clear()",
        ),
        (
            "app = FastAPI(lifespan=lifespan)",
            "# 将 lifespan 注册到 FastAPI 应用\napp = FastAPI(lifespan=lifespan)",
        ),
        (
            "@app.get(\"/items/{item_id}\")\nasync def read_items(item_id: str):",
            '@app.get("/items/{item_id}")\nasync def read_items(item_id: str):\n    """按 ID 读取 lifespan 阶段填充的 item。"""',
        ),
        (
            "def test_read_items():",
            "def test_read_items():\n    \"\"\"验证 lifespan 在 TestClient 生命周期内的启动与清理行为。\"\"\"",
        ),
        (
            "    # Before the lifespan starts, \"items\" is still empty",
            "    # lifespan 启动前 items 仍为空",
        ),
        (
            "        # Inside the \"with TestClient\" block, the lifespan starts and items added",
            "        # with 块内 lifespan 已启动，items 已填充",
        ),
        (
            "        # After the requests is done, the items are still there",
            "        # 请求完成后 items 仍保留",
        ),
        (
            "    # The end of the \"with TestClient\" block simulates terminating the app, so\n"
            "    # the lifespan ends and items are cleaned up",
            "    # 退出 with 块模拟应用终止，lifespan 结束并清理 items",
        ),
    ],
    "docs_src/async_tests/app_a_py310/main.py": [
        (
            "from fastapi import FastAPI",
            '"""被测应用：最小 FastAPI 示例，供 httpx 异步测试调用。"""\n\nfrom fastapi import FastAPI',
        ),
        (
            "@app.get(\"/\")\nasync def root():",
            '@app.get("/")\nasync def root():\n    """返回简单 JSON 消息。"""',
        ),
    ],
    "docs_src/async_tests/app_a_py310/test_main.py": [
        (
            "import pytest",
            '"""使用 httpx AsyncClient 对 app_a 进行异步 HTTP 测试。"""\n\nimport pytest',
        ),
        (
            "@pytest.mark.anyio\nasync def test_root():",
            '@pytest.mark.anyio\nasync def test_root():\n    """异步 GET 根路径，断言 200 与 JSON 内容。"""',
        ),
        (
            "    async with AsyncClient(\n        transport=ASGITransport(app=app), base_url=\"http://test\"\n    ) as ac:",
            "    # ASGITransport 将请求直接转发到 FastAPI app，无需真实网络\n"
            "    async with AsyncClient(\n"
            '        transport=ASGITransport(app=app), base_url="http://test"\n'
            "    ) as ac:",
        ),
    ],
    "docs_src/authentication_error_status_code/tutorial001_an_py310.py": [
        (
            "from typing import Annotated",
            '"""教程：自定义 HTTPBearer，未认证时返回 403 而非默认 401。"""\n\nfrom typing import Annotated',
        ),
        (
            "class HTTPBearer403(HTTPBearer):",
            'class HTTPBearer403(HTTPBearer):\n    """HTTPBearer 子类，认证失败时抛出 403 Forbidden。"""',
        ),
        (
            "    def make_not_authenticated_error(self) -> HTTPException:",
            "    def make_not_authenticated_error(self) -> HTTPException:\n        \"\"\"覆盖默认行为：未认证返回 403 而非 401。\"\"\"",
        ),
        (
            "CredentialsDep = Annotated[HTTPAuthorizationCredentials, Depends(HTTPBearer403())]",
            "# Annotated 依赖：注入 Bearer 令牌凭证\n"
            "CredentialsDep = Annotated[HTTPAuthorizationCredentials, Depends(HTTPBearer403())]",
        ),
        (
            "@app.get(\"/me\")\ndef read_me(credentials: CredentialsDep):",
            '@app.get("/me")\ndef read_me(credentials: CredentialsDep):\n    """需有效 Bearer 令牌，返回认证成功消息。"""',
        ),
    ],
    "docs_src/background_tasks/tutorial001_py310.py": [
        (
            "from fastapi import BackgroundTasks, FastAPI",
            '"""教程：BackgroundTasks 在响应返回后继续执行写文件等耗时操作。"""\n\nfrom fastapi import BackgroundTasks, FastAPI',
        ),
        (
            "def write_notification(email: str, message=\"\"):",
            'def write_notification(email: str, message=""):\n    """后台任务：将通知内容写入 log.txt。"""',
        ),
        (
            "@app.post(\"/send-notification/{email}\")\nasync def send_notification(email: str, background_tasks: BackgroundTasks):",
            '@app.post("/send-notification/{email}")\nasync def send_notification(email: str, background_tasks: BackgroundTasks):\n    """立即返回响应，通知写入在后台异步执行。"""',
        ),
        (
            "    background_tasks.add_task(write_notification, email, message=\"some notification\")",
            '    # 注册后台任务，响应发送后 Starlette 会调度执行\n    background_tasks.add_task(write_notification, email, message="some notification")',
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
        print("Marked 10 files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
