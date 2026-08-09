"""教程 006：用 deprecated=True 将路径操作标记为已弃用（OpenAPI 中会显示删除线）。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/", tags=["items"])
async def read_items():
    """列出物品示例路由。"""
    return [{"name": "Foo", "price": 42}]


@app.get("/users/", tags=["users"])
async def read_users():
    """列出用户示例路由。"""
    return [{"username": "johndoe"}]


@app.get("/elements/", tags=["items"], deprecated=True)
async def read_elements():
    """已弃用的 elements 路由；文档中会标注 deprecated。"""
    return [{"item_id": "Foo"}]
