from collections.abc import Mapping, Sequence
from typing import Annotated, Any, TypedDict

from annotated_doc import Doc
from pydantic import BaseModel, create_model
from starlette.exceptions import HTTPException as StarletteHTTPException
from starlette.exceptions import WebSocketException as StarletteWebSocketException


class EndpointContext(TypedDict, total=False):
    function: str
    path: str
    file: str
    line: int


class HTTPException(StarletteHTTPException):
    """
    可在业务代码中抛出的 HTTP 异常，用于向客户端展示错误。

    适用于客户端错误、认证无效、数据无效等场景，不用于服务端代码自身的错误。

    详见 [FastAPI 错误处理文档](https://fastapi.tiangolo.com/tutorial/handling-errors/)。

    ## 示例

    ```python
    from fastapi import FastAPI, HTTPException

    app = FastAPI()

    items = {"foo": "The Foo Wrestlers"}


    @app.get("/items/{item_id}")
    async def read_item(item_id: str):
        if item_id not in items:
            raise HTTPException(status_code=404, detail="Item not found")
        return {"item": items[item_id]}
    ```
    """

    def __init__(
        self,
        status_code: Annotated[
            int,
            Doc(
                """
                发送给客户端的 HTTP 状态码。

                详见 [FastAPI 错误处理文档](https://fastapi.tiangolo.com/tutorial/handling-errors/#use-httpexception)
                """
            ),
        ],
        detail: Annotated[
            Any,
            Doc(
                """
                放入 JSON 响应 `detail` 键中、发送给客户端的任意数据。

                详见 [FastAPI 错误处理文档](https://fastapi.tiangolo.com/tutorial/handling-errors/#use-httpexception)
                """
            ),
        ] = None,
        headers: Annotated[
            Mapping[str, str] | None,
            Doc(
                """
                在响应中发送给客户端的任意 headers。

                详见 [FastAPI 错误处理文档](https://fastapi.tiangolo.com/tutorial/handling-errors/#add-custom-headers)

                """
            ),
        ] = None,
    ) -> None:
        super().__init__(status_code=status_code, detail=detail, headers=headers)


class WebSocketException(StarletteWebSocketException):
    """
    可在业务代码中抛出的 WebSocket 异常，用于向客户端展示错误。

    适用于客户端错误、认证无效、数据无效等场景，不用于服务端代码自身的错误。

    详见 [FastAPI WebSocket 文档](https://fastapi.tiangolo.com/advanced/websockets/)。

    ## 示例

    ```python
    from typing import Annotated

    from fastapi import (
        Cookie,
        FastAPI,
        WebSocket,
        WebSocketException,
        status,
    )

    app = FastAPI()

    @app.websocket("/items/{item_id}/ws")
    async def websocket_endpoint(
        *,
        websocket: WebSocket,
        session: Annotated[str | None, Cookie()] = None,
        item_id: str,
    ):
        if session is None:
            raise WebSocketException(code=status.WS_1008_POLICY_VIOLATION)
        await websocket.accept()
        while True:
            data = await websocket.receive_text()
            await websocket.send_text(f"Session cookie is: {session}")
            await websocket.send_text(f"Message text was: {data}, for item ID: {item_id}")
    ```
    """

    def __init__(
        self,
        code: Annotated[
            int,
            Doc(
                """
                来自[规范定义的有效关闭码](https://datatracker.ietf.org/doc/html/rfc6455#section-7.4.1)之一。
                """
            ),
        ],
        reason: Annotated[
            str | None,
            Doc(
                """
                关闭 WebSocket 连接的原因。

                为 UTF-8 编码数据。原因的含义由应用自行解释，
                WebSocket 规范并未规定。

                可包含人类可读文本，或供客户端代码解析的内容等。
                """
            ),
        ] = None,
    ) -> None:
        super().__init__(code=code, reason=reason)


RequestErrorModel: type[BaseModel] = create_model("Request")
WebSocketErrorModel: type[BaseModel] = create_model("WebSocket")


class FastAPIError(RuntimeError):
    """
    FastAPI 专用的通用错误。
    """


class DependencyScopeError(FastAPIError):
    """
    某依赖项声明它依赖另一个作用域无效（更窄）的依赖项时抛出。
    """


class ValidationException(Exception):
    def __init__(
        self,
        errors: Sequence[Any],
        *,
        endpoint_ctx: EndpointContext | None = None,
    ) -> None:
        self._errors = errors
        self.endpoint_ctx = endpoint_ctx

        ctx = endpoint_ctx or {}
        self.endpoint_function = ctx.get("function")
        self.endpoint_path = ctx.get("path")
        self.endpoint_file = ctx.get("file")
        self.endpoint_line = ctx.get("line")

    def errors(self) -> Sequence[Any]:
        return self._errors

    def _format_endpoint_context(self) -> str:
        if not (self.endpoint_file and self.endpoint_line and self.endpoint_function):
            if self.endpoint_path:
                return f"\n  Endpoint: {self.endpoint_path}"
            return ""

        context = f'\n  File "{self.endpoint_file}", line {self.endpoint_line}, in {self.endpoint_function}'
        if self.endpoint_path:
            context += f"\n    {self.endpoint_path}"
        return context

    def __str__(self) -> str:
        message = f"{len(self._errors)} validation error{'s' if len(self._errors) != 1 else ''}:\n"
        for err in self._errors:
            message += f"  {err}\n"
        message += self._format_endpoint_context()
        return message.rstrip()


class RequestValidationError(ValidationException):
    def __init__(
        self,
        errors: Sequence[Any],
        *,
        body: Any = None,
        endpoint_ctx: EndpointContext | None = None,
    ) -> None:
        super().__init__(errors, endpoint_ctx=endpoint_ctx)
        self.body = body


class WebSocketRequestValidationError(ValidationException):
    def __init__(
        self,
        errors: Sequence[Any],
        *,
        endpoint_ctx: EndpointContext | None = None,
    ) -> None:
        super().__init__(errors, endpoint_ctx=endpoint_ctx)


class ResponseValidationError(ValidationException):
    def __init__(
        self,
        errors: Sequence[Any],
        *,
        body: Any = None,
        endpoint_ctx: EndpointContext | None = None,
    ) -> None:
        super().__init__(errors, endpoint_ctx=endpoint_ctx)
        self.body = body


class PydanticV1NotSupportedError(FastAPIError):
    """
    使用了 pydantic.v1 模型，已不再受支持。
    """


class FastAPIDeprecationWarning(UserWarning):
    """
    自定义弃用警告；DeprecationWarning 会被忽略。
    参考：https://sethmlarson.dev/deprecations-via-warnings-dont-work-for-python-libraries
    """
