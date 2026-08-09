"""教程 001：构造 JSONResponse 时通过 headers 参数附加自定义响应头。"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/headers/")
def get_headers():
    """headers 字典会写入 HTTP 响应头；Content-Language 等可按需设置。"""
    content = {"message": "Hello World"}
    headers = {"X-Cat-Dog": "alone in the world", "Content-Language": "en-US"}
    return JSONResponse(content=content, headers=headers)
