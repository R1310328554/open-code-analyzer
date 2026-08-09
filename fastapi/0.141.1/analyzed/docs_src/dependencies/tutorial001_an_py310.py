"""教程 001（Annotated）：用 Annotated[dict, Depends(...)] 声明依赖。"""

from typing import Annotated

from fastapi import Depends, FastAPI

app = FastAPI()


async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):
    """与 tutorial001 相同的共享查询参数依赖。"""
    return {"q": q, "skip": skip, "limit": limit}


@app.get("/items/")
async def read_items(commons: Annotated[dict, Depends(common_parameters)]):
    """Annotated 将类型与 Depends 元数据合并，便于复用与 IDE 提示。"""
    return commons


@app.get("/users/")
async def read_users(commons: Annotated[dict, Depends(common_parameters)]):
    """users 端点同样注入 commons。"""
    return commons
