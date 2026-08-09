"""教程 004：嵌套 Pydantic 子模型（Image）作为 Item 字段。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Image(BaseModel):
    """嵌套子模型：图片 URL 与名称。"""
    url: str
    name: str


class Item(BaseModel):
    """Item 模型，含 tags 集合与可选嵌套 image。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()
    image: Image | None = None  # 可选嵌套对象，body 中可含 image 子结构


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """接收含嵌套 Image 与 tags 的复杂 body 并返回。"""
    results = {"item_id": item_id, "item": item}
    return results
