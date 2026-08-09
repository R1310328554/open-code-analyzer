"""教程 004：关键字-only 参数与 Body(gt=0) 校验（非 Annotated 写法）。"""

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
    """User 请求体模型。"""
    username: str
    full_name: str | None = None


@app.put("/items/{item_id}")
async def update_item(
    *,
    item_id: int,
    item: Item,
    user: User,
    importance: int = Body(gt=0),  # body 单值，必须大于 0
    q: str | None = None,
):
    # 组装响应；q 为可选 query 参数
    results = {"item_id": item_id, "item": item, "user": user, "importance": importance}
    if q:
        # 仅在提供 query q 时附加
        results.update({"q": q})
    return results
