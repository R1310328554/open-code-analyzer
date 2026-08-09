#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-17b docs_src [10:20]."""
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
    for ln in Path("/tmp/fastapi_w17b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
MARK_NOTE = "wave17b query_params_str_validations [10:20]"

GUARD_FILES = [
    VER / "analyzed/docs_src/python_types/tutorial001_py310.py",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/support/TransactionTemplate.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/query_params_str_validations/tutorial003_py310.py": '''\
"""教程 003：可选查询参数 q 用 Query 的 min_length/max_length 约束长度（3–50）。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(default=None, min_length=3, max_length=50)):
    """q 省略时为 None；提供时须满足长度约束，否则 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial003_an_py310.py": '''\
"""教程 003（Annotated）：Annotated + Query 声明可选 q 的长度校验。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: Annotated[str | None, Query(min_length=3, max_length=50)] = None,
):
    """Annotated 写法等价于 Query(default=None, ...)；校验规则相同。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial004_py310.py": '''\
"""教程 004：Query 增加 pattern 正则，q 须完全匹配 ^fixedquery$。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: str | None = Query(
        default=None, min_length=3, max_length=50, pattern="^fixedquery$"
    ),
):
    """pattern 与 min/max_length 同时生效；不匹配时返回 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial004_an_py310.py": '''\
"""教程 004（Annotated）：Query 的 pattern 约束查询字符串格式。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: Annotated[
        str | None, Query(min_length=3, max_length=50, pattern="^fixedquery$")
    ] = None,
):
    """Annotated 形式集中声明长度与正则校验。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial005_py310.py": '''\
"""教程 005：Query 设置 default="fixedquery"，省略 q 时使用默认值并仍校验 min_length。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str = Query(default="fixedquery", min_length=3)):
    """q 非 Optional；未传参时默认 fixedquery（长度已满足 min_length=3）。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial005_an_py310.py": '''\
"""教程 005（Annotated）：带默认值的必填 Query 与 min_length。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str, Query(min_length=3)] = "fixedquery"):
    """默认写在参数侧；Query 仅承载校验元数据。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial006_py310.py": '''\
"""教程 006：无 default 的 Query 使 q 成为必填查询参数，min_length=3。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str = Query(min_length=3)):
    """缺少 q 时返回 422；提供的 q 须至少 3 个字符。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial006_an_py310.py": '''\
"""教程 006（Annotated）：Annotated[str, Query(...)] 声明必填 q 与长度下限。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str, Query(min_length=3)]):
    """无默认值 ⇒ 客户端必须显式传入 q。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial006c_py310.py": '''\
"""教程 006c：str | None 且无 default——q 可选，但若提供须满足 min_length=3。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(min_length=3)):
    """与 tutorial006 不同：省略 q 合法；传入空串或过短会 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/query_params_str_validations/tutorial006c_an_py310.py": '''\
"""教程 006c（Annotated）：可选 Union 类型 + Query min_length 组合示例。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str | None, Query(min_length=3)]):
    """Annotated 可选写法；行为与 tutorial006c 非 Annotated 版一致。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
''',
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    if rel not in ANNOTATED:
        raise KeyError(f"no annotation template: {rel}")
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
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
    """Clear completed wave-17 batch from batch.json after w17b is marked done."""
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
    if failures:
        print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
        return 1
    tree_count = tree_guard()
    print(json.dumps({"ok": ok, "failures": failures, "note": MARK_NOTE, "tree_count": tree_count}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
