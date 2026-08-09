"""教程 007：HTTP Basic 安全校验——compare_digest 替代 ==，避免凭据枚举时序泄露。"""

import secrets

from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

app = FastAPI()  # 创建 FastAPI 应用实例

security = HTTPBasic()


def get_current_username(credentials: HTTPBasicCredentials = Depends(security)):
    """逐字节常量时间比较；勿用 == 直接比较密码字符串。"""
    current_username_bytes = credentials.username.encode("utf8")
    correct_username_bytes = b"stanleyjobson"
    is_correct_username = secrets.compare_digest(
        current_username_bytes, correct_username_bytes
    )
    current_password_bytes = credentials.password.encode("utf8")
    correct_password_bytes = b"swordfish"
    is_correct_password = secrets.compare_digest(
        current_password_bytes, correct_password_bytes
    )
    if not (is_correct_username and is_correct_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Basic"},
        )
    return credentials.username


@app.get("/users/me")
def read_current_user(username: str = Depends(get_current_username)):
    """认证成功后仅返回 username，不暴露密码。"""
    return {"username": username}
