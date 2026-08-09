"""FastAPI 应用类：框架主入口，封装 Starlette 并提供 OpenAPI 与路径操作装饰器。"""

import os
from collections.abc import Awaitable, Callable, Coroutine, Sequence
from enum import Enum
from typing import Annotated, Any, Literal, TypeVar

from annotated_doc import Doc
from fastapi import routing
from fastapi.datastructures import Default, DefaultPlaceholder
from fastapi.exception_handlers import (
    http_exception_handler,
    request_validation_exception_handler,
    websocket_request_validation_exception_handler,
)
from fastapi.exceptions import RequestValidationError, WebSocketRequestValidationError
from fastapi.logger import logger
from fastapi.middleware.asyncexitstack import AsyncExitStackMiddleware
from fastapi.openapi.docs import (
    get_redoc_html,
    get_swagger_ui_html,
    get_swagger_ui_oauth2_redirect_html,
)
from fastapi.openapi.utils import get_openapi
from fastapi.params import Depends
from fastapi.types import DecoratedCallable, IncEx
from fastapi.utils import generate_unique_id
from starlette.applications import Starlette
from starlette.datastructures import State
from starlette.exceptions import HTTPException
from starlette.middleware import Middleware
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.middleware.errors import ServerErrorMiddleware
from starlette.middleware.exceptions import ExceptionMiddleware
from starlette.requests import Request
from starlette.responses import HTMLResponse, JSONResponse, Response
from starlette.routing import BaseRoute
from starlette.types import ASGIApp, ExceptionHandler, Lifespan, Receive, Scope, Send
from typing_extensions import deprecated

AppType = TypeVar("AppType", bound="FastAPI")


