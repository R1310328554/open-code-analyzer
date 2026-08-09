import json
from typing import Annotated, Any

from annotated_doc import Doc
from fastapi.encoders import jsonable_encoder
from starlette.responses import HTMLResponse


def _html_safe_json(value: Any) -> str:
    """将值序列化为 JSON，并转义 HTML 特殊字符。

    防止 JSON 嵌入 `<script>` 标签时发生注入。
    """
    return (
        json.dumps(value)
        .replace("<", "\\u003c")
        .replace(">", "\\u003e")
        .replace("&", "\\u0026")
    )


swagger_ui_default_parameters: Annotated[
    dict[str, Any],
    Doc(
        """
        Swagger UI 的默认配置。

        可作为模板添加其他所需配置。
        """
    ),
] = {
    "dom_id": "#swagger-ui",
    "layout": "BaseLayout",
    "deepLinking": True,
    "showExtensions": True,
    "showCommonExtensions": True,
}


def get_swagger_ui_html(
    *,
    openapi_url: Annotated[
        str,
        Doc(
            """
            Swagger UI 应加载的 OpenAPI URL。

            FastAPI 通常自动使用默认 URL `/openapi.json`。

            详见
            [FastAPI 条件 OpenAPI 文档](https://fastapi.tiangolo.com/how-to/conditional-openapi/#conditional-openapi-from-settings-and-env-vars)
            """
        ),
    ],
    title: Annotated[
        str,
        Doc(
            """
            HTML `<title>` 内容，通常显示在浏览器标签页。

            详见
            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)
            """
        ),
    ],
    swagger_js_url: Annotated[
        str,
        Doc(
            """
            加载 Swagger UI JavaScript 的 URL。

            通常设置为 CDN 地址。

            详见
            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)
            """
        ),
    ] = "https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js",
    swagger_css_url: Annotated[
        str,
        Doc(
            """
            加载 Swagger UI CSS 的 URL。

            通常设置为 CDN 地址。

            详见
            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)
            """
        ),
    ] = "https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css",
    swagger_favicon_url: Annotated[
        str,
        Doc(
            """
            使用的 favicon URL，通常显示在浏览器标签页。
            """
        ),
    ] = "https://fastapi.tiangolo.com/img/favicon.png",
    oauth2_redirect_url: Annotated[
        str | None,
        Doc(
            """
            OAuth2 重定向 URL，通常由 FastAPI 自动处理。

            详见
            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)
            """
        ),
    ] = None,
    init_oauth: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            Swagger UI OAuth2 初始化配置字典。

            可用配置选项详见
            [Swagger UI 文档](https://swagger.io/docs/open-source-tools/swagger-ui/usage/oauth2/)。
            """
        ),
    ] = None,
    swagger_ui_parameters: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            Swagger UI 配置参数。

            默认为 [swagger_ui_default_parameters][fastapi.openapi.docs.swagger_ui_default_parameters]。

            详见
            [FastAPI 配置 Swagger UI 文档](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/)。
            """
        ),
    ] = None,
) -> HTMLResponse:
    """
    生成并返回加载 Swagger UI 的 HTML，用于交互式 API 文档（通常在 `/docs` 提供）。

    仅在需要覆盖部分内容（例如 Swagger UI 的 JavaScript 与 CSS URL）时自行调用。

    详见
    [FastAPI 配置 Swagger UI 文档](https://fastapi.tiangolo.com/how-to/configure-swagger-ui/)
    与 [FastAPI 自托管文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)。
    """
    current_swagger_ui_parameters = swagger_ui_default_parameters.copy()
    if swagger_ui_parameters:
        current_swagger_ui_parameters.update(swagger_ui_parameters)

    html = f"""
    <!DOCTYPE html>
    <html>
    <head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link type="text/css" rel="stylesheet" href="{swagger_css_url}">
    <link rel="shortcut icon" href="{swagger_favicon_url}">
    <title>{title}</title>
    </head>
    <body>
    <div id="swagger-ui">
    </div>
    <script src="{swagger_js_url}"></script>
    <!-- 页面现已可用 `SwaggerUIBundle` -->
    <script>
    const ui = SwaggerUIBundle({{
        url: '{openapi_url}',
    """

    for key, value in current_swagger_ui_parameters.items():
        html += f"{_html_safe_json(key)}: {_html_safe_json(jsonable_encoder(value))},\n"

    if oauth2_redirect_url:
        html += f"oauth2RedirectUrl: window.location.origin + '{oauth2_redirect_url}',"

    html += """
    presets: [
        SwaggerUIBundle.presets.apis,
        SwaggerUIBundle.SwaggerUIStandalonePreset
        ],
    })"""

    if init_oauth:
        html += f"""
        ui.initOAuth({_html_safe_json(jsonable_encoder(init_oauth))})
        """

    html += """
    </script>
    </body>
    </html>
    """
    return HTMLResponse(html)


