"""教程 003：同步 def 路由——无 await 时 FastAPI 在线程池中运行，避免阻塞事件循环。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/")
def root():
    """普通 def 路由；FastAPI 自动包装为 async，内部在线程池执行。"""
    return {"message": "Hello World"}
