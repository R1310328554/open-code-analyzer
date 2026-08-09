"""教程 002b：用 Enum 成员作为 OpenAPI tags（而非字符串字面量）。"""

from enum import Enum

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


class Tags(Enum):
    """OpenAPI 分组标签；Enum 值会序列化为字符串写入 schema。"""
    items = "items"
    users = "users"


@app.get("/items/", tags=[Tags.items])
async def get_items():
    """列出物品；tags 使用 Tags.items 枚举成员。"""
    return ["Portal gun", "Plumbus"]


@app.get("/users/", tags=[Tags.users])
async def read_users():
    """列出用户；tags 使用 Tags.users 枚举成员。"""
    return ["Rick", "Morty"]
