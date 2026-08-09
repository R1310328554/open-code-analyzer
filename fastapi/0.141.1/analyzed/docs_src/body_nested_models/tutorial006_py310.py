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
