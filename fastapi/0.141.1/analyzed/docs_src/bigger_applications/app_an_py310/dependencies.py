"""大型应用示例：共享依赖项（X-Token 请求头与 query token 校验）。"""

from typing import Annotated

from fastapi import Header, HTTPException


async def get_token_header(x_token: Annotated[str, Header()]):
    """校验 X-Token 请求头，无效时抛出 400。"""
    # 模拟密钥校验
    if x_token != "fake-super-secret-token":
        raise HTTPException(status_code=400, detail="X-Token header invalid")


async def get_query_token(token: str):
    """校验 query 参数 token，非 jessica 时抛出 400。"""
    if token != "jessica":
        raise HTTPException(status_code=400, detail="No Jessica token provided")
