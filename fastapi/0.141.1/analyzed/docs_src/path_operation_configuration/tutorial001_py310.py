"""教程 001：status_code 指定成功响应的 HTTP 状态码（如 POST 创建返回 201）。"""

from fastapi import FastAPI, status
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """创建 item 的请求/响应体。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post("/items/", status_code=status.HTTP_201_CREATED)
async def create_item(item: Item) -> Item:
    """默认 POST 为 200；HTTP_201_CREATED 表示资源已创建。"""
    return item
