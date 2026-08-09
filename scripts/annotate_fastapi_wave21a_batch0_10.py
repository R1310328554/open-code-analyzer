#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-21a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w21a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_fastapi_wave21a_batch0_10.py"
MARK_NOTE = "wave21a docs_src [0:10]"

GUARD_FILES = [
    VER / "analyzed/docs_src/query_params/tutorial001_py310.py",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

ANNOTATED: dict[str, str] = {
    "docs_src/response_model/tutorial004_py310.py": '''\
"""教程 004：response_model_exclude_unset=True——仅序列化实际赋值字段，省略未设置默认值。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品模型；exclude_unset 使返回 dict 中未赋字段不会出现在 JSON 里。"""

    name: str
    description: str | None = None
    price: float
    tax: float = 10.5
    tags: list[str] = []


items = {
    "foo": {"name": "Foo", "price": 50.2},
    "bar": {"name": "Bar", "description": "The bartenders", "price": 62, "tax": 20.2},
    "baz": {"name": "Baz", "description": None, "price": 50.2, "tax": 10.5, "tags": []},
}


@app.get("/items/{item_id}", response_model=Item, response_model_exclude_unset=True)
async def read_item(item_id: str):
    """foo 仅含 name/price 时响应不含 description、tax、tags 等未设置键。"""
    return items[item_id]
''',
    "docs_src/response_model/tutorial005_py310.py": '''\
"""教程 005：response_model_include / exclude（集合语法）——按字段白名单或黑名单裁剪响应。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """全量字段模型；不同路由通过 include/exclude 控制对外暴露的子集。"""

    name: str
    description: str | None = None
    price: float
    tax: float = 10.5


items = {
    "foo": {"name": "Foo", "price": 50.2},
    "bar": {"name": "Bar", "description": "The Bar fighters", "price": 62, "tax": 20.2},
    "baz": {
        "name": "Baz",
        "description": "There goes my baz",
        "price": 50.2,
        "tax": 10.5,
    },
}


@app.get(
    "/items/{item_id}/name",
    response_model=Item,
    response_model_include={"name", "description"},
)
async def read_item_name(item_id: str):
    """include 仅保留 name 与 description；price、tax 不会出现在响应 JSON。"""
    return items[item_id]


@app.get("/items/{item_id}/public", response_model=Item, response_model_exclude={"tax"})
async def read_item_public_data(item_id: str):
    """exclude 移除 tax；其余 Item 字段照常序列化。"""
    return items[item_id]
''',
    "docs_src/response_model/tutorial006_py310.py": '''\
"""教程 006：response_model_include / exclude（列表语法）——与 tutorial005 等效，写法略有不同。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """全量字段模型；include/exclude 接受 list 与 set，语义相同。"""

    name: str
    description: str | None = None
    price: float
    tax: float = 10.5


items = {
    "foo": {"name": "Foo", "price": 50.2},
    "bar": {"name": "Bar", "description": "The Bar fighters", "price": 62, "tax": 20.2},
    "baz": {
        "name": "Baz",
        "description": "There goes my baz",
        "price": 50.2,
        "tax": 10.5,
    },
}


@app.get(
    "/items/{item_id}/name",
    response_model=Item,
    response_model_include=["name", "description"],
)
async def read_item_name(item_id: str):
    """response_model_include 列表形式；仅输出 name 与 description。"""
    return items[item_id]


@app.get("/items/{item_id}/public", response_model=Item, response_model_exclude=["tax"])
async def read_item_public_data(item_id: str):
    """response_model_exclude 列表形式；从响应 JSON 中剔除 tax。"""
    return items[item_id]
''',
    "docs_src/response_status_code/__init__.py": '''\
"""FastAPI 文档示例：为路由指定 HTTP 响应状态码（status_code）。"""
''',
    "docs_src/response_status_code/tutorial001_py310.py": '''\
"""教程 001：路径装饰器 status_code=201——创建资源成功时返回 201 Created。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/items/", status_code=201)
async def create_item(name: str):
    """status_code=201 覆盖默认 200；OpenAPI 文档同步标注创建成功状态码。"""
    return {"name": name}
''',
    "docs_src/response_status_code/tutorial002_py310.py": '''\
"""教程 002：使用 status.HTTP_201_CREATED 常量代替裸数字，可读性与 IDE 补全更好。"""

from fastapi import FastAPI, status

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/items/", status_code=status.HTTP_201_CREATED)
async def create_item(name: str):
    """status 模块提供命名常量；与 tutorial001 行为一致。"""
    return {"name": name}
''',
    "docs_src/schema_extra_example/__init__.py": '''\
"""FastAPI 文档示例：在 OpenAPI schema 中添加 json_schema_extra 示例数据。"""
''',
    "docs_src/schema_extra_example/tutorial001_py310.py": '''\
"""教程 001：model_config json_schema_extra——在模型级 OpenAPI schema 嵌入 examples。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """model_config["json_schema_extra"]["examples"] 写入 Swagger UI 示例请求体。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None

    model_config = {
        "json_schema_extra": {
            "examples": [
                {
                    "name": "Foo",
                    "description": "A very nice Item",
                    "price": 35.4,
                    "tax": 3.2,
                }
            ]
        }
    }


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """Item 的 schema 示例会出现在 /docs 交互界面，便于客户端理解字段格式。"""
    results = {"item_id": item_id, "item": item}
    return results
''',
    "docs_src/schema_extra_example/tutorial002_py310.py": '''\
"""教程 002：Field(examples=[...])——逐字段在 OpenAPI schema 中声明示例值。"""

from fastapi import FastAPI
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """每个 Field 的 examples 会独立写入对应属性的 schema 元数据。"""

    name: str = Field(examples=["Foo"])
    description: str | None = Field(default=None, examples=["A very nice Item"])
    price: float = Field(examples=[35.4])
    tax: float | None = Field(default=None, examples=[3.2])


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """字段级 examples 比 model_config 更细粒度；Swagger 按属性展示示例。"""
    results = {"item_id": item_id, "item": item}
    return results
''',
    "docs_src/schema_extra_example/tutorial003_an_py310.py": '''\
"""教程 003（Annotated）：Body(examples=[...])——为单个请求体参数附加 OpenAPI 示例。"""

from typing import Annotated

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """请求体模型；示例通过 Annotated + Body 绑定到 item 参数。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(
    item_id: int,
    item: Annotated[
        Item,
        Body(
            examples=[
                {
                    "name": "Foo",
                    "description": "A very nice Item",
                    "price": 35.4,
                    "tax": 3.2,
                }
            ],
        ),
    ],
):
    """Body(examples=...) 覆盖该 endpoint 请求体示例；适合多参数时单独标注。"""
    results = {"item_id": item_id, "item": item}
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
    index_file = Path("/tmp/git-index-fastapi-w21a")
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
        "fastapi 0.141.1: Chinese-annotate wave 21a docs_src [0:10]",
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
        "queue: mark fastapi 0.141.1 wave21a docs_src [0:10] done",
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
