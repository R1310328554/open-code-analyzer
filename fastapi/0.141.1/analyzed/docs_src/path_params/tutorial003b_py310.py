"""教程 003b：同一 HTTP 方法与路径重复注册——后定义会覆盖先定义（文档示例，勿在生产代码中这样写）。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/users")
async def read_users():
    """第一个 GET /users 处理器（会被下方同名路由覆盖）。"""
    return ["Rick", "Morty"]


@app.get("/users")
async def read_users2():
    """第二个 GET /users 处理器；实际生效的是本函数。"""
    return ["Bean", "Elfo"]
