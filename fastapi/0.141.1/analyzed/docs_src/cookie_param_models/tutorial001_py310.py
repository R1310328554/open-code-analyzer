"""教程 001：Pydantic Cookie 模型（非 Annotated 默认参数写法）。"""

from fastapi import Cookie, FastAPI
from pydantic import BaseModel

app = FastAPI()


class Cookies(BaseModel):
    """Cookie 字段模型；字段名须与 Cookie 键一致。"""
    session_id: str  # 必填 Cookie
    fatebook_tracker: str | None = None
    googall_tracker: str | None = None


@app.get("/items/")
async def read_items(cookies: Cookies = Cookie()):
    """`= Cookie()` 声明整个模型来自 Cookie 参数。"""
    return cookies
