"""教程 012（Annotated）：应用级 dependencies——所有路由共享 Header 校验。"""

from typing import Annotated

from fastapi import Depends, FastAPI, Header, HTTPException


async def verify_token(x_token: Annotated[str, Header()]):
    """校验 X-Token 请求头；无效则 HTTP 400。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: Annotated[str, Header()]):
    """校验 X-Key 请求头；通过时返回 x_key。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


app = FastAPI(dependencies=[Depends(verify_token), Depends(verify_key)])  # 全局依赖


@app.get("/items/")
async def read_items():
    """无需重复声明依赖，应用级列表已对所有路由生效。"""
    return [{"item": "Portal Gun"}, {"item": "Plumbus"}]


@app.get("/users/")
async def read_users():
    """同样受 verify_token / verify_key 保护。"""
    return [{"username": "Rick"}, {"username": "Morty"}]
