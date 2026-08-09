#!/usr/bin/env python3
"""Chinese-annotate FastAPI 0.141.1 applications.py and routing.py."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "fastapi/0.141.1"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"

FILES = [
    "fastapi/applications.py",
    "fastapi/routing.py",
]

PREPEND = {
    "fastapi/applications.py": '"""FastAPI 应用类：框架主入口，封装 Starlette 并提供 OpenAPI 与路径操作装饰器。"""\n\n',
    "fastapi/routing.py": '"""FastAPI 路由层：APIRoute、APIRouter、请求处理与前端静态文件路由。"""\n\n',
}

# Global phrase replacements (order matters — longer/more specific first)
GLOBAL_PHRASES: list[tuple[str, str]] = [
    ("Read more about the available configuration options in the", "可用配置选项详见"),
    ("Read more about it in the\n                [FastAPI docs for Path Operation Configuration](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#deprecate-a-path-operation).", "详见\n                [FastAPI 路径操作配置文档（弃用路径操作）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#deprecate-a-path-operation)。"),
    ("Read more about it in the\n                [FastAPI docs for Query Parameters and String Validations](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi).", "详见\n                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。"),
    ("Read more about it in the\n                [FastAPI docs for Dependencies in path operation decorators](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/).", "详见\n                [FastAPI 路径操作装饰器中的依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-in-path-operation-decorators/)。"),
    ("Read more about it in the\n                [FastAPI docs for Path Operation Configuration](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags).", "详见\n                [FastAPI 路径操作配置文档（tags）](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/#tags)。"),
    ("Read more about it in the\n                [FastAPI docs for Path Operation Configuration](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/).", "详见\n                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。"),
    ("Read more about it in the\n                [FastAPI docs for Response Status Code](https://fastapi.tiangolo.com/tutorial/response-status-code/).", "详见\n                [FastAPI 响应状态码文档](https://fastapi.tiangolo.com/tutorial/response-status-code/)。"),
    ("Read more about it in the\n                [FastAPI docs for Response Model](https://fastapi.tiangolo.com/tutorial/response-model/).", "详见\n                [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。"),
    ("Read more about it in the\n                [FastAPI docs for Response Model - Return Type](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude).", "详见\n                [FastAPI 响应模型文档（include/exclude）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_include-and-response_model_exclude)。"),
    ("Read more about it in the\n                [FastAPI docs for Response Model - Return Type](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none).", "详见\n                [FastAPI 响应模型文档（exclude_none）](https://fastapi.tiangolo.com/tutorial/response-model/#response_model_exclude_none)。"),
    ("Read more about it in the\n                [FastAPI docs for Response Model - Return Type](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter).", "详见\n                [FastAPI 响应模型文档（exclude_unset）](https://fastapi.tiangolo.com/tutorial/response-model/#use-the-response_model_exclude_unset-parameter)。"),
    ("Read more about it in the\n                [FastAPI docs for Custom Response - HTML, Stream, File, others](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse).", "详见\n                [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#redirectresponse)。"),
    ("Read more about it in the\n                [FastAPI docs for Custom Response - HTML, Stream, File, others](https://fastapi.tiangolo.com/advanced/custom-response/#default-response-class).", "详见\n                [FastAPI 自定义响应文档（默认响应类）](https://fastapi.tiangolo.com/advanced/custom-response/#default-response-class)。"),
    ("Read more about it in the\n                [FastAPI docs for Path Operation Advanced Configuration](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema).", "详见\n                [FastAPI 路径操作高级配置文档](https://fastapi.tiangolo.com/advanced/path-operation-advanced-configuration/#custom-openapi-path-operation-schema)。"),
    ("Read more about it in the\n                [FastAPI docs for OpenAPI Callbacks](https://fastapi.tiangolo.com/advanced/openapi-callbacks/).", "详见\n                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。"),
    ("Read more about it in the\n                [FastAPI docs for OpenAPI Webhooks](https://fastapi.tiangolo.com/advanced/openapi-webhooks/).", "详见\n                [FastAPI OpenAPI Webhooks 文档](https://fastapi.tiangolo.com/advanced/openapi-webhooks/)。"),
    ("Read more about it in the\n                [FastAPI docs for Additional Responses in OpenAPI](https://fastapi.tiangolo.com/advanced/additional-responses/).", "详见\n                [FastAPI OpenAPI 附加响应文档](https://fastapi.tiangolo.com/advanced/additional-responses/)。"),
    ("Read more about it in the\n                [FastAPI docs for Bigger Applications - Multiple Files](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies).", "详见\n                [FastAPI 大型应用文档（include_router 自定义前缀/tags/响应/依赖）](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies)。"),
    ("Read more about it in the\n                [FastAPI docs for WebSockets](https://fastapi.tiangolo.com/advanced/websockets/).", "详见\n                [FastAPI WebSocket 文档](https://fastapi.tiangolo.com/advanced/websockets/)。"),
    ("Read more about it in the\n                [FastAPI docs about how to Generate Clients](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function).", "详见\n                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。"),
    ("Read more about it in the\n                [FastAPI docs for Strict Content-Type](https://fastapi.tiangolo.com/advanced/strict-content-type/).", "详见\n                [FastAPI Strict Content-Type 文档](https://fastapi.tiangolo.com/advanced/strict-content-type/)。"),
    ("Read more about it in the\n                [FastAPI docs for Global Dependencies](https://fastapi.tiangolo.com/tutorial/dependencies/global-dependencies/).", "详见\n                [FastAPI 全局依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/global-dependencies/)。"),
    ("Read more about it in the\n                [FastAPI docs for Middleware](https://fastapi.tiangolo.com/tutorial/middleware/).", "详见\n                [FastAPI 中间件文档](https://fastapi.tiangolo.com/tutorial/middleware/)。"),
    ("Read more about it in the\n                [FastAPI docs for Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/).", "详见\n                [FastAPI 错误处理文档](https://fastapi.tiangolo.com/tutorial/handling-errors/)。"),
    ("Read more about it in the\n                [FastAPI docs for Testing Dependencies with Overrides](https://fastapi.tiangolo.com/advanced/testing-dependencies/).", "详见\n                [FastAPI 测试依赖覆盖文档](https://fastapi.tiangolo.com/advanced/testing-dependencies/)。"),
    ("Read more about it in the\n                [FastAPI docs about how to Configure Swagger UI](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/).", "详见\n                [FastAPI 配置 Swagger UI 文档](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/)。"),
    ("Read more about it in the\n                [FastAPI docs about how to separate schemas for input and output](https://fastapi.tiangolo.com/how-to/separate-openapi-schemas)", "详见\n                [FastAPI 分离输入/输出 schema 文档](https://fastapi.tiangolo.com/how-to/separate-openapi-schemas)"),
    ("Read more about it in the\n                [FastAPI docs for `lifespan`](https://fastapi.tiangolo.com/advanced/events/).", "详见\n                [FastAPI lifespan 文档](https://fastapi.tiangolo.com/advanced/events/)。"),
    ("Read more in the [FastAPI docs for `lifespan`](https://fastapi.tiangolo.com/advanced/events/).", "详见 [FastAPI lifespan 文档](https://fastapi.tiangolo.com/advanced/events/)。"),
    ("Read more in the\n                [FastAPI docs for Metadata and Docs URLs](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api).", "详见\n                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。"),
    ("Read more in the\n                [FastAPI docs for Metadata and Docs URLs](https://fastapi.tiangolo.com/tutorial/metadata/#openapi-url).", "详见\n                [FastAPI 元数据与文档 URL 文档（openapi_url）](https://fastapi.tiangolo.com/tutorial/metadata/#openapi-url)。"),
    ("Read more in the\n                [FastAPI docs for Metadata and Docs URLs](https://fastapi.tiangolo.com/tutorial/metadata/#docs-urls).", "详见\n                [FastAPI 元数据与文档 URL 文档（docs/redoc）](https://fastapi.tiangolo.com/tutorial/metadata/#docs-urls)。"),
    ("Read more in the\n                [FastAPI docs for Metadata and Docs URLs](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-tags).", "详见\n                [FastAPI 元数据与文档 URL 文档（tags）](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-tags)。"),
    ("Read more at the\n                [FastAPI docs for Metadata and Docs URLs](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api).", "详见\n                [FastAPI 元数据与文档 URL 文档](https://fastapi.tiangolo.com/tutorial/metadata/#metadata-for-api)。"),
    ("Read more about it at the\n                [FastAPI docs for Behind a Proxy](https://fastapi.tiangolo.com/advanced/behind-a-proxy/).", "详见\n                [FastAPI 反向代理文档](https://fastapi.tiangolo.com/advanced/behind-a-proxy/)。"),
    ("Read more about it in the\n                [FastAPI docs for Behind a Proxy](https://fastapi.tiangolo.com/advanced/behind-a-proxy/#disable-automatic-server-from-root-path).", "详见\n                [FastAPI 反向代理文档（禁用 root_path 自动 server）](https://fastapi.tiangolo.com/advanced/behind-a-proxy/#disable-automatic-server-from-root-path)。"),
    ("Read more in the\n                [FastAPI docs for Behind a Proxy](https://fastapi.tiangolo.com/advanced/behind-a-proxy/#additional-servers).", "详见\n                [FastAPI 反向代理文档（附加 servers）](https://fastapi.tiangolo.com/advanced/behind-a-proxy/#additional-servers)。"),
    ("Read more in the\n                [Starlette docs for Applications](https://starlette.dev/applications/#starlette.applications.Starlette).", "详见\n                [Starlette 应用文档](https://starlette.dev/applications/#starlette.applications.Starlette)。"),
    ("Read more about it in the\n                [Starlette docs for Applications](https://starlette.dev/applications/#storing-state-on-the-app-instance).", "详见\n                [Starlette 应用文档（在 app 实例上存储 state）](https://starlette.dev/applications/#storing-state-on-the-app-instance)。"),
    ("Read more in the\n        [FastAPI docs for OpenAPI](https://fastapi.tiangolo.com/how-to/extending-openapi/).", "详见\n        [FastAPI 扩展 OpenAPI 文档](https://fastapi.tiangolo.com/how-to/extending-openapi/)。"),
    ("Read more in the\n        [FastAPI docs for First Steps](https://fastapi.tiangolo.com/tutorial/first-steps/).", "详见\n        [FastAPI 入门文档](https://fastapi.tiangolo.com/tutorial/first-steps/)。"),
    ("Read more in the\n        [FastAPI docs for Bigger Applications](https://fastapi.tiangolo.com/tutorial/bigger-applications/).", "详见\n        [FastAPI 大型应用文档](https://fastapi.tiangolo.com/tutorial/bigger-applications/)。"),
    ("Read more in the\n        [FastAPI docs for Bigger Applications - Multiple Files](https://fastapi.tiangolo.com/tutorial/bigger-applications/).", "详见\n        [FastAPI 大型应用（多文件）文档](https://fastapi.tiangolo.com/tutorial/bigger-applications/)。"),
    ("Read more in the\n        [FastAPI docs for Lifespan Events](https://fastapi.tiangolo.com/advanced/events/).", "详见\n        [FastAPI 生命周期事件文档](https://fastapi.tiangolo.com/advanced/events/)。"),
    ("Read more about it in the\n        [FastAPI docs for Lifespan Events](https://fastapi.tiangolo.com/advanced/events/#alternative-events-deprecated).", "详见\n        [FastAPI 生命周期事件文档（已弃用的替代方案）](https://fastapi.tiangolo.com/advanced/events/#alternative-events-deprecated)。"),
    ("Read more in the\n        [FastAPI docs for WebSockets](https://fastapi.tiangolo.com/advanced/websockets/).", "详见\n        [FastAPI WebSocket 文档](https://fastapi.tiangolo.com/advanced/websockets/)。"),
    ("Read more in the\n        [FastAPI docs for Middleware](https://fastapi.tiangolo.com/tutorial/middleware/).", "详见\n        [FastAPI 中间件文档](https://fastapi.tiangolo.com/tutorial/middleware/)。"),
    ("Read more in the\n        [FastAPI docs for Handling Errors](https://fastapi.tiangolo.com/tutorial/handling-errors/).", "详见\n        [FastAPI 错误处理文档](https://fastapi.tiangolo.com/tutorial/handling-errors/)。"),
    ("And in the\n                [FastAPI docs for Bigger Applications](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies).", "另见\n                [FastAPI 大型应用文档](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies)。"),
    ("Read more about it in the", "详见"),
    ("Read more in the", "详见"),
    ("Read more at the", "详见"),
    ("*path operations*", "*路径操作*"),
    ("*path operation*", "*路径操作*"),
    ("You probably shouldn't use this parameter", "**注意**：通常不应使用此参数"),
    ("**Note**: you probably shouldn't use this parameter", "**注意**：通常不应使用此参数"),
    ("**Note** This is the version of your application", "**注意** 这是应用版本"),
    ("**Note**: This is available since OpenAPI 3.1.0", "**注意**：自 OpenAPI 3.1.0"),
    ("Boolean indicating if debug tracebacks should be returned on server\n                errors.", "服务端错误时是否返回调试堆栈跟踪。"),
    ("The title of the API.", "API 标题。"),
    ("A short summary of the API.", "API 简短摘要。"),
    ("The version of the API.", "API 版本。"),
    ("A description of the API. Supports Markdown (using\n                [CommonMark syntax](https://commonmark.org/)).", "API 描述，支持 Markdown（[CommonMark 语法](https://commonmark.org/)）。"),
    ("It will be added to the generated OpenAPI (e.g. visible at `/docs`).", "将写入生成的 OpenAPI（例如在 `/docs` 可见）。"),
    ("This affects the generated OpenAPI (e.g. visible at `/docs`).", "影响生成的 OpenAPI（例如在 `/docs` 可见）。"),
    ("The URL where the OpenAPI schema will be served from.", "提供 OpenAPI schema 的 URL。"),
    ("If you set it to `None`, no OpenAPI schema will be served publicly, and\n                the default automatic endpoints `/docs` and `/redoc` will also be\n                disabled.", "设为 `None` 时不公开提供 OpenAPI schema，\n                默认的 `/docs` 与 `/redoc` 端点也会禁用。"),
    ("The path to the automatic interactive API documentation.\n                It is handled in the browser by Swagger UI.", "自动交互式 API 文档路径，由 Swagger UI 在浏览器中展示。"),
    ("The default URL is `/docs`. You can disable it by setting it to `None`.", "默认 URL 为 `/docs`，设为 `None` 可禁用。"),
    ("If `openapi_url` is set to `None`, this will be automatically disabled.", "若 `openapi_url` 为 `None`，将自动禁用。"),
    ("The path to the alternative automatic interactive API documentation\n                provided by ReDoc.", "ReDoc 提供的替代交互式 API 文档路径。"),
    ("The default URL is `/redoc`. You can disable it by setting it to `None`.", "默认 URL 为 `/redoc`，设为 `None` 可禁用。"),
    ("The OAuth2 redirect endpoint for the Swagger UI.", "Swagger UI 的 OAuth2 重定向端点。"),
    ("By default it is `/docs/oauth2-redirect`.", "默认为 `/docs/oauth2-redirect`。"),
    ("This is only used if you use OAuth2 (with the \"Authorize\" button)\n                with Swagger UI.", "仅在使用 OAuth2（Swagger UI 的 Authorize 按钮）时需要。"),
    ("OAuth2 configuration for the Swagger UI, by default shown at `/docs`.", "Swagger UI 的 OAuth2 配置，默认在 `/docs` 展示。"),
    ("Parameters to configure Swagger UI, the autogenerated interactive API\n                documentation (by default at `/docs`).", "配置 Swagger UI（默认 `/docs` 的自动生成交互式 API 文档）的参数。"),
    ("A list of routes to serve incoming HTTP and WebSocket requests.", "处理 HTTP 与 WebSocket 请求的路由列表。"),
    ("You normally wouldn't use this parameter with FastAPI, it is inherited\n                from Starlette and supported for compatibility.", "FastAPI 中通常不使用此参数，继承自 Starlette 以保持兼容。"),
    ("In FastAPI, you normally would use the *path operation methods*,\n                like `app.get()`, `app.post()`, etc.", "在 FastAPI 中通常使用*路径操作方法*，如 `app.get()`、`app.post()` 等。"),
    ("In FastAPI, you normally would use the *path operation methods*,\n                like `router.get()`, `router.post()`, etc.", "在 FastAPI 中通常使用*路径操作方法*，如 `router.get()`、`router.post()` 等。"),
    ("A `Lifespan` context manager handler. This replaces `startup` and\n                `shutdown` functions with a single context manager.", "`Lifespan` 上下文管理器处理器，用单一上下文管理器替代 `startup`/`shutdown` 函数。"),
    ("A list of startup event handler functions.", "startup 事件处理器函数列表。"),
    ("A list of shutdown event handler functions.", "shutdown 事件处理器函数列表。"),
    ("You should instead use the `lifespan` handlers.", "应改用 `lifespan` 处理器。"),
    ("List of middleware to be added when creating the application.", "创建应用时添加的中间件列表。"),
    ("In FastAPI you would normally do this with `app.add_middleware()`\n                instead.", "在 FastAPI 中通常改用 `app.add_middleware()`。"),
    ("A dictionary with handlers for exceptions.", "异常处理器字典。"),
    ("In FastAPI, you would normally use the decorator\n                `@app.exception_handler()`.", "在 FastAPI 中通常使用 `@app.exception_handler()` 装饰器。"),
    ("A state object for the application. This is the same object for the\n                entire application, it doesn't change from request to request.", "应用级 state 对象，整个应用共享，不随请求变化。"),
    ("You normally wouldn't use this in FastAPI, for most of the cases you\n                would instead use FastAPI dependencies.", "FastAPI 中多数场景应使用依赖项，而非此对象。"),
    ("This is simply inherited from Starlette.", "直接继承自 Starlette。"),
    ("The `app.webhooks` attribute is an `APIRouter` with the *path\n                operations* that will be used just for documentation of webhooks.", "`app.webhooks` 是 `APIRouter`，其*路径操作*仅用于 webhook 文档。"),
    ("OpenAPI callbacks that should apply to all *path operations*.", "应用于所有*路径操作*的 OpenAPI 回调。"),
    ("Add OpenAPI webhooks. This is similar to `callbacks` but it doesn't\n                depend on specific *path operations*.", "添加 OpenAPI webhooks，类似 `callbacks` 但不依赖特定*路径操作*。"),
    ("A dictionary with overrides for the dependencies.", "依赖覆盖字典。"),
    ("Each key is the original dependency callable, and the value is the\n                actual dependency that should be called.", "键为原始依赖 callable，值为实际应调用的依赖。"),
    ("This is for testing, to replace expensive dependencies with testing\n                versions.", "用于测试，将昂贵依赖替换为测试版本。"),
    ("A list of global dependencies, they will be applied to each\n                *path operation*, including in sub-routers.", "全局依赖列表，应用于每个*路径操作*（含子路由）。"),
    ("A URL to the Terms of Service for your API.", "API 服务条款 URL。"),
    ("A dictionary with the contact information for the exposed API.", "对外 API 的联系信息字典。"),
    ("It can contain several fields.", "可包含多个字段。"),
    ("A dictionary with the license information for the exposed API.", "对外 API 的许可证信息字典。"),
    ("A dictionary with the license information for the exposed API.", "对外 API 的许可证信息字典。"),
    ("This field allows you to provide additional external documentation links.", "提供额外外部文档链接。"),
    ("If provided, it must be a dictionary containing:", "若提供，必须是包含以下键的字典："),
    ("Whether to generate separate OpenAPI schemas for request body and\n                response body when the results would be more precise.", "当结果更精确时，是否为请求体与响应体生成独立的 OpenAPI schema。"),
    ("This is particularly useful when automatically generating clients.", "自动生成客户端时尤其有用。"),
    ("For example, if you have a model like:", "例如模型："),
    ("When `Item` is used for input, a request body, `tags` is not required,\n                the client doesn't have to provide it.", "输入时 `tags` 非必填，客户端可不提供。"),
    ("But when using `Item` for output, for a response body, `tags` is always\n                available because it has a default value, even if it's just an empty\n                list. So, the client should be able to always expect it.", "输出时 `tags` 因有默认值（可为空列表）始终存在，客户端应始终预期该字段。"),
    ("In this case, there would be two different schemas, one for input and\n                another one for output.", "此时输入与输出各有一套 schema。"),
    ("Enable strict checking for request Content-Type headers.", "启用对请求 Content-Type 头的严格检查。"),
    ("When `True` (the default), requests with a body that do not include\n                a `Content-Type` header will **not** be parsed as JSON.", "默认 `True` 时，带 body 但无 `Content-Type` 的请求**不会**按 JSON 解析。"),
    ("This prevents potential cross-site request forgery (CSRF) attacks", "这可防止潜在的 CSRF 攻击"),
    ("that exploit the browser's ability to send requests without a\n                Content-Type header, bypassing CORS preflight checks. In particular\n                applicable for apps that need to be run locally (in localhost).", "利用浏览器可无 Content-Type 发请求、绕过 CORS 预检的特性，尤其适用于本地（localhost）运行的应用。"),
    ("When `False`, requests without a `Content-Type` header will have\n                their body parsed as JSON, which maintains compatibility with\n                certain clients that don't send `Content-Type` headers.", "`False` 时无 Content-Type 的请求 body 仍按 JSON 解析，兼容不发送该头的客户端。"),
    ("The version string of OpenAPI.", "OpenAPI 版本字符串。"),
    ("FastAPI will generate OpenAPI version 3.1.0, and will output that as\n                the OpenAPI version. But some tools, even though they might be\n                compatible with OpenAPI 3.1.0, might not recognize it as a valid.", "FastAPI 生成 OpenAPI 3.1.0，但部分工具可能不识别该版本。"),
    ("So you could override this value to trick those tools into using\n                the generated OpenAPI. Have in mind that this is a hack. But if you\n                avoid using features added in OpenAPI 3.1.0, it might work for your\n                use case.", "可覆盖此值以兼容旧工具，属变通方案；若未使用 3.1.0 新特性可能可行。"),
    ("This is not passed as a parameter to the `FastAPI` class to avoid\n                giving the false idea that FastAPI would generate a different OpenAPI\n                schema. It is only available as an attribute.", "不作为 `FastAPI` 构造参数，避免误解；仅作为属性可用。"),
    ("Whether to detect and redirect slashes in URLs when the client doesn't\n                use the same format.", "客户端 URL 斜杠格式不一致时，是否检测并重定向。"),
    ("With this app, if a client goes to `/items` (without a trailing slash),\n                they will be automatically redirected with an HTTP status code of 307\n                to `/items/`.", "客户端访问 `/items`（无尾斜杠）时，将以 307 重定向到 `/items/`。"),
    ("A path prefix handled by a proxy that is not seen by the application\n                but is seen by external clients, which affects things like Swagger UI.", "代理处理的路径前缀，应用不可见但外部客户端可见，影响 Swagger UI 等。"),
    ("To disable automatically generating the URLs in the `servers` field\n                in the autogenerated OpenAPI using the `root_path`.", "禁用根据 `root_path` 在自动生成 OpenAPI 的 `servers` 字段中生成 URL。"),
    ("A `list` of `dict`s with connectivity information to a target server.", "目标服务器连接信息的 `dict` 列表。"),
    ("You would use it, for example, if your application is served from\n                different domains and you want to use the same Swagger UI in the\n                browser to interact with each of them (instead of having multiple\n                browser tabs open). Or if you want to leave fixed the possible URLs.", "例如多域名部署时，用同一 Swagger UI 交互，或固定可选 URL。"),
    ("If the servers `list` is not provided, or is an empty `list`, the\n                `servers` property in the generated OpenAPI will be:", "未提供或为空时，生成 OpenAPI 的 `servers` 为："),
    ("A list of tags used by OpenAPI, these are the same `tags` you can set\n                in the *path operations*, like:", "OpenAPI 使用的 tag 列表，与*路径操作*中的 `tags` 相同，例如："),
    ("The order of the tags can be used to specify the order shown in\n                tools like Swagger UI, used in the automatic path `/docs`.", "tag 顺序可控制 Swagger UI（`/docs`）中的展示顺序。"),
    ("It's not required to specify all the tags used.", "无需列出所有使用过的 tag。"),
    ("The tags that are not declared MAY be organized randomly or based\n                on the tools' logic. Each tag name in the list MUST be unique.", "未声明的 tag 可能随机排序；列表中 tag 名必须唯一。"),
    ("The value of each item is a `dict` containing:", "每项为包含以下键的 `dict`："),
    ("The URL path to be used for this *path operation*.", "此*路径操作*的 URL 路径。"),
    ("For example, in `http://example.com/items`, the path is `/items`.", "例如 `http://example.com/items` 的路径为 `/items`。"),
    ("The type to use for the response.", "响应使用的类型。"),
    ("It could be any valid Pydantic *field* type. So, it doesn't have to\n                be a Pydantic model, it could be other things, like a `list`, `dict`,\n                etc.", "可为任意有效 Pydantic *字段*类型，如 `list`、`dict` 等，不限于模型。"),
    ("It will be used for:", "用途："),
    ("* Documentation: the generated OpenAPI (and the UI at `/docs`) will\n                    show it as the response (JSON Schema).", "* 文档：OpenAPI（及 `/docs` UI）展示为响应 JSON Schema。"),
    ("* Serialization: you could return an arbitrary object and the\n                    `response_model` would be used to serialize that object into the\n                    corresponding JSON.", "* 序列化：任意返回对象经 `response_model` 序列化为 JSON。"),
    ("* Filtering: the JSON sent to the client will only contain the data\n                    (fields) defined in the `response_model`. If you returned an object\n                    that contains an attribute `password` but the `response_model` does\n                    not include that field, the JSON sent to the client would not have\n                    that `password`.", "* 过滤：客户端 JSON 仅含 `response_model` 定义的字段；若返回含 `password` 但模型未定义，则不会出现在 JSON 中。"),
    ("* Validation: whatever you return will be serialized with the\n                    `response_model`, converting any data as necessary to generate the\n                    corresponding JSON. But if the data in the object returned is not\n                    valid, that would mean a violation of the contract with the client,\n                    so it's an error from the API developer. So, FastAPI will raise an\n                    error and return a 500 error code (Internal Server Error).", "* 校验：返回数据经 `response_model` 序列化；无效数据视为 API 开发者违约，FastAPI 报错并返回 500。"),
    ("The default status code to be used for the response.", "响应默认状态码。"),
    ("You could override the status code by returning a response directly.", "可直接返回 Response 覆盖状态码。"),
    ("A list of tags to be applied to the *path operation*.", "应用于*路径操作*的 tag 列表。"),
    ("A list of dependencies (using `Depends()`) to be applied to the\n                *path operation*.", "应用于*路径操作*的依赖列表（`Depends()`）。"),
    ("A summary for the *path operation*.", "*路径操作*摘要。"),
    ("A description for the *path operation*.", "*路径操作*描述。"),
    ("If not provided, it will be extracted automatically from the docstring\n                of the *path operation function*.", "未提供时从*路径操作函数* docstring 自动提取。"),
    ("It can contain Markdown.", "可含 Markdown。"),
    ("The description for the default response.", "默认响应的描述。"),
    ("Additional responses that could be returned by this *path operation*.", "此*路径操作*可能返回的附加响应。"),
    ("Mark this *path operation* as deprecated.", "将此*路径操作*标记为已弃用。"),
    ("Custom operation ID to be used by this *path operation*.", "此*路径操作*的自定义 operation ID。"),
    ("By default, it is generated automatically.", "默认自动生成。"),
    ("If you provide a custom operation ID, you need to make sure it is\n                unique for the whole API.", "自定义 operation ID 须在整个 API 内唯一。"),
    ("You can customize the\n                operation ID generation with the parameter\n                `generate_unique_id_function` in the `FastAPI` class.", "可通过 `FastAPI` 的 `generate_unique_id_function` 自定义生成逻辑。"),
    ("Configuration passed to Pydantic to include only certain fields in the\n                response data.", "传给 Pydantic：响应数据仅包含指定字段。"),
    ("Configuration passed to Pydantic to exclude certain fields in the\n                response data.", "传给 Pydantic：响应数据排除指定字段。"),
    ("Configuration passed to Pydantic to define if the response model\n                should be serialized by alias when an alias is used.", "传给 Pydantic：有 alias 时是否按 alias 序列化响应模型。"),
    ("Configuration passed to Pydantic to define if the response data\n                should have all the fields, including the ones that were not set and\n                have their default values. This is different from\n                `response_model_exclude_defaults` in that if the fields are set,\n                they will be included in the response, even if the value is the same\n                as the default.", "传给 Pydantic：是否包含未设置但具默认值的字段；与 `response_model_exclude_defaults` 不同，已设置字段即使等于默认值仍会包含。"),
    ("Configuration passed to Pydantic to define if the response data\n                should have all the fields, including the ones that have the same value\n                as the default. This is different from `response_model_exclude_unset`\n                in that if the fields are set but contain the same default values,\n                they will be excluded from the response.", "传给 Pydantic：是否包含与默认值相同的字段；与 `response_model_exclude_unset` 不同，已设置且等于默认值的字段会被排除。"),
    ("When `True`, default values are omitted from the response.", "`True` 时响应中省略默认值。"),
    ("Configuration passed to Pydantic to define if the response data should\n                exclude fields set to `None`.", "传给 Pydantic：是否排除值为 `None` 的字段。"),
    ("This is much simpler (less smart) than `response_model_exclude_unset`\n                and `response_model_exclude_defaults`. You probably want to use one of\n                those two instead of this one, as those allow returning `None` values\n                when it makes sense.", "比 `response_model_exclude_unset`/`response_model_exclude_defaults` 简单；通常优先使用后两者。"),
    ("Include this *path operation* in the generated OpenAPI schema.", "是否将此*路径操作*包含在 OpenAPI schema 中。"),
    ("To include (or not) all the *path operations* in the generated OpenAPI.", "是否将所有*路径操作*包含在 OpenAPI 中。"),
    ("To include (or not) all the *path operations* in this router in the\n                generated OpenAPI.", "是否将此路由中所有*路径操作*包含在 OpenAPI 中。"),
    ("Response class to be used for this *path operation*.", "此*路径操作*使用的响应类。"),
    ("This will not be used if you return a response directly.", "若直接返回 Response 则不会使用。"),
    ("Extra metadata to be included in the OpenAPI schema for this *path\n                operation*.", "写入此*路径操作* OpenAPI schema 的额外元数据。"),
    ("List of *path operations* that will be used as OpenAPI callbacks.", "用作 OpenAPI 回调的*路径操作*列表。"),
    ("This is only for OpenAPI documentation, the callbacks won't be used\n                directly.", "仅用于 OpenAPI 文档，回调不会直接调用。"),
    ("Customize the function used to generate unique IDs for the *path\n                operations* shown in the generated OpenAPI.", "自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。"),
    ("This is particularly useful when automatically generating clients or\n                SDKs for your API.", "自动生成 API 客户端或 SDK 时尤其有用。"),
    ("A list of tags to be applied to all the *path operations* in this\n                router.", "应用于此路由所有*路径操作*的 tag 列表。"),
    ("A list of dependencies (using `Depends()`) to be applied to all the\n                *path operations* in this router.", "应用于此路由所有*路径操作*的依赖列表。"),
    ("Mark all *path operations* as deprecated. You probably don't need it,\n                but it's available.", "将所有*路径操作*标记为已弃用，通常不需要但可用。"),
    ("Mark all the *path operations* in this router as deprecated.", "将此路由中所有*路径操作*标记为已弃用。"),
    ("Include (or not) all the *path operations* in this router in the\n                generated OpenAPI schema.", "是否将此路由中所有*路径操作*包含在 OpenAPI schema 中。"),
    ("Additional responses to be shown in OpenAPI.", "在 OpenAPI 中展示的附加响应。"),
    ("The default response class to be used.", "使用的默认响应类。"),
    ("Default response class to be used for the *path operations* in this\n                router.", "此路由*路径操作*的默认响应类。"),
    ("Check that the frontend directory exists when the app is created. When\n                set to `\"auto\"`, skip the check with a warning when `FASTAPI_ENV` is\n                `\"development\"`, and check it otherwise. The `fastapi dev` command\n                sets `FASTAPI_ENV` to `\"development\"` if it is not already set.", "创建应用时检查前端目录；`\"auto\"` 时在 `FASTAPI_ENV` 为 `\"development\"` 下跳过检查并警告，否则检查。`fastapi dev` 未设置时会将 `FASTAPI_ENV` 设为 `\"development\"`。"),
    ("The directory containing the static frontend build output.", "静态前端构建输出目录。"),
    ("The URL path prefix where the frontend build should be served.", "提供前端构建的 URL 路径前缀。"),
    ("The fallback file behavior for missing frontend paths.", "前端路径缺失时的回退文件行为。"),
    ("A list of dependencies (using `Depends()`) to be used for this\n                WebSocket.", "此 WebSocket 使用的依赖列表（`Depends()`）。"),
    ("WebSocket path.", "WebSocket 路径。"),
    ("A name for the WebSocket. Only used internally.", "WebSocket 名称，仅内部使用。"),
    ("Name for this *path operation*. Only used internally.", "此*路径操作*名称，仅内部使用。"),
    ("The type of event. `startup` or `shutdown`.", "事件类型：`startup` 或 `shutdown`。"),
    ("The type of middleware. Currently only supports `http`.", "中间件类型，目前仅支持 `http`。"),
    ("The Exception class this would handle, or a status code.", "要处理的 Exception 类或状态码。"),
    ("Default function handler for this router. Used to handle\n                404 Not Found errors.", "此路由默认处理器，处理 404 Not Found。"),
    ("Only used internally by FastAPI to handle dependency overrides.", "仅 FastAPI 内部用于依赖覆盖。"),
    ("You shouldn't need to use it. It normally points to the `FastAPI` app\n                object.", "通常无需使用，一般指向 `FastAPI` 应用对象。"),
    ('Doc("An optional path prefix for the router.")', 'Doc("路由的可选路径前缀。")'),
    ('Doc("The `APIRouter` to include.")', 'Doc("要包含的 `APIRouter`。")'),
    ("on_event is deprecated, use lifespan event handlers instead.", "`on_event` 已弃用，请改用 lifespan 事件处理器。"),
    ('"openapi_prefix" has been deprecated in favor of "root_path", which\n                follows more closely the ASGI standard, is simpler, and more\n                automatic.', '"openapi_prefix" 已弃用，请改用更符合 ASGI 标准且更简单的 "root_path"。'),
    ("Generate the OpenAPI schema of the application. This is called by FastAPI\n        internally.", "生成应用的 OpenAPI schema，由 FastAPI 内部调用。"),
    ("The first time it is called it stores the result in the attribute\n        `app.openapi_schema`, and next times it is called, it just returns that same\n        result. To avoid the cost of generating the schema every time.", "首次调用结果存入 `app.openapi_schema`，之后直接返回，避免重复生成。"),
    ("If you need to modify the generated OpenAPI schema, you could modify it.", "如需修改生成的 OpenAPI schema，可直接修改。"),
    ("Add a middleware to the application.", "向应用添加中间件。"),
    ("Add an exception handler to the app.", "向应用添加异常处理器。"),
    ("Add an event handler for the application.", "向应用添加事件处理器。"),
    ("`on_event` is deprecated, use `lifespan` event handlers instead.", "`on_event` 已弃用，请改用 `lifespan` 事件处理器。"),
    ("Add an event handler for the router.", "向路由添加事件处理器。"),
    ("Decorate a WebSocket function.", "装饰 WebSocket 处理函数。"),
    ("Include an `APIRouter` in the same app.", "在同一应用中包含 `APIRouter`。"),
    ("Include another `APIRouter` in the same current `APIRouter`.", "在当前 `APIRouter` 中包含另一个 `APIRouter`。"),
    ("Add a *path operation* using an HTTP GET operation.", "使用 HTTP GET 添加*路径操作*。"),
    ("Add a *path operation* using an HTTP POST operation.", "使用 HTTP POST 添加*路径操作*。"),
    ("Add a *path operation* using an HTTP PUT operation.", "使用 HTTP PUT 添加*路径操作*。"),
    ("Add a *path operation* using an HTTP DELETE operation.", "使用 HTTP DELETE 添加*路径操作*。"),
    ("Add a *path operation* using an HTTP OPTIONS operation.", "使用 HTTP OPTIONS 添加*路径操作*。"),
    ("Add a *path operation* using an HTTP HEAD operation.", "使用 HTTP HEAD 添加*路径操作*。"),
    ("Add a *path operation* using an HTTP PATCH operation.", "使用 HTTP PATCH 添加*路径操作*。"),
    ("Add a *path operation* using an HTTP TRACE operation.", "使用 HTTP TRACE 添加*路径操作*。"),
    ("Serve a static frontend build as low-priority routes.", "以低优先级路由提供静态前端构建。"),
    ("Use this for frontend tools that build static files into a directory,\n        such as `dist`. **FastAPI** path operations are checked first, and\n        the frontend files are checked only if no normal route matched.", "适用于将静态文件构建到目录（如 `dist`）的前端工具。**FastAPI** 先匹配*路径操作*，无匹配时才检查前端文件。"),
    ("A typical project could look like this:", "典型项目结构："),
    ("Then in `app/main.py`:", "在 `app/main.py` 中："),
    ("## Example", "## 示例"),
    ("**Example**", "**示例**"),
    ("**Example**:", "**示例**："),
]

POST_CLEANUP: list[tuple[str, str]] = [
    ("The URL path to be used for this *路径操作*.", "此*路径操作*的 URL 路径。"),
    ("Response class to be used for this *路径操作*.", "此*路径操作*使用的响应类。"),
    ("Name for this *路径操作*. Only used internally.", "此*路径操作*的名称，仅内部使用。"),
    ("A URL prefix for the OpenAPI URL.", "OpenAPI URL 的 URL 前缀。"),
    (
        "Extra keyword arguments to be stored in the app, not used by FastAPI\n                anywhere.",
        "存储在应用中的额外关键字参数，FastAPI 内部不使用。",
    ),
    (
        "                                ChimichangApp API helps you do awesome stuff. 🚀\n\n                                ## Items\n\n                                You can **read items**.\n\n                                ## Users\n\n                                You will be able to:\n\n                                * **Create users** (_not implemented_).\n                                * **Read users** (_not implemented_).\n\n                                ",
        "                                ChimichangApp API 帮你做很酷的事。🚀\n\n                                ## Items\n\n                                你可以**读取 items**。\n\n                                ## Users\n\n                                你将能够：\n\n                                * **创建 users**（_未实现_）。\n                                * **读取 users**（_未实现_）。\n\n                                ",
    ),
    (
        "A list of tags to be applied to all the *路径操作* in this\n                router.",
        "应用于此路由所有*路径操作*的 tag 列表。",
    ),
    (
        "A list of dependencies (using `Depends()`) to be applied to all the\n                *路径操作* in this router.",
        "应用于此路由所有*路径操作*的依赖列表（`Depends()`）。",
    ),
    ("A list of tags to be applied to the *路径操作*.", "应用于*路径操作*的 tag 列表。"),
    (
        "A list of dependencies (using `Depends()`) to be applied to the\n                *路径操作*.",
        "应用于*路径操作*的依赖列表（`Depends()`）。",
    ),
    (
        "A list of tags used by OpenAPI, these are the same `tags` you can set\n                in the *路径操作*, like:",
        "OpenAPI 使用的 tag 列表，与*路径操作*中的 `tags` 相同，例如：",
    ),
    (
        "A list of global dependencies, they will be applied to each\n                *路径操作*, including in sub-routers.",
        "全局依赖列表，应用于每个*路径操作*（含子路由）。",
    ),
    ("You probably don't need it, but it's available.", "通常不需要，但可用。"),
    ("A summary for the *路径操作*.", "*路径操作*摘要。"),
    ("A description for the *路径操作*.", "*路径操作*描述。"),
    (
        "Additional responses that could be returned by this *路径操作*.",
        "此*路径操作*可能返回的附加响应。",
    ),
    ("Mark this *路径操作* as deprecated.", "将此*路径操作*标记为已弃用。"),
    (
        "Custom operation ID to be used by this *路径操作*.",
        "此*路径操作*的自定义 operation ID。",
    ),
    (
        "Include this *路径操作* in the generated OpenAPI schema.",
        "是否将此*路径操作*包含在 OpenAPI schema 中。",
    ),
    (
        "List of *路径操作* that will be used as OpenAPI callbacks.",
        "用作 OpenAPI 回调的*路径操作*列表。",
    ),
    (
        "Mark all *路径操作* in this router as deprecated.",
        "将此路由中所有*路径操作*标记为已弃用。",
    ),
    (
        "To include (or not) all the *路径操作* in this router in the\n                generated OpenAPI.",
        "是否将此路由中所有*路径操作*包含在 OpenAPI 中。",
    ),
    (
        "Include (or not) all the *路径操作* in this router in the\n                generated OpenAPI schema.",
        "是否将此路由中所有*路径操作*包含在 OpenAPI schema 中。",
    ),
    (
        "[FastAPI docs for First Steps](https://fastapi.tiangolo.com/tutorial/first-steps/).",
        "[FastAPI 入门文档](https://fastapi.tiangolo.com/tutorial/first-steps/)。",
    ),
    (
        "[FastAPI docs for Bigger Applications - Multiple Files](https://fastapi.tiangolo.com/tutorial/bigger-applications/).",
        "[FastAPI 大型应用（多文件）文档](https://fastapi.tiangolo.com/tutorial/bigger-applications/)。",
    ),
]

FILE_EXACT_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "fastapi/applications.py": [
        (
            'class FastAPI(Starlette):\n    """\n    `FastAPI` app class, the main entrypoint to use FastAPI.',
            'class FastAPI(Starlette):\n    """\n    `FastAPI` 应用类，使用 FastAPI 的主入口。',
        ),
    ],
    "fastapi/routing.py": [
        (
            'class APIRouter(routing.Router):\n    """\n    `APIRouter` class, used to group *path operations*, for example to structure\n    an app in multiple files. It would then be included in the `FastAPI` app, or\n    in another `APIRouter` (ultimately included in the app).',
            'class APIRouter(routing.Router):\n    """\n    `APIRouter` 类，用于分组*路径操作*，例如将应用拆分为多文件。\n    随后可包含在 `FastAPI` 应用或另一个 `APIRouter` 中（最终包含在应用中）。',
        ),
        (
            'def request_response(\n    func: Callable[[Request], Awaitable[Response] | Response],\n) -> ASGIApp:\n    """\n    Takes a function or coroutine `func(request) -> response`,\n    and returns an ASGI application.\n    """',
            'def request_response(\n    func: Callable[[Request], Awaitable[Response] | Response],\n) -> ASGIApp:\n    """\n    接收函数或 coroutine `func(request) -> response`，返回 ASGI 应用。\n    """',
        ),
        (
            'def websocket_session(\n    func: Callable[[WebSocket], Awaitable[None]],\n) -> ASGIApp:\n    """\n    Takes a coroutine `func(session)`, and returns an ASGI application.\n    """',
            'def websocket_session(\n    func: Callable[[WebSocket], Awaitable[None]],\n) -> ASGIApp:\n    """\n    接收 coroutine `func(session)`，返回 ASGI 应用。\n    """',
        ),
        (
            'class _AsyncLiftContextManager(AbstractAsyncContextManager[_T]):\n    """\n    Wraps a synchronous context manager to make it async.\n\n    This is vendored from Starlette to avoid importing private symbols.\n    """',
            'class _AsyncLiftContextManager(AbstractAsyncContextManager[_T]):\n    """\n    将同步上下文管理器包装为异步。\n\n    从 Starlette 复制，避免导入私有符号。\n    """',
        ),
        (
            'def _extract_endpoint_context(func: Any) -> EndpointContext:\n    """Extract endpoint context with caching to avoid repeated file I/O."""',
            'def _extract_endpoint_context(func: Any) -> EndpointContext:\n    """带缓存地提取端点上下文，避免重复文件 I/O。"""',
        ),
        (
            'def _wrap_gen_lifespan_context(\n    lifespan_context: Callable[[Any], Generator[Any, Any, Any]],\n) -> Callable[[Any], AbstractAsyncContextManager[Any]]:\n    """\n    Wrap a generator-based lifespan context into an async context manager.\n\n    This is vendored from Starlette to avoid importing private symbols.\n    """',
            'def _wrap_gen_lifespan_context(\n    lifespan_context: Callable[[Any], Generator[Any, Any, Any]],\n) -> Callable[[Any], AbstractAsyncContextManager[Any]]:\n    """\n    将基于生成器的 lifespan 上下文包装为异步上下文管理器。\n\n    从 Starlette 复制，避免导入私有符号。\n    """',
        ),
        (
            'class _DefaultLifespan:\n    """\n    Default lifespan context manager that runs on_startup and on_shutdown handlers.\n\n    This is a copy of the Starlette _DefaultLifespan class that was removed\n    in Starlette. FastAPI keeps it to maintain backward compatibility with\n    on_startup and on_shutdown event handlers.\n\n    Ref: https://github.com/Kludex/starlette/pull/3117\n    """',
            'class _DefaultLifespan:\n    """\n    运行 on_startup 与 on_shutdown 处理器的默认 lifespan 上下文管理器。\n\n    这是 Starlette 中已移除的 _DefaultLifespan 类的副本。\n    FastAPI 保留它以维持与 on_startup/on_shutdown 事件处理器的向后兼容。\n\n    参考：https://github.com/Kludex/starlette/pull/3117\n    """',
        ),
        (
            '    async def _startup(self) -> None:\n        """\n        Run any `.on_startup` event handlers.\n\n        This method is kept for backward compatibility after Starlette removed\n        support for on_startup/on_shutdown handlers.\n\n        Ref: https://github.com/Kludex/starlette/pull/3117\n        """',
            '    async def _startup(self) -> None:\n        """\n        运行所有 `.on_startup` 事件处理器。\n\n        Starlette 移除 on_startup/on_shutdown 支持后，\n        此方法保留以维持向后兼容。\n\n        参考：https://github.com/Kludex/starlette/pull/3117\n        """',
        ),
        (
            '    async def _shutdown(self) -> None:\n        """\n        Run any `.on_shutdown` event handlers.\n\n        This method is kept for backward compatibility after Starlette removed\n        support for on_startup/on_shutdown handlers.\n\n        Ref: https://github.com/Kludex/starlette/pull/3117\n        """',
            '    async def _shutdown(self) -> None:\n        """\n        运行所有 `.on_shutdown` 事件处理器。\n\n        Starlette 移除 on_startup/on_shutdown 支持后，\n        此方法保留以维持向后兼容。\n\n        参考：https://github.com/Kludex/starlette/pull/3117\n        """',
        ),
        (
            '    def add_event_handler(\n        self,\n        event_type: str,\n        func: Callable[[], Any],\n    ) -> None:\n        """\n        Add an event handler function for startup or shutdown.\n\n        This method is kept for backward compatibility after Starlette removed\n        support for on_startup/on_shutdown handlers.\n\n        Ref: https://github.com/Kludex/starlette/pull/3117\n        """',
            '    def add_event_handler(\n        self,\n        event_type: str,\n        func: Callable[[], Any],\n    ) -> None:\n        """\n        添加 startup 或 shutdown 事件处理函数。\n\n        Starlette 移除 on_startup/on_shutdown 支持后，\n        此方法保留以维持向后兼容。\n\n        参考：https://github.com/Kludex/starlette/pull/3117\n        """',
        ),
        (
            '                        """Read from the producer and forward to the output,\n                        inserting keepalive comments on timeout."""',
            '                        """从生产者读取并转发到输出，\n                        超时时插入 keepalive 注释。"""',
        ),
    ],
}

