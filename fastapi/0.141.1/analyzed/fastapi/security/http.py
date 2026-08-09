import binascii
from base64 import b64decode
from typing import Annotated

from annotated_doc import Doc
from fastapi.exceptions import HTTPException
from fastapi.openapi.models import HTTPBase as HTTPBaseModel
from fastapi.openapi.models import HTTPBearer as HTTPBearerModel
from fastapi.security.base import SecurityBase
from fastapi.security.utils import get_authorization_scheme_param
from pydantic import BaseModel
from starlette.requests import Request
from starlette.status import HTTP_401_UNAUTHORIZED


class HTTPBasicCredentials(BaseModel):
    """
    使用 `HTTPBasic` 作为依赖项时返回的 HTTP Basic 凭据。

    详见
    [FastAPI HTTP Basic 认证文档](https://fastapi.tiangolo.com/advanced/security/http-basic-auth/)。
    """

    username: Annotated[str, Doc("HTTP Basic 用户名。")]
    password: Annotated[str, Doc("HTTP Basic 密码。")]


class HTTPAuthorizationCredentials(BaseModel):
    """
    使用 `HTTPBearer` 或 `HTTPDigest` 作为依赖项时返回的 HTTP 授权凭据。

    Authorization 头按第一个空格拆分：

    前半部分为 `scheme`，后半部分为 `credentials`。

    例如 HTTP Bearer 令牌方案下，客户端发送：

    ```
    Authorization: Bearer deadbeef12346
    ```

    此时：

    * `scheme` 为 `"Bearer"`
    * `credentials` 为 `"deadbeef12346"`
    """

    scheme: Annotated[
        str,
        Doc(
            """
            从 Authorization 头提取的认证方案名。
            """
        ),
    ]
    credentials: Annotated[
        str,
        Doc(
            """
            从 Authorization 头提取的凭据字符串。
            """
        ),
    ]


class HTTPBase(SecurityBase):
    model: HTTPBaseModel

    def __init__(
        self,
        *,
        scheme: str,
        scheme_name: str | None = None,
        description: str | None = None,
        auto_error: bool = True,
    ):
        self.model = HTTPBaseModel(scheme=scheme, description=description)
        self.scheme_name = scheme_name or self.__class__.__name__
        self.auto_error = auto_error

    def make_authenticate_headers(self) -> dict[str, str]:
        return {"WWW-Authenticate": f"{self.model.scheme.title()}"}

    def make_not_authenticated_error(self) -> HTTPException:
        return HTTPException(
            status_code=HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers=self.make_authenticate_headers(),
        )

    async def __call__(self, request: Request) -> HTTPAuthorizationCredentials | None:
        authorization = request.headers.get("Authorization")
        scheme, credentials = get_authorization_scheme_param(authorization)
        if not (authorization and scheme and credentials):
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        return HTTPAuthorizationCredentials(scheme=scheme, credentials=credentials)


class HTTPBasic(HTTPBase):
    """
    HTTP Basic 认证。

    参考：https://datatracker.ietf.org/doc/html/rfc7617

    ## 用法

    创建实例并在 `Depends()` 中作为依赖项使用。

    依赖项结果为包含 `username` 与 `password` 的 `HTTPBasicCredentials` 对象。

    详见
    [FastAPI HTTP Basic 认证文档](https://fastapi.tiangolo.com/advanced/security/http-basic-auth/)。

    ## 示例

    ```python
    from typing import Annotated

    from fastapi import Depends, FastAPI
    from fastapi.security import HTTPBasic, HTTPBasicCredentials

    app = FastAPI()

    security = HTTPBasic()


    @app.get("/users/me")
    def read_current_user(credentials: Annotated[HTTPBasicCredentials, Depends(security)]):
        return {"username": credentials.username, "password": credentials.password}
    ```
    """

    def __init__(
        self,
        *,
        scheme_name: Annotated[
            str | None,
            Doc(
                """
                安全方案名称。

                将包含在生成的 OpenAPI 文档中（例如可在 `/docs` 查看）。
                """
            ),
        ] = None,
        realm: Annotated[
            str | None,
            Doc(
                """
                HTTP Basic 认证的 realm 域。
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
                默认情况下，若未提供 HTTP Basic 认证（Authorization 头），
                `HTTPBasic` 将自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Basic 认证不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景。

                也适用于多种可选认证方式之一（例如 HTTP Basic 或 Bearer 令牌）。
                """
            ),
        ] = True,
    ):
        self.model = HTTPBaseModel(scheme="basic", description=description)
        self.scheme_name = scheme_name or self.__class__.__name__
        self.realm = realm
        self.auto_error = auto_error

    def make_authenticate_headers(self) -> dict[str, str]:
        if self.realm:
            return {"WWW-Authenticate": f'Basic realm="{self.realm}"'}
        return {"WWW-Authenticate": "Basic"}

    async def __call__(  # type: ignore
        self, request: Request
    ) -> HTTPBasicCredentials | None:
        authorization = request.headers.get("Authorization")
        scheme, param = get_authorization_scheme_param(authorization)
        if not authorization or scheme.lower() != "basic":
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        try:
            data = b64decode(param).decode("ascii")
        except (ValueError, UnicodeDecodeError, binascii.Error) as e:
            raise self.make_not_authenticated_error() from e
        username, separator, password = data.partition(":")
        if not separator:
            raise self.make_not_authenticated_error()
        return HTTPBasicCredentials(username=username, password=password)


