"""教程 001-01：函数返回类型注解 Item / list[Item]——FastAPI 据此生成 OpenAPI 响应 schema。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品模型：name/price 必填；description、tax 与 tags 可选。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: list[str] = []


@app.post("/items/")
async def create_item(item: Item) -> Item:
    """返回类型 Item 声明 POST 响应结构；FastAPI 校验并序列化输出。"""
    return item


@app.get("/items/")
async def read_items() -> list[Item]:
    """返回类型 list[Item] 声明 GET 列表响应为 Item 数组。"""
    return [
        Item(name="Portal Gun", price=42.0),
        Item(name="Plumbus", price=32.0),
    ]
