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
