"""教程 001-02（Annotated）：将 Annotated 依赖提取为类型别名 CommonsDep。"""

from typing import Annotated

from fastapi import Depends, FastAPI

app = FastAPI()


async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):
    """共享查询参数依赖函数。"""
    return {"q": q, "skip": skip, "limit": limit}


# 类型别名：多处复用时避免重复写 Annotated[dict, Depends(...)]
CommonsDep = Annotated[dict, Depends(common_parameters)]


@app.get("/items/")
async def read_items(commons: CommonsDep):
    """使用 CommonsDep 简化参数声明。"""
    return commons


@app.get("/users/")
async def read_users(commons: CommonsDep):
    """别名在 items 与 users 路由间保持一致。"""
    return commons
