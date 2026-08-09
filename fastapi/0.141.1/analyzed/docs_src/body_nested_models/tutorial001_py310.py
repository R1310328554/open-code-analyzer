"""教程 001：body 模型中的 list 字段（未标注元素类型）。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 模型，tags 为 list 类型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: list = []  # 默认空列表；生产环境建议用 Field(default_factory=list)


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """接收含 tags 列表的嵌套 body 并返回。"""
    results = {"item_id": item_id, "item": item}
    return results
