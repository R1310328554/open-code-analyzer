"""教程 002（反例）：直接返回 UserIn 会把 password 一并暴露给客户端——生产环境勿用。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()  # 创建 FastAPI 应用实例


class UserIn(BaseModel):
    """含 password 的输入模型；若作为响应返回则敏感字段会泄露。"""

    username: str
    password: str
    email: EmailStr
    full_name: str | None = None


# Don't do this in production!
@app.post("/user/")
async def create_user(user: UserIn) -> UserIn:
    """返回 UserIn 时 password 会出现在响应 JSON 中；应改用 response_model 过滤。"""
    return user
