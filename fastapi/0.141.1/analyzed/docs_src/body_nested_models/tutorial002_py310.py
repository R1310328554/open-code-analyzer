"""教程 002：body 模型中的 list[str] 带类型注解。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 模型，tags 为字符串列表。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: list[str] = []  # 元素类型为 str，OpenAPI 会展示数组项 schema


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """接收含类型化 tags 列表的 body 并返回。"""
    results = {"item_id": item_id, "item": item}
    return results
