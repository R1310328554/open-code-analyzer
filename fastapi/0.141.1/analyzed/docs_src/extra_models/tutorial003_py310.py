"""教程 003：Union 响应模型 PlaneItem | CarItem——按返回数据动态选择 schema。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class BaseItem(BaseModel):
    """物品基类：description 与 type 字段。"""
    description: str
    type: str


class CarItem(BaseItem):
    """汽车物品：type 固定为 car。"""
    type: str = "car"


class PlaneItem(BaseItem):
    """飞机物品：type 固定为 plane，额外含 size。"""
    type: str = "plane"
    size: int


items = {
    "item1": {"description": "All my friends drive a low rider", "type": "car"},
    "item2": {
        "description": "Music is my aeroplane, it's my aeroplane",
        "type": "plane",
        "size": 5,
    },
}


@app.get("/items/{item_id}", response_model=PlaneItem | CarItem)
async def read_item(item_id: str):
    """返回 dict；FastAPI 按 type 字段匹配 PlaneItem 或 CarItem schema。"""
    return items[item_id]
