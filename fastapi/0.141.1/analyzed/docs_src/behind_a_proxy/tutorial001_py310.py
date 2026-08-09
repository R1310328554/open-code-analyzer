"""教程 001：通过 Request.scope 读取 ASGI root_path（代理注入的挂载前缀）。"""

from fastapi import FastAPI, Request

app = FastAPI()


@app.get("/app")
def read_main(request: Request):
    """返回问候语及当前请求的 root_path。"""
    # scope["root_path"] 由反向代理或 uvicorn --root-path 设置
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
