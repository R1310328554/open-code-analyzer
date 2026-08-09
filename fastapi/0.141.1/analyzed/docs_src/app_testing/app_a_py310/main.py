"""被测应用：最小 FastAPI 示例，提供根路径 GET 接口。"""

"""被测应用：最小 FastAPI 示例，提供根路径 GET 接口。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/")
async def read_main():
    """返回 Hello World JSON。"""
    """返回 Hello World JSON。"""
    return {"msg": "Hello World"}
