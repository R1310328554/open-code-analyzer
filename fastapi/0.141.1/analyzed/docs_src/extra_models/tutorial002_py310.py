"""教程 002：UserBase 基类继承——UserIn/UserOut/UserInDB 共享公共字段定义。"""

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr

app = FastAPI()


class UserBase(BaseModel):
    """基类：username、email、full_name 三字段由子类复用。"""
    username: str
    email: EmailStr
    full_name: str | None = None


class UserIn(UserBase):
    """输入模型：在基类基础上增加 password。"""
    password: str


class UserOut(UserBase):
    """输出模型：继承基类字段，无需额外声明。"""
    pass


class UserInDB(UserBase):
    """数据库模型：在基类基础上增加 hashed_password。"""
    hashed_password: str


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
    """继承写法与 tutorial001 等价；response_model 仍过滤敏感字段。"""
    user_saved = fake_save_user(user_in)
    return user_saved
