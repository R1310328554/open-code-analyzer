"""教程 005：openapi_extra 注入自定义 OpenAPI 扩展字段（如 x-aperture-labs-portal）。"""

from fastapi import FastAPI

app = FastAPI()


@app.get("/items/", openapi_extra={"x-aperture-labs-portal": "blue"})
async def read_items():
    """x-* 扩展键会写入生成的 OpenAPI 文档，供工具链或门户读取。"""
    return [{"item_id": "portal-gun"}]
