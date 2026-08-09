"""教程 006：路由级 dependencies 的非 Annotated 写法，Header 参数校验令牌。"""

from fastapi import Depends, FastAPI, Header, HTTPException

app = FastAPI()


async def verify_token(x_token: str = Header()):
    """校验 X-Token 请求头；失败时抛出 HTTPException。"""
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def verify_key(x_key: str = Header()):
    """校验 X-Key 请求头；通过时返回 x_key 供后续依赖使用。"""
    if x_key != "fake-super-secret-key":
        raise HTTPException(status_code=400, detail="X-Key header invalid")
    return x_key


@app.get("/items/", dependencies=[Depends(verify_token), Depends(verify_key)])
async def read_items():
    """dependencies 列表中的依赖在 read_items 之前执行，返回值不注入路径函数。"""
    return [{"item": "Foo"}, {"item": "Bar"}]
