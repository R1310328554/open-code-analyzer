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
