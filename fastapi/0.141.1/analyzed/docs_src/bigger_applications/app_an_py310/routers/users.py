"""users 路由：演示多路径与 tags 组织。"""

from fastapi import APIRouter

# 路径在装饰器中写全（含 /users 前缀）
router = APIRouter()


@router.get("/users/", tags=["users"])
async def read_users():
    """返回用户列表。"""
    return [{"username": "Rick"}, {"username": "Morty"}]


@router.get("/users/me", tags=["users"])
async def read_user_me():
    """返回当前用户（示例固定值）。"""
    return {"username": "fakecurrentuser"}


@router.get("/users/{username}", tags=["users"])
async def read_user(username: str):
    """按 username 路径参数返回用户信息。"""
    return {"username": username}
