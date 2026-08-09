"""教程 002：为路由添加 tags，生成客户端时可按 tag 分组 API 类/模块。"""

from fastapi import FastAPI
from pydantic import BaseModel  # 请求/响应模型

app = FastAPI()  # OpenAPI 文档含 tags 元数据


class Item(BaseModel):
    """商品资源模型。"""
    name: str  # 商品名称
    price: float  # 单价


class ResponseMessage(BaseModel):
    """操作结果消息。"""
    message: str  # 反馈文本


class User(BaseModel):
    """用户资源模型。"""
    username: str  # 登录名
    email: str  # 邮箱地址


@app.post("/items/", response_model=ResponseMessage, tags=["items"])  # items 分组
async def create_item(item: Item):
    """创建商品。"""
    return {"message": "Item received"}  # 成功确认


@app.get("/items/", response_model=list[Item], tags=["items"])  # 同 tag 下列表接口
async def get_items():
    """列出所有商品。"""
    return [
        {"name": "Plumbus", "price": 3},
        {"name": "Portal Gun", "price": 9001},
    ]


@app.post("/users/", response_model=ResponseMessage, tags=["users"])  # users 分组，客户端可生成 UsersApi
async def create_user(user: User):
    """创建用户。"""
    return {"message": "User received"}  # 成功确认
