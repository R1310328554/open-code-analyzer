"""教程 006b：response_class=RedirectResponse，路由只需返回目标 URL 字符串。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()


@app.get("/fastapi", response_class=RedirectResponse)
async def redirect_fastapi():
    """返回 URL 字符串；FastAPI 用 RedirectResponse 包装并写入 Location 头。"""
    return "https://fastapi.tiangolo.com"
