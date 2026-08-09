"""教程 002（Annotated）：get_current_user 依赖链——令牌解码为 User 模型。"""

from typing import Annotated

from fastapi import Depends, FastAPI
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")


class User(BaseModel):
    """当前用户模型；示例用 fake_decode_token 模拟 JWT 解码结果。"""

    username: str
    email: str | None = None
    full_name: str | None = None
    disabled: bool | None = None


def fake_decode_token(token):
    """演示用解码：将 token 拼接后缀生成 User，生产环境应验证 JWT 签名。"""
    return User(
        username=token + "fakedecoded", email="john@example.com", full_name="John Doe"
    )


async def get_current_user(token: Annotated[str, Depends(oauth2_scheme)]):
    """第一层依赖：oauth2_scheme 提供 token，解码后返回 User 实例。"""
    user = fake_decode_token(token)
    return user


@app.get("/users/me")
async def read_users_me(current_user: Annotated[User, Depends(get_current_user)]):
    """第二层依赖：get_current_user 注入已解码的 User，/users/me 返回当前用户。"""
    return current_user