class FastAPI(Starlette):
    """
    `FastAPI` 应用类，使用 FastAPI 的主入口。

    详见
    [FastAPI 入门文档](https://fastapi.tiangolo.com/tutorial/first-steps/)。

    ## 示例

    ```python
    from fastapi import FastAPI

    app = FastAPI()
    ```
    """

    def __init__(
        self: AppType,
        *,
        debug: Annotated[
            bool,
            Doc(
                """
                服务端错误时是否返回调试堆栈跟踪。

                详见
                [Starlette 应用文档](https://starlette.dev/applications/#starlette.applications.Starlette)。
                """
            ),
        ] = False,
        routes: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                **注意**：通常不应使用此参数, it is inherited
                from Starlette and supported for compatibility.

                ---

                处理 HTTP 与 WebSocket 请求的路由列表。
                """
            ),
            deprecated(
                """
                FastAPI 中通常不使用此参数，继承自 Starlette 以保持兼容。

                在 FastAPI 中通常使用*路径操作方法*，如 `app.get()`、`app.post()` 等。
                """
            ),
        ] = None,
        title: Annotated[
            str,
            Doc(
                """
                API 标题。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(title="ChimichangApp")
                ```
                """
            ),
        ] = "FastAPI",
        summary: Annotated[
            str | None,
            Doc(
                """
                API 简短摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(summary="Deadpond's favorite app. Nuff said.")
                ```
                """
            ),
        ] = None,
        description: Annotated[
            str,
            Doc(
                '''
                API 描述，支持 Markdown（[CommonMark 语法](https://commonmark.org/)）。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(
                    description="""
                                ChimichangApp API 帮你做很酷的事。🚀

                                ## Items

                                你可以**读取 items**。

                                ## Users

                                你将能够：

                                * **创建 users**（_未实现_）。
                                * **读取 users**（_未实现_）。

                                """
                )
                ```
                '''
            ),
        ] = "",
        version: Annotated[
            str,
            Doc(
                """
                API 版本。

                **注意** 这是应用版本, not the version of
                the OpenAPI specification nor the version of FastAPI being used.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(version="0.0.1")
                ```
                """
            ),
        ] = "0.1.0",
        openapi_url: Annotated[
            str | None,
            Doc(
                """
                提供 OpenAPI schema 的 URL。

                设为 `None` 时不公开提供 OpenAPI schema，
                默认的 `/docs` 与 `/redoc` 端点也会禁用。

                详见
                [FastAPI 元数据与文档 URL 文档（openapi_url）](https://fastapi.tiangolo.com/tutorial/metadata/#openapi-url)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(openapi_url="/api/v1/openapi.json")
                ```
                """
            ),
        ] = "/openapi.json",
        openapi_tags: Annotated[
            list[dict[str, Any]] | None,
            Doc(
                """
                OpenAPI 使用的 tag 列表，与*路径操作*中的 `tags` 相同，例如：

                * `@app.get("/users/", tags=["users"])`
                * `@app.get("/items/", tags=["items"])`

                tag 顺序可控制 Swagger UI（`/docs`）中的展示顺序。

                无需列出所有使用过的 tag。

                未声明的 tag 可能随机排序；列表中 tag 名必须唯一。

                每项为包含以下键的 `dict`：

                * `name`: The name of the tag.
                * `description`: A short description of the tag.
                    [CommonMark syntax](https://commonmark.org/) MAY be used for rich
                    text representation.
                * `externalDocs`: Additional external documentation for this tag. If
                    provided, it would contain a `dict` with:
                    * `description`: A short description of the target documentation.
                        [CommonMark syntax](https://commonmark.org/) MAY be used for
                        rich text representation.
                    * `url`: The URL for the target documentation. Value MUST be in
                        the form of a URL.

                详见
                [FastAPI 元数据与文档 URL 文档（tags）](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-tags)。

                **示例**

                ```python
                from fastapi import FastAPI

                tags_metadata = [
                    {
                        "name": "users",
                        "description": "Operations with users. The **login** logic is also here.",
                    },
                    {
                        "name": "items",
                        "description": "Manage items. So _fancy_ they have their own docs.",
                        "externalDocs": {
                            "description": "Items external docs",
                            "url": "https://fastapi.tiangolo.com/",
                        },
                    },
                ]

                app = FastAPI(openapi_tags=tags_metadata)
                ```
                """
            ),
        ] = None,
        servers: Annotated[
            list[dict[str, str | Any]] | None,
            Doc(
                """
                目标服务器连接信息的 `dict` 列表。

                例如多域名部署时，用同一 Swagger UI 交互，或固定可选 URL。

                未提供或为空时，生成 OpenAPI 的 `servers` 为：

                * a `dict` with a `url` value of the application's mounting point
                (`root_path`) if it's different from `/`.
                * otherwise, the `servers` property will be omitted from the OpenAPI
                schema.

                Each item in the `list` is a `dict` containing:

                * `url`: A URL to the target host. This URL supports Server Variables
                and MAY be relative, to indicate that the host location is relative
                to the location where the OpenAPI document is being served. Variable
                substitutions will be made when a variable is named in `{`brackets`}`.
                * `description`: An optional string describing the host designated by
                the URL. [CommonMark syntax](https://commonmark.org/) MAY be used for
                rich text representation.
                * `variables`: A `dict` between a variable name and its value. The value
                    is used for substitution in the server's URL template.

                详见
                [FastAPI 反向代理文档（附加 servers）](https://fastapi.tiangolo.com/advanced/behind-a-proxy/#additional-servers)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(
                    servers=[
                        {"url": "https://stag.example.com", "description": "Staging environment"},
                        {"url": "https://prod.example.com", "description": "Production environment"},
                    ]
                )
                ```
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                全局依赖列表，应用于每个*路径操作*（含子路由）。

                详见
                [FastAPI 全局依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/global-dependencies/)。

                **示例**

                ```python
                from fastapi import Depends, FastAPI

                from .dependencies import func_dep_1, func_dep_2

                app = FastAPI(dependencies=[Depends(func_dep_1), Depends(func_dep_2)])
                ```
                """
            ),
        ] = None,
        default_response_class: Annotated[
            type[Response],
            Doc(
                """
                使用的默认响应类。

                详见
                [FastAPI docs for Custom Response - HTML, Stream, File, others](https://fastapi.tiangolo.com/advanced/custom-response/#default-response-class).

                **示例**

                ```python
                from fastapi import FastAPI
                from fastapi.responses import ORJSONResponse

                app = FastAPI(default_response_class=ORJSONResponse)
                ```
                """
            ),
        ] = Default(JSONResponse),
        redirect_slashes: Annotated[
            bool,
            Doc(
                """
                客户端 URL 斜杠格式不一致时，是否检测并重定向。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(redirect_slashes=True)  # the default

                @app.get("/items/")
                async def read_items():
                    return [{"item_id": "Foo"}]
                ```

                客户端访问 `/items`（无尾斜杠）时，将以 307 重定向到 `/items/`。
                """
            ),
        ] = True,
        docs_url: Annotated[
            str | None,
            Doc(
                """
                自动交互式 API 文档路径，由 Swagger UI 在浏览器中展示。

                默认 URL 为 `/docs`，设为 `None` 可禁用。

                若 `openapi_url` 为 `None`，将自动禁用。

                详见
                [FastAPI 元数据与文档 URL 文档（docs/redoc）](https://fastapi.tiangolo.com/tutorial/metadata/#docs-urls)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(docs_url="/documentation", redoc_url=None)
                ```
                """
            ),
        ] = "/docs",
        redoc_url: Annotated[
            str | None,
            Doc(
                """
                ReDoc 提供的替代交互式 API 文档路径。

                默认 URL 为 `/redoc`，设为 `None` 可禁用。

                若 `openapi_url` 为 `None`，将自动禁用。

                详见
                [FastAPI 元数据与文档 URL 文档（docs/redoc）](https://fastapi.tiangolo.com/tutorial/metadata/#docs-urls)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(docs_url="/documentation", redoc_url="redocumentation")
                ```
                """
            ),
        ] = "/redoc",
        swagger_ui_oauth2_redirect_url: Annotated[
            str | None,
            Doc(
                """
                Swagger UI 的 OAuth2 重定向端点。

                默认为 `/docs/oauth2-redirect`。

                仅在使用 OAuth2（Swagger UI 的 Authorize 按钮）时需要。
                """
            ),
        ] = "/docs/oauth2-redirect",
        swagger_ui_init_oauth: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                Swagger UI 的 OAuth2 配置，默认在 `/docs` 展示。

                可用配置选项详见
                [Swagger UI docs](https://swagger.io/docs/open-source-tools/swagger-ui/usage/oauth2/).
                """
            ),
        ] = None,
        middleware: Annotated[
            Sequence[Middleware] | None,
            Doc(
                """
                创建应用时添加的中间件列表。

                在 FastAPI 中通常改用 `app.add_middleware()`。

                详见
                [FastAPI docs for Middleware](https://fastapi.tiangolo.com/tutorial/middleware/).
                """
            ),
        ] = None,
        exception_handlers: Annotated[
            dict[
                int | type[Exception],
                Callable[[Request, Any], Coroutine[Any, Any, Response]],
            ]
            | None,
            Doc(
                """
                异常处理器字典。

                在 FastAPI 中通常使用 `@app.exception_handler()` 装饰器。

                详见
                [FastAPI docs for Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/).
                """
            ),
        ] = None,
        on_startup: Annotated[
            Sequence[Callable[[], Any]] | None,
            Doc(
                """
                startup 事件处理器函数列表。

                应改用 `lifespan` 处理器。

                详见 [FastAPI lifespan 文档](https://fastapi.tiangolo.com/advanced/events/)。
                """
            ),
        ] = None,
        on_shutdown: Annotated[
            Sequence[Callable[[], Any]] | None,
            Doc(
                """
                shutdown 事件处理器函数列表。

                应改用 `lifespan` 处理器。

                详见
                [FastAPI docs for `lifespan`](https://fastapi.tiangolo.com/advanced/events/).
                """
            ),
        ] = None,
        lifespan: Annotated[
            Lifespan[AppType] | None,
            Doc(
                """
                `Lifespan` 上下文管理器处理器，用单一上下文管理器替代 `startup`/`shutdown` 函数。

                详见
                [FastAPI docs for `lifespan`](https://fastapi.tiangolo.com/advanced/events/).
                """
            ),
        ] = None,
        terms_of_service: Annotated[
            str | None,
            Doc(
                """
                API 服务条款 URL。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。

                **示例**

                ```python
                app = FastAPI(terms_of_service="http://example.com/terms/")
                ```
                """
            ),
        ] = None,
        contact: Annotated[
            dict[str, str | Any] | None,
            Doc(
                """
                对外 API 的联系信息字典。

                可包含多个字段。

                * `name`: (`str`) The name of the contact person/organization.
                * `url`: (`str`) A URL pointing to the contact information. MUST be in
                    the format of a URL.
                * `email`: (`str`) The email address of the contact person/organization.
                    MUST be in the format of an email address.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。

                **示例**

                ```python
                app = FastAPI(
                    contact={
                        "name": "Deadpoolio the Amazing",
                        "url": "http://x-force.example.com/contact/",
                        "email": "dp@x-force.example.com",
                    }
                )
                ```
                """
            ),
        ] = None,
        license_info: Annotated[
            dict[str, str | Any] | None,
            Doc(
                """
                对外 API 的许可证信息字典。

                可包含多个字段。

                * `name`: (`str`) **REQUIRED** (if a `license_info` is set). The
                    license name used for the API.
                * `identifier`: (`str`) An [SPDX](https://spdx.dev/) license expression
                    for the API. The `identifier` field is mutually exclusive of the `url`
                    field. Available since OpenAPI 3.1.0, FastAPI 0.99.0.
                * `url`: (`str`) A URL to the license used for the API. This MUST be
                    the format of a URL.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。

                **示例**

                ```python
                app = FastAPI(
                    license_info={
                        "name": "Apache 2.0",
                        "url": "https://www.apache.org/licenses/LICENSE-2.0.html",
                    }
                )
                ```
                """
            ),
        ] = None,
        openapi_prefix: Annotated[
            str,
            Doc(
                """
                OpenAPI URL 的 URL 前缀。
                """
            ),
            deprecated(
                """
                "openapi_prefix" 已弃用，请改用更符合 ASGI 标准且更简单的 "root_path"。
                """
            ),
        ] = "",
        root_path: Annotated[
            str,
            Doc(
                """
                代理处理的路径前缀，应用不可见但外部客户端可见，影响 Swagger UI 等。

                详见
                [FastAPI 反向代理文档](https://fastapi.tiangolo.com/advanced/behind-a-proxy/)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(root_path="/api/v1")
                ```
                """
            ),
        ] = "",
        root_path_in_servers: Annotated[
            bool,
            Doc(
                """
                禁用根据 `root_path` 在自动生成 OpenAPI 的 `servers` 字段中生成 URL。

                详见
                [FastAPI 反向代理文档（禁用 root_path 自动 server）](https://fastapi.tiangolo.com/advanced/behind-a-proxy/#disable-automatic-server-from-root-path)。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI(root_path_in_servers=False)
                ```
                """
            ),
        ] = True,
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                在 OpenAPI 中展示的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 附加响应文档](https://fastapi.tiangolo.com/advanced/additional-responses/)。

                另见
                [FastAPI 大型应用文档](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies)。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                OpenAPI callbacks that should apply to all *路径操作*.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        webhooks: Annotated[
            routing.APIRouter | None,
            Doc(
                """
                Add OpenAPI webhooks. This is similar to `callbacks` but it doesn't
                depend on specific *路径操作*.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                **注意**：自 OpenAPI 3.1.0, FastAPI 0.99.0.

                详见
                [FastAPI OpenAPI Webhooks 文档](https://fastapi.tiangolo.com/advanced/openapi-webhooks/)。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                Mark all *路径操作* as deprecated. You probably don't need it,
                but it's available.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（弃用路径操作）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#deprecate-a-path-operation)。
                """
            ),
        ] = None,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                To include (or not) all the *路径操作* in the generated OpenAPI.
                通常不需要，但可用。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        swagger_ui_parameters: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                配置 Swagger UI（默认 `/docs` 的自动生成交互式 API 文档）的参数。

                详见
                [FastAPI 配置 Swagger UI 文档](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
        separate_input_output_schemas: Annotated[
            bool,
            Doc(
                """
                当结果更精确时，是否为请求体与响应体生成独立的 OpenAPI schema。

                自动生成客户端时尤其有用。

                例如模型：

                ```python
                from pydantic import BaseModel

                class Item(BaseModel):
                    name: str
                    tags: list[str] = []
                ```

                输入时 `tags` 非必填，客户端可不提供。

                输出时 `tags` 因有默认值（可为空列表）始终存在，客户端应始终预期该字段。

                此时输入与输出各有一套 schema。

                详见
                [FastAPI 分离输入/输出 schema 文档](https://fastapi.tiangolo.com/how-to/separate-openapi-schemas)
                """
            ),
        ] = True,
        openapi_external_docs: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                提供额外外部文档链接。
                若提供，必须是包含以下键的字典：

                * `description`: A brief description of the external documentation.
                * `url`: The URL pointing to the external documentation. The value **MUST**
                be a valid URL format.

                **示例**:

                ```python
                from fastapi import FastAPI

                external_docs = {
                    "description": "Detailed API Reference",
                    "url": "https://example.com/api-docs",
                }

                app = FastAPI(openapi_external_docs=external_docs)
                ```
                """
            ),
        ] = None,
        strict_content_type: Annotated[
            bool,
            Doc(
                """
                启用对请求 Content-Type 头的严格检查。

                默认 `True` 时，带 body 但无 `Content-Type` 的请求**不会**按 JSON 解析。

                这可防止潜在的 CSRF 攻击
                利用浏览器可无 Content-Type 发请求、绕过 CORS 预检的特性，尤其适用于本地（localhost）运行的应用。

                `False` 时无 Content-Type 的请求 body 仍按 JSON 解析，兼容不发送该头的客户端。

                详见
                [FastAPI Strict Content-Type 文档](https://fastapi.tiangolo.com/advanced/strict-content-type/)。
                """
            ),
        ] = True,
        **extra: Annotated[
            Any,
            Doc(
                """
                存储在应用中的额外关键字参数，FastAPI 内部不使用。
                """
            ),
        ],
    ) -> None:
        self.debug = debug
        self.title = title
        self.summary = summary
        self.description = description
        self.version = version
        self.terms_of_service = terms_of_service
        self.contact = contact
        self.license_info = license_info
        self.openapi_url = openapi_url
        self.openapi_tags = openapi_tags
        self.root_path_in_servers = root_path_in_servers
        self.docs_url = docs_url
        self.redoc_url = redoc_url
        self.swagger_ui_oauth2_redirect_url = swagger_ui_oauth2_redirect_url
        self.swagger_ui_init_oauth = swagger_ui_init_oauth
        self.swagger_ui_parameters = swagger_ui_parameters
        self.servers = servers or []
        self.separate_input_output_schemas = separate_input_output_schemas
        self.openapi_external_docs = openapi_external_docs
        self.extra = extra
        self.openapi_version: Annotated[
            str,
            Doc(
                """
                OpenAPI 版本字符串。

                FastAPI 生成 OpenAPI 3.1.0，但部分工具可能不识别该版本。

                可覆盖此值以兼容旧工具，属变通方案；若未使用 3.1.0 新特性可能可行。

                不作为 `FastAPI` 构造参数，避免误解；仅作为属性可用。

                **示例**

                ```python
                from fastapi import FastAPI

                app = FastAPI()

                app.openapi_version = "3.0.2"
                ```
                """
            ),
        ] = "3.1.0"
        self.openapi_schema: dict[str, Any] | None = None
        self._openapi_routes_version: int | None = None
        if self.openapi_url:
            assert self.title, "A title must be provided for OpenAPI, e.g.: 'My API'"
            assert self.version, "A version must be provided for OpenAPI, e.g.: '2.1.0'"
        # TODO: remove when discarding the openapi_prefix parameter
        if openapi_prefix:
            logger.warning(
                '"openapi_prefix" has been deprecated in favor of "root_path", which '
                "follows more closely the ASGI standard, is simpler, and more "
                "automatic. Check the docs at "
                "https://fastapi.tiangolo.com/advanced/sub-applications/"
            )
        self.webhooks: Annotated[
            routing.APIRouter,
            Doc(
                """
                `app.webhooks` 是 `APIRouter`，其*路径操作*仅用于 webhook 文档。

                详见
                [FastAPI OpenAPI Webhooks 文档](https://fastapi.tiangolo.com/advanced/openapi-webhooks/)。
                """
            ),
        ] = webhooks or routing.APIRouter()
        self.root_path = root_path or openapi_prefix
        self.state: Annotated[
            State,
            Doc(
                """
                应用级 state 对象，整个应用共享，不随请求变化。

                FastAPI 中多数场景应使用依赖项，而非此对象。

                直接继承自 Starlette。

                详见
                [Starlette 应用文档（在 app 实例上存储 state）](https://starlette.dev/applications/#storing-state-on-the-app-instance)。
                """
            ),
        ] = State()
        self.dependency_overrides: Annotated[
            dict[Callable[..., Any], Callable[..., Any]],
            Doc(
                """
                依赖覆盖字典。

                键为原始依赖 callable，值为实际应调用的依赖。

                用于测试，将昂贵依赖替换为测试版本。

                详见
                [FastAPI 测试依赖覆盖文档](https://fastapi.tiangolo.com/advanced/testing-dependencies/)。
                """
            ),
        ] = {}
        self.router: routing.APIRouter = routing.APIRouter(
            routes=routes,
            redirect_slashes=redirect_slashes,
            dependency_overrides_provider=self,
            on_startup=on_startup,
            on_shutdown=on_shutdown,
            lifespan=lifespan,
            default_response_class=default_response_class,
            dependencies=dependencies,
            callbacks=callbacks,
            deprecated=deprecated,
            include_in_schema=include_in_schema,
            responses=responses,
            generate_unique_id_function=generate_unique_id_function,
            strict_content_type=strict_content_type,
        )
        self.exception_handlers: dict[
            Any, Callable[[Request, Any], Response | Awaitable[Response]]
        ] = {} if exception_handlers is None else dict(exception_handlers)
        self.exception_handlers.setdefault(HTTPException, http_exception_handler)
        self.exception_handlers.setdefault(
            RequestValidationError, request_validation_exception_handler
        )

        # Starlette still has incorrect type specification for the handlers
        self.exception_handlers.setdefault(
            WebSocketRequestValidationError,
            websocket_request_validation_exception_handler,  # type: ignore[arg-type]
        )  # ty: ignore[no-matching-overload]

        self.user_middleware: list[Middleware] = (
            [] if middleware is None else list(middleware)
        )
        self.middleware_stack: ASGIApp | None = None
        self.setup()

    def build_middleware_stack(self) -> ASGIApp:
        # 覆盖 Starlette：在 ExceptionMiddleware 内、用户中间件内添加 AsyncExitStackMiddleware
        debug = self.debug
        error_handler = None
        exception_handlers: dict[Any, ExceptionHandler] = {}

        for key, value in self.exception_handlers.items():
            if key in (500, Exception):
                error_handler = value
            else:
                exception_handlers[key] = value

        middleware = (
            [Middleware(ServerErrorMiddleware, handler=error_handler, debug=debug)]
            + self.user_middleware
            + [
                Middleware(
                    ExceptionMiddleware,
                    handlers=exception_handlers,
                    debug=debug,
                ),
                # 添加 FastAPI 专用的 AsyncExitStackMiddleware 以关闭文件。
                # 此前也用于关闭带 yield 的依赖，现依赖有独立 AsyncExitStack，
                # 以支持流式响应并保持与旧版（如 0.117.1）在 yield 依赖内 except HTTPException 的兼容。
                # This needs to happen after user middlewares because those create a
                # new contextvars context copy by using a new AnyIO task group.
                # This AsyncExitStack preserves the context for contextvars, not
                # strictly necessary for closing files but it was one of the original
                # intentions.
                # If the AsyncExitStack lived outside of the custom middlewares and
                # contextvars were set, for example in a dependency with 'yield'
                # in that internal contextvars context, the values would not be
                # available in the outer context of the AsyncExitStack.
                # By placing the middleware and the AsyncExitStack here, inside all
                # user middlewares, the same context is used.
                # This is currently not needed, only for closing files, but used to be
                # important when dependencies with yield were closed here.
                Middleware(AsyncExitStackMiddleware),
            ]
        )

        app = self.router
        for cls, args, kwargs in reversed(middleware):
            app = cls(app, *args, **kwargs)
        return app

    def openapi(self) -> dict[str, Any]:
        """
        生成应用的 OpenAPI schema，由 FastAPI 内部调用。

        首次调用结果存入 `app.openapi_schema`，之后直接返回，避免重复生成。

        如需修改生成的 OpenAPI schema，可直接修改。

        详见
        [FastAPI 扩展 OpenAPI 文档](https://fastapi.tiangolo.com/how-to/extending-openapi/)。
        """
        routes_version = self.router._get_routes_version()
        if not self.openapi_schema or self._openapi_routes_version != routes_version:
            self.openapi_schema = get_openapi(
                title=self.title,
                version=self.version,
                openapi_version=self.openapi_version,
                summary=self.summary,
                description=self.description,
                terms_of_service=self.terms_of_service,
                contact=self.contact,
                license_info=self.license_info,
                routes=self.routes,
                webhooks=self.webhooks.routes,
                tags=self.openapi_tags,
                servers=self.servers,
                separate_input_output_schemas=self.separate_input_output_schemas,
                external_docs=self.openapi_external_docs,
            )
            self._openapi_routes_version = routes_version
        return self.openapi_schema

    def setup(self) -> None:
        if self.openapi_url:

            async def openapi(req: Request) -> JSONResponse:
                root_path = req.scope.get("root_path", "").rstrip("/")
                schema = self.openapi()
                if root_path and self.root_path_in_servers:
                    server_urls = {s.get("url") for s in schema.get("servers", [])}
                    if root_path not in server_urls:
                        schema = dict(schema)
                        schema["servers"] = [{"url": root_path}] + schema.get(
                            "servers", []
                        )
                return JSONResponse(schema)

            self.add_route(self.openapi_url, openapi, include_in_schema=False)
        if self.openapi_url and self.docs_url:

            async def swagger_ui_html(req: Request) -> HTMLResponse:
                root_path = req.scope.get("root_path", "").rstrip("/")
                openapi_url = root_path + self.openapi_url
                oauth2_redirect_url = self.swagger_ui_oauth2_redirect_url
                if oauth2_redirect_url:
                    oauth2_redirect_url = root_path + oauth2_redirect_url
                return get_swagger_ui_html(
                    openapi_url=openapi_url,
                    title=f"{self.title} - Swagger UI",
                    oauth2_redirect_url=oauth2_redirect_url,
                    init_oauth=self.swagger_ui_init_oauth,
                    swagger_ui_parameters=self.swagger_ui_parameters,
                )

            self.add_route(self.docs_url, swagger_ui_html, include_in_schema=False)

            if self.swagger_ui_oauth2_redirect_url:

                async def swagger_ui_redirect(req: Request) -> HTMLResponse:
                    return get_swagger_ui_oauth2_redirect_html()

                self.add_route(
                    self.swagger_ui_oauth2_redirect_url,
                    swagger_ui_redirect,
                    include_in_schema=False,
                )
        if self.openapi_url and self.redoc_url:

            async def redoc_html(req: Request) -> HTMLResponse:
                root_path = req.scope.get("root_path", "").rstrip("/")
                openapi_url = root_path + self.openapi_url
                return get_redoc_html(
                    openapi_url=openapi_url, title=f"{self.title} - ReDoc"
                )

            self.add_route(self.redoc_url, redoc_html, include_in_schema=False)

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        if self.root_path:
            scope["root_path"] = self.root_path
        await super().__call__(scope, receive, send)

    def add_api_route(
        self,
        path: str,
        endpoint: Callable[..., Any],
        *,
        response_model: Any = Default(None),
        status_code: int | None = None,
        tags: list[str | Enum] | None = None,
        dependencies: Sequence[Depends] | None = None,
        summary: str | None = None,
        description: str | None = None,
        response_description: str = "Successful Response",
        responses: dict[int | str, dict[str, Any]] | None = None,
        deprecated: bool | None = None,
        methods: list[str] | None = None,
        operation_id: str | None = None,
        response_model_include: IncEx | None = None,
        response_model_exclude: IncEx | None = None,
        response_model_by_alias: bool = True,
        response_model_exclude_unset: bool = False,
        response_model_exclude_defaults: bool = False,
        response_model_exclude_none: bool = False,
        include_in_schema: bool = True,
        response_class: type[Response] | DefaultPlaceholder = Default(JSONResponse),
        name: str | None = None,
        openapi_extra: dict[str, Any] | None = None,
        generate_unique_id_function: Callable[[routing.APIRoute], str] = Default(
            generate_unique_id
        ),
    ) -> None:
        self.router.add_api_route(
            path,
            endpoint=endpoint,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=methods,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def frontend(
        self,
        path: Annotated[
            str,
            Doc(
                """
                提供前端构建的 URL 路径前缀。
                """
            ),
        ],
        *,
        directory: Annotated[
            str | os.PathLike[str],
            Doc(
                """
                静态前端构建输出目录。
                """
            ),
        ],
        fallback: Annotated[
            Literal["auto", "index.html", "404.html"] | None,
            Doc(
                """
                前端路径缺失时的回退文件行为。
                """
            ),
        ] = "auto",
        check_dir: Annotated[
            bool | Literal["auto"],
            Doc(
                """
                创建应用时检查前端目录；`"auto"` 时在 `FASTAPI_ENV` 为 `"development"` 下跳过检查并警告，否则检查。`fastapi dev` 未设置时会将 `FASTAPI_ENV` 设为 `"development"`。
                """
            ),
        ] = "auto",
    ) -> None:
        """
        以低优先级路由提供静态前端构建。

        适用于将静态文件构建到目录（如 `dist`）的前端工具。**FastAPI** 先匹配*路径操作*，无匹配时才检查前端文件。

        典型项目结构：

        ```text
        .
        ├── pyproject.toml
        ├── app
        │   ├── __init__.py
        │   └── main.py
        └── dist
            ├── index.html
            └── assets
                └── app.js
        ```

        在 `app/main.py` 中：

        ```python
        from fastapi import FastAPI

        app = FastAPI()
        app.frontend("/", directory="dist")
        ```
        """
        check_dir = routing._resolve_frontend_check_dir(
            directory=directory, check_dir=check_dir
        )
        self.router.frontend(
            path,
            directory=directory,
            fallback=fallback,
            check_dir=check_dir,
        )

    def api_route(
        self,
        path: str,
        *,
        response_model: Any = Default(None),
        status_code: int | None = None,
        tags: list[str | Enum] | None = None,
        dependencies: Sequence[Depends] | None = None,
        summary: str | None = None,
        description: str | None = None,
        response_description: str = "Successful Response",
        responses: dict[int | str, dict[str, Any]] | None = None,
        deprecated: bool | None = None,
        methods: list[str] | None = None,
        operation_id: str | None = None,
        response_model_include: IncEx | None = None,
        response_model_exclude: IncEx | None = None,
        response_model_by_alias: bool = True,
        response_model_exclude_unset: bool = False,
        response_model_exclude_defaults: bool = False,
        response_model_exclude_none: bool = False,
        include_in_schema: bool = True,
        response_class: type[Response] = Default(JSONResponse),
        name: str | None = None,
        openapi_extra: dict[str, Any] | None = None,
        generate_unique_id_function: Callable[[routing.APIRoute], str] = Default(
            generate_unique_id
        ),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.router.add_api_route(
                path,
                func,
                response_model=response_model,
                status_code=status_code,
                tags=tags,
                dependencies=dependencies,
                summary=summary,
                description=description,
                response_description=response_description,
                responses=responses,
                deprecated=deprecated,
                methods=methods,
                operation_id=operation_id,
                response_model_include=response_model_include,
                response_model_exclude=response_model_exclude,
                response_model_by_alias=response_model_by_alias,
                response_model_exclude_unset=response_model_exclude_unset,
                response_model_exclude_defaults=response_model_exclude_defaults,
                response_model_exclude_none=response_model_exclude_none,
                include_in_schema=include_in_schema,
                response_class=response_class,
                name=name,
                openapi_extra=openapi_extra,
                generate_unique_id_function=generate_unique_id_function,
            )
            return func

        return decorator

    def add_api_websocket_route(
        self,
        path: str,
        endpoint: Callable[..., Any],
        name: str | None = None,
        *,
        dependencies: Sequence[Depends] | None = None,
    ) -> None:
        self.router.add_api_websocket_route(
            path,
            endpoint,
            name=name,
            dependencies=dependencies,
        )

    def websocket(
        self,
        path: Annotated[
            str,
            Doc(
                """
                WebSocket 路径。
                """
            ),
        ],
        name: Annotated[
            str | None,
            Doc(
                """
                WebSocket 名称，仅内部使用。
                """
            ),
        ] = None,
        *,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                此 WebSocket 使用的依赖列表（`Depends()`）。

                详见
                [FastAPI WebSocket 文档](https://fastapi.tiangolo.com/advanced/websockets/)。
                """
            ),
        ] = None,
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        装饰 WebSocket 处理函数。

        详见
        [FastAPI docs for WebSockets](https://fastapi.tiangolo.com/advanced/websockets/).

        **示例**

        ```python
        from fastapi import FastAPI, WebSocket

        app = FastAPI()

        @app.websocket("/ws")
        async def websocket_endpoint(websocket: WebSocket):
            await websocket.accept()
            while True:
                data = await websocket.receive_text()
                await websocket.send_text(f"Message text was: {data}")
        ```
        """

        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_api_websocket_route(
                path,
                func,
                name=name,
                dependencies=dependencies,
            )
            return func

        return decorator

    def include_router(
        self,
        router: Annotated[routing.APIRouter, Doc("要包含的 `APIRouter`。")],
        *,
        prefix: Annotated[str, Doc("路由的可选路径前缀。")] = "",
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于此路由所有*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于此路由所有*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 大型应用文档（include_router 自定义前缀/tags/响应/依赖）](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies)。

                **示例**

                ```python
                from fastapi import Depends, FastAPI

                from .dependencies import get_token_header
                from .internal import admin

                app = FastAPI()

                app.include_router(
                    admin.router,
                    dependencies=[Depends(get_token_header)],
                )
                ```
                """
            ),
        ] = None,
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                在 OpenAPI 中展示的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 附加响应文档](https://fastapi.tiangolo.com/advanced/additional-responses/)。

                另见
                [FastAPI 大型应用文档](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies)。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                Mark all the *路径操作* in this router as deprecated.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                **示例**

                ```python
                from fastapi import FastAPI

                from .internal import old_api

                app = FastAPI()

                app.include_router(
                    old_api.router,
                    deprecated=True,
                )
                ```
                """
            ),
        ] = None,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此路由中所有*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                **示例**

                ```python
                from fastapi import FastAPI

                from .internal import old_api

                app = FastAPI()

                app.include_router(
                    old_api.router,
                    include_in_schema=False,
                )
                ```
                """
            ),
        ] = True,
        default_response_class: Annotated[
            type[Response],
            Doc(
                """
                Default response class to be used for the *路径操作* in this
                router.

                详见
                [FastAPI docs for Custom Response - HTML, Stream, File, others](https://fastapi.tiangolo.com/advanced/custom-response/#default-response-class).

                **示例**

                ```python
                from fastapi import FastAPI
                from fastapi.responses import ORJSONResponse

                from .internal import old_api

                app = FastAPI()

                app.include_router(
                    old_api.router,
                    default_response_class=ORJSONResponse,
                )
                ```
                """
            ),
        ] = Default(JSONResponse),
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> None:
        """
        在同一应用中包含 `APIRouter`。

        详见
        [FastAPI docs for Bigger Applications](https://fastapi.tiangolo.com/tutorial/bigger-applications/).

        ## 示例

        ```python
        from fastapi import FastAPI

        from .users import users_router

        app = FastAPI()

        app.include_router(users_router)
        ```
        """
        self.router.include_router(
            router,
            prefix=prefix,
            tags=tags,
            dependencies=dependencies,
            responses=responses,
            deprecated=deprecated,
            include_in_schema=include_in_schema,
            default_response_class=default_response_class,
            callbacks=callbacks,
            generate_unique_id_function=generate_unique_id_function,
        )

    def get(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP GET operation.

        ## 示例

        ```python
        from fastapi import FastAPI

        app = FastAPI()

        @app.get("/items/")
        def read_items():
            return [{"name": "Empanada"}, {"name": "Arepa"}]
        ```
        """
        return self.router.get(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def put(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP PUT operation.

        ## 示例

        ```python
        from fastapi import FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()

        @app.put("/items/{item_id}")
        def replace_item(item_id: str, item: Item):
            return {"message": "Item replaced", "id": item_id}
        ```
        """
        return self.router.put(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def post(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP POST operation.

        ## 示例

        ```python
        from fastapi import FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()

        @app.post("/items/")
        def create_item(item: Item):
            return {"message": "Item created"}
        ```
        """
        return self.router.post(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def delete(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP DELETE operation.

        ## 示例

        ```python
        from fastapi import FastAPI

        app = FastAPI()

        @app.delete("/items/{item_id}")
        def delete_item(item_id: str):
            return {"message": "Item deleted"}
        ```
        """
        return self.router.delete(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def options(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP OPTIONS operation.

        ## 示例

        ```python
        from fastapi import FastAPI

        app = FastAPI()

        @app.options("/items/")
        def get_item_options():
            return {"additions": ["Aji", "Guacamole"]}
        ```
        """
        return self.router.options(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def head(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP HEAD operation.

        ## 示例

        ```python
        from fastapi import FastAPI, Response

        app = FastAPI()

        @app.head("/items/", status_code=204)
        def get_items_headers(response: Response):
            response.headers["X-Cat-Dog"] = "Alone in the world"
        ```
        """
        return self.router.head(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def patch(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP PATCH operation.

        ## 示例

        ```python
        from fastapi import FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()

        @app.patch("/items/")
        def update_item(item: Item):
            return {"message": "Item updated in place"}
        ```
        """
        return self.router.patch(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def trace(
        self,
        path: Annotated[
            str,
            Doc(
                """
                此*路径操作*的 URL 路径。

                例如 `http://example.com/items` 的路径为 `/items`。
                """
            ),
        ],
        *,
        response_model: Annotated[
            Any,
            Doc(
                """
                响应使用的类型。

                可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。

                用途：

                * 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。
                * 序列化：任意返回对象经 `response_model` 序列化为 JSON。
                * 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。
                * 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。

                详见
                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。
                """
            ),
        ] = Default(None),
        status_code: Annotated[
            int | None,
            Doc(
                """
                响应默认状态码。

                可直接返回 Response 覆盖状态码。

                详见
                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。
                """
            ),
        ] = None,
        tags: Annotated[
            list[str | Enum] | None,
            Doc(
                """
                应用于*路径操作*的 tag 列表。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。
                """
            ),
        ] = None,
        dependencies: Annotated[
            Sequence[Depends] | None,
            Doc(
                """
                应用于*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。
                """
            ),
        ] = None,
        summary: Annotated[
            str | None,
            Doc(
                """
                *路径操作*摘要。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                *路径操作*描述。

                未提供时从*路径操作函数* docstring 自动提取。

                可含 Markdown。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        response_description: Annotated[
            str,
            Doc(
                """
                默认响应的描述。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = "Successful Response",
        responses: Annotated[
            dict[int | str, dict[str, Any]] | None,
            Doc(
                """
                此*路径操作*可能返回的附加响应。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = None,
        operation_id: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的自定义 operation ID。

                默认自动生成。

                自定义 operation ID 须在整个 API 内唯一。

                可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = None,
        response_model_include: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据仅包含指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_exclude: Annotated[
            IncEx | None,
            Doc(
                """
                传给 Pydantic：响应数据排除指定字段。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = None,
        response_model_by_alias: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。

                详见
                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。
                """
            ),
        ] = True,
        response_model_exclude_unset: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_defaults: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。

                `True` 时响应中省略默认值。

                详见
                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。
                """
            ),
        ] = False,
        response_model_exclude_none: Annotated[
            bool,
            Doc(
                """
                传给 Pydantic：是否排除值为 `None` 的字段。

                比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。

                详见
                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。
                """
            ),
        ] = False,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        response_class: Annotated[
            type[Response],
            Doc(
                """
                此*路径操作*使用的响应类。

                若直接返回 Response 则不会使用。

                详见
                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。
                """
            ),
        ] = Default(JSONResponse),
        name: Annotated[
            str | None,
            Doc(
                """
                此*路径操作*的名称，仅内部使用。
                """
            ),
        ] = None,
        callbacks: Annotated[
            list[BaseRoute] | None,
            Doc(
                """
                用作 OpenAPI 回调的*路径操作*列表。

                仅用于 OpenAPI 文档，回调不会直接调用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        openapi_extra: Annotated[
            dict[str, Any] | None,
            Doc(
                """
                写入此*路径操作* OpenAPI schema 的额外元数据。

                详见
                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。
                """
            ),
        ] = None,
        generate_unique_id_function: Annotated[
            Callable[[routing.APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        Add a *路径操作* using an HTTP TRACE operation.

        ## 示例

        ```python
        from fastapi import FastAPI

        app = FastAPI()

        @app.trace("/items/{item_id}")
        def trace_item(item_id: str):
            return None
        ```
        """
        return self.router.trace(
            path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema,
            response_class=response_class,
            name=name,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
        )

    def websocket_route(
        self, path: str, name: str | None = None
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.router.add_websocket_route(path, func, name=name)
            return func

        return decorator

    @deprecated(
        """
        `on_event` 已弃用，请改用 lifespan 事件处理器。

        详见
        [FastAPI docs for Lifespan Events](https://fastapi.tiangolo.com/advanced/events/).
        """
    )
    def on_event(
        self,
        event_type: Annotated[
            str,
            Doc(
                """
                事件类型：`startup` 或 `shutdown`。
                """
            ),
        ],
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        向应用添加事件处理器。

        `on_event` 已弃用，请改用 `lifespan` 事件处理器。

        详见
        [FastAPI 生命周期事件文档（已弃用的替代方案）](https://fastapi.tiangolo.com/advanced/events/#alternative-events-deprecated)。
        """
        return self.router.on_event(event_type)  # ty: ignore[deprecated]

    def middleware(
        self,
        middleware_type: Annotated[
            str,
            Doc(
                """
                中间件类型，目前仅支持 `http`。
                """
            ),
        ],
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        向应用添加中间件。

        详见
        [FastAPI docs for Middleware](https://fastapi.tiangolo.com/tutorial/middleware/).

        ## 示例

        ```python
        import time
        from typing import Awaitable, Callable

        from fastapi import FastAPI, Request, Response

        app = FastAPI()


        @app.middleware("http")
        async def add_process_time_header(
            request: Request, call_next: Callable[[Request], Awaitable[Response]]
        ) -> Response:
            start_time = time.time()
            response = await call_next(request)
            process_time = time.time() - start_time
            response.headers["X-Process-Time"] = str(process_time)
            return response
        ```
        """

        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_middleware(BaseHTTPMiddleware, dispatch=func)
            return func

        return decorator

    def exception_handler(
        self,
        exc_class_or_status_code: Annotated[
            int | type[Exception],
            Doc(
                """
                要处理的 Exception 类或状态码。
                """
            ),
        ],
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        """
        向应用添加异常处理器。

        详见
        [FastAPI docs for Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/).

        ## 示例

        ```python
        from fastapi import FastAPI, Request
        from fastapi.responses import JSONResponse


        class UnicornException(Exception):
            def __init__(self, name: str):
                self.name = name


        app = FastAPI()


        @app.exception_handler(UnicornException)
        async def unicorn_exception_handler(request: Request, exc: UnicornException):
            return JSONResponse(
                status_code=418,
                content={"message": f"Oops! {exc.name} did something. There goes a rainbow..."},
            )
        ```
        """

        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_exception_handler(exc_class_or_status_code, func)
            return func

        return decorator
