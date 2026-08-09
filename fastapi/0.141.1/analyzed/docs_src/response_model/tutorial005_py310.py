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
