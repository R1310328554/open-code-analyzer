"""教程 001：用 response_class=UJSONResponse 返回更快的 JSON 序列化。"""

from fastapi import FastAPI
from fastapi.responses import UJSONResponse

app = FastAPI()


@app.get("/items/", response_class=UJSONResponse)  # 声明端点使用 UJSON 编码
async def read_items():
    """仍返回 Python 对象；FastAPI 用 UJSONResponse 序列化。"""
    return [{"item_id": "Foo"}]
