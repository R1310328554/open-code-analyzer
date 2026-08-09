"""内部 admin 路由：挂载于 /admin，需 X-Token 依赖。"""

from fastapi import APIRouter

# 子路由，由 main 以 prefix=/admin 挂载
router = APIRouter()


@router.post("/")
async def update_admin():
    """管理员占位更新端点。"""
    return {"message": "Admin getting schwifty"}
