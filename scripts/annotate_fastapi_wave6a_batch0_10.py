#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-6a docs_src slice [0:10]."""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text())["files"][:10]

ANNOTATED: dict[str, str] = {
    "docs_src/body_nested_models/tutorial005_py310.py": '''\
"""教程 005：Item 含 set[str] tags 与可选嵌套 Image（HttpUrl 校验）。"""

from fastapi import FastAPI
from pydantic import BaseModel, HttpUrl

app = FastAPI()


class Image(BaseModel):
    """嵌套子模型：图片 URL（HttpUrl）与名称。"""
    url: HttpUrl
    name: str


class Item(BaseModel):
    """Item 模型；tags 为集合类型，image 为可选嵌套对象。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()  # JSON 数组解析为 set，自动去重
    image: Image | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """接收含 set tags 与可选嵌套 Image 的 body 并返回。"""
    results = {"item_id": item_id, "item": item}
    return results
''',
    "docs_src/body_nested_models/tutorial006_py310.py": '''\
"""教程 006：Item 含可选 list[Image] 嵌套模型列表。"""

from fastapi import FastAPI
from pydantic import BaseModel, HttpUrl

app = FastAPI()


class Image(BaseModel):
    """嵌套子模型：图片 URL 与名称。"""
    url: HttpUrl
    name: str


class Item(BaseModel):
    """Item 模型；images 为可选 Image 对象列表。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()
    images: list[Image] | None = None  # body 中可为 null 或 Image 数组


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """接收含嵌套 Image 列表的复杂 body 并返回。"""
    results = {"item_id": item_id, "item": item}
    return results
''',
    "docs_src/body_nested_models/tutorial007_py310.py": '''\
"""教程 007：Offer 模型嵌套 list[Item]，Item 再嵌套 list[Image]。"""

from fastapi import FastAPI
from pydantic import BaseModel, HttpUrl

app = FastAPI()


class Image(BaseModel):
    """嵌套子模型：图片 URL 与名称。"""
    url: HttpUrl
    name: str


class Item(BaseModel):
    """Item 子模型，可含 tags 集合与 images 列表。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()
    images: list[Image] | None = None


class Offer(BaseModel):
    """Offer 顶层模型，items 为 Item 对象列表。"""
    name: str
    description: str | None = None
    price: float
    items: list[Item]  # 多层嵌套：Offer → Item → Image


@app.post("/offers/")
async def create_offer(offer: Offer):
    """创建含嵌套 Item 列表的 Offer。"""
    return offer
''',
    "docs_src/body_nested_models/tutorial008_py310.py": '''\
"""教程 008：请求体直接为 list[Image]，非包裹在 Pydantic 模型内。"""

from fastapi import FastAPI
from pydantic import BaseModel, HttpUrl

app = FastAPI()


class Image(BaseModel):
    """图片子模型：URL 与名称。"""
    url: HttpUrl
    name: str


@app.post("/images/multiple/")
async def create_multiple_images(images: list[Image]):
    """body 顶层为 JSON 数组，每项解析为 Image。"""
    return images
''',
    "docs_src/body_nested_models/tutorial009_py310.py": '''\
"""教程 009：请求体为 dict[int, float]（索引 → 权重映射）。"""

from fastapi import FastAPI

app = FastAPI()


@app.post("/index-weights/")
async def create_index_weights(weights: dict[int, float]):
    """接收键为 int、值为 float 的字典 body；JSON 键会转为整数。"""
    return weights
''',
    "docs_src/body_updates/__init__.py": '''\
"""FastAPI 文档示例：PUT 全量替换与 PATCH 部分更新（exclude_unset）。"""
''',
    "docs_src/body_updates/tutorial001_py310.py": '''\
"""教程 001：PUT 全量更新；jsonable_encoder 将模型转为可 JSON 序列化 dict。"""

from fastapi import FastAPI
from fastapi.encoders import jsonable_encoder
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 模型；字段均可选，tax 与 tags 有默认值。"""
    name: str | None = None
    description: str | None = None
    price: float | None = None
    tax: float = 10.5
    tags: list[str] = []


items = {
    "foo": {"name": "Foo", "price": 50.2},
    "bar": {"name": "Bar", "description": "The bartenders", "price": 62, "tax": 20.2},
    "baz": {"name": "Baz", "description": None, "price": 50.2, "tax": 10.5, "tags": []},
}


@app.get("/items/{item_id}", response_model=Item)
async def read_item(item_id: str):
    """读取内存中的 Item 记录。"""
    return items[item_id]


@app.put("/items/{item_id}", response_model=Item)
async def update_item(item_id: str, item: Item):
    """PUT 全量替换：body 覆盖存储项，未传字段使用模型默认值。"""
    update_item_encoded = jsonable_encoder(item)  # 转为 dict，便于持久化
    items[item_id] = update_item_encoded
    return update_item_encoded
''',
    "docs_src/body_updates/tutorial002_py310.py": '''\
"""教程 002：PATCH 部分更新；exclude_unset 仅合并客户端实际发送的字段。"""

from fastapi import FastAPI
from fastapi.encoders import jsonable_encoder
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 模型；字段均可选，便于部分更新。"""
    name: str | None = None
    description: str | None = None
    price: float | None = None
    tax: float = 10.5
    tags: list[str] = []


items = {
    "foo": {"name": "Foo", "price": 50.2},
    "bar": {"name": "Bar", "description": "The bartenders", "price": 62, "tax": 20.2},
    "baz": {"name": "Baz", "description": None, "price": 50.2, "tax": 10.5, "tags": []},
}


@app.get("/items/{item_id}", response_model=Item)
async def read_item(item_id: str):
    """读取内存中的 Item 记录。"""
    return items[item_id]


@app.patch("/items/{item_id}")
async def update_item(item_id: str, item: Item) -> Item:
    """PATCH 部分更新：仅覆盖 body 中显式提供的字段，保留其余存储值。"""
    stored_item_data = items[item_id]
    stored_item_model = Item(**stored_item_data)
    update_data = item.model_dump(exclude_unset=True)  # 忽略未发送字段
    updated_item = stored_item_model.model_copy(update=update_data)
    items[item_id] = jsonable_encoder(updated_item)
    return updated_item
''',
    "docs_src/conditional_openapi/__init__.py": '''\
"""FastAPI 文档示例：通过配置条件启用或禁用 OpenAPI（/docs、/openapi.json）。"""
''',
    "docs_src/conditional_openapi/tutorial001_py310.py": '''\
"""教程 001：BaseSettings 控制 openapi_url；设为 None 可关闭 OpenAPI 端点。"""

from fastapi import FastAPI
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置；openapi_url 默认 "/openapi.json"，生产环境可设为 None。"""
    openapi_url: str = "/openapi.json"


settings = Settings()

app = FastAPI(openapi_url=settings.openapi_url)  # 从环境变量/配置读取


@app.get("/")
def root():
    """根路由；OpenAPI 是否可用由 settings 决定。"""
    return {"message": "Hello World"}
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
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
