#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-5a docs_src slice [0:10]."""
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
    "docs_src/body/tutorial003_py310.py": '''\
"""教程 003：PUT 路径参数与 Pydantic 请求体组合更新资源。"""

from fastapi import FastAPI
from pydantic import BaseModel


class Item(BaseModel):
    """Item 请求体模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


app = FastAPI()


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """根据 item_id 更新物品，并将 path 与 body 字段合并返回。"""
    return {"item_id": item_id, **item.model_dump()}
''',
    "docs_src/body/tutorial004_py310.py": '''\
"""教程 004：同时接收路径参数、请求体与可选查询参数 q。"""

from fastapi import FastAPI
from pydantic import BaseModel


class Item(BaseModel):
    """Item 请求体模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


app = FastAPI()


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item, q: str | None = None):
    """更新物品；若提供查询参数 q 则一并放入响应。"""
    result = {"item_id": item_id, **item.model_dump()}
    if q:
        result.update({"q": q})
    return result
''',
    "docs_src/body_fields/__init__.py": '''\
"""FastAPI 文档示例：请求体字段（Body Fields）与 Pydantic Field 元数据。"""
''',
    "docs_src/body_fields/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Body(embed=True) 嵌套 body，Field 声明校验与 OpenAPI 元数据。"""

from typing import Annotated

from fastapi import Body, FastAPI
from pydantic import BaseModel, Field

app = FastAPI()


class Item(BaseModel):
    """Item 模型；部分字段通过 Field 附加 title、长度与数值约束。"""
    name: str
    description: str | None = Field(
        default=None, title="The description of the item", max_length=300
    )
    price: float = Field(gt=0, description="The price must be greater than zero")
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Annotated[Item, Body(embed=True)]):
    """更新物品；embed=True 使 JSON body 形如 {"item": {...}} 而非顶层字段。"""
    results = {"item_id": item_id, "item": item}
    return results
''',
    "docs_src/body_fields/tutorial001_py310.py": '''\
"""教程 001：Body(embed=True) 嵌套 body 与 Field 校验（传统 Body 默认参数语法）。"""

from fastapi import Body, FastAPI
from pydantic import BaseModel, Field

app = FastAPI()


class Item(BaseModel):
    """Item 模型；Field 为 description、price 等字段提供 OpenAPI 与校验规则。"""
    name: str
    description: str | None = Field(
        default=None, title="The description of the item", max_length=300
    )
    price: float = Field(gt=0, description="The price must be greater than zero")
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item = Body(embed=True)):
    """更新物品；body 须嵌套在 item 键下，便于与路径参数区分。"""
    results = {"item_id": item_id, "item": item}
    return results
''',
    "docs_src/body_multiple_params/__init__.py": '''\
"""FastAPI 文档示例：路径、查询与请求体等多类参数同存于一个操作。"""
''',
    "docs_src/body_multiple_params/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Path 约束 item_id，可选查询 q 与可选 body item。"""

from typing import Annotated

from fastapi import FastAPI, Path
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 请求体模型（可选）。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    item_id: Annotated[int, Path(title="The ID of the item to get", ge=0, le=1000)],
    q: str | None = None,
    item: Item | None = None,
):
    """组合 path、query、body；仅将客户端实际提供的字段写入响应。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    if item:
        results.update({"item": item})
    return results
''',
    "docs_src/body_multiple_params/tutorial001_py310.py": '''\
"""教程 001：Path(...) 声明路径约束，可选 q 与 item（关键字-only 参数）。"""

from fastapi import FastAPI, Path
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 请求体模型（可选）。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    *,
    item_id: int = Path(title="The ID of the item to get", ge=0, le=1000),
    q: str | None = None,
    item: Item | None = None,
):
    """* 强制 keyword-only，避免 Path 默认值与 body 参数顺序混淆。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    if item:
        results.update({"item": item})
    return results
''',
    "docs_src/body_multiple_params/tutorial002_py310.py": '''\
"""教程 002：同一请求中接收多个 Pydantic body 模型（Item 与 User）。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """物品 body 模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


class User(BaseModel):
    """用户 body 模型。"""
    username: str
    full_name: str | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item, user: User):
    """FastAPI 将 JSON 中 item、user 两个键分别解析为对应模型。"""
    results = {"item_id": item_id, "item": item, "user": user}
    return results
''',
    "docs_src/body_multiple_params/tutorial003_an_py310.py": '''\
"""教程 003（Annotated）：多 body 模型外加 Body() 声明的标量 importance。"""

from typing import Annotated

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """物品 body 模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


class User(BaseModel):
    """用户 body 模型。"""
    username: str
    full_name: str | None = None


@app.put("/items/{item_id}")
async def update_item(
    item_id: int, item: Item, user: User, importance: Annotated[int, Body()]
):
    """importance 为单独 body 字段，与 item、user 模型键并列出现在 JSON 中。"""
    results = {"item_id": item_id, "item": item, "user": user, "importance": importance}
    return results
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
            "wave5a",
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
