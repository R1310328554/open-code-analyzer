"""教程 001：OAuth2PasswordBearer 依赖——经典 Depends 写法提取访问令牌。"""

from fastapi import Depends, FastAPI
from fastapi.security import OAuth2PasswordBearer

app = FastAPI()  # 创建 FastAPI 应用实例

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")  # 声明 OAuth2 密码流


@app.get("/items/")
async def read_items(token: str = Depends(oauth2_scheme)):
    """请求头须含 Authorization: Bearer <token>；依赖项返回纯 token 字符串。"""
    return {"token": token}
