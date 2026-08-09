#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-4a docs_src slice [0:10]."""
from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][:10]

ANNOTATED: dict[str, str] = {
    "docs_src/background_tasks/tutorial002_an_py310.py": '''\
"""教程 002（Annotated）：依赖项中注册 BackgroundTasks，响应返回后继续写日志。"""

from typing import Annotated

from fastapi import BackgroundTasks, Depends, FastAPI

app = FastAPI()


def write_log(message: str):
    """后台任务：将消息追加写入 log.txt。"""
    with open("log.txt", mode="a") as log:
        log.write(message)


def get_query(background_tasks: BackgroundTasks, q: str | None = None):
    """依赖项：若存在查询参数 q，将其记录到后台日志并返回 q。"""
    if q:
        message = f"found query: {q}\\n"
        # 依赖项内也可向 BackgroundTasks 注册任务，与路径操作共享同一任务队列
        background_tasks.add_task(write_log, message)
    return q


@app.post("/send-notification/{email}")
async def send_notification(
    email: str, background_tasks: BackgroundTasks, q: Annotated[str, Depends(get_query)]
):
    """发送通知；邮件与查询参数均通过后台任务写入日志。"""
    message = f"message to {email}\\n"
    background_tasks.add_task(write_log, message)
    return {"message": "Message sent"}
''',
    "docs_src/background_tasks/tutorial002_py310.py": '''\
"""教程 002：依赖项中注册 BackgroundTasks（传统 Depends 语法）。"""

from fastapi import BackgroundTasks, Depends, FastAPI

app = FastAPI()


def write_log(message: str):
    """后台任务：将消息追加写入 log.txt。"""
    with open("log.txt", mode="a") as log:
        log.write(message)


def get_query(background_tasks: BackgroundTasks, q: str | None = None):
    """依赖项：若存在查询参数 q，将其记录到后台日志并返回 q。"""
    if q:
        message = f"found query: {q}\\n"
        background_tasks.add_task(write_log, message)
    return q


@app.post("/send-notification/{email}")
async def send_notification(
    email: str, background_tasks: BackgroundTasks, q: str = Depends(get_query)
):
    """发送通知；邮件与查询参数均通过后台任务写入日志。"""
    message = f"message to {email}\\n"
    background_tasks.add_task(write_log, message)
    return {"message": "Message sent"}
''',
    "docs_src/behind_a_proxy/__init__.py": '''\
"""FastAPI 文档示例：反向代理后的 root_path 与 OpenAPI servers 配置。"""
''',
    "docs_src/behind_a_proxy/tutorial001_01_py310.py": '''\
"""教程 001-01：无代理时的基础应用（对照组）。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/")
def read_items():
    """返回示例物品列表。"""
    return ["plumbus", "portal gun"]
''',
    "docs_src/behind_a_proxy/tutorial001_py310.py": '''\
"""教程 001：通过 Request.scope 读取 ASGI root_path（代理注入的挂载前缀）。"""

from fastapi import FastAPI, Request

app = FastAPI()


@app.get("/app")
def read_main(request: Request):
    """返回问候语及当前请求的 root_path。"""
    # scope["root_path"] 由反向代理或 uvicorn --root-path 设置
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
''',
    "docs_src/behind_a_proxy/tutorial002_py310.py": '''\
"""教程 002：在 FastAPI 构造时声明 root_path，告知应用挂载前缀。"""

from fastapi import FastAPI, Request

# root_path 应与代理剥离的路径前缀一致，用于 OpenAPI 与路由解析
app = FastAPI(root_path="/api/v1")


@app.get("/app")
def read_main(request: Request):
    """验证 root_path 配置是否生效。"""
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
''',
    "docs_src/behind_a_proxy/tutorial003_py310.py": '''\
"""教程 003：同时配置 servers 与 root_path，OpenAPI 文档展示多环境 URL。"""

from fastapi import FastAPI, Request

app = FastAPI(
    # servers 列出客户端可访问的完整基础 URL（含域名）
    servers=[
        {"url": "https://stag.example.com", "description": "Staging environment"},
        {"url": "https://prod.example.com", "description": "Production environment"},
    ],
    # root_path 为应用在代理后的挂载路径
    root_path="/api/v1",
)


@app.get("/app")
def read_main(request: Request):
    """返回 root_path，便于调试代理配置。"""
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
''',
    "docs_src/behind_a_proxy/tutorial004_py310.py": '''\
"""教程 004：root_path_in_servers=False，禁止自动将 root_path 拼入 servers URL。"""

from fastapi import FastAPI, Request

app = FastAPI(
    servers=[
        {"url": "https://stag.example.com", "description": "Staging environment"},
        {"url": "https://prod.example.com", "description": "Production environment"},
    ],
    root_path="/api/v1",
    # 显式禁用：不在自动生成的 server 条目前缀 root_path
    root_path_in_servers=False,
)


@app.get("/app")
def read_main(request: Request):
    """与 tutorial003 相同端点，对比 OpenAPI servers 生成差异。"""
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
''',
    "docs_src/bigger_applications/__init__.py": '''\
"""FastAPI 文档示例：大型应用结构（多 router、依赖与子包）。"""
''',
    "docs_src/bigger_applications/app_an_py310/__init__.py": '''\
"""示例包 app_an：bigger_applications 教程的应用布局（Annotated 语法变体）。"""
''',
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    if rel not in ANNOTATED:
        raise KeyError(f"no annotation template: {rel}")
    dst = ANALYZED / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    content = ANNOTATED[rel]
    if not has_chinese(content):
        raise ValueError(f"No Chinese content for: {rel}")
    dst.write_text(content, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        return 1
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "fastapi",
            "--version",
            "0.141.1",
            "--note",
            "wave4a-batch0-10",
            *BATCH_FILES,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
