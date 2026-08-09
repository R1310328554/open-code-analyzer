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
