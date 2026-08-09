"""教程 001：jsonable_encoder 将含 datetime 的 Pydantic 模型转为 JSON 兼容 dict。"""

from datetime import datetime

from fastapi import FastAPI
from fastapi.encoders import jsonable_encoder
from pydantic import BaseModel

# 模拟持久化存储（内存 dict）
fake_db = {}


class Item(BaseModel):
    """请求体模型；timestamp 为 datetime，默认 JSON 无法直接序列化。"""
    title: str
    timestamp: datetime  # datetime 需经 jsonable_encoder 转为 ISO 字符串
    description: str | None = None


app = FastAPI()


@app.put("/items/{id}")
def update_item(id: str, item: Item):
    """PUT 更新；jsonable_encoder 处理 datetime 等非 JSON 原生类型。"""
    json_compatible_item_data = jsonable_encoder(item)  # 转为 str/float/list/dict 等可 JSON 类型
    fake_db[id] = json_compatible_item_data  # 存入 fake_db 供后续读取
