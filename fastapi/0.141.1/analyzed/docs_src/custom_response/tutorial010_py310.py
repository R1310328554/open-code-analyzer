"""教程 010：default_response_class=HTMLResponse 为整个应用设置默认响应类型。"""

from fastapi import FastAPI
from fastapi.responses import HTMLResponse

app = FastAPI(default_response_class=HTMLResponse)  # 全局默认 HTML，无需每路由声明


@app.get("/items/")
async def read_items():
    """返回 HTML 字符串；未指定 response_class 时使用应用级默认值。"""
    return "<h1>Items</h1><p>This is a list of items.</p>"
