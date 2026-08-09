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
