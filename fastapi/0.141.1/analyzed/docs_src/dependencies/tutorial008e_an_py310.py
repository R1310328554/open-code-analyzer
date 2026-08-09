"""教程 008e（Annotated）：Depends(..., scope="function") 提前结束 yield 依赖生命周期。"""

from typing import Annotated

from fastapi import Depends, FastAPI

app = FastAPI()


def get_username():
    """yield 用户名；finally 在 function scope 下于 handler 返回后执行。"""
    try:
        yield "Rick"
    finally:
        print("Cleanup up before response is sent")  # 早于默认 request scope 的清理时机


@app.get("/users/me")
def get_user_me(username: Annotated[str, Depends(get_username, scope="function")]):
    """Annotated + scope=\"function\"：类型与依赖元数据合一，清理在函数级提前触发。"""
    return username
