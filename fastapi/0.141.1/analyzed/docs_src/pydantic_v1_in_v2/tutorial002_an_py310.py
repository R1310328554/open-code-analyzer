"""教程 002：FastAPI 路径操作可直接使用 pydantic.v1 的 BaseModel 作为请求/响应模型。"""

"""教程 002：FastAPI 路径操作可直接使用 pydantic.v1 的 BaseModel 作为请求/响应模型。"""

"""教程 002：FastAPI 路径操作可直接使用 pydantic.v1 的 BaseModel 作为请求/响应模型。"""

from fastapi import FastAPI
from pydantic.v1 import BaseModel


class Item(BaseModel):
    """pydantic.v1 物品模型。"""
    """pydantic.v1 物品模型。"""
    """pydantic.v1 物品模型。"""
    name: str
    description: str | None = None
    size: float


app = FastAPI()  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例


@app.post("/items/")
async def create_item(item: Item) -> Item:
    """接收 v1 Item 请求体并原样返回。"""
    """接收 v1 Item 请求体并原样返回。"""
    """接收 v1 Item 请求体并原样返回。"""
    return item
