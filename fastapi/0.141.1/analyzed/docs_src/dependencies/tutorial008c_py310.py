"""教程 008c：yield 依赖捕获 InternalError 但不重新抛出（仅打印，异常被“吞掉”）。"""

from fastapi import Depends, FastAPI, HTTPException

app = FastAPI()


class InternalError(Exception):
    """内部业务异常；本示例演示捕获后不 re-raise 的行为。"""
    pass


def get_username():
    """捕获 InternalError 后只 print，不 raise——客户端可能收不到该错误。"""
    try:
        yield "Rick"  # 正常注入用户名
    except InternalError:  # 捕获但不 re-raise，演示“吞异常”反模式
        print("Oops, we didn't raise again, Britney 😱")  # 仅日志，异常不再向上传播


@app.get("/items/{item_id}")
def get_item(item_id: str, username: str = Depends(get_username)):
    """访问 portal-gun 时 raise InternalError，依赖会捕获但不转 HTTP 响应。"""
    if item_id == "portal-gun":
        raise InternalError(  # 在 yield 之后抛出，进入依赖 except
            f"The portal gun is too dangerous to be owned by {username}"
        )
    if item_id != "plumbus":
        raise HTTPException(
            status_code=404, detail="Item not found, there's only a plumbus here"
        )
    return item_id
