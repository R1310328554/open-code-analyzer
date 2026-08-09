"""教程 006（Annotated）：HTTP Basic 认证——HTTPBasic 依赖解析 Authorization 头。"""

from typing import Annotated

from fastapi import Depends, FastAPI
from fastapi.security import HTTPBasic, HTTPBasicCredentials

app = FastAPI()  # 创建 FastAPI 应用实例

security = HTTPBasic()  # 创建 Basic 认证依赖；缺失头时默认 401


@app.get("/users/me")
def read_current_user(credentials: Annotated[HTTPBasicCredentials, Depends(security)]):
    """Depends(security) 注入 username/password；本示例直接回显（勿在生产返回密码）。"""
    return {"username": credentials.username, "password": credentials.password}
