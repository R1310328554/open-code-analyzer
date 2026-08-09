"""教程 001：Cookie(default=None) 声明可选 Cookie 参数（非 Annotated 写法）。"""

from fastapi import Cookie, FastAPI

app = FastAPI()


@app.get("/items/")
async def read_items(ads_id: str | None = Cookie(default=None)):
    """读取名为 ads_id 的 Cookie；缺失时默认 None。"""
    return {"ads_id": ads_id}
