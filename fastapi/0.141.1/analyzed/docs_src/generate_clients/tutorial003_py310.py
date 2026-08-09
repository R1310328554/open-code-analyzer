"""教程 003：自定义 generate_unique_id_function——operationId 形如 {tag}-{route.name}。"""

from fastapi import FastAPI
from fastapi.routing import APIRoute
from pydantic import BaseModel


def custom_generate_unique_id(route: APIRoute):
    """按路由首个 tag 与函数名生成唯一 operationId，供 openapi-generator 等工具使用。"""
    return f"{route.tags[0]}-{route.name}"


app = FastAPI(generate_unique_id_function=custom_generate_unique_id)  # 全局自定义 operationId


class Item(BaseModel):
    """商品资源模型。"""
    name: str
    price: float


class ResponseMessage(BaseModel):
    """通用操作结果消息体。"""
    message: str


class User(BaseModel):
    """用户资源模型。"""
    username: str
    email: str


@app.post("/items/", response_model=ResponseMessage, tags=["items"])
async def create_item(item: Item):
    """创建商品；operationId 为 items-create_item。"""
    return {"message": "Item received"}


@app.get("/items/", response_model=list[Item], tags=["items"])
async def get_items():
    """列出商品；operationId 为 items-get_items。"""
    return [
        {"name": "Plumbus", "price": 3},
        {"name": "Portal Gun", "price": 9001},
    ]


@app.post("/users/", response_model=ResponseMessage, tags=["users"])
async def create_user(user: User):
    """创建用户；operationId 为 users-create_user。"""
    return {"message": "User received"}
