"""教程 001（Annotated）：从请求 Cookie 读取可选 ads_id 参数。"""

from typing import Annotated

from fastapi import Cookie, FastAPI

app = FastAPI()


@app.get("/items/")
async def read_items(ads_id: Annotated[str | None, Cookie()] = None):
    """Cookie() 将同名 Cookie 解析为 ads_id；未携带时返回 None。"""
    return {"ads_id": ads_id}
