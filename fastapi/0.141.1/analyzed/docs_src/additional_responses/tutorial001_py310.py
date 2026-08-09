"""
附加响应示例 001：为路径操作声明额外状态码（如 404）的响应模型。
"""

from fastapi import FastAPI
from fastapi.responses import JSONResponse
from pydantic import BaseModel


class Item(BaseModel):
    id: str
    value: str


class Message(BaseModel):
    message: str


app = FastAPI()


@app.get("/items/{item_id}", response_model=Item, responses={404: {"model": Message}})
async def read_item(item_id: str):
    if item_id == "foo":
        return {"id": "foo", "value": "there goes my hero"}
    return JSONResponse(status_code=404, content={"message": "Item not found"})
