"""大型应用示例入口：组合全局依赖、users/items 路由与 internal admin。"""

from fastapi import Depends, FastAPI

from .dependencies import get_query_token, get_token_header
from .internal import admin
from .routers import items, users

# 应用级依赖：所有路径操作均需有效 query token
app = FastAPI(dependencies=[Depends(get_query_token)])


# 注册用户路由（无额外 prefix）
app.include_router(users.router)
# items 路由自带 /items prefix 与 X-Token 依赖
app.include_router(items.router)
app.include_router(
    admin.router,
    prefix="/admin",
    tags=["admin"],
    dependencies=[Depends(get_token_header)],  # admin 路由额外要求 X-Token
    responses={418: {"description": "I'm a teapot"}},
)


@app.get("/")
async def root():
    """根路径健康检查。"""
    return {"message": "Hello Bigger Applications!"}
