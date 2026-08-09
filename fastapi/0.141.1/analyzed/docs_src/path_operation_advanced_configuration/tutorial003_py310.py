"""教程 003：include_in_schema=False 将路径操作从 OpenAPI/Swagger 文档中隐藏。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/", include_in_schema=False)
async def read_items():
    """路由仍可正常访问，但不会出现在 /docs 与 openapi.json 中。"""
    return [{"item_id": "Foo"}]
