from typing import Annotated

from annotated_doc import Doc
from fastapi.openapi.models import APIKey, APIKeyIn
from fastapi.security.base import SecurityBase
from starlette.exceptions import HTTPException
from starlette.requests import Request
from starlette.status import HTTP_401_UNAUTHORIZED


class APIKeyBase(SecurityBase):
    model: APIKey

    def __init__(
        self,
        location: APIKeyIn,
        name: str,
        description: str | None,
        scheme_name: str | None,
        auto_error: bool,
    ):
        self.auto_error = auto_error

        self.model: APIKey = APIKey(
            **{"in": location},  # ty: ignore[invalid-argument-type]
            name=name,
            description=description,
        )
        self.scheme_name = scheme_name or self.__class__.__name__

    def make_not_authenticated_error(self) -> HTTPException:
        """
        API Key 认证未标准化 WWW-Authenticate 头，但 HTTP 规范要求
        401 "Unauthorized" 响应必须包含 WWW-Authenticate 头。

        参考：https://datatracker.ietf.org/doc/html/rfc9110#name-401-unauthorized

        因此本方法发送自定义 challenge `APIKey`。
        """
        return HTTPException(
            status_code=HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "APIKey"},
        )

    def check_api_key(self, api_key: str | None) -> str | None:
        if not api_key:
            if self.auto_error:
                raise self.make_not_authenticated_error()
            return None
        return api_key


class APIKeyQuery(APIKeyBase):
    """
    通过查询参数进行 API Key 认证。

    定义请求中应携带 API Key 的查询参数名称，并将其集成到 OpenAPI 文档。
    自动从查询参数提取 Key 值作为依赖项结果，但不定义如何将 Key 分发给客户端。

    ## 用法

    创建实例并在 `Depends()` 中作为依赖项使用。

    依赖项结果为包含 Key 值的字符串。

    ## 示例

    ```python
    from fastapi import Depends, FastAPI
    from fastapi.security import APIKeyQuery

    app = FastAPI()

    query_scheme = APIKeyQuery(name="api_key")


    @app.get("/items/")
    async def read_items(api_key: str = Depends(query_scheme)):
        return {"api_key": api_key}
    ```
    """

    def __init__(
        self,
        *,
        name: Annotated[
            str,
            Doc("查询参数名称。"),
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
                默认情况下，若未提供查询参数，`APIKeyQuery` 将
                自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当查询参数不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景。

                也适用于多种可选认证方式之一（例如查询参数或 HTTP Bearer 令牌）。
                """
            ),
        ] = True,
    ):
        super().__init__(
            location=APIKeyIn.query,
            name=name,
            scheme_name=scheme_name,
            description=description,
            auto_error=auto_error,
        )

    async def __call__(self, request: Request) -> str | None:
        api_key = request.query_params.get(self.model.name)
        return self.check_api_key(api_key)


class APIKeyHeader(APIKeyBase):
    """
    通过请求头进行 API Key 认证。

    定义请求中应携带 API Key 的请求头名称，并将其集成到 OpenAPI 文档。
    自动从请求头提取 Key 值作为依赖项结果，但不定义如何将 Key 分发给客户端。

    ## 用法

    创建实例并在 `Depends()` 中作为依赖项使用。

    依赖项结果为包含 Key 值的字符串。

    ## 示例

    ```python
    from fastapi import Depends, FastAPI
    from fastapi.security import APIKeyHeader

    app = FastAPI()

    header_scheme = APIKeyHeader(name="x-key")


    @app.get("/items/")
    async def read_items(key: str = Depends(header_scheme)):
        return {"key": key}
    ```
    """

    def __init__(
        self,
        *,
        name: Annotated[str, Doc("请求头名称。")],
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
                默认情况下，若未提供请求头，`APIKeyHeader` 将
                自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当请求头不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景。

                也适用于多种可选认证方式之一（例如请求头或 HTTP Bearer 令牌）。
                """
            ),
        ] = True,
    ):
        super().__init__(
            location=APIKeyIn.header,
            name=name,
            scheme_name=scheme_name,
            description=description,
            auto_error=auto_error,
        )

    async def __call__(self, request: Request) -> str | None:
        api_key = request.headers.get(self.model.name)
        return self.check_api_key(api_key)


class APIKeyCookie(APIKeyBase):
    """
    通过 Cookie 进行 API Key 认证。

    定义请求中应携带 API Key 的 Cookie 名称，并将其集成到 OpenAPI 文档。
    自动从 Cookie 提取 Key 值作为依赖项结果，但不定义如何设置该 Cookie。

    ## 用法

    创建实例并在 `Depends()` 中作为依赖项使用。

    依赖项结果为包含 Key 值的字符串。

    ## 示例

    ```python
    from fastapi import Depends, FastAPI
    from fastapi.security import APIKeyCookie

    app = FastAPI()

    cookie_scheme = APIKeyCookie(name="session")


    @app.get("/items/")
    async def read_items(session: str = Depends(cookie_scheme)):
        return {"session": session}
    ```
    """

    def __init__(
        self,
        *,
        name: Annotated[str, Doc("Cookie 名称。")],
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
                默认情况下，若未提供 Cookie，`APIKeyCookie` 将
                自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Cookie 不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景。

                也适用于多种可选认证方式之一（例如 Cookie 或 HTTP Bearer 令牌）。
                """
            ),
        ] = True,
    ):
        super().__init__(
            location=APIKeyIn.cookie,
            name=name,
            scheme_name=scheme_name,
            description=description,
            auto_error=auto_error,
        )

    async def __call__(self, request: Request) -> str | None:
        api_key = request.cookies.get(self.model.name)
        return self.check_api_key(api_key)
