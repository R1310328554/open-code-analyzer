"""被测应用：带 X-Token 请求头认证的 Items CRUD API。"""

"""被测应用：带 X-Token 请求头认证的 Items CRUD API。"""

from typing import Annotated

from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel

# 模拟密钥令牌，用于校验 X-Token 请求头
# 模拟密钥令牌，用于校验 X-Token 请求头
fake_secret_token = "coneofsilence"

# 模拟内存数据库
# 模拟内存数据库
fake_db = {
    "foo": {"id": "foo", "title": "Foo", "description": "There goes my hero"},
    "bar": {"id": "bar", "title": "Bar", "description": "The bartenders"},
}

app = FastAPI()


class Item(BaseModel):
    """Item 资源的数据模型。"""
    """Item 资源的数据模型。"""
    id: str
    title: str
    description: str | None = None


@app.get("/items/{item_id}", response_model=Item)
async def read_main(item_id: str, x_token: Annotated[str, Header()]):
    """按 ID 读取 Item，需有效 X-Token。"""
    """按 ID 读取 Item，需有效 X-Token。"""
    # 校验请求头令牌
    # 校验请求头令牌
    if x_token != fake_secret_token:
        raise HTTPException(status_code=400, detail="Invalid X-Token header")
    if item_id not in fake_db:
        raise HTTPException(status_code=404, detail="Item not found")
    return fake_db[item_id]


@app.post("/items/")
async def create_item(item: Item, x_token: Annotated[str, Header()]) -> Item:
    """创建 Item，ID 冲突时返回 409。"""
    """创建 Item，ID 冲突时返回 409。"""
    if x_token != fake_secret_token:
        raise HTTPException(status_code=400, detail="Invalid X-Token header")
    if item.id in fake_db:
        raise HTTPException(status_code=409, detail="Item already exists")
    fake_db[item.id] = item.model_dump()
    return item
