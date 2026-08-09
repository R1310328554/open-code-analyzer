"""教程 003：同一应用中混用 pydantic.v1 输入模型与 Pydantic v2 response_model。"""

from fastapi import FastAPI
from pydantic import BaseModel as BaseModelV2
from pydantic.v1 import BaseModel


class Item(BaseModel):
    """请求体：pydantic.v1 模型。"""
    name: str
    description: str | None = None
    size: float


class ItemV2(BaseModelV2):
    """响应：Pydantic v2 模型（由 response_model 序列化输出）。"""
    name: str
    description: str | None = None
    size: float


app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/items/", response_model=ItemV2)
async def create_item(item: Item):
    """接受 v1 Item，FastAPI 按 ItemV2 校验并序列化响应。"""
    return item
