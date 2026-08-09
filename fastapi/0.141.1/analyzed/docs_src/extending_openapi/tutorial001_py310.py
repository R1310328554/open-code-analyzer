"""教程 001：覆盖 app.openapi 以自定义 OpenAPI schema（标题、版本、扩展字段）。"""

from fastapi import FastAPI
from fastapi.openapi.utils import get_openapi

app = FastAPI()


@app.get("/items/")
async def read_items():
    return [{"name": "Foo"}]


def custom_openapi():
    """生成并缓存自定义 OpenAPI schema；重复调用直接返回缓存。"""
    if app.openapi_schema:  # 已生成则复用，避免重复计算
        return app.openapi_schema
    openapi_schema = get_openapi(  # 基于当前 routes 构建 schema
        title="Custom title",  # 自定义 API 标题
        version="2.5.0",  # 自定义 schema 版本号
        summary="This is a very custom OpenAPI schema",  # 简短摘要
        description="Here's a longer description of the custom **OpenAPI** schema",  # 详细描述（支持 Markdown）
        routes=app.routes,  # 从已注册路由自动提取 paths
    )
    openapi_schema["info"]["x-logo"] = {  # 扩展字段：文档页 logo
        "url": "https://fastapi.tiangolo.com/img/logo-margin/logo-teal.png"
    }
    app.openapi_schema = openapi_schema  # 缓存到 app，供 /openapi.json 使用
    return app.openapi_schema


app.openapi = custom_openapi  # 替换默认 openapi 生成函数
