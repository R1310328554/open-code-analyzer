"""教程 003：在路径操作装饰器上设置 summary 与 description（OpenAPI 文档摘要与说明）。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """创建物品时的请求/响应模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post(
    "/items/",
    summary="Create an item",  # OpenAPI 中显示的简短标题（此处保留英文原文）
    description="Create an item with all the information, name, description, price, tax and a set of unique tags",  # 路径操作的长描述
)
async def create_item(item: Item) -> Item:
    """接收 Item 请求体并原样返回（演示 summary/description 配置）。"""
    return item
