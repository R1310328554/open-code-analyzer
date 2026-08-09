#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-17a docs_src slice [0:10]."""
from __future__ import annotations

import json
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
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w17a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
W17B_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w17b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/query_params/tutorial001_py310.py": '''\
"""教程 001：查询参数 skip、limit 带默认值——用于分页切片 fake_items_db。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例

fake_items_db = [{"item_name": "Foo"}, {"item_name": "Bar"}, {"item_name": "Baz"}]


@app.get("/items/")
async def read_item(skip: int = 0, limit: int = 10):
    """skip/limit 来自 ?skip=&limit= 查询串；未传时使用默认值 0 与 10。"""
    return fake_items_db[skip : skip + limit]
''',
    "docs_src/query_params/tutorial002_py310.py": '''\
"""教程 002：可选查询参数 q——`str | None = None` 表示可省略。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_item(item_id: str, q: str | None = None):
    """item_id 来自路径；q 来自 ?q= 查询串，省略时 q 为 None。"""
    if q:
        return {"item_id": item_id, "q": q}
    return {"item_id": item_id}
''',
    "docs_src/query_params/tutorial003_py310.py": '''\
"""教程 003：多个查询参数——可选 q 与带默认值的 bool 型 short。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_item(item_id: str, q: str | None = None, short: bool = False):
    """short=False 时附带长 description；?short=true 可省略描述字段。"""
    item = {"item_id": item_id}
    if q:
        item.update({"q": q})
    if not short:
        item.update(
            {"description": "This is an amazing item that has a long description"}
        )
    return item
''',
    "docs_src/query_params/tutorial004_py310.py": '''\
"""教程 004：多路径参数与查询参数并存——user_id、item_id 来自路径，q/short 来自查询串。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/users/{user_id}/items/{item_id}")
async def read_user_item(
    user_id: int, item_id: str, q: str | None = None, short: bool = False
):
    """路径与查询参数可任意组合；FastAPI 按类型与位置自动解析。"""
    item = {"item_id": item_id, "owner_id": user_id}
    if q:
        item.update({"q": q})
    if not short:
        item.update(
            {"description": "This is an amazing item that has a long description"}
        )
    return item
''',
    "docs_src/query_params/tutorial005_py310.py": '''\
"""教程 005：必填查询参数——无默认值的 needy 必须出现在 ?needy= 中，否则 422。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_user_item(item_id: str, needy: str):
    """needy 无默认值，FastAPI 将其视为必填查询参数。"""
    item = {"item_id": item_id, "needy": needy}
    return item
''',
    "docs_src/query_params/tutorial006_py310.py": '''\
"""教程 006：必填与可选查询参数混用——needy 必填，skip/limit 可选。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_user_item(
    item_id: str, needy: str, skip: int = 0, limit: int | None = None
):
    """limit 可为 None（未传）；skip 默认 0；needy 始终必填。"""
    item = {"item_id": item_id, "needy": needy, "skip": skip, "limit": limit}
    return item
''',
    "docs_src/query_params_str_validations/__init__.py": '''\
"""FastAPI 文档示例：查询参数字符串校验（Query 的 max_length、pattern 等）。"""
''',
    "docs_src/query_params_str_validations/tutorial001_py310.py": '''\
"""教程 001：可选字符串查询参数 q——后续示例将为其添加长度等校验。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = None):
    """q 为可选查询参数；传入时合并进响应 JSON。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial002_an_py310.py": '''\
"""教程 002（Annotated）：Query(max_length=50) 限制 q 最长 50 字符，超长返回 422。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str | None, Query(max_length=50)] = None):
    """Annotated 将校验元数据与类型绑定；与 tutorial002 传统写法等价。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial002_py310.py": '''\
"""教程 002：Query(default=None, max_length=50) 为 q 添加最大长度校验。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(default=None, max_length=50)):
    """显式 Query() 声明查询参数校验；default=None 保持可选。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
}

GUARD_FILES = [
    VER / "analyzed/docs_src/python_types/tutorial001_py310.py",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/support/TransactionTemplate.java",
]


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


def tree_guard() -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"]).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if not path.exists():
            raise RuntimeError(f"guard file missing: {path}")
        if not has_chinese(path.read_text(encoding="utf-8")):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def update_batch_json() -> None:
    """Advance batch.json to wave-17b after w17a is marked done."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["files"] = W17B_FILES
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
    tree_count = tree_guard()
    print(json.dumps({"tree_count": tree_count, "annotated": len(BATCH_FILES)}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
