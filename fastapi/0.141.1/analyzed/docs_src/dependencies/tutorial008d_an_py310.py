"""教程 008d（Annotated）：yield 依赖捕获后 re-raise InternalError。"""

from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException

app = FastAPI()


class InternalError(Exception):
    pass


def get_username():
    """except 中 raise 裸异常，与 tutorial008d Depends 版行为一致。"""
    try:
        yield "Rick"
    except InternalError:
        print("We don't swallow the internal error here, we raise again 😎")
        raise  # 重新抛出，不吞掉路径操作中的 InternalError


@app.get("/items/{item_id}")
def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):
    """Annotated 写法；InternalError 经依赖 re-raise 后由 FastAPI 处理。"""
    if item_id == "portal-gun":
        raise InternalError(
            f"The portal gun is too dangerous to be owned by {username}"
        )
    if item_id != "plumbus":
        raise HTTPException(
            status_code=404, detail="Item not found, there's only a plumbus here"
        )
    return item_id
