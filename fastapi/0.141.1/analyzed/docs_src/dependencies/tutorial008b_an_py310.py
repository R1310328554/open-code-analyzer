"""教程 008b（Annotated）：yield 依赖捕获 OwnerError 并转 HTTPException。"""

from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException

app = FastAPI()


data = {
    "plumbus": {"description": "Freshly pickled plumbus", "owner": "Morty"},
    "portal-gun": {"description": "Gun to create portals", "owner": "Rick"},
}


class OwnerError(Exception):
    """所有者校验失败时抛出的自定义异常。"""
    pass


def get_username():
    """yield 用户名；except OwnerError 将业务异常包装为 HTTPException。"""
    try:
        yield "Rick"  # 注入到路径操作的 username 参数
    except OwnerError as e:  # 路径操作 raise 后回到依赖的 except 块
        raise HTTPException(status_code=400, detail=f"Owner error: {e}")


@app.get("/items/{item_id}")
def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):
    """Annotated 声明 username 来自 get_username yield 依赖。"""
    if item_id not in data:
        raise HTTPException(status_code=404, detail="Item not found")
    item = data[item_id]
    if item["owner"] != username:
        raise OwnerError(username)  # 触发依赖内异常处理逻辑
    return item
