"""教程 003-01：返回类型 BaseUser——运行时仍接收 UserIn，但响应 JSON 不含 password。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()  # 创建 FastAPI 应用实例


class BaseUser(BaseModel):
    """对外暴露的用户字段：username、email 与 full_name。"""

    username: str
    email: EmailStr
    full_name: str | None = None


class UserIn(BaseUser):
    """请求体模型，在 BaseUser 基础上增加 password。"""

    password: str


@app.post("/user/")
async def create_user(user: UserIn) -> BaseUser:
    """注解 -> BaseUser 使 FastAPI 过滤 password；仅 BaseUser 字段写入响应。"""
    return user
