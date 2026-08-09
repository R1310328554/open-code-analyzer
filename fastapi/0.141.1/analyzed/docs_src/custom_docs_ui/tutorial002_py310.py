"""教程 002：自定义文档 UI，Swagger/ReDoc 静态文件从本地 /static 目录提供。"""

from fastapi import FastAPI
from fastapi.openapi.docs import (
    get_redoc_html,
    get_swagger_ui_html,
    get_swagger_ui_oauth2_redirect_html,
)
from fastapi.staticfiles import StaticFiles

app = FastAPI(docs_url=None, redoc_url=None)

app.mount("/static", StaticFiles(directory="static"), name="static")  # 本地静态资源


@app.get("/docs", include_in_schema=False)
async def custom_swagger_ui_html():
    """Swagger UI 使用 /static 下的 bundle 与 css，适合离线或内网部署。"""
    return get_swagger_ui_html(
        openapi_url=app.openapi_url,
        title=app.title + " - Swagger UI",
        oauth2_redirect_url=app.swagger_ui_oauth2_redirect_url,
        swagger_js_url="/static/swagger-ui-bundle.js",
        swagger_css_url="/static/swagger-ui.css",
    )


@app.get(app.swagger_ui_oauth2_redirect_url, include_in_schema=False)
async def swagger_ui_redirect():
    """OAuth2 授权回调页。"""
    return get_swagger_ui_oauth2_redirect_html()


@app.get("/redoc", include_in_schema=False)
async def redoc_html():
    """ReDoc 使用本地 /static/redoc.standalone.js。"""
    return get_redoc_html(
        openapi_url=app.openapi_url,
        title=app.title + " - ReDoc",
        redoc_js_url="/static/redoc.standalone.js",
    )


@app.get("/users/{username}")
async def read_user(username: str):
    """示例业务路由。"""
    return {"message": f"Hello {username}"}
