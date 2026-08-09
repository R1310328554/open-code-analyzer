"""教程 008e：Depends(scope="function") 在路径操作返回后立即执行 yield 后/finally 清理。"""

from fastapi import Depends, FastAPI

app = FastAPI()


def get_username():
    """默认 scope 为 request；scope=\"function\" 时 handler 结束即触发 finally。"""
    try:
        yield "Rick"  # 注入当前用户名
    finally:
        print("Cleanup up before response is sent")  # finally：在响应发送前执行清理


@app.get("/users/me")
def get_user_me(username: str = Depends(get_username, scope="function")):
    """scope=\"function\"：handler 返回后立刻运行依赖的 finally，不必等整个请求结束。"""
    return username
