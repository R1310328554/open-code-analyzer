#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-14a docs_src slice [0:10]."""
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
    for ln in Path("/tmp/fastapi_w14a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
W14B_FILES = [
    ln.strip()
    for ln in Path("/tmp/fastapi_w14b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

ANNOTATED: dict[str, str] = {
    "docs_src/path_operation_advanced_configuration/tutorial001_py310.py": '''\
"""教程 001：operation_id 自定义 OpenAPI 中该操作的唯一标识符。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/", operation_id="some_specific_id_you_define")
async def read_items():
    """默认 operation_id 由函数名推导；显式指定便于客户端代码生成与文档引用。"""
    return [{"item_id": "Foo"}]
''',
    "docs_src/path_operation_advanced_configuration/tutorial002_py310.py": '''\
"""教程 002：generate_unique_id_function 全局自定义 operation_id 生成规则。"""

from fastapi import FastAPI
from fastapi.routing import APIRoute


def custom_generate_unique_id(route: APIRoute) -> str:
    """以路由处理函数名 route.name 作为 operation_id（覆盖默认路径+方法拼接）。"""
    return route.name


app = FastAPI(generate_unique_id_function=custom_generate_unique_id)


@app.get("/items/")
async def read_items():
    """本例 operation_id 为 read_items，而非默认的 read_items_items__get。"""
    return [{"item_id": "Foo"}]
''',
    "docs_src/path_operation_advanced_configuration/tutorial003_py310.py": '''\
"""教程 003：include_in_schema=False 将路径操作从 OpenAPI/Swagger 文档中隐藏。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/", include_in_schema=False)
async def read_items():
    """路由仍可正常访问，但不会出现在 /docs 与 openapi.json 中。"""
    return [{"item_id": "Foo"}]
''',
    "docs_src/path_operation_advanced_configuration/tutorial004_py310.py": '''\
"""教程 004：summary 与函数 docstring 丰富 OpenAPI 操作描述与字段说明。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """创建 item 的请求/响应模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post("/items/", summary="Create an item")
async def create_item(item: Item) -> Item:
    """
    创建 item，字段说明如下：

    - **name**：每个 item 必须有名称
    - **description**：较长描述
    - **price**：必填
    - **tax**：无税时可省略
    - **tags**：该 item 的唯一标签字符串集合
    \f
    :param item: 用户提交的请求体。
    """
    return item
''',
    "docs_src/path_operation_advanced_configuration/tutorial005_py310.py": '''\
"""教程 005：openapi_extra 注入自定义 OpenAPI 扩展字段（如 x-aperture-labs-portal）。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/", openapi_extra={"x-aperture-labs-portal": "blue"})
async def read_items():
    """x-* 扩展键会写入生成的 OpenAPI 文档，供工具链或门户读取。"""
    return [{"item_id": "portal-gun"}]
''',
    "docs_src/path_operation_advanced_configuration/tutorial006_py310.py": '''\
"""教程 006：openapi_extra 自定义 requestBody  schema，配合 Request 手动读取原始体。"""

from fastapi import FastAPI, Request

app = FastAPI()


def magic_data_reader(raw_body: bytes):
    """演示性解析：忽略真实 JSON，固定返回 size 与占位 content。"""
    return {
        "size": len(raw_body),
        "content": {
            "name": "Maaaagic",
            "price": 42,
            "description": "Just kiddin', no magic here. ✨",
        },
    }


@app.post(
    "/items/",
    openapi_extra={
        "requestBody": {
            "content": {
                "application/json": {
                    "schema": {
                        "required": ["name", "price"],
                        "type": "object",
                        "properties": {
                            "name": {"type": "string"},
                            "price": {"type": "number"},
                            "description": {"type": "string"},
                        },
                    }
                }
            },
            "required": True,
        },
    },
)
async def create_item(request: Request):
    """OpenAPI 展示自定义 schema；实际用 request.body() 自行处理字节流。"""
    raw_body = await request.body()
    data = magic_data_reader(raw_body)
    return data
''',
    "docs_src/path_operation_advanced_configuration/tutorial007_py310.py": '''\
"""教程 007：openapi_extra 声明 application/x-yaml 请求体，手动解析并 Pydantic 校验。"""

import yaml
from fastapi import FastAPI, HTTPException, Request
from pydantic import BaseModel, ValidationError

app = FastAPI()


class Item(BaseModel):
    """YAML 请求体对应的模型。"""
    name: str
    tags: list[str]


@app.post(
    "/items/",
    openapi_extra={
        "requestBody": {
            "content": {"application/x-yaml": {"schema": Item.model_json_schema()}},
            "required": True,
        },
    },
)
async def create_item(request: Request):
    """读取原始 YAML，safe_load 后 model_validate；解析/校验失败返回 422。"""
    raw_body = await request.body()
    try:
        data = yaml.safe_load(raw_body)
    except yaml.YAMLError:
        raise HTTPException(status_code=422, detail="Invalid YAML")
    try:
        item = Item.model_validate(data)
    except ValidationError as e:
        raise HTTPException(status_code=422, detail=e.errors(include_url=False))
    return item
''',
    "docs_src/path_operation_configuration/__init__.py": '''\
"""FastAPI 文档示例：路径操作配置（status_code、tags、response_model 等装饰器参数）。"""
''',
    "docs_src/path_operation_configuration/tutorial001_py310.py": '''\
"""教程 001：status_code 指定成功响应的 HTTP 状态码（如 POST 创建返回 201）。"""

from fastapi import FastAPI, status
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """创建 item 的请求/响应体。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post("/items/", status_code=status.HTTP_201_CREATED)
async def create_item(item: Item) -> Item:
    """默认 POST 为 200；HTTP_201_CREATED 表示资源已创建。"""
    return item
''',
    "docs_src/path_operation_configuration/tutorial002_py310.py": '''\
"""教程 002：tags 为路径操作分组，在 Swagger UI 中按标签折叠展示。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """items 相关接口共用的模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post("/items/", tags=["items"])
async def create_item(item: Item) -> Item:
    """tags=["items"] 将该操作归入 items 分组。"""
    return item


@app.get("/items/", tags=["items"])
async def read_items():
    """与 create_item 同属 items 标签。"""
    return [{"name": "Foo", "price": 42}]


@app.get("/users/", tags=["users"])
async def read_users():
    """users 标签下的独立分组。"""
    return [{"username": "johndoe"}]
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
    """Mark w14a done; keep w14b files in batch.json."""
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["files"] = W14B_FILES
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
            "wave14a path_operation_* [0:10]",
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_json()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
