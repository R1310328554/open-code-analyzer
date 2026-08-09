"""
附加响应示例 003：为不同状态码配置描述、示例与额外响应模型。
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


@app.get(
    "/items/{item_id}",
    response_model=Item,
    responses={
        404: {"model": Message, "description": "未找到该条目"},
        200: {
            "description": "按 ID 请求的条目",
            "content": {
                "application/json": {
                    "example": {"id": "bar", "value": "The bar tenders"}
                }
            },
        },
    },
)
async def read_item(item_id: str):
    if item_id == "foo":
        return {"id": "foo", "value": "there goes my hero"}
    else:
        return JSONResponse(status_code=404, content={"message": "Item not found"})
