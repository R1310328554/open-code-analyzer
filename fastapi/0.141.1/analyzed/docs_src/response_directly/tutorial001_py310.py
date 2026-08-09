"""教程 001：用 jsonable_encoder 序列化 Pydantic 模型后手动返回 JSONResponse。"""

from datetime import datetime

from fastapi import FastAPI
from fastapi.encoders import jsonable_encoder
from fastapi.responses import JSONResponse
from pydantic import BaseModel


class Item(BaseModel):
    """示例资源模型：含 datetime 字段，需编码后才能 JSON 序列化。"""

    title: str
    timestamp: datetime
    description: str | None = None


app = FastAPI()  # 创建 FastAPI 应用实例


@app.put("/items/{id}")
def update_item(id: str, item: Item):
    """jsonable_encoder 将 datetime 等类型转为 JSON 兼容值；再包成 JSONResponse。"""
    json_compatible_item_data = jsonable_encoder(item)
    return JSONResponse(content=json_compatible_item_data)
