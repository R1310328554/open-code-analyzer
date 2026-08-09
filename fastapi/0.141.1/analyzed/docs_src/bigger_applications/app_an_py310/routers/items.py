"""items 路由：带 router 级 X-Token 依赖的 CRUD 示例。"""

from fastapi import APIRouter, Depends, HTTPException

from ..dependencies import get_token_header

router = APIRouter(
    prefix="/items",
    tags=["items"],
    dependencies=[Depends(get_token_header)],  # 本 router 下所有端点需 X-Token
    responses={404: {"description": "Not found"}},
)


# 模拟内存 item 存储
fake_items_db = {"plumbus": {"name": "Plumbus"}, "gun": {"name": "Portal Gun"}}


@router.get("/")
async def read_items():
    """列出全部 items。"""
    return fake_items_db


@router.get("/{item_id}")
async def read_item(item_id: str):
    """按 ID 读取单个 item，不存在时 404。"""
    if item_id not in fake_items_db:
        raise HTTPException(status_code=404, detail="Item not found")
    return {"name": fake_items_db[item_id]["name"], "item_id": item_id}


@router.put(
    "/{item_id}",
    tags=["custom"],
    responses={403: {"description": "Operation forbidden"}},
)
async def update_item(item_id: str):
    """更新 item；仅允许更新 plumbus，否则 403。"""
    # 演示路径操作级自定义响应与业务限制
    if item_id != "plumbus":
        raise HTTPException(
            status_code=403, detail="You can only update the item: plumbus"
        )
    return {"item_id": item_id, "name": "The great Plumbus"}
