"""教程 002（Annotated）：Cookie 模型禁止额外字段（extra=forbid）。"""

from typing import Annotated

from fastapi import Cookie, FastAPI
from pydantic import BaseModel

app = FastAPI()


class Cookies(BaseModel):
    """仅允许声明过的 Cookie；未知 Cookie 将导致校验失败。"""
    model_config = {"extra": "forbid"}  # 拒绝模型未定义的 Cookie

    session_id: str  # 必填 Cookie
    fatebook_tracker: str | None = None
    googall_tracker: str | None = None


@app.get("/items/")
async def read_items(cookies: Annotated[Cookies, Cookie()]):
    """返回校验通过的 Cookie 模型；多余 Cookie 会触发 422。"""
    return cookies
