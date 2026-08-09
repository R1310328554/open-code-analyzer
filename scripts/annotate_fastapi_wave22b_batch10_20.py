#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-22b docs_src [10:20]."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w22b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave22b_batch10_20.py"
MARK_NOTE = "wave22b docs_src [10:20]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springboot/4.1.0/analyzed/core/spring-boot/src/main/java/org/springframework/boot/context/properties/PropertyMapper.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/separate_openapi_schemas/__init__.py": '''\
"""FastAPI 文档示例：分离输入/输出 OpenAPI 模式（separate_input_output_schemas）。"""
''',
    "docs_src/separate_openapi_schemas/tutorial001_py310.py": '''\
"""教程 001：默认分离输入/输出 schema——POST 与 GET 对同一模型生成不同 OpenAPI 定义。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 默认 separate_input_output_schemas=True


class Item(BaseModel):
    """商品模型；description 可选，POST 请求体与 GET 响应共用此类型。"""

    name: str
    description: str | None = None


@app.post("/items/")
def create_item(item: Item):
    """POST 端点：OpenAPI 为输入生成 Item-Input schema（必填字段更严格）。"""
    return item


@app.get("/items/")
def read_items() -> list[Item]:
    """GET 端点：OpenAPI 为输出生成 Item-Output schema；响应可含未在请求中出现的字段。"""
    return [
        Item(
            name="Portal Gun",
            description="Device to travel through the multi-rick-verse",
        ),
        Item(name="Plumbus"),
    ]
''',
    "docs_src/separate_openapi_schemas/tutorial002_py310.py": '''\
"""教程 002：separate_input_output_schemas=False——输入与输出共用同一 OpenAPI schema。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(separate_input_output_schemas=False)  # 关闭输入/输出 schema 分离


class Item(BaseModel):
    """商品字段；关闭分离后 POST 与 GET 在 /docs 中显示相同的 Item schema。"""

    name: str
    description: str | None = None


@app.post("/items/")
def create_item(item: Item):
    """与 tutorial001 逻辑相同；OpenAPI 不再区分 Item-Input / Item-Output。"""
    return item


@app.get("/items/")
def read_items() -> list[Item]:
    """适合输入输出结构完全一致的场景；简化文档但失去细粒度 schema 差异。"""
    return [
        Item(
            name="Portal Gun",
            description="Device to travel through the multi-rick-verse",
        ),
        Item(name="Plumbus"),
    ]
''',
    "docs_src/server_sent_events/__init__.py": '''\
"""FastAPI 文档示例：Server-Sent Events（SSE）流式响应与 EventSourceResponse。"""
''',
    "docs_src/server_sent_events/tutorial001_py310.py": '''\
"""教程 001：EventSourceResponse 基础——yield Pydantic 模型自动编码为 SSE data 字段。"""

from collections.abc import AsyncIterable, Iterable

from fastapi import FastAPI
from fastapi.sse import EventSourceResponse
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """流式推送的商品条目；yield 时自动 JSON 序列化为 SSE 的 data:"""

    name: str
    description: str | None


items = [
    Item(name="Plumbus", description="A multi-purpose household device."),
    Item(name="Portal Gun", description="A portal opening device."),
    Item(name="Meeseeks Box", description="A box that summons a Meeseeks."),
]


@app.get("/items/stream", response_class=EventSourceResponse)
async def sse_items() -> AsyncIterable[Item]:
    """异步生成器 + 返回类型注解；每个 Item 编码为一帧 text/event-stream。"""
    for item in items:
        yield item


@app.get("/items/stream-no-async", response_class=EventSourceResponse)
def sse_items_no_async() -> Iterable[Item]:
    """同步生成器同样支持；FastAPI 在后台线程中迭代 yield。"""
    for item in items:
        yield item


@app.get("/items/stream-no-annotation", response_class=EventSourceResponse)
async def sse_items_no_annotation():
    """无返回类型注解时仍可工作；OpenAPI 无法推断流式 payload 结构。"""
    for item in items:
        yield item


@app.get("/items/stream-no-async-no-annotation", response_class=EventSourceResponse)
def sse_items_no_async_no_annotation():
    """同步 + 无注解的最简写法；生产环境建议保留 AsyncIterable[Item] 注解。"""
    for item in items:
        yield item
''',
    "docs_src/server_sent_events/tutorial002_py310.py": '''\
"""教程 002：ServerSentEvent——显式设置 event/id/retry 字段控制 SSE 协议语义。"""

from collections.abc import AsyncIterable

from fastapi import FastAPI
from fastapi.sse import EventSourceResponse, ServerSentEvent
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """带价格的商品；作为 ServerSentEvent.data 写入 JSON data: 行。"""

    name: str
    price: float


items = [
    Item(name="Plumbus", price=32.99),
    Item(name="Portal Gun", price=999.99),
    Item(name="Meeseeks Box", price=49.99),
]


@app.get("/items/stream", response_class=EventSourceResponse)
async def stream_items() -> AsyncIterable[ServerSentEvent]:
    """首帧 comment 被客户端忽略；后续帧带 event 名、递增 id 与 retry 重连间隔。"""
    yield ServerSentEvent(comment="stream of item updates")
    for i, item in enumerate(items):
        yield ServerSentEvent(data=item, event="item_update", id=str(i + 1), retry=5000)
''',
    "docs_src/server_sent_events/tutorial003_py310.py": '''\
"""教程 003：ServerSentEvent(raw_data=...)——发送原始文本行，跳过 JSON 序列化。"""

from collections.abc import AsyncIterable

from fastapi import FastAPI
from fastapi.sse import EventSourceResponse, ServerSentEvent

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/logs/stream", response_class=EventSourceResponse)
async def stream_logs() -> AsyncIterable[ServerSentEvent]:
    """raw_data 直接写入 data: 行，适合日志、纯文本等非 JSON 内容。"""
    logs = [
        "2025-01-01 INFO  Application started",
        "2025-01-01 DEBUG Connected to database",
        "2025-01-01 WARN  High memory usage detected",
    ]
    for log_line in logs:
        yield ServerSentEvent(raw_data=log_line)
''',
    "docs_src/server_sent_events/tutorial004_py310.py": '''\
"""教程 004：Last-Event-ID 请求头——断线重连后从上次事件 id 之后继续推送。"""

from collections.abc import AsyncIterable
from typing import Annotated

from fastapi import FastAPI, Header
from fastapi.sse import EventSourceResponse, ServerSentEvent
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品条目；每个 ServerSentEvent.id 供客户端重连时上报。"""

    name: str
    price: float


items = [
    Item(name="Plumbus", price=32.99),
    Item(name="Portal Gun", price=999.99),
    Item(name="Meeseeks Box", price=49.99),
]


@app.get("/items/stream", response_class=EventSourceResponse)
async def stream_items(
    last_event_id: Annotated[int | None, Header()] = None,
) -> AsyncIterable[ServerSentEvent]:
    """浏览器重连时自动发送 Last-Event-ID；start 跳过已接收的事件索引。"""
    start = last_event_id + 1 if last_event_id is not None else 0
    for i, item in enumerate(items):
        if i < start:
            continue
        yield ServerSentEvent(data=item, id=str(i))
''',
    "docs_src/server_sent_events/tutorial005_py310.py": '''\
"""教程 005：POST + SSE——接收 Prompt 请求体，逐词流式返回 token 事件。"""

from collections.abc import AsyncIterable

from fastapi import FastAPI
from fastapi.sse import EventSourceResponse, ServerSentEvent
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Prompt(BaseModel):
    """聊天提示词；POST 请求体在流式响应开始前一次性解析。"""

    text: str


@app.post("/chat/stream", response_class=EventSourceResponse)
async def stream_chat(prompt: Prompt) -> AsyncIterable[ServerSentEvent]:
    """模拟 LLM 逐 token 输出；最后一帧 event=done 标记流结束。"""
    words = prompt.text.split()
    for word in words:
        yield ServerSentEvent(data=word, event="token")
    yield ServerSentEvent(raw_data="[DONE]", event="done")
''',
    "docs_src/settings/__init__.py": '''\
"""FastAPI 文档示例：应用配置（Settings）与 pydantic-settings 环境变量加载。"""
''',
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    if rel not in ANNOTATED:
        raise KeyError(f"no annotation template: {rel}")
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists() and rel.endswith("__init__.py"):
        dst.parent.mkdir(parents=True, exist_ok=True)
    elif not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    else:
        dst.parent.mkdir(parents=True, exist_ok=True)
        if not dst.exists():
            shutil.copy2(src, dst)
    content = ANNOTATED[rel]
    if not has_chinese(content):
        raise ValueError(f"No Chinese content for: {rel}")
    dst.write_text(content, encoding="utf-8")


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-fastapi-w22b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def update_batch_json() -> None:
    """Remove completed wave-22b files from batch.json after marking done."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done_set = {ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()}
    batch["files"] = [f for f in batch.get("files", []) if f not in done_set]
    batch["done"] = len(done_set)
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    if not batch["files"]:
        batch["claimed_at"] = None
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def confirm_chinese() -> dict[str, bool]:
    return {
        rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_FILES
    }


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

    analyzed_paths = [f"fastapi/0.141.1/analyzed/{rel}" for rel in BATCH_FILES]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "fastapi 0.141.1: Chinese-annotate wave 22b docs_src [10:20]",
        [*analyzed_paths, script_path],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "fastapi",
            "--version",
            "0.141.1",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_json()
    queue_paths = [
        "fastapi/0.141.1/_reports/class-queue/done.txt",
        "fastapi/0.141.1/_reports/class-queue/pending.txt",
        "fastapi/0.141.1/_reports/class-queue/batch.json",
        "fastapi/0.141.1/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark fastapi 0.141.1 wave22b docs_src [10:20] done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    chinese = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
