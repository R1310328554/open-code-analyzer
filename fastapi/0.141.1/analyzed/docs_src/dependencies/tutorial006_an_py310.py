"""教程 006（Annotated）：路由级 dependencies 在路径函数执行前校验请求头。"""

from typing import Annotated

from fastapi import Depends, FastAPI, Header, HTTPException

app = FastAPI()


async def verify_token(x_token: Annotated[str, Header()]):
    """校验 X-Token 请求头；失败时抛出 HTTPException。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: Annotated[str, Header()]):
    """校验 X-Key 请求头；通过时返回 x_key 供后续依赖使用。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


@app.get("/items/", dependencies=[Depends(verify_token), Depends(verify_key)])
async def read_items():
    """dependencies 列表中的依赖在 read_items 之前执行，返回值不注入路径函数。"""
    return [{"item": "Foo"}, {"item": "Bar"}]
