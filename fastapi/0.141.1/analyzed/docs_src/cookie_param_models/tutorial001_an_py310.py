"""教程 001（Annotated）：用 Pydantic 模型一次性声明多个 Cookie 字段。"""

from typing import Annotated

from fastapi import Cookie, FastAPI
from pydantic import BaseModel

app = FastAPI()


class Cookies(BaseModel):
    """从请求 Cookie 解析出的字段集合。"""
    session_id: str  # 必填 Cookie
    fatebook_tracker: str | None = None  # 可选追踪 Cookie
    googall_tracker: str | None = None  # 可选追踪 Cookie


@app.get("/items/")
async def read_items(cookies: Annotated[Cookies, Cookie()]):
    """Cookie() 将模型各字段映射为同名 Cookie 并注入。"""
    return cookies
