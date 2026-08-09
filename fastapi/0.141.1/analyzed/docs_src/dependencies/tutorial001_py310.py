"""教程 001：Depends 共享查询参数依赖，多路由复用 common_parameters。"""

from fastapi import Depends, FastAPI

app = FastAPI()


async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):
    """依赖函数：从查询字符串解析 q、skip、limit 并返回 dict。"""
    return {"q": q, "skip": skip, "limit": limit}


@app.get("/items/")
async def read_items(commons: dict = Depends(common_parameters)):
    """Depends(common_parameters) 注入共享查询参数字典。"""
    return commons


@app.get("/users/")
async def read_users(commons: dict = Depends(common_parameters)):
    """同一依赖可在多个路径操作中复用。"""
    return commons
