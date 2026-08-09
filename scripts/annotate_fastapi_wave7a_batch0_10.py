#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-7a docs_src slice [0:10]."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ANALYZED = VER / "analyzed"
WAVE7A_FILES = Path("/tmp/fastapi_w7a.txt").read_text().strip().splitlines()

ANNOTATED: dict[str, str] = {
    "docs_src/cookie_params/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：从请求 Cookie 读取可选 ads_id 参数。"""

from typing import Annotated

from fastapi import Cookie, FastAPI

app = FastAPI()


@app.get("/items/")
async def read_items(ads_id: Annotated[str | None, Cookie()] = None):
    """Cookie() 将同名 Cookie 解析为 ads_id；未携带时返回 None。"""
    return {"ads_id": ads_id}
''',
    "docs_src/cookie_params/tutorial001_py310.py": '''\
"""教程 001：Cookie(default=None) 声明可选 Cookie 参数（非 Annotated 写法）。"""

from fastapi import Cookie, FastAPI

app = FastAPI()


@app.get("/items/")
async def read_items(ads_id: str | None = Cookie(default=None)):
    """读取名为 ads_id 的 Cookie；缺失时默认 None。"""
    return {"ads_id": ads_id}
''',
    "docs_src/cors/__init__.py": '''\
"""FastAPI 文档示例：CORS 跨域资源共享（CORSMiddleware 配置）。"""
''',
    "docs_src/cors/tutorial001_py310.py": '''\
"""教程 001：注册 CORSMiddleware，允许指定来源的跨域请求。"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

origins = [
    "http://localhost.tiangolo.com",
    "https://localhost.tiangolo.com",
    "http://localhost",
    "http://localhost:8080",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,  # 白名单来源；生产环境应收紧
    allow_credentials=True,  # 允许携带 Cookie 等凭证
    allow_methods=["*"],  # 允许所有 HTTP 方法
    allow_headers=["*"],  # 允许所有请求头
)


@app.get("/")
async def main():
    """示例路由；浏览器跨域访问时会收到 CORS 响应头。"""
    return {"message": "Hello World"}
''',
    "docs_src/custom_docs_ui/__init__.py": '''\
"""FastAPI 文档示例：自定义 Swagger UI / ReDoc 文档页面（CDN 或本地静态资源）。"""
''',
    "docs_src/custom_docs_ui/tutorial001_py310.py": '''\
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
''',
    "docs_src/custom_docs_ui/tutorial002_py310.py": '''\
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
''',
    "docs_src/custom_request_and_route/__init__.py": '''\
"""FastAPI 文档示例：自定义 Request 与 APIRoute（如 gzip 解压请求体）。"""
''',
    "docs_src/custom_request_and_route/tutorial001_an_py310.py": '''\
"""教程 001（Annotated）：自定义 GzipRequest/GzipRoute 自动解压 gzip 编码 body。"""

import gzip
from collections.abc import Callable
from typing import Annotated

from fastapi import Body, FastAPI, Request, Response
from fastapi.routing import APIRoute


class GzipRequest(Request):
    """扩展 Request：Content-Encoding 含 gzip 时先解压再返回 body。"""

    async def body(self) -> bytes:
        if not hasattr(self, "_body"):
            body = await super().body()
            if "gzip" in self.headers.getlist("Content-Encoding"):
                body = gzip.decompress(body)  # 解压后再交给路由处理
            self._body = body
        return self._body


class GzipRoute(APIRoute):
    """自定义路由类：将每个请求的 Request 替换为 GzipRequest。"""

    def get_route_handler(self) -> Callable:
        original_route_handler = super().get_route_handler()

        async def custom_route_handler(request: Request) -> Response:
            request = GzipRequest(request.scope, request.receive)
            return await original_route_handler(request)

        return custom_route_handler


app = FastAPI()
app.router.route_class = GzipRoute  # 全局启用 gzip 感知路由


@app.post("/sum")
async def sum_numbers(numbers: Annotated[list[int], Body()]):
    """接收整数列表 body 并返回求和；客户端可 gzip 压缩 payload。"""
    return {"sum": sum(numbers)}
''',
    "docs_src/custom_request_and_route/tutorial001_py310.py": '''\
"""教程 001：GzipRequest/GzipRoute 自动解压 gzip body（Body() 经典写法）。"""

import gzip
from collections.abc import Callable

from fastapi import Body, FastAPI, Request, Response
from fastapi.routing import APIRoute


class GzipRequest(Request):
    """扩展 Request：Content-Encoding 含 gzip 时先解压再返回 body。"""

    async def body(self) -> bytes:
        if not hasattr(self, "_body"):
            body = await super().body()
            if "gzip" in self.headers.getlist("Content-Encoding"):
                body = gzip.decompress(body)
            self._body = body
        return self._body


class GzipRoute(APIRoute):
    """自定义路由类：将每个请求的 Request 替换为 GzipRequest。"""

    def get_route_handler(self) -> Callable:
        original_route_handler = super().get_route_handler()

        async def custom_route_handler(request: Request) -> Response:
            request = GzipRequest(request.scope, request.receive)
            return await original_route_handler(request)

        return custom_route_handler


app = FastAPI()
app.router.route_class = GzipRoute


@app.post("/sum")
async def sum_numbers(numbers: list[int] = Body()):
    """接收整数列表 body 并返回求和。"""
    return {"sum": sum(numbers)}
''',
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def annotate_file(rel: str) -> None:
    if rel not in ANNOTATED:
        raise KeyError(f"no annotation template: {rel}")
    dst = ANALYZED / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    content = ANNOTATED[rel]
    if not has_chinese(content):
        raise ValueError(f"No Chinese content for: {rel}")
    dst.write_text(content, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    for rel in WAVE7A_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
