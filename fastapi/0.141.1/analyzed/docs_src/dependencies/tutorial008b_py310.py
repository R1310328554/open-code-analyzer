"""教程 008b：yield 依赖内捕获路径操作抛出的异常并转为 HTTPException。"""

from fastapi import Depends, FastAPI, HTTPException

app = FastAPI()


# Rick and Morty 主题模拟数据
data = {
    "plumbus": {"description": "Freshly pickled plumbus", "owner": "Morty"},
    "portal-gun": {"description": "Gun to create portals", "owner": "Rick"},
}


class OwnerError(Exception):
    """自定义业务异常：物品所有者不匹配。"""
    pass


def get_username():
    """yield 依赖：路径操作中 raise OwnerError 时在此 except 并转为 400。"""
    try:
        yield "Rick"  # 模拟当前登录用户
    except OwnerError as e:  # 捕获路径操作在 yield 之后抛出的 OwnerError
        raise HTTPException(status_code=400, detail=f"Owner error: {e}")  # 转为 HTTP 错误响应


@app.get("/items/{item_id}")
def get_item(item_id: str, username: str = Depends(get_username)):
    """若 item 的 owner 与 username 不符则 raise OwnerError，由依赖捕获。"""
    if item_id not in data:
        raise HTTPException(status_code=404, detail="Item not found")
    item = data[item_id]
    if item["owner"] != username:
        raise OwnerError(username)  # 在 yield 之后抛出，进入依赖的 except
    return item
