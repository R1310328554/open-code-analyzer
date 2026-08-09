from typing import Annotated

from annotated_doc import Doc
from fastapi.openapi.models import OpenIdConnect as OpenIdConnectModel
from fastapi.security.base import SecurityBase
from starlette.exceptions import HTTPException
from starlette.requests import Request
from starlette.status import HTTP_401_UNAUTHORIZED


class OpenIdConnect(SecurityBase):
    """
    OpenID Connect 认证类，其实例可用作依赖项。

    **警告**：这仅是用于在 FastAPI 中将组件与 OpenAPI 关联的桩实现，
    并未实现完整的 OpenID Connect 方案，例如不会真正使用 OpenID Connect URL。
    你需要在代码中子类化并实现完整逻辑。
    """

    def __init__(
        self,
        *,
        openIdConnectUrl: Annotated[
            str,
            Doc(
                """
            OpenID Connect 配置 URL。
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
                默认情况下，若未提供 OpenID Connect 认证所需的 HTTP Authorization 头，
                将自动终止请求并向客户端返回错误。

                若 `auto_error` 设为 `False`，当 Authorization 头不可用时，
                依赖项结果将为 `None` 而非抛出错误。

                适用于可选认证场景。

                也适用于多种可选认证方式之一（例如 OpenID Connect 或 Cookie）。
                """
            ),
        ] = True,
    ):
        self.model = OpenIdConnectModel(
            openIdConnectUrl=openIdConnectUrl, description=description
        )
        self.scheme_name = scheme_name or self.__class__.__name__
        self.auto_error = auto_error

    def make_not_authenticated_error(self) -> HTTPException:
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
