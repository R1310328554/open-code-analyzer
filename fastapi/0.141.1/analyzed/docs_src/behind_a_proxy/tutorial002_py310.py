"""教程 002：在 FastAPI 构造时声明 root_path，告知应用挂载前缀。"""

from fastapi import FastAPI, Request

# root_path 应与代理剥离的路径前缀一致，用于 OpenAPI 与路由解析
app = FastAPI(root_path="/api/v1")


@app.get("/app")
def read_main(request: Request):
    """验证 root_path 配置是否生效。"""
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
