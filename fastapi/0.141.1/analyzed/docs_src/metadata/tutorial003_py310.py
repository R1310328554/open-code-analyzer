"""教程 003：自定义 Swagger UI 路径（docs_url）并禁用 ReDoc（redoc_url=None）。"""

from fastapi import FastAPI

app = FastAPI(docs_url="/documentation", redoc_url=None)  # Swagger UI 在 /documentation；不挂载 ReDoc


@app.get("/items/")
async def read_items():
    """示例 GET 路由。"""
    return [{"name": "Foo"}]
