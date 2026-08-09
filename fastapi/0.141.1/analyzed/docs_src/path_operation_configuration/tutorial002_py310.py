"""教程 002：tags 为路径操作分组，在 Swagger UI 中按标签折叠展示。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """items 相关接口共用的模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()


@app.post("/items/", tags=["items"])
async def create_item(item: Item) -> Item:
    """tags=["items"] 将该操作归入 items 分组。"""
    return item


@app.get("/items/", tags=["items"])
async def read_items():
    """与 create_item 同属 items 标签。"""
    return [{"name": "Foo", "price": 42}]


@app.get("/users/", tags=["users"])
async def read_users():
    """users 标签下的独立分组。"""
    return [{"username": "johndoe"}]
