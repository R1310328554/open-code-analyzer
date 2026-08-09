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
