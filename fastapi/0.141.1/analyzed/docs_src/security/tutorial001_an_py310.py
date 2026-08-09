"""教程 001（Annotated）：OAuth2PasswordBearer——从 Authorization 头提取 Bearer 令牌。"""

from typing import Annotated

from fastapi import Depends, FastAPI
from fastapi.security import OAuth2PasswordBearer

app = FastAPI()  # 创建 FastAPI 应用实例

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")  # tokenUrl 指向获取令牌的端点


@app.get("/items/")
async def read_items(token: Annotated[str, Depends(oauth2_scheme)]):
    """Depends(oauth2_scheme) 解析 Bearer token；缺失时自动返回 401。"""
    return {"token": token}