COMMENT_REPLACEMENTS: list[tuple[str, str]] = [
    (
        "# Copy of starlette.routing.request_response modified to include the\n# dependencies' AsyncExitStack",
        "# 修改自 starlette.routing.request_response，加入依赖的 AsyncExitStack",
    ),
    (
        "# Copy of starlette.routing.websocket_session modified to include the\n# dependencies' AsyncExitStack",
        "# 修改自 starlette.routing.websocket_session，加入依赖的 AsyncExitStack",
    ),
    (
        "# Vendored from starlette.routing to avoid importing private symbols",
        "# 从 starlette.routing 复制，避免导入私有符号",
    ),
    (
        "# Cache for endpoint context to avoid re-extracting on every request",
        "# 缓存端点上下文，避免每次请求重复提取",
    ),
    (
        "# Duplicate/override from Starlette to add AsyncExitStackMiddleware\n        # inside of ExceptionMiddleware, inside of custom user middlewares",
        "# 覆盖 Starlette：在 ExceptionMiddleware 内、用户中间件内添加 AsyncExitStackMiddleware",
    ),
    (
        "# Add FastAPI-specific AsyncExitStackMiddleware for closing files.",
        "# 添加 FastAPI 专用的 AsyncExitStackMiddleware 以关闭文件。",
    ),
    (
        "# Before this was also used for closing dependencies with yield but\n                # those now have their own AsyncExitStack, to properly support\n                # streaming responses while keeping compatibility with the previous\n                # versions (as of writing 0.117.1) that allowed doing\n                # except HTTPException inside a dependency with yield.",
        "# 此前也用于关闭带 yield 的依赖，现依赖有独立 AsyncExitStack，\n                # 以支持流式响应并保持与旧版（如 0.117.1）在 yield 依赖内 except HTTPException 的兼容。",
    ),
    (
        "# Starts customization",
        "# 开始定制",
    ),
    (
        "# Continues customization",
        "# 继续定制",
    ),
    (
        "# Same as in Starlette",
        "# 与 Starlette 相同",
    ),
    (
        "# Only called by get_request_handler. Has been split into its own function to\n    # facilitate profiling endpoints, since inner functions are harder to profile.",
        "# 仅由 get_request_handler 调用。拆成独立函数便于分析端点性能（内层函数较难 profiling）。",
    ),
    (
        "# If status_code was set, use it, otherwise use the default from the\n    # response class, in the case of redirect it's 307",
        "# 若设置了 status_code 则使用，否则用响应类默认值（重定向为 307）",
    ),
    (
        "# Extract endpoint context for error messages",
        "# 提取端点上下文用于错误消息",
    ),
    (
        "# For mounted sub-apps, include the mount path prefix",
        "# 挂载子应用时包含 mount 路径前缀",
    ),
    (
        "# Read body and auto-close files",
        "# 读取 body 并自动关闭文件",
    ),
    (
        "# If a middleware raises an HTTPException, it should be raised again",
        "# 中间件抛出 HTTPException 时应再次抛出",
    ),
    (
        "# Solve dependencies and run path operation function, auto-closing dependencies",
        "# 解析依赖并运行路径操作函数，自动关闭依赖",
    ),
    (
        "# Shared serializer for stream items (JSONL and SSE).\n            # Validates against stream_item_field when set, then\n            # serializes to JSON bytes.",
        "# 流式项（JSONL/SSE）共享序列化器：若设置了 stream_item_field 则校验，再序列化为 JSON 字节。",
    ),
    (
        "# Generator endpoint: stream as Server-Sent Events",
        "# 生成器端点：以 Server-Sent Events 流式输出",
    ),
    (
        "# TODO: probably move this out of the Route / Route Group, same in APIRoute\n    # this should probably be top level FastAPI logic, not part of APIRoute and\n    # duplicated here",
        "# TODO：可能移出 Route/Route Group（APIRoute 同理），\n    #  ideally 作为 FastAPI 顶层逻辑，而非在 APIRoute 等处重复",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, pairs: list[tuple[str, str]], *, required: bool = False) -> str:
    for old, new in pairs:
        if old == new:
            continue
        if old not in text:
            if required:
                raise ValueError(f"Missing pattern:\n{old[:160]}...")
            continue
        text = text.replace(old, new)
    return text


def annotate_file(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    if rel in PREPEND and not text.startswith('"""'):
        text = PREPEND[rel] + text
    text = apply_replacements(text, FILE_EXACT_REPLACEMENTS.get(rel, []), required=True)
    text = apply_replacements(text, GLOBAL_PHRASES)
    text = apply_replacements(text, POST_CLEANUP)
    text = apply_replacements(text, COMMENT_REPLACEMENTS)
    if not has_chinese(text):
        raise ValueError(f"No Chinese content after annotation: {rel}")
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in FILES:
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
