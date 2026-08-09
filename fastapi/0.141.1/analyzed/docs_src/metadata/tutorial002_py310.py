"""教程 002：自定义 OpenAPI JSON schema 的 URL 路径（openapi_url）。"""

from fastapi import FastAPI

app = FastAPI(openapi_url="/api/v1/openapi.json")  # 默认 /openapi.json，此处改为 /api/v1/openapi.json


@app.get("/items/")
async def read_items():
    """示例路由；OpenAPI schema 仍会为所有路径自动生成。"""
    return [{"name": "Foo"}]
