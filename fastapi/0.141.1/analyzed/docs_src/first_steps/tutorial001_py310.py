"""教程 001：最小 FastAPI 应用——async 路由返回 JSON Hello World。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/")
async def root():
    """GET / 返回 {"message": "Hello World"}；async 路由在 I/O 等待时可让出事件循环。"""
    return {"message": "Hello World"}
