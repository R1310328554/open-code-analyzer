"""教程 002：get_current_user 依赖链——Depends 嵌套将 token 解析为 User。"""

from fastapi import Depends, FastAPI
from fastapi.security import OAuth2PasswordBearer
from pydantic import BaseModel

app = FastAPI()  # 创建 FastAPI 应用实例

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")


class User(BaseModel):
    """用户字段；fake_decode_token 仅作教学演示，勿用于生产。"""

    username: str
    email: str | None = None
    full_name: str | None = None
    disabled: bool | None = None


def fake_decode_token(token):
    """模拟令牌解码；真实场景应校验签名、过期时间与 issuer。"""
    return User(
        username=token + "fakedecoded", email="john@example.com", full_name="John Doe"
    )


async def get_current_user(token: str = Depends(oauth2_scheme)):
    """Depends(oauth2_scheme) 注入 token，再经 fake_decode_token 转为 User。"""
    user = fake_decode_token(token)
    return user


@app.get("/users/me")
async def read_users_me(current_user: User = Depends(get_current_user)):
    """Depends(get_current_user) 自动完成认证；端点直接拿到 User 对象。"""
    return current_user
