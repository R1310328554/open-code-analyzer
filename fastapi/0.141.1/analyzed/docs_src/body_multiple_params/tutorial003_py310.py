"""教程 003：路径参数、多个 body 模型与单值 body 字段（Body）并存。"""

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 请求体模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


class User(BaseModel):
    """User 请求体模型（与 Item 同为 body 参数）。"""
    username: str
    full_name: str | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item, user: User, importance: int = Body()):
    """合并 item、user 两个模型与 importance 单值 body 字段后返回。"""
    # 汇总路径参数与全部 body 字段
    results = {"item_id": item_id, "item": item, "user": user, "importance": importance}
    return results
