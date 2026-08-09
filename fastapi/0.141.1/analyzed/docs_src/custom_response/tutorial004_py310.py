"""教程 004：response_class 与辅助函数配合，分离 HTML 生成逻辑。"""

from fastapi import FastAPI
from fastapi.responses import HTMLResponse

app = FastAPI()


def generate_html_response():
    """生成 HTMLResponse；可在多处复用。"""
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
    return HTMLResponse(content=html_content, status_code=200)


@app.get("/items/", response_class=HTMLResponse)  # 声明响应媒体类型
async def read_items():
    """返回 HTMLResponse 实例；response_class 用于 OpenAPI 文档。"""
    return generate_html_response()
