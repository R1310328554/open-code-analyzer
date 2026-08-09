"""教程 002：response_class=HTMLResponse，路由函数返回 HTML 字符串。"""

from fastapi import FastAPI
from fastapi.responses import HTMLResponse

app = FastAPI()


@app.get("/items/", response_class=HTMLResponse)  # 文档与 Content-Type 为 text/html
async def read_items():
    """返回 HTML 字符串；FastAPI 包装为 HTMLResponse。"""
    return """
    <html>
        <head>
            <title>Some HTML in here</title>
        </head>
        <body>
            <h1>Look ma! HTML!</h1>
        </body>
    </html>
    """
