"""教程 001：手动构造 JSONResponse 并通过 set_cookie 写入响应 Cookie。"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse

app = FastAPI()  # 创建 FastAPI 应用实例


@app.post("/cookie/")
def create_cookie():
    """先构建 JSONResponse，再 set_cookie；适合完全自定义响应对象。"""
    content = {"message": "Come to the dark side, we have cookies"}
    response = JSONResponse(content=content)
    response.set_cookie(key="fakesession", value="fake-cookie-session-value")
    return response
