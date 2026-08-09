"""OAuth2 安全方案：密码流、Bearer 令牌与作用域。"""

from typing import Annotated, Any, cast

from annotated_doc import Doc
from fastapi.exceptions import HTTPException
from fastapi.openapi.models import OAuth2 as OAuth2Model
from fastapi.openapi.models import OAuthFlows as OAuthFlowsModel
from fastapi.param_functions import Form
from fastapi.security.base import SecurityBase
from fastapi.security.utils import get_authorization_scheme_param
from starlette.requests import Request
from starlette.status import HTTP_401_UNAUTHORIZED


class OAuth2PasswordRequestForm:
    """
    依赖类：以表单数据收集 OAuth2 密码流的 `username` 与 `password`。

    OAuth2 规范要求密码流使用表单（非 JSON），且字段名必须为 `username` 与 `password`。

    所有初始化参数均从请求中提取。

    详见
    [FastAPI 简单 OAuth2 密码与 Bearer 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).

    ## Example

    ```python
    from typing import Annotated

    from fastapi import Depends, FastAPI
    from fastapi.security import OAuth2PasswordRequestForm

    app = FastAPI()


    @app.post("/login")
    def login(form_data: Annotated[OAuth2PasswordRequestForm, Depends()]):
        data = {}
        data["scopes"] = []
        for scope in form_data.scopes:
            data["scopes"].append(scope)
        if form_data.client_id:
            data["client_id"] = form_data.client_id
        if form_data.client_secret:
            data["client_secret"] = form_data.client_secret
        return data
    ```

    OAuth2 中 `items:read` 等 scope 是不透明字符串中的单个作用域；应用可自行用冒号等分隔
    以组织权限，但这属于应用层约定，非规范要求。
    """

    def __init__(
        self,
        *,
        grant_type: Annotated[
            str | None,
            Form(pattern="^password$"),
            Doc(
                """
                OAuth2 规范要求 grant_type 必须为固定字符串 "password"。
                本依赖类较宽松，允许不传；若需强制，请使用 `OAuth2PasswordRequestFormStrict`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ] = None,
        username: Annotated[
            str,
            Form(),
            Doc(
                """
                `username` 字符串，OAuth2 规范要求字段名必须为 `username`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ],
        password: Annotated[
            str,
            Form(json_schema_extra={"format": "password"}),
            Doc(
                """
                `password` 字符串，OAuth2 规范要求字段名必须为 `password`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ],
        scope: Annotated[
            str,
            Form(),
            Doc(
                """
                单个字符串，内含以空格分隔的多个 scope，例如：

                ```python
                "items:read items:write users:read profile openid"
                ```

                表示 scopes：`items:read`、`items:write`、`users:read`、`profile`、`openid`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ] = "",
        client_id: Annotated[
            str | None,
            Form(),
            Doc(
                """
                若有 `client_id`，可作为表单字段发送；但 OAuth2 规范建议用 HTTP Basic 发送 client 凭据。
                """
            ),
        ] = None,
        client_secret: Annotated[
            str | None,
            Form(json_schema_extra={"format": "password"}),
            Doc(
                """
                若有 `client_secret`（及 `client_id`），可作为表单字段发送；
                但 OAuth2 规范建议用 HTTP Basic 发送。
                """
            ),
        ] = None,
    ):
        self.grant_type = grant_type
        self.username = username
        self.password = password
        self.scopes = scope.split()
        self.client_id = client_id
        self.client_secret = client_secret


class OAuth2PasswordRequestFormStrict(OAuth2PasswordRequestForm):
    """
    依赖类：以表单数据收集 OAuth2 密码流的 `username` 与 `password`（严格模式）。

    The OAuth2 specification dictates that for a password flow the data should be
    collected using form data (instead of JSON) and that it should have the specific
    fields `username` and `password`.

    All the initialization parameters are extracted from the request.

    与 `OAuth2PasswordRequestForm` 的唯一区别：`OAuth2PasswordRequestFormStrict` 强制
    客户端发送 `grant_type="password"`，而宽松版中 `grant_type` 可选。

    Read more about it in the
    [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).

    ## Example

    ```python
    from typing import Annotated

    from fastapi import Depends, FastAPI
    from fastapi.security import OAuth2PasswordRequestForm

    app = FastAPI()


    @app.post("/login")
    def login(form_data: Annotated[OAuth2PasswordRequestFormStrict, Depends()]):
        data = {}
        data["scopes"] = []
        for scope in form_data.scopes:
            data["scopes"].append(scope)
        if form_data.client_id:
            data["client_id"] = form_data.client_id
        if form_data.client_secret:
            data["client_secret"] = form_data.client_secret
        return data
    ```

    Note that for OAuth2 the scope `items:read` is a single scope in an opaque string.
    You could have custom internal logic to separate it by colon characters (`:`) or
    similar, and get the two parts `items` and `read`. Many applications do that to
    group and organize permissions, you could do it as well in your application, just
    know that it is application specific, it's not part of the specification.


    grant_type: the OAuth2 spec says it is required and MUST be the fixed string "password".
        This dependency is strict about it. If you want to be permissive, use instead the
        OAuth2PasswordRequestForm dependency class.
    username: username string. The OAuth2 spec requires the exact field name "username".
    password: password string. The OAuth2 spec requires the exact field name "password".
    scope: Optional string. Several scopes (each one a string) separated by spaces. E.g.
        "items:read items:write users:read profile openid"
    client_id: optional string. OAuth2 recommends sending the client_id and client_secret (if any)
        using HTTP Basic auth, as: client_id:client_secret
    client_secret: optional string. OAuth2 recommends sending the client_id and client_secret (if any)
        using HTTP Basic auth, as: client_id:client_secret
    """

    def __init__(
        self,
        grant_type: Annotated[
            str,
            Form(pattern="^password$"),
            Doc(
                """
                OAuth2 规范要求 grant_type 必须为 "password"；本依赖严格强制。
                若需宽松行为，请使用 `OAuth2PasswordRequestForm`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ],
        username: Annotated[
            str,
            Form(),
            Doc(
                """
                `username` 字符串，OAuth2 规范要求字段名必须为 `username`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ],
        password: Annotated[
            str,
            Form(),
            Doc(
                """
                `password` 字符串，OAuth2 规范要求字段名必须为 `password`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ],
        scope: Annotated[
            str,
            Form(),
            Doc(
                """
                单个字符串，内含以空格分隔的多个 scope，例如：

                ```python
                "items:read items:write users:read profile openid"
                ```

                表示 scopes：`items:read`、`items:write`、`users:read`、`profile`、`openid`。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ] = "",
        client_id: Annotated[
            str | None,
            Form(),
            Doc(
                """
                若有 `client_id`，可作为表单字段发送；但 OAuth2 规范建议用 HTTP Basic 发送 client 凭据。
                """
            ),
        ] = None,
        client_secret: Annotated[
            str | None,
            Form(),
            Doc(
                """
                若有 `client_secret`（及 `client_id`），可作为表单字段发送；
                但 OAuth2 规范建议用 HTTP Basic 发送。
                """
            ),
        ] = None,
    ):
        super().__init__(
            grant_type=grant_type,
            username=username,
            password=password,
            scope=scope,
            client_id=client_id,
            client_secret=client_secret,
        )


class OAuth2(SecurityBase):
    """
    OAuth2 认证基类，其实例用作依赖项；其他 OAuth2 类继承并定制各流。

    通常无需新建子类，使用现有子类即可；支持多流时可组合使用。

    详见
    [FastAPI 安全文档](https://fastapi.tiangolo.com/tutorial/security/).
    """

    def __init__(
        self,
        *,
        flows: Annotated[
            OAuthFlowsModel | dict[str, dict[str, Any]],
            Doc(
                """
                OAuth2 流配置字典。
                """
            ),
        ] = OAuthFlowsModel(),
        scheme_name: Annotated[
            str | None,
            Doc(
                """
                安全方案名称。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                安全方案描述。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """
            ),
        ] = None,
        auto_error: Annotated[
            bool,
            Doc(
                """
                默认情况下，若未提供 OAuth2 认证所需的 HTTP Authorization 头，
                将自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Authorization 头不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景，也适用于多种可选认证方式之一（例如 OAuth2 或 Cookie）。
                """
            ),
        ] = True,
    ):
        self.model = OAuth2Model(
            flows=cast(OAuthFlowsModel, flows), description=description
        )
        self.scheme_name = scheme_name or self.__class__.__name__
        self.auto_error = auto_error

    def make_not_authenticated_error(self) -> HTTPException:
        """
        OAuth 2 规范未定义应使用的 challenge，因 Bearer 并非唯一认证方式。

        声明其他 challenge 属于应用特定行为，规范未定义。

        出于实用考虑，本方法默认使用 `Bearer` challenge。

        若实现非 Bearer 的 OAuth2 方案，可覆盖此方法。

        参考：https://datatracker.ietf.org/doc/html/rfc6749
        """
        return HTTPException(
            status_code=HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )

    async def __call__(self, request: Request) -> str | None:
        authorization = request.headers.get("Authorization")
        if not authorization:
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        return authorization


class OAuth2PasswordBearer(OAuth2):
    """
    使用密码流获取 Bearer 令牌的 OAuth2 认证，其实例用作依赖项。

    Read more about it in the
    [FastAPI docs for Simple OAuth2 with Password and Bearer](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
    """

    def __init__(
        self,
        tokenUrl: Annotated[
            str,
            Doc(
                """
                获取 OAuth2 令牌的 URL，即依赖 `OAuth2PasswordRequestForm` 的 *path operation*。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ],
        scheme_name: Annotated[
            str | None,
            Doc(
                """
                安全方案名称。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """
            ),
        ] = None,
        scopes: Annotated[
            dict[str, str] | None,
            Doc(
                """
                使用本依赖的 *path operations* 所需的 OAuth2 作用域。

                详见
                [FastAPI 简单 OAuth2 文档](https://fastapi.tiangolo.com/tutorial/security/simple-oauth2/).
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                安全方案描述。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """
            ),
        ] = None,
        auto_error: Annotated[
            bool,
            Doc(
                """
                默认情况下，若未提供 OAuth2 认证所需的 HTTP Authorization 头，
                将自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Authorization 头不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景，也适用于多种可选认证方式之一（例如 OAuth2 或 Cookie）。
                """
            ),
        ] = True,
        refreshUrl: Annotated[
            str | None,
            Doc(
                """
                刷新令牌并获取新令牌的 URL。
                """
            ),
        ] = None,
    ):
        if not scopes:
            scopes = {}
        flows = OAuthFlowsModel(
            password=cast(
                Any,
                {
                    "tokenUrl": tokenUrl,
                    "refreshUrl": refreshUrl,
                    "scopes": scopes,
                },
            )
        )
        super().__init__(
            flows=flows,
            scheme_name=scheme_name,
            description=description,
            auto_error=auto_error,
        )

    async def __call__(self, request: Request) -> str | None:
        authorization = request.headers.get("Authorization")
        scheme, param = get_authorization_scheme_param(authorization)
        if not authorization or scheme.lower() != "bearer":
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        return param


class OAuth2AuthorizationCodeBearer(OAuth2):
    """
    使用授权码流获取 Bearer 令牌的 OAuth2 认证，其实例用作依赖项。
    """

    def __init__(
        self,
        authorizationUrl: str,
        tokenUrl: Annotated[
            str,
            Doc(
                """
                获取 OAuth2 令牌的 URL。
                """
            ),
        ],
        refreshUrl: Annotated[
            str | None,
            Doc(
                """
                刷新令牌并获取新令牌的 URL。
                """
            ),
        ] = None,
        scheme_name: Annotated[
            str | None,
            Doc(
                """
                安全方案名称。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """
            ),
        ] = None,
        scopes: Annotated[
            dict[str, str] | None,
            Doc(
                """
                使用本依赖的 *path operations* 所需的 OAuth2 作用域。
                """
            ),
        ] = None,
        description: Annotated[
            str | None,
            Doc(
                """
                安全方案描述。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """
            ),
        ] = None,
        auto_error: Annotated[
            bool,
            Doc(
                """
                默认情况下，若未提供 OAuth2 认证所需的 HTTP Authorization 头，
                将自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Authorization 头不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景，也适用于多种可选认证方式之一（例如 OAuth2 或 Cookie）。
                """
            ),
        ] = True,
    ):
        if not scopes:
            scopes = {}
        flows = OAuthFlowsModel(
            authorizationCode=cast(
                Any,
                {
                    "authorizationUrl": authorizationUrl,
                    "tokenUrl": tokenUrl,
                    "refreshUrl": refreshUrl,
                    "scopes": scopes,
                },
            )
        )
        super().__init__(
            flows=flows,
            scheme_name=scheme_name,
            description=description,
            auto_error=auto_error,
        )

    async def __call__(self, request: Request) -> str | None:
        authorization = request.headers.get("Authorization")
        scheme, param = get_authorization_scheme_param(authorization)
        if not authorization or scheme.lower() != "bearer":
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None  # pragma: nocover
        return param


class SecurityScopes:
    """
    可在依赖参数中声明，以获取同链路上所有依赖所需的 OAuth2 作用域。

    这样同一 *path operation* 中多个依赖可有不同 scope，并可在单处访问全部所需 scope。

    详见
    [FastAPI OAuth2 作用域文档](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/).
    """

    def __init__(
        self,
        scopes: Annotated[
            list[str] | None,
            Doc(
                """
                由 FastAPI 自动填充。
                """
            ),
        ] = None,
    ):
        self.scopes: Annotated[
            list[str],
            Doc(
                """
                所有依赖所需的作用域列表。
                """
            ),
        ] = scopes or []
        self.scope_str: Annotated[
            str,
            Doc(
                """
                所有依赖所需作用域的空格分隔字符串，符合 OAuth2 规范。
                """
            ),
        ] = " ".join(self.scopes)
