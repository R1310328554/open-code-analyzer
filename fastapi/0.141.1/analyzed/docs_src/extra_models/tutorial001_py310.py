"""教程 001：UserIn/UserOut/UserInDB 分离——response_model 过滤 password 与 hashed_password。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()


class UserIn(BaseModel):
    """请求体模型：含明文 password，客户端创建用户时提交。"""
    username: str
    password: str
    email: EmailStr
    full_name: str | None = None


class UserOut(BaseModel):
    """响应模型：不含 password，仅返回可公开字段。"""
    username: str
    email: EmailStr
    full_name: str | None = None


class UserInDB(BaseModel):
    """内部存储模型：含 hashed_password，不直接暴露给客户端。"""
    username: str
    hashed_password: str
    email: EmailStr
    full_name: str | None = None


def fake_password_hasher(raw_password: str):
    """模拟密码哈希（示例用，非真实加密）。"""
    return "supersecret" + raw_password


def fake_save_user(user_in: UserIn):
    """将 UserIn 转为 UserInDB 并持久化（此处仅 print）。"""
    hashed_password = fake_password_hasher(user_in.password)
    user_in_db = UserInDB(**user_in.model_dump(), hashed_password=hashed_password)
    print("User saved! ..not really")
    return user_in_db


@app.post("/user/", response_model=UserOut)
async def create_user(user_in: UserIn):
    """返回 UserInDB 实例；response_model=UserOut 过滤掉 hashed_password。"""
    user_saved = fake_save_user(user_in)
    return user_saved
