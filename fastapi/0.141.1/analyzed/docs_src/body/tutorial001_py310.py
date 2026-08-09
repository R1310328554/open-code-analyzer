"""教程 001：使用 Pydantic 模型声明 POST 请求体并原样返回。"""

from fastapi import FastAPI
from pydantic import BaseModel


class Item(BaseModel):
    """Item 请求体模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


app = FastAPI()


@app.post("/items/")
async def create_item(item: Item):
    """接收 JSON body，FastAPI 自动解析为 Item 并返回。"""
    return item
