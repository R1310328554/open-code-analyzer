"""教程 006：在路由内显式返回 RedirectResponse 实例完成 HTTP 重定向。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()


@app.get("/typer")
async def redirect_typer():
    """构造 RedirectResponse；默认 307 临时重定向到 Typer 官网。"""
    return RedirectResponse("https://typer.tiangolo.com")
