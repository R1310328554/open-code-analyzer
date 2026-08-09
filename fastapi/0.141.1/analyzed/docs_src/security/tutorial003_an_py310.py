"""教程 003（Annotated）：OAuth2 密码流登录——/token 换令牌，依赖链校验当前活跃用户。"""

from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel

fake_users_db = {
    "johndoe": {
        "username": "johndoe",
        "full_name": "John Doe",
        "email": "johndoe@example.com",
        "hashed_password": "fakehashedsecret",
        "disabled": False,
    },
    "alice": {
        "username": "alice",
        "full_name": "Alice Wonderson",
        "email": "alice@example.com",
        "hashed_password": "fakehashedsecret2",
        "disabled": True,
    },
}

app = FastAPI()  # 创建 FastAPI 应用实例


def fake_hash_password(password: str):
    """演示用哈希：前缀 fakehashed + 明文；生产环境须用 bcrypt/argon2 等。"""
    return "fakehashed" + password


oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")  # 声明 Bearer 提取依赖，tokenUrl 指向 /token


class User(BaseModel):
    """对外暴露的用户字段；不含 hashed_password。"""

    username: str
    email: str | None = None
    full_name: str | None = None
    disabled: bool | None = None


class UserInDB(User):
    """数据库用户模型，含密码哈希。"""

    hashed_password: str


def get_user(db, username: str):
    """按用户名从假数据库取 UserInDB；不存在则返回 None。"""
    if username in db:
        user_dict = db[username]
        return UserInDB(**user_dict)


def fake_decode_token(token):
    """演示用解码：直接把 token 当 username 查库——毫无安全性，见 tutorial004 JWT 版。"""
    # This doesn't provide any security at all
    # Check the next version
    user = get_user(fake_users_db, token)
    return user


async def get_current_user(token: Annotated[str, Depends(oauth2_scheme)]):
    """第一层依赖：oauth2_scheme 提取 Bearer token，fake_decode_token 解析为 UserInDB。"""
    user = fake_decode_token(token)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return user


async def get_current_active_user(
    current_user: Annotated[User, Depends(get_current_user)],
):
    """第二层依赖：拒绝 disabled=True 的用户，返回 400 Inactive user。"""
    if current_user.disabled:
        raise HTTPException(status_code=400, detail="Inactive user")
    return current_user


@app.post("/token")
async def login(form_data: Annotated[OAuth2PasswordRequestForm, Depends()]):
    """OAuth2 密码流：校验 username/password，成功返回 access_token（此处为 username 字符串）。"""
    user_dict = fake_users_db.get(form_data.username)
    if not user_dict:
        raise HTTPException(status_code=400, detail="Incorrect username or password")
    user = UserInDB(**user_dict)
    hashed_password = fake_hash_password(form_data.password)
    if not hashed_password == user.hashed_password:
        raise HTTPException(status_code=400, detail="Incorrect username or password")

    return {"access_token": user.username, "token_type": "bearer"}


@app.get("/users/me")
async def read_users_me(
    current_user: Annotated[User, Depends(get_current_active_user)],
):
    """受保护端点：Depends(get_current_active_user) 自动完成认证与活跃检查。"""
    return current_user
