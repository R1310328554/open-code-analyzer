"""教程 008c（Annotated）：演示 yield 依赖吞掉 InternalError 而不 re-raise。"""

from typing import Annotated

from fastapi import Depends, FastAPI, HTTPException

app = FastAPI()


class InternalError(Exception):
    """路径操作抛出的内部异常。"""
    pass


def get_username():
    """except InternalError 后仅打印，不 raise HTTPException。"""
    try:
        yield "Rick"
    except InternalError:  # 与 Depends 版相同：捕获后不向上抛出
        print("Oops, we didn't raise again, Britney 😱")


@app.get("/items/{item_id}")
def get_item(item_id: str, username: Annotated[str, Depends(get_username)]):
    """Annotated 注入 username；InternalError 由依赖静默处理。"""
    if item_id == "portal-gun":
        raise InternalError(
            f"The portal gun is too dangerous to be owned by {username}"
        )
    if item_id != "plumbus":
        raise HTTPException(
            status_code=404, detail="Item not found, there's only a plumbus here"
        )
    return item_id
