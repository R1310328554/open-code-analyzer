"""教程 003：同时配置 servers 与 root_path，OpenAPI 文档展示多环境 URL。"""

from fastapi import FastAPI, Request

app = FastAPI(
    # servers 列出客户端可访问的完整基础 URL（含域名）
    servers=[
        {"url": "https://stag.example.com", "description": "Staging environment"},
        {"url": "https://prod.example.com", "description": "Production environment"},
    ],
    # root_path 为应用在代理后的挂载路径
    root_path="/api/v1",
)


@app.get("/app")
def read_main(request: Request):
    """返回 root_path，便于调试代理配置。"""
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
