"""教程 003：在路由内显式返回 HTMLResponse(content=..., status_code=...)。"""

from fastapi import FastAPI
from fastapi.responses import HTMLResponse

app = FastAPI()


@app.get("/items/")
async def read_items():
    """手动构造 HTMLResponse，可指定状态码与 headers。"""
    html_content = """
    <html>
        <head>
            <title>Some HTML in here</title>
        </head>
        <body>
            <h1>Look ma! HTML!</h1>
        </body>
    </html>
    """
    return HTMLResponse(content=html_content, status_code=200)  # 完全控制响应对象
