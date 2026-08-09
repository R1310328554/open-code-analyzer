#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-20a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w20a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave20a_batch0_10.py"
MARK_NOTE = "wave20a [0:10]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/response_change_status_code/tutorial001_py310.py": '''\
"""教程 001：根据资源是否存在动态修改 Response.status_code（200 或 201）。"""

from fastapi import FastAPI, Response, status

app = FastAPI()  # 创建 FastAPI 应用实例

tasks = {"foo": "Listen to the Bar Fighters"}  # 模拟内存任务存储


@app.put("/get-or-create-task/{task_id}", status_code=200)
def get_or_create_task(task_id: str, response: Response):
    """路径装饰器默认 200；若任务不存在则写入后改为 201 Created。"""
    if task_id not in tasks:
        tasks[task_id] = "This didn't exist before"
        response.status_code = status.HTTP_201_CREATED
    return tasks[task_id]
''',
    "docs_src/response_cookies/__init__.py": '''\
"""FastAPI 文档示例：在 JSON 响应中设置 Cookie。"""
''',
    "docs_src/response_cookies/tutorial001_py310.py": '''\
"""教程 001：手动构造 JSONResponse 并通过 set_cookie 写入响应 Cookie。"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/cookie/")
def create_cookie():
    """先构建 JSONResponse，再 set_cookie；适合完全自定义响应对象。"""
    content = {"message": "Come to the dark side, we have cookies"}
    response = JSONResponse(content=content)
    response.set_cookie(key="fakesession", value="fake-cookie-session-value")
    return response
''',
    "docs_src/response_cookies/tutorial002_py310.py": '''\
"""教程 002：注入 Response 参数并在返回 JSON 前设置 Cookie。"""

from fastapi import FastAPI, Response

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/cookie-and-object/")
def create_cookie(response: Response):
    """FastAPI 将 response 参数识别为 Starlette Response；可直接 set_cookie。"""
    response.set_cookie(key="fakesession", value="fake-cookie-session-value")
    return {"message": "Come to the dark side, we have cookies"}
''',
    "docs_src/response_directly/__init__.py": '''\
"""FastAPI 文档示例：直接返回 Response 或 JSONResponse 对象。"""
''',
    "docs_src/response_directly/tutorial001_py310.py": '''\
"""教程 001：用 jsonable_encoder 序列化 Pydantic 模型后手动返回 JSONResponse。"""

from datetime import datetime

from fastapi import FastAPI
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from pydantic import BaseModel


class Item(BaseModel):
    """示例资源模型：含 datetime 字段，需编码后才能 JSON 序列化。"""

    title: str
    timestamp: datetime
    description: str | None = None


app = FastAPI()  # 创建 FastAPI 应用实例


@app.put("/items/{id}")
def update_item(id: str, item: Item):
    """jsonable_encoder 将 datetime 等类型转为 JSON 兼容值；再包成 JSONResponse。"""
    json_compatible_item_data = jsonable_encoder(item)
    return JSONResponse(content=json_compatible_item_data)
''',
    "docs_src/response_directly/tutorial002_py310.py": '''\
"""教程 002：直接返回 Response 并指定 media_type 输出非 JSON 内容（如 XML）。"""

from fastapi import FastAPI, Response

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/legacy/")
def get_legacy_data():
    """media_type=application/xml 告知客户端响应体格式；绕过默认 JSON 序列化。"""
    data = """<?xml version="1.0"?>
    <shampoo>
    <Header>
        Apply shampoo here.
    </Header>
    <Body>
        You'll have to use soap here.
    </Body>
    </shampoo>
    """
    return Response(content=data, media_type="application/xml")
''',
    "docs_src/response_headers/__init__.py": '''\
"""FastAPI 文档示例：在响应中添加自定义 HTTP 头。"""
''',
    "docs_src/response_headers/tutorial001_py310.py": '''\
"""教程 001：构造 JSONResponse 时通过 headers 参数附加自定义响应头。"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/headers/")
def get_headers():
    """headers 字典会写入 HTTP 响应头；Content-Language 等可按需设置。"""
    content = {"message": "Hello World"}
    headers = {"X-Cat-Dog": "alone in the world", "Content-Language": "en-US"}
    return JSONResponse(content=content, headers=headers)
''',
    "docs_src/response_headers/tutorial002_py310.py": '''\
"""教程 002：注入 Response 参数并直接修改 response.headers。"""

from fastapi import FastAPI, Response

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/headers-and-object/")
def get_headers(response: Response):
    """与 tutorial001 等效思路：先改 headers，再返回将被序列化的 JSON 体。"""
    response.headers["X-Cat-Dog"] = "alone in the world"
    return {"message": "Hello World"}
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
    index_file = Path("/tmp/git-index-fastapi-w20a")
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
        "fastapi 0.141.1: Chinese-annotate wave 20a docs_src [0:10]",
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
    queue_paths = [
        "fastapi/0.141.1/_reports/class-queue/done.txt",
        "fastapi/0.141.1/_reports/class-queue/pending.txt",
        "fastapi/0.141.1/_reports/class-queue/batch.json",
        "fastapi/0.141.1/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark fastapi 0.141.1 wave20a docs_src [0:10] done",
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
