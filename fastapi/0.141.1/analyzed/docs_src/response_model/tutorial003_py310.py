"""教程 003：response_model=UserOut——输入 UserIn、输出 UserOut，过滤 password 字段。"""

from typing import Any

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()  # 创建 FastAPI 应用实例


class UserIn(BaseModel):
    """请求体：含 password 的完整注册信息。"""

    username: str
    password: str
    email: EmailStr
    full_name: str | None = None


class UserOut(BaseModel):
    """响应体：仅暴露 username、email 与 full_name，不含 password。"""

    username: str
    email: EmailStr
    full_name: str | None = None


@app.post("/user/", response_model=UserOut)
async def create_user(user: UserIn) -> Any:
    """返回 UserIn 实例；response_model=UserOut 自动剔除 password 再序列化。"""
    return user
