"""教程 003：固定路径 `/users/me` 须声明在参数化路径 `/users/{user_id}` 之前，避免被误匹配。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/users/me")
async def read_user_me():
    """返回当前用户标识；必须在 `{user_id}` 路由之前注册。"""
    return {"user_id": "the current user"}


@app.get("/users/{user_id}")
async def read_user(user_id: str):
    """按 user_id 查询用户；若 `/users/me` 在后，`me` 会被当作 user_id。"""
    return {"user_id": user_id}
