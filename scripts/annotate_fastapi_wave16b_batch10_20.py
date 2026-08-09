#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-16b docs_src [10:20]."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w16b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
W16A_FILES = BATCH_FILES[:0]  # wave16b only; w16a handled separately
MARK_NOTE = "wave16b python_types/query_param_models/query_params [10:20]"

ANNOTATED: dict[str, str] = {
    "docs_src/python_types/tutorial009_py310.py": '''\
"""教程 009：`str | None` 联合类型表示可选参数；默认 None 时走通用问候分支。"""


def say_hi(name: str | None = None):
    """name 为 None 时打印 Hello World，否则按名字个性化问候。"""
    if name is not None:
        print(f"Hey {name}!")
    else:
        print("Hello World")
''',
    "docs_src/python_types/tutorial010_py310.py": '''\
"""教程 010：自定义类可作为类型注解，静态检查器会校验传入对象是否为该类型。"""


class Person:
    """简单人物模型，构造时保存 name 属性。"""

    def __init__(self, name: str):
        self.name = name


def get_person_name(one_person: Person):
    """参数须为 Person 实例；通过 .name 返回姓名字符串。"""
    return one_person.name
''',
    "docs_src/python_types/tutorial011_py310.py": '''\
"""教程 011：Pydantic BaseModel 自动校验并转换外部数据（类型强制与默认值）。"""

from datetime import datetime

from pydantic import BaseModel


class User(BaseModel):
    """用户模型：id 转 int，signup_ts 解析 datetime，friends 各元素转 int。"""
    id: int
    name: str = "John Doe"  # 缺省时使用默认名
    signup_ts: datetime | None = None
    friends: list[int] = []  # 模型内可变默认值可安全复用


external_data = {
    "id": "123",  # 字符串会被强制转为 int
    "signup_ts": "2017-06-01 12:22",  # 自动解析为 datetime
    "friends": [1, "2", b"3"],  # 各元素统一转为 int
}
user = User(**external_data)
print(user)
# > User id=123 name='John Doe' signup_ts=datetime.datetime(2017, 6, 1, 12, 22) friends=[1, 2, 3]
print(user.id)
# > 123
''',
    "docs_src/python_types/tutorial013_py310.py": '''\
"""教程 013：`Annotated[T, metadata]` 附加元数据，不影响运行时类型或校验行为。"""

from typing import Annotated


def say_hello(name: Annotated[str, "this is just metadata"]) -> str:
    """第二个参数仅为文档/工具元数据，运行时仍按 str 处理。"""
    return f"Hello {name}"
''',
    "docs_src/query_param_models/__init__.py": '''\
"""FastAPI 文档示例：用 Pydantic 模型声明查询参数（Query 模型）。"""
''',
    "docs_src/query_param_models/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：Pydantic 模型 + Query() 一次声明多个查询参数。"""

from typing import Annotated, Literal

from fastapi import FastAPI, Query
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class FilterParams(BaseModel):
    """列表过滤查询参数：limit/offset/order_by/tags 均从 URL 查询字符串解析。"""
    limit: int = Field(100, gt=0, le=100)  # 1–100
    offset: int = Field(0, ge=0)  # 分页偏移 ≥0
    order_by: Literal["created_at", "updated_at"] = "created_at"  # 排序字段枚举
    tags: list[str] = []  # 重复 query key 解析为列表


@app.get("/items/")
async def read_items(filter_query: Annotated[FilterParams, Query()]):
    """Query() 将模型各字段映射为查询参数并校验后注入。"""
    return filter_query
''',
    "docs_src/query_param_models/tutorial001_py310.py": '''\
"""教程 001：FilterParams = Query() 将 Pydantic 模型字段声明为查询参数（非 Annotated 写法）。"""

from typing import Literal

from fastapi import FastAPI, Query
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class FilterParams(BaseModel):
    """列表过滤查询参数：limit/offset/order_by/tags 均从 URL 查询字符串解析。"""
    limit: int = Field(100, gt=0, le=100)  # 1–100
    offset: int = Field(0, ge=0)  # 分页偏移 ≥0
    order_by: Literal["created_at", "updated_at"] = "created_at"  # 排序字段枚举
    tags: list[str] = []  # 重复 query key 解析为列表


@app.get("/items/")
async def read_items(filter_query: FilterParams = Query()):
    """Query() 将模型各字段映射为查询参数并校验后注入。"""
    return filter_query
''',
    "docs_src/query_param_models/tutorial002_an_py310.py": '''\
"""教程 002（Annotated）：Query 模型设置 extra=forbid，拒绝未声明的额外查询参数。"""

from typing import Annotated, Literal

from fastapi import FastAPI, Query
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class FilterParams(BaseModel):
    """过滤参数模型；extra=forbid 时未知 query key 会触发 422。"""
    model_config = {"extra": "forbid"}

    limit: int = Field(100, gt=0, le=100)  # 1–100
    offset: int = Field(0, ge=0)  # 分页偏移 ≥0
    order_by: Literal["created_at", "updated_at"] = "created_at"  # 排序字段枚举
    tags: list[str] = []  # 重复 query key 解析为列表


@app.get("/items/")
async def read_items(filter_query: Annotated[FilterParams, Query()]):
    """仅允许模型已声明字段；额外 query 参数会被 Pydantic 拒绝。"""
    return filter_query
''',
    "docs_src/query_param_models/tutorial002_py310.py": '''\
"""教程 002：extra=forbid 的 Query 模型（非 Annotated 写法）。"""

from typing import Literal

from fastapi import FastAPI, Query
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class FilterParams(BaseModel):
    """过滤参数模型；extra=forbid 时未知 query key 会触发 422。"""
    model_config = {"extra": "forbid"}

    limit: int = Field(100, gt=0, le=100)  # 1–100
    offset: int = Field(0, ge=0)  # 分页偏移 ≥0
    order_by: Literal["created_at", "updated_at"] = "created_at"  # 排序字段枚举
    tags: list[str] = []  # 重复 query key 解析为列表


@app.get("/items/")
async def read_items(filter_query: FilterParams = Query()):
    """仅允许模型已声明字段；额外 query 参数会被 Pydantic 拒绝。"""
    return filter_query
''',
    "docs_src/query_params/__init__.py": '''\
"""FastAPI 文档示例：查询参数（query parameters）基础用法。"""
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


def update_batch_queue() -> None:
    """Keep wave-16a files in batch.json after wave-16b is marked done."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    w16a = [
        ln.strip()
        for ln in Path("/tmp/fastapi_w16a.txt").read_text(encoding="utf-8").splitlines()
        if ln.strip()
    ] if Path("/tmp/fastapi_w16a.txt").exists() else batch["files"][:10]
    batch["files"] = w16a
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
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
    print(json.dumps({"ok": ok, "failures": failures, "note": MARK_NOTE}, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
