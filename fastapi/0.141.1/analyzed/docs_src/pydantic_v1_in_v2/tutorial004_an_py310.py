"""教程 004：迁移期间从 fastapi.temp_pydantic_v1_params 导入 Body 等 v1 参数工具。"""

"""教程 004：迁移期间从 fastapi.temp_pydantic_v1_params 导入 Body 等 v1 参数工具。"""

from typing import Annotated

from fastapi import FastAPI
from fastapi.temp_pydantic_v1_params import Body
from pydantic.v1 import BaseModel


class Item(BaseModel):
    """pydantic.v1 物品模型。"""
    """pydantic.v1 物品模型。"""
    name: str
    description: str | None = None
    size: float


app = FastAPI()  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例


@app.post("/items/")
async def create_item(item: Annotated[Item, Body(embed=True)]) -> Item:
    """embed=True 时 JSON body 须为 {"item": {...}} 结构。"""
    """embed=True 时 JSON body 须为 {"item": {...}} 结构。"""
    return item
