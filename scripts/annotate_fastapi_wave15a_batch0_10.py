#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-15a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w15a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
W15B_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w15b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/path_params/tutorial004_py310.py": '''\
"""教程 004：`:path` 转换器捕获含斜杠的完整路径段（如 files/home/user/file.txt）。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/files/{file_path:path}")
async def read_file(file_path: str):
    """`{file_path:path}` 匹配 `/files/` 后的整段路径，含多级目录。"""
    return {"file_path": file_path}
''',
    "docs_src/path_params/tutorial005_py310.py": '''\
"""教程 005：路径参数使用 Enum，仅接受枚举成员对应的字符串值。"""

from enum import Enum

from fastapi import FastAPI


class ModelName(str, Enum):
    """可选模型名称；路径中须为 alexnet、resnet 或 lenet 之一。"""
    alexnet = "alexnet"
    resnet = "resnet"
    lenet = "lenet"


app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/models/{model_name}")
async def get_model(model_name: ModelName):
    """非法枚举值会返回 422；合法值按成员分支返回不同 message。"""
    if model_name is ModelName.alexnet:
        return {"model_name": model_name, "message": "Deep Learning FTW!"}

    if model_name.value == "lenet":
        return {"model_name": model_name, "message": "LeCNN all the images"}

    return {"model_name": model_name, "message": "Have some residuals"}
''',
    "docs_src/path_params_numeric_validations/__init__.py": '''\
"""FastAPI 文档示例：路径参数数值校验（Path 的 ge/le/gt/lt 及元数据）。"""
''',
    "docs_src/path_params_numeric_validations/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Annotated[int, Path(...)] 声明路径参数；Query(alias=...) 指定查询参数名。"""

from typing import Annotated

from fastapi import FastAPI, Path, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: Annotated[int, Path(title="The ID of the item to get")],
    q: Annotated[str | None, Query(alias="item-query")] = None,
):
    """Path title 写入 OpenAPI；查询参数在 URL 中以 item-query 出现。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/path_params_numeric_validations/tutorial001_py310.py": '''\
"""教程 001：Path(title=...) 为路径参数添加 OpenAPI 元数据；Query(alias=...) 指定查询参数名。"""

from fastapi import FastAPI, Path, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: int = Path(title="The ID of the item to get"),
    q: str | None = Query(default=None, alias="item-query"),
):
    """与 Annotated 版等价：item_id 来自路径，q 来自 ?item-query= 查询串。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/path_params_numeric_validations/tutorial002_an_py310.py": '''\
"""教程 002（Annotated）：无默认值的 q 在前会被视为查询参数；Path 显式标记 item_id。"""

from typing import Annotated

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    q: str, item_id: Annotated[int, Path(title="The ID of the item to get")]
):
    """参数顺序敏感：未用 Path/Query 标注的简单类型 q 解析为查询参数。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/path_params_numeric_validations/tutorial002_py310.py": '''\
"""教程 002：无默认值的 q 在前会被视为查询参数；item_id 须用 Path() 显式标记。"""

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(q: str, item_id: int = Path(title="The ID of the item to get")):
    """与 Annotated 版等价：q 来自查询串，item_id 来自路径 {item_id}。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/path_params_numeric_validations/tutorial003_an_py310.py": '''\
"""教程 003（Annotated）：关键字参数顺序下 Path 与查询参数 q 的声明。"""

from typing import Annotated

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: Annotated[int, Path(title="The ID of the item to get")], q: str
):
    """item_id 在前且带 Path；q 无默认值时作为必填查询参数。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/path_params_numeric_validations/tutorial003_py310.py": '''\
"""教程 003：`*` 强制关键字参数，消除顺序歧义——item_id 为路径参数，q 为查询参数。"""

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(*, item_id: int = Path(title="The ID of the item to get"), q: str):
    """`*` 之后参数必须按名传递，避免与 tutorial002 的顺序陷阱混淆。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
''',
    "docs_src/path_params_numeric_validations/tutorial004_an_py310.py": '''\
"""教程 004（Annotated）：Path(ge=1) 要求 item_id ≥ 1，否则返回 422 校验错误。"""

from typing import Annotated

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: Annotated[int, Path(title="The ID of the item to get", ge=1)], q: str
):
    """ge=1 为数值下界；0 或负数路径会被 FastAPI 自动拒绝。"""
    results = {"item_id": item_id}
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


def update_batch_json() -> None:
    """Mark w15a done; keep w15b files in batch.json."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["files"] = W15B_FILES
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
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "fastapi",
            "--version",
            "0.141.1",
            "--note",
            "wave15a path_params/numeric_validations [0:10]",
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_json()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