def get_redoc_html(
    *,
    openapi_url: Annotated[
        str,
        Doc(
            """
            ReDoc 应加载的 OpenAPI URL。

            FastAPI 通常自动使用默认 URL `/openapi.json`。

            详见
            [FastAPI 条件 OpenAPI 文档](https://fastapi.tiangolo.com/how-to/conditional-openapi/#conditional-openapi-from-settings-and-env-vars)
            """
        ),
    ],
    title: Annotated[
        str,
        Doc(
            """
            HTML `<title>` 内容，通常显示在浏览器标签页。

            详见
            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)
            """
        ),
    ],
    redoc_js_url: Annotated[
        str,
        Doc(
            """
            加载 ReDoc JavaScript 的 URL。

            通常设置为 CDN 地址。

            详见
            [FastAPI 自定义文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)
            """
        ),
    ] = "https://cdn.jsdelivr.net/npm/redoc@2/bundles/redoc.standalone.js",
    redoc_favicon_url: Annotated[
        str,
        Doc(
            """
            使用的 favicon URL，通常显示在浏览器标签页。
            """
        ),
    ] = "https://fastapi.tiangolo.com/img/favicon.png",
    with_google_fonts: Annotated[
        bool,
        Doc(
            """
            是否加载并使用 Google Fonts。
            """
        ),
    ] = True,
) -> HTMLResponse:
    """
    生成并返回加载 ReDoc 的 HTML，用于备选 API 文档（通常在 `/redoc` 提供）。

    仅在需要覆盖部分内容（例如 ReDoc 的 JavaScript URL）时自行调用。

    详见
    [FastAPI 自托管文档 UI 静态资源文档](https://fastapi.tiangolo.com/how-to/custom-docs-ui-assets/)。
    """
    html = f"""
    <!DOCTYPE html>
    <html>
    <head>
    <title>{title}</title>
    <!-- 自适应布局所需 -->
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    """
    if with_google_fonts:
        html += """
    <link href="https://fonts.googleapis.com/css?family=Montserrat:300,400,700|Roboto:300,400,700" rel="stylesheet">
    """
    html += f"""
    <link rel="shortcut icon" href="{redoc_favicon_url}">
    <!--
    ReDoc 不修改外层页面样式
    -->
    <style>
      body {{
        margin: 0;
        padding: 0;
      }}
    </style>
    </head>
    <body>
    <noscript>
        ReDoc requires Javascript to function. Please enable it to browse the documentation.
    </noscript>
    <redoc spec-url="{openapi_url}"></redoc>
    <script src="{redoc_js_url}"> </script>
    </body>
    </html>
    """
    return HTMLResponse(html)


def get_swagger_ui_oauth2_redirect_html() -> HTMLResponse:
    """
    生成 Swagger UI OAuth2 重定向 HTML 响应。

    通常无需自行使用或修改。
    """
    # 复制自 https://github.com/swagger-api/swagger-ui/blob/v4.14.0/dist/oauth2-redirect.html
    html = """
    <!doctype html>
    <html lang="en-US">
    <head>
        <title>Swagger UI: OAuth2 Redirect</title>
    </head>
    <body>
    <script>
        'use strict';
        function run () {
            var oauth2 = window.opener.swaggerUIRedirectOauth2;
            var sentState = oauth2.state;
            var redirectUrl = oauth2.redirectUrl;
            var isValid, qp, arr;

            if (/code|token|error/.test(window.location.hash)) {
                qp = window.location.hash.substring(1).replace('?', '&');
            } else {
                qp = location.search.substring(1);
            }

            arr = qp.split("&");
            arr.forEach(function (v,i,_arr) { _arr[i] = '"' + v.replace('=', '":"') + '"';});
            qp = qp ? JSON.parse('{' + arr.join() + '}',
                    function (key, value) {
                        return key === "" ? value : decodeURIComponent(value);
                    }
            ) : {};

            isValid = qp.state === sentState;

            if ((
              oauth2.auth.schema.get("flow") === "accessCode" ||
              oauth2.auth.schema.get("flow") === "authorizationCode" ||
              oauth2.auth.schema.get("flow") === "authorization_code"
            ) && !oauth2.auth.code) {
                if (!isValid) {
                    oauth2.errCb({
                        authId: oauth2.auth.name,
                        source: "auth",
                        level: "warning",
                        message: "Authorization may be unsafe, passed state was changed in server. The passed state wasn't returned from auth server."
                    });
                }

                if (qp.code) {
                    delete oauth2.state;
                    oauth2.auth.code = qp.code;
                    oauth2.callback({auth: oauth2.auth, redirectUrl: redirectUrl});
                } else {
                    let oauthErrorMsg;
                    if (qp.error) {
                        oauthErrorMsg = "["+qp.error+"]: " +
                            (qp.error_description ? qp.error_description+ ". " : "no accessCode received from the server. ") +
                            (qp.error_uri ? "More info: "+qp.error_uri : "");
                    }

                    oauth2.errCb({
                        authId: oauth2.auth.name,
                        source: "auth",
                        level: "error",
                        message: oauthErrorMsg || "[Authorization failed]: no accessCode received from the server."
                    });
                }
            } else {
                oauth2.callback({auth: oauth2.auth, token: qp, isValid: isValid, redirectUrl: redirectUrl});
            }
            window.close();
        }

        if (document.readyState !== 'loading') {
            run();
        } else {
            document.addEventListener('DOMContentLoaded', function () {
                run();
            });
        }
    </script>
    </body>
    </html>
        """
    return HTMLResponse(content=html)
