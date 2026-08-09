"""教程 006c：RedirectResponse 配合 status_code=302 指定重定向状态码。"""

from fastapi import FastAPI
from fastapi.responses import RedirectResponse

app = FastAPI()


@app.get("/pydantic", response_class=RedirectResponse, status_code=302)
async def redirect_pydantic():
    """302 Found；response_class 与 status_code 可同时在装饰器上声明。"""
    return "https://docs.pydantic.dev/"
