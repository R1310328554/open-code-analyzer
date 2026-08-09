#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 wave-1b middleware/security [15:30]."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_FILES = json.loads((VER / "_reports/class-queue/batch.json").read_text())["files"][15:30]

PREPEND: dict[str, str] = {
    "fastapi/middleware/cors.py": '"""从 Starlette 重新导出 CORS 中间件。"""\n\n',
    "fastapi/middleware/gzip.py": '"""从 Starlette 重新导出 GZip 压缩中间件。"""\n\n',
    "fastapi/middleware/httpsredirect.py": '"""从 Starlette 重新导出 HTTPS 重定向中间件。"""\n\n',
    "fastapi/middleware/trustedhost.py": '"""从 Starlette 重新导出受信任主机中间件。"""\n\n',
    "fastapi/middleware/wsgi.py": '"""从 Starlette 重新导出 WSGI 中间件，用于挂载 WSGI 应用。"""\n\n',
    "fastapi/openapi/__init__.py": '"""OpenAPI 相关工具与文档 UI 生成模块。"""\n',
    "fastapi/openapi/constants.py": '"""OpenAPI 生成所用的常量定义。"""\n\n',
    "fastapi/requests.py": '"""从 Starlette 重新导出 HTTP 连接与请求类型。"""\n\n',
    "fastapi/security/__init__.py": '"""FastAPI 安全方案（API Key、HTTP、OAuth2、OpenID Connect 等）。"""\n\n',
}

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "fastapi/middleware/asyncexitstack.py": [
        (
            "# Used mainly to close files after the request is done, dependencies are closed\n# in their own AsyncExitStack\nclass AsyncExitStackMiddleware:",
            "# 主要用于在请求结束后关闭文件；依赖项在各自的 AsyncExitStack 中关闭\nclass AsyncExitStackMiddleware:",
        ),
        (
            "class AsyncExitStackMiddleware:\n    def __init__(",
            'class AsyncExitStackMiddleware:\n    """在 ASGI scope 中注入 AsyncExitStack，供依赖项与资源清理使用。"""\n\n    def __init__(',
        ),
    ],
    "fastapi/openapi/constants.py": [
        (
            'METHODS_WITH_BODY = {"GET", "HEAD", "POST", "PUT", "DELETE", "PATCH"}',
            '# 可能携带请求体的 HTTP 方法集合\nMETHODS_WITH_BODY = {"GET", "HEAD", "POST", "PUT", "DELETE", "PATCH"}',
        ),
        (
            'REF_PREFIX = "#/components/schemas/"',
            '# OpenAPI 组件 schema 引用前缀\nREF_PREFIX = "#/components/schemas/"',
        ),
        (
            'REF_TEMPLATE = "#/components/schemas/{model}"',
            '# schema 引用模板，{model} 为模型名\nREF_TEMPLATE = "#/components/schemas/{model}"',
        ),
    ],
    "fastapi/security/base.py": [
        (
            "class SecurityBase:\n    model: SecurityBaseModel",
            'class SecurityBase:\n    """所有安全方案依赖的基类。"""\n\n    model: SecurityBaseModel',
        ),
    ],
    "fastapi/responses.py": [
        (
            'class UJSONResponse(JSONResponse):\n    """JSON response using the ujson library to serialize data to JSON.\n\n    **Deprecated**: `UJSONResponse` is deprecated. FastAPI now serializes data\n    directly to JSON bytes via Pydantic when a return type or response model is\n    set, which is faster and doesn\'t need a custom response class.\n\n    Read more in the\n    [FastAPI docs for Custom Response](https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model)\n    and the\n    [FastAPI docs for Response Model](https://fastapi.tiangolo.com/tutorial/response-model/).\n\n    **Note**: `ujson` is not included with FastAPI and must be installed\n    separately, e.g. `pip install ujson`.\n    """',
            'class UJSONResponse(JSONResponse):\n    """使用 ujson 库将数据序列化为 JSON 的响应类。\n\n    **已弃用**：`UJSONResponse` 已弃用。当设置了返回类型或响应模型时，\n    FastAPI 现通过 Pydantic 直接将数据序列化为 JSON 字节，速度更快且无需自定义响应类。\n\n    详见\n    [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model)\n    与\n    [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。\n\n    **注意**：FastAPI 不包含 `ujson`，需单独安装，例如 `pip install ujson`。\n    """',
        ),
        (
            'class ORJSONResponse(JSONResponse):\n    """JSON response using the orjson library to serialize data to JSON.\n\n    **Deprecated**: `ORJSONResponse` is deprecated. FastAPI now serializes data\n    directly to JSON bytes via Pydantic when a return type or response model is\n    set, which is faster and doesn\'t need a custom response class.\n\n    Read more in the\n    [FastAPI docs for Custom Response](https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model)\n    and the\n    [FastAPI docs for Response Model](https://fastapi.tiangolo.com/tutorial/response-model/).\n\n    **Note**: `orjson` is not included with FastAPI and must be installed\n    separately, e.g. `pip install orjson`.\n    """',
            'class ORJSONResponse(JSONResponse):\n    """使用 orjson 库将数据序列化为 JSON 的响应类。\n\n    **已弃用**：`ORJSONResponse` 已弃用。当设置了返回类型或响应模型时，\n    FastAPI 现通过 Pydantic 直接将数据序列化为 JSON 字节，速度更快且无需自定义响应类。\n\n    详见\n    [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model)\n    与\n    [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。\n\n    **注意**：FastAPI 不包含 `orjson`，需单独安装，例如 `pip install orjson`。\n    """',
        ),
    ],
    "fastapi/security/open_id_connect_url.py": [
        (
            'class OpenIdConnect(SecurityBase):\n    """\n    OpenID Connect authentication class. An instance of it would be used as a\n    dependency.\n\n    **Warning**: this is only a stub to connect the components with OpenAPI in FastAPI,\n    but it doesn\'t implement the full OpenIdConnect scheme, for example, it doesn\'t use\n    the OpenIDConnect URL. You would need to subclass it and implement it in your\n    code.\n    """',
            'class OpenIdConnect(SecurityBase):\n    """\n    OpenID Connect 认证类，其实例可用作依赖项。\n\n    **警告**：这仅是用于在 FastAPI 中将组件与 OpenAPI 关联的桩实现，\n    并未实现完整的 OpenID Connect 方案，例如不会真正使用 OpenID Connect URL。\n    你需要在代码中子类化并实现完整逻辑。\n    """',
        ),
        (
            'Doc(\n                """\n            The OpenID Connect URL.\n            """\n            )',
            'Doc(\n                """\n            OpenID Connect 配置 URL。\n            """\n            )',
        ),
        (
            'Doc(\n                """\n                Security scheme name.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
            'Doc(\n                """\n                安全方案名称。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                Security scheme description.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
            'Doc(\n                """\n                安全方案描述。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                By default, if no HTTP Authorization header is provided, required for\n                OpenID Connect authentication, it will automatically cancel the request\n                and send the client an error.\n\n                If `auto_error` is set to `False`, when the HTTP Authorization header\n                is not available, instead of erroring out, the dependency result will\n                be `None`.\n\n                This is useful when you want to have optional authentication.\n\n                It is also useful when you want to have authentication that can be\n                provided in one of multiple optional ways (for example, with OpenID\n                Connect or in a cookie).\n                """\n            )',
            'Doc(\n                """\n                默认情况下，若未提供 OpenID Connect 认证所需的 HTTP Authorization 头，\n                将自动终止请求并向客户端返回错误。\n\n                若 `auto_error` 设为 `False`，当 Authorization 头不可用时，\n                依赖项结果将为 `None` 而非抛出错误。\n\n                适用于可选认证场景。\n\n                也适用于多种可选认证方式之一（例如 OpenID Connect 或 Cookie）。\n                """\n            )',
        ),
    ],
    "fastapi/security/api_key.py": [
        (
            'def make_not_authenticated_error(self) -> HTTPException:\n        """\n        The WWW-Authenticate header is not standardized for API Key authentication but\n        the HTTP specification requires that an error of 401 "Unauthorized" must\n        include a WWW-Authenticate header.\n\n        Ref: https://datatracker.ietf.org/doc/html/rfc9110#name-401-unauthorized\n\n        For this, this method sends a custom challenge `APIKey`.\n        """',
            'def make_not_authenticated_error(self) -> HTTPException:\n        """\n        API Key 认证未标准化 WWW-Authenticate 头，但 HTTP 规范要求\n        401 "Unauthorized" 响应必须包含 WWW-Authenticate 头。\n\n        参考：https://datatracker.ietf.org/doc/html/rfc9110#name-401-unauthorized\n\n        因此本方法发送自定义 challenge `APIKey`。\n        """',
        ),
        (
            'class APIKeyQuery(APIKeyBase):\n    """\n    API key authentication using a query parameter.\n\n    This defines the name of the query parameter that should be provided in the request\n    with the API key and integrates that into the OpenAPI documentation. It extracts\n    the key value sent in the query parameter automatically and provides it as the\n    dependency result. But it doesn\'t define how to send that API key to the client.\n\n    ## Usage\n\n    Create an instance object and use that object as the dependency in `Depends()`.\n\n    The dependency result will be a string containing the key value.\n\n    ## Example\n\n    ```python\n    from fastapi import Depends, FastAPI\n    from fastapi.security import APIKeyQuery\n\n    app = FastAPI()\n\n    query_scheme = APIKeyQuery(name="api_key")\n\n\n    @app.get("/items/")\n    async def read_items(api_key: str = Depends(query_scheme)):\n        return {"api_key": api_key}\n    ```\n    """',
            'class APIKeyQuery(APIKeyBase):\n    """\n    通过查询参数进行 API Key 认证。\n\n    定义请求中应携带 API Key 的查询参数名称，并将其集成到 OpenAPI 文档。\n    自动从查询参数提取 Key 值作为依赖项结果，但不定义如何将 Key 分发给客户端。\n\n    ## 用法\n\n    创建实例并在 `Depends()` 中作为依赖项使用。\n\n    依赖项结果为包含 Key 值的字符串。\n\n    ## 示例\n\n    ```python\n    from fastapi import Depends, FastAPI\n    from fastapi.security import APIKeyQuery\n\n    app = FastAPI()\n\n    query_scheme = APIKeyQuery(name="api_key")\n\n\n    @app.get("/items/")\n    async def read_items(api_key: str = Depends(query_scheme)):\n        return {"api_key": api_key}\n    ```\n    """',
        ),
        (
            'Doc("Query parameter name.")',
            'Doc("查询参数名称。")',
        ),
        (
            'class APIKeyHeader(APIKeyBase):\n    """\n    API key authentication using a header.\n\n    This defines the name of the header that should be provided in the request with\n    the API key and integrates that into the OpenAPI documentation. It extracts\n    the key value sent in the header automatically and provides it as the dependency\n    result. But it doesn\'t define how to send that key to the client.\n\n    ## Usage\n\n    Create an instance object and use that object as the dependency in `Depends()`.\n\n    The dependency result will be a string containing the key value.\n\n    ## Example\n\n    ```python\n    from fastapi import Depends, FastAPI\n    from fastapi.security import APIKeyHeader\n\n    app = FastAPI()\n\n    header_scheme = APIKeyHeader(name="x-key")\n\n\n    @app.get("/items/")\n    async def read_items(key: str = Depends(header_scheme)):\n        return {"key": key}\n    ```\n    """',
            'class APIKeyHeader(APIKeyBase):\n    """\n    通过请求头进行 API Key 认证。\n\n    定义请求中应携带 API Key 的请求头名称，并将其集成到 OpenAPI 文档。\n    自动从请求头提取 Key 值作为依赖项结果，但不定义如何将 Key 分发给客户端。\n\n    ## 用法\n\n    创建实例并在 `Depends()` 中作为依赖项使用。\n\n    依赖项结果为包含 Key 值的字符串。\n\n    ## 示例\n\n    ```python\n    from fastapi import Depends, FastAPI\n    from fastapi.security import APIKeyHeader\n\n    app = FastAPI()\n\n    header_scheme = APIKeyHeader(name="x-key")\n\n\n    @app.get("/items/")\n    async def read_items(key: str = Depends(header_scheme)):\n        return {"key": key}\n    ```\n    """',
        ),
        (
            'Doc("Header name.")',
            'Doc("请求头名称。")',
        ),
        (
            'class APIKeyCookie(APIKeyBase):\n    """\n    API key authentication using a cookie.\n\n    This defines the name of the cookie that should be provided in the request with\n    the API key and integrates that into the OpenAPI documentation. It extracts\n    the key value sent in the cookie automatically and provides it as the dependency\n    result. But it doesn\'t define how to set that cookie.\n\n    ## Usage\n\n    Create an instance object and use that object as the dependency in `Depends()`.\n\n    The dependency result will be a string containing the key value.\n\n    ## Example\n\n    ```python\n    from fastapi import Depends, FastAPI\n    from fastapi.security import APIKeyCookie\n\n    app = FastAPI()\n\n    cookie_scheme = APIKeyCookie(name="session")\n\n\n    @app.get("/items/")\n    async def read_items(session: str = Depends(cookie_scheme)):\n        return {"session": session}\n    ```\n    """',
            'class APIKeyCookie(APIKeyBase):\n    """\n    通过 Cookie 进行 API Key 认证。\n\n    定义请求中应携带 API Key 的 Cookie 名称，并将其集成到 OpenAPI 文档。\n    自动从 Cookie 提取 Key 值作为依赖项结果，但不定义如何设置该 Cookie。\n\n    ## 用法\n\n    创建实例并在 `Depends()` 中作为依赖项使用。\n\n    依赖项结果为包含 Key 值的字符串。\n\n    ## 示例\n\n    ```python\n    from fastapi import Depends, FastAPI\n    from fastapi.security import APIKeyCookie\n\n    app = FastAPI()\n\n    cookie_scheme = APIKeyCookie(name="session")\n\n\n    @app.get("/items/")\n    async def read_items(session: str = Depends(cookie_scheme)):\n        return {"session": session}\n    ```\n    """',
        ),
        (
            'Doc("Cookie name.")',
            'Doc("Cookie 名称。")',
        ),
        (
            'Doc(\n                """\n                By default, if the query parameter is not provided, `APIKeyQuery` will\n                automatically cancel the request and send the client an error.\n\n                If `auto_error` is set to `False`, when the query parameter is not\n                available, instead of erroring out, the dependency result will be\n                `None`.\n\n                This is useful when you want to have optional authentication.\n\n                It is also useful when you want to have authentication that can be\n                provided in one of multiple optional ways (for example, in a query\n                parameter or in an HTTP Bearer token).\n                """\n            )',
            'Doc(\n                """\n                默认情况下，若未提供查询参数，`APIKeyQuery` 将\n                自动终止请求并向客户端返回错误。\n\n                若 `auto_error` 设为 `False`，当查询参数不可用时，\n                依赖项结果将为 `None` 而非抛出错误。\n\n                适用于可选认证场景。\n\n                也适用于多种可选认证方式之一（例如查询参数或 HTTP Bearer 令牌）。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                By default, if the header is not provided, `APIKeyHeader` will\n                automatically cancel the request and send the client an error.\n\n                If `auto_error` is set to `False`, when the header is not available,\n                instead of erroring out, the dependency result will be `None`.\n\n                This is useful when you want to have optional authentication.\n\n                It is also useful when you want to have authentication that can be\n                provided in one of multiple optional ways (for example, in a header or\n                in an HTTP Bearer token).\n                """\n            )',
            'Doc(\n                """\n                默认情况下，若未提供请求头，`APIKeyHeader` 将\n                自动终止请求并向客户端返回错误。\n\n                若 `auto_error` 设为 `False`，当请求头不可用时，\n                依赖项结果将为 `None` 而非抛出错误。\n\n                适用于可选认证场景。\n\n                也适用于多种可选认证方式之一（例如请求头或 HTTP Bearer 令牌）。\n                """\n            )',
        ),
        (
            'Doc(\n                """\n                By default, if the cookie is not provided, `APIKeyCookie` will\n                automatically cancel the request and send the client an error.\n\n                If `auto_error` is set to `False`, when the cookie is not available,\n                instead of erroring out, the dependency result will be `None`.\n\n                This is useful when you want to have optional authentication.\n\n                It is also useful when you want to have authentication that can be\n                provided in one of multiple optional ways (for example, in a cookie or\n                in an HTTP Bearer token).\n                """\n            )',
            'Doc(\n                """\n                默认情况下，若未提供 Cookie，`APIKeyCookie` 将\n                自动终止请求并向客户端返回错误。\n\n                若 `auto_error` 设为 `False`，当 Cookie 不可用时，\n                依赖项结果将为 `None` 而非抛出错误。\n\n                适用于可选认证场景。\n\n                也适用于多种可选认证方式之一（例如 Cookie 或 HTTP Bearer 令牌）。\n                """\n            )',
        ),
    ],
    "fastapi/openapi/docs.py": [
        (
            'def _html_safe_json(value: Any) -> str:\n    """Serialize a value to JSON with HTML special characters escaped.\n\n    This prevents injection when the JSON is embedded inside a <script> tag.\n    """',
            'def _html_safe_json(value: Any) -> str:\n    """将值序列化为 JSON，并转义 HTML 特殊字符。\n\n    防止 JSON 嵌入 `<script>` 标签时发生注入。\n    """',
        ),
        (
            'Doc(\n        """\n        Default configurations for Swagger UI.\n\n        You can use it as a template to add any other configurations needed.\n        """\n    )',
            'Doc(\n        """\n        Swagger UI 的默认配置。\n\n        可作为模板添加其他所需配置。\n        """\n    )',
        ),
        (
            'Doc(\n            """\n            The OpenAPI URL that Swagger UI should load and use.\n\n            This is normally done automatically by FastAPI using the default URL\n            `/openapi.json`.\n\n            Read more about it in the\n            [FastAPI docs for Conditional OpenAPI](https://fastapi.tiangolo.com/how-to/conditional-openapi/#conditional-openapi-from-settings-and-env-vars)\n            """\n        )',
            'Doc(\n            """\n            Swagger UI 应加载的 OpenAPI URL。\n\n            FastAPI 通常自动使用默认 URL `/openapi.json`。\n\n            详见\n            [FastAPI 条件 OpenAPI 文档](https://fastapi.tiangolo.com/how-to/conditional-openapi/#conditional-openapi-from-settings-and-env-vars)\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            The HTML `<title>` content, normally shown in the browser tab.\n\n            Read more about it in the\n            [FastAPI docs for Custom Docs UI Static Assets](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
            'Doc(\n            """\n            HTML `<title>` 内容，通常显示在浏览器标签页。\n\n            详见\n            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            The URL to use to load the Swagger UI JavaScript.\n\n            It is normally set to a CDN URL.\n\n            Read more about it in the\n            [FastAPI docs for Custom Docs UI Static Assets](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
            'Doc(\n            """\n            加载 Swagger UI JavaScript 的 URL。\n\n            通常设置为 CDN 地址。\n\n            详见\n            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            The URL to use to load the Swagger UI CSS.\n\n            It is normally set to a CDN URL.\n\n            Read more about it in the\n            [FastAPI docs for Custom Docs UI Static Assets](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
            'Doc(\n            """\n            加载 Swagger UI CSS 的 URL。\n\n            通常设置为 CDN 地址。\n\n            详见\n            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            The URL of the favicon to use. It is normally shown in the browser tab.\n            """\n        )',
            'Doc(\n            """\n            使用的 favicon URL，通常显示在浏览器标签页。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            The OAuth2 redirect URL, it is normally automatically handled by FastAPI.\n\n            Read more about it in the\n            [FastAPI docs for Custom Docs UI Static Assets](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
            'Doc(\n            """\n            OAuth2 重定向 URL，通常由 FastAPI 自动处理。\n\n            详见\n            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            A dictionary with Swagger UI OAuth2 initialization configurations.\n\n            Read more about the available configuration options in the\n            [Swagger UI docs](https://swagger.io/docs/open-source-tools/swagger-ui/usage/oauth2/).\n            """\n        )',
            'Doc(\n            """\n            Swagger UI OAuth2 初始化配置字典。\n\n            可用配置选项详见\n            [Swagger UI 文档](https://swagger.io/docs/open-source-tools/swagger-ui/usage/oauth2/)。\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Configuration parameters for Swagger UI.\n\n            It defaults to [swagger_ui_default_parameters][fastapi.openapi.docs.swagger_ui_default_parameters].\n\n            Read more about it in the\n            [FastAPI docs about how to Configure Swagger UI](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/).\n            """\n        )',
            'Doc(\n            """\n            Swagger UI 配置参数。\n\n            默认为 [swagger_ui_default_parameters][fastapi.openapi.docs.swagger_ui_default_parameters]。\n\n            详见\n            [FastAPI 配置 Swagger UI 文档](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/)。\n            """\n        )',
        ),
        (
            '    """\n    Generate and return the HTML  that loads Swagger UI for the interactive\n    API docs (normally served at `/docs`).\n\n    You would only call this function yourself if you needed to override some parts,\n    for example the URLs to use to load Swagger UI\'s JavaScript and CSS.\n\n    Read more about it in the\n    [FastAPI docs for Configure Swagger UI](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/)\n    and the [FastAPI docs for Custom Docs UI Static Assets (Self-Hosting)](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/).\n    """',
            '    """\n    生成并返回加载 Swagger UI 的 HTML，用于交互式 API 文档（通常在 `/docs` 提供）。\n\n    仅在需要覆盖部分内容（例如 Swagger UI 的 JavaScript 与 CSS URL）时自行调用。\n\n    详见\n    [FastAPI 配置 Swagger UI 文档](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/)\n    与 [FastAPI 自托管文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)。\n    """',
        ),
        (
            '    <!-- `SwaggerUIBundle` is now available on the page -->',
            '    <!-- 页面现已可用 `SwaggerUIBundle` -->',
        ),
        (
            'Doc(\n            """\n            The OpenAPI URL that ReDoc should load and use.\n\n            This is normally done automatically by FastAPI using the default URL\n            `/openapi.json`.\n\n            Read more about it in the\n            [FastAPI docs for Conditional OpenAPI](https://fastapi.tiangolo.com/how-to/conditional-openapi/#conditional-openapi-from-settings-and-env-vars)\n            """\n        )',
            'Doc(\n            """\n            ReDoc 应加载的 OpenAPI URL。\n\n            FastAPI 通常自动使用默认 URL `/openapi.json`。\n\n            详见\n            [FastAPI 条件 OpenAPI 文档](https://fastapi.tiangolo.com/how-to/conditional-openapi/#conditional-openapi-from-settings-and-env-vars)\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            The URL to use to load the ReDoc JavaScript.\n\n            It is normally set to a CDN URL.\n\n            Read more about it in the\n            [FastAPI docs for Custom Docs UI Static Assets](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
            'Doc(\n            """\n            加载 ReDoc JavaScript 的 URL。\n\n            通常设置为 CDN 地址。\n\n            详见\n            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)\n            """\n        )',
        ),
        (
            'Doc(\n            """\n            Load and use Google Fonts.\n            """\n        )',
            'Doc(\n            """\n            是否加载并使用 Google Fonts。\n            """\n        )',
        ),
        (
            '    """\n    Generate and return the HTML response that loads ReDoc for the alternative\n    API docs (normally served at `/redoc`).\n\n    You would only call this function yourself if you needed to override some parts,\n    for example the URLs to use to load ReDoc\'s JavaScript and CSS.\n\n    Read more about it in the\n    [FastAPI docs for Custom Docs UI Static Assets (Self-Hosting)](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/).\n    """',
            '    """\n    生成并返回加载 ReDoc 的 HTML，用于备选 API 文档（通常在 `/redoc` 提供）。\n\n    仅在需要覆盖部分内容（例如 ReDoc 的 JavaScript URL）时自行调用。\n\n    详见\n    [FastAPI 自托管文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)。\n    """',
        ),
        (
            '    <!-- needed for adaptive design -->',
            '    <!-- 自适应布局所需 -->',
        ),
        (
            '    <!--\n    ReDoc doesn\'t change outer page styles\n    -->',
            '    <!--\n    ReDoc 不修改外层页面样式\n    -->',
        ),
        (
            '    """\n    Generate the HTML response with the OAuth2 redirection for Swagger UI.\n\n    You normally don\'t need to use or change this.\n    """',
            '    """\n    生成 Swagger UI OAuth2 重定向 HTML 响应。\n\n    通常无需自行使用或修改。\n    """',
        ),
        (
            '    # copied from https://github.com/swagger-api/swagger-ui/blob/v4.14.0/dist/oauth2-redirect.html',
            '    # 复制自 https://github.com/swagger-api/swagger-ui/blob/v4.14.0/dist/oauth2-redirect.html',
        ),
    ],
}

# Shared security scheme Doc blocks (api_key + open_id_connect_url)
_SECURITY_SCHEME_DOC = (
    'Doc(\n                """\n                Security scheme name.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
    'Doc(\n                """\n                安全方案名称。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
)
_SECURITY_DESC_DOC = (
    'Doc(\n                """\n                Security scheme description.\n\n                It will be included in the generated OpenAPI (e.g. visible at `/docs`).\n                """\n            )',
    'Doc(\n                """\n                安全方案描述。\n\n                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。\n                """\n            )',
)

for _rel in ("fastapi/security/api_key.py",):
    FILE_REPLACEMENTS.setdefault(_rel, [])
    for old, new in (_SECURITY_SCHEME_DOC, _SECURITY_DESC_DOC):
        if (old, new) not in FILE_REPLACEMENTS[_rel]:
            FILE_REPLACEMENTS[_rel].append((old, new))


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    if rel in PREPEND and not text.startswith('"""'):
        text = PREPEND[rel] + text
    for old, new in FILE_REPLACEMENTS.get(rel, []):
        if old == new:
            continue
        count = text.count(old)
        if count == 0:
            raise ValueError(f"Pattern not found in {rel}:\n{old[:120]}...")
        text = text.replace(old, new)
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
