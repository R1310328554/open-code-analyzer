"""教程 006：HTTP Basic 认证——经典 Depends(security) 获取 HTTPBasicCredentials。"""

from fastapi import Depends, FastAPI
from fastapi.security import HTTPBasic, HTTPBasicCredentials

app = FastAPI()  # 创建 FastAPI 应用实例

security = HTTPBasic()


@app.get("/users/me")
def read_current_user(credentials: HTTPBasicCredentials = Depends(security)):
    """客户端须发送 Authorization: Basic base64(user:pass)；依赖项解析为 credentials。"""
    return {"username": credentials.username, "password": credentials.password}
