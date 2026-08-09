"""教程 004：root_path_in_servers=False，禁止自动将 root_path 拼入 servers URL。"""

from fastapi import FastAPI, Request

app = FastAPI(
    servers=[
        {"url": "https://stag.example.com", "description": "Staging environment"},
        {"url": "https://prod.example.com", "description": "Production environment"},
    ],
    root_path="/api/v1",
    # 显式禁用：不在自动生成的 server 条目前缀 root_path
    root_path_in_servers=False,
)


@app.get("/app")
def read_main(request: Request):
    """与 tutorial003 相同端点，对比 OpenAPI servers 生成差异。"""
    return {"message": "Hello World", "root_path": request.scope.get("root_path")}
