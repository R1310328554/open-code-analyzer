"""教程 012：应用级 dependencies 的非 Annotated 写法。"""

from fastapi import Depends, FastAPI, Header, HTTPException


async def verify_token(x_token: str = Header()):
    """校验 X-Token 请求头；无效则 HTTP 400。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: str = Header()):
    """校验 X-Key 请求头；通过时返回 x_key。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


app = FastAPI(dependencies=[Depends(verify_token), Depends(verify_key)])  # 全局依赖


@app.get("/items/")
async def read_items():
    """FastAPI(...) 的 dependencies 参数作用于该应用下全部路由。"""
    return [{"item": "Portal Gun"}, {"item": "Plumbus"}]


@app.get("/users/")
async def read_users():
    """每个端点执行前都会先运行 verify_token 与 verify_key。"""
    return [{"username": "Rick"}, {"username": "Morty"}]
