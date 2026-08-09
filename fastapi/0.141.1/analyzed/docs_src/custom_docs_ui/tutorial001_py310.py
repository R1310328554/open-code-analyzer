"""教程 001：禁用内置 /docs，改用手动路由挂载 CDN 版 Swagger UI 与 ReDoc。"""

from fastapi import FastAPI
from fastapi.openapi.docs import (
    get_redoc_html,
    get_swagger_ui_html,
    get_swagger_ui_oauth2_redirect_html,
)

app = FastAPI(docs_url=None, redoc_url=None)  # 关闭默认文档端点


@app.get("/docs", include_in_schema=False)
async def custom_swagger_ui_html():
    """返回自定义 Swagger UI HTML，静态资源从 unpkg CDN 加载。"""
    return get_swagger_ui_html(
        openapi_url=app.openapi_url,
        title=app.title + " - Swagger UI",
        oauth2_redirect_url=app.swagger_ui_oauth2_redirect_url,
        swagger_js_url="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js",
        swagger_css_url="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css",
    )


@app.get(app.swagger_ui_oauth2_redirect_url, include_in_schema=False)
async def swagger_ui_redirect():
    """OAuth2 授权回调页，供 Swagger UI 完成 redirect 流程。"""
    return get_swagger_ui_oauth2_redirect_html()


@app.get("/redoc", include_in_schema=False)
async def redoc_html():
    """返回自定义 ReDoc HTML，JS 从 unpkg CDN 加载。"""
    return get_redoc_html(
        openapi_url=app.openapi_url,
        title=app.title + " - ReDoc",
        redoc_js_url="https://unpkg.com/redoc@2/bundles/redoc.standalone.js",
    )


@app.get("/users/{username}")
async def read_user(username: str):
    """示例业务路由，与文档 UI 配置无关。"""
    return {"message": f"Hello {username}"}
