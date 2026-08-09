"""教程 002：Cookie 模型 extra=forbid（非 Annotated 写法）。"""

from fastapi import Cookie, FastAPI
from pydantic import BaseModel

app = FastAPI()


class Cookies(BaseModel):
    """声明允许的 Cookie 集合；额外 Cookie 不被接受。"""
    model_config = {"extra": "forbid"}  # Pydantic 拒绝未声明字段

    session_id: str  # 必填 Cookie
    fatebook_tracker: str | None = None
    googall_tracker: str | None = None


@app.get("/items/")
async def read_items(cookies: Cookies = Cookie()):
    """解析 Cookie 为模型；未知 Cookie 键会校验失败。"""
    return cookies