class HTTPBearer(HTTPBase):
    """
    HTTP Bearer 令牌认证。

    ## 用法

    创建实例并在 `Depends()` 中作为依赖项使用。

    依赖项结果为包含 `scheme` 与 `credentials` 的 `HTTPAuthorizationCredentials` 对象。

    ## 示例

    ```python
    from typing import Annotated

    from fastapi import Depends, FastAPI
    from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

    app = FastAPI()

    security = HTTPBearer()


    @app.get("/users/me")
    def read_current_user(
        credentials: Annotated[HTTPAuthorizationCredentials, Depends(security)]
    ):
        return {"scheme": credentials.scheme, "credentials": credentials.credentials}
    ```
    """

    def __init__(
        self,
        *,
        bearerFormat: Annotated[str | None, Doc("Bearer 令牌格式说明。")] = None,
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
                默认情况下，若未在 `Authorization` 头中提供 Bearer 令牌，
                `HTTPBearer` 将自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Bearer 令牌不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景。

                也适用于多种可选认证方式之一（例如 Bearer 令牌或 Cookie）。
                """
            ),
        ] = True,
    ):
        self.model = HTTPBearerModel(bearerFormat=bearerFormat, description=description)
        self.scheme_name = scheme_name or self.__class__.__name__
        self.auto_error = auto_error

    async def __call__(self, request: Request) -> HTTPAuthorizationCredentials | None:
        authorization = request.headers.get("Authorization")
        scheme, credentials = get_authorization_scheme_param(authorization)
        if not (authorization and scheme and credentials):
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        if scheme.lower() != "bearer":
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        return HTTPAuthorizationCredentials(scheme=scheme, credentials=credentials)


class HTTPDigest(HTTPBase):
    """
    HTTP Digest 认证。

    **警告**：这仅是用于在 FastAPI 中将组件与 OpenAPI 关联的桩实现，
    并未实现完整的 Digest 方案，需在代码中子类化并实现。

    参考：https://datatracker.ietf.org/doc/html/rfc7616

    ## 用法

    创建实例并在 `Depends()` 中作为依赖项使用。

    依赖项结果为包含 `scheme` 与 `credentials` 的 `HTTPAuthorizationCredentials` 对象。

    ## 示例

    ```python
    from typing import Annotated

    from fastapi import Depends, FastAPI
    from fastapi.security import HTTPAuthorizationCredentials, HTTPDigest

    app = FastAPI()

    security = HTTPDigest()


    @app.get("/users/me")
    def read_current_user(
        credentials: Annotated[HTTPAuthorizationCredentials, Depends(security)]
    ):
        return {"scheme": credentials.scheme, "credentials": credentials.credentials}
    ```
    """

    def __init__(
        self,
        *,
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
                默认情况下，若未提供 HTTP Digest 认证，`HTTPDigest` 将
                自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Digest 认证不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景。

                也适用于多种可选认证方式之一（例如 HTTP Digest 或 Cookie）。
                """
            ),
        ] = True,
    ):
        self.model = HTTPBaseModel(scheme="digest", description=description)
        self.scheme_name = scheme_name or self.__class__.__name__
        self.auto_error = auto_error

    async def __call__(self, request: Request) -> HTTPAuthorizationCredentials | None:
        authorization = request.headers.get("Authorization")
        scheme, credentials = get_authorization_scheme_param(authorization)
        if not (authorization and scheme and credentials):
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        if scheme.lower() != "digest":
            if self.auto_error:
                raise self.make_not_authenticated_error()
            else:
                return None
        return HTTPAuthorizationCredentials(scheme=scheme, credentials=credentials)
