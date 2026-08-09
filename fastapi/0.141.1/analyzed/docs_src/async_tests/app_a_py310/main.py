"""被测应用：最小 FastAPI 示例，供 httpx 异步测试调用。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/")
async def root():
    """返回简单 JSON 消息。"""
    return {"message": "Tomato"}
