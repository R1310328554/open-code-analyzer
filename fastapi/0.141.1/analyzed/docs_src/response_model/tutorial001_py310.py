"""教程 001：response_model=Item / list[Item]——显式声明响应模型，返回 Any 也可被过滤序列化。"""

from typing import Any

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例


class Item(BaseModel):
    """商品字段定义；response_model 会据此过滤/校验实际返回的 JSON。"""

    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: list[str] = []


@app.post("/items/", response_model=Item)
async def create_item(item: Item) -> Any:
    """response_model=Item 覆盖返回类型注解；即使标注 Any 也按 Item 序列化。"""
    return item


@app.get("/items/", response_model=list[Item])
async def read_items() -> Any:
    """可返回 dict 列表；FastAPI 按 list[Item] 校验并转换为 Item JSON。"""
    return [
        {"name": "Portal Gun", "price": 42.0},
        {"name": "Plumbus", "price": 32.0},
    ]
