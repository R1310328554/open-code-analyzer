"""教程 001：最小 CRUD 风格 API，供 openapi-generator 生成客户端 SDK。"""

from fastapi import FastAPI
from pydantic import BaseModel  # 请求/响应模型，驱动 OpenAPI schema

app = FastAPI()  # 自动生成 /openapi.json 供代码生成工具读取


class Item(BaseModel):
    """商品模型；字段类型会写入 OpenAPI components/schemas。"""
    name: str  # 商品名称
    price: float  # 单价


class ResponseMessage(BaseModel):
    """通用操作结果消息体。"""
    message: str  # 人类可读反馈


@app.post("/items/", response_model=ResponseMessage)  # POST 创建，响应 schema 固定为 ResponseMessage
async def create_item(item: Item):
    """接收 Item JSON，返回确认消息。"""
    return {"message": "item received"}  # 实际项目可持久化 item


@app.get("/items/", response_model=list[Item])  # GET 列表，响应为 Item 数组
async def get_items():
    """返回示例商品列表。"""
    return [  # 硬编码示例数据，生成器据此推断 list[Item] 结构
        {"name": "Plumbus", "price": 3},
        {"name": "Portal Gun", "price": 9001},
    ]
