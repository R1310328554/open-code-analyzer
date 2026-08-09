"""教程 008d：yield 依赖捕获 InternalError 后 re-raise，让 FastAPI 正常处理异常。"""

from fastapi import Depends, FastAPI, HTTPException

app = FastAPI()


class InternalError(Exception):
    """需在依赖中捕获并重新抛出的内部异常。"""
    pass


def get_username():
    """捕获 InternalError 做清理/日志后 raise，异常继续向上传播。"""
    try:
        yield "Rick"
    except InternalError:  # 可在 re-raise 前执行清理或记录
        print("We don't swallow the internal error here, we raise again 😎")  # 记录后 re-raise
        raise  # 裸 raise 保留原始 traceback，FastAPI 可转为 500 等


@app.get("/items/{item_id}")
def get_item(item_id: str, username: str = Depends(get_username)):
    """portal-gun 触发 InternalError；依赖 re-raise 后由框架处理。"""
    if item_id == "portal-gun":
        raise InternalError(
            f"The portal gun is too dangerous to be owned by {username}"
        )
    if item_id != "plumbus":
        raise HTTPException(
            status_code=404, detail="Item not found, there's only a plumbus here"
        )
    return item_id
