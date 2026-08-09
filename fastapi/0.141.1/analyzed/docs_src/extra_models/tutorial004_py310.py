"""教程 004：response_model=list[Item]——声明响应为 Item 对象数组。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """列表元素模型：name 与 description。"""
    name: str
    description: str


items = [
    {"name": "Foo", "description": "There comes my hero"},
    {"name": "Red", "description": "It's my aeroplane"},
]


@app.get("/items/", response_model=list[Item])
async def read_items():
    """返回 dict 列表；FastAPI 逐项校验并按 Item schema 序列化。"""
    return items
