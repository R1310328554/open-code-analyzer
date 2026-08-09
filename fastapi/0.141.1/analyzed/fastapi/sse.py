from typing import Annotated, Any

from annotated_doc import Doc
from pydantic import AfterValidator, BaseModel, Field, model_validator
from starlette.responses import StreamingResponse

# 符合 OpenAPI 3.2 规范的 SSE 事件 schema
#（第 4.14.4 节 "Special Considerations for Server-Sent Events"）
_SSE_EVENT_SCHEMA: dict[str, Any] = {
    "type": "object",
    "properties": {
        "data": {"type": "string"},
        "event": {"type": "string"},
        "id": {"type": "string"},
        "retry": {"type": "integer", "minimum": 0},
    },
}


class EventSourceResponse(StreamingResponse):
    """`text/event-stream` 媒体类型的流式响应。

    在带 `yield` 的路径操作上设置 `response_class=EventSourceResponse`
    以启用 Server-Sent Events (SSE) 响应。

    支持**任意 HTTP 方法**（`GET`、`POST` 等），兼容 MCP 等通过 `POST` 流式传输 SSE 的协议。

    实际编码逻辑在 FastAPI 路由层；本类主要作为标记并设置正确的 `Content-Type`。
    """

    media_type = "text/event-stream"


def _check_single_line(v: str | None, field_name: str) -> str | None:
    if v is not None and ("\r" in v or "\n" in v):
        raise ValueError(f"SSE '{field_name}' must be a single line")
    return v


def _check_event_single_line(v: str | None) -> str | None:
    return _check_single_line(v, "event")


def _check_id_valid(v: str | None) -> str | None:
    if v is not None and "\0" in v:
        raise ValueError("SSE 'id' must not contain null characters")
    return _check_single_line(v, "id")


class ServerSentEvent(BaseModel):
    """表示单个 Server-Sent Event。

    在使用 `response_class=EventSourceResponse` 的路径操作函数中 `yield` 时，
    每个 `ServerSentEvent` 会编码为
    [SSE 线路格式](https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream)
    （`text/event-stream`）。

    若 yield 普通对象（dict、Pydantic 模型等），会自动 JSON 编码并作为 `data:` 字段发送。

    所有 `data` 值**包括普通字符串**都会 JSON 序列化。

    例如 `data="hello"` 在线路上产生 `data: "hello"`（带引号）。
    """

    data: Annotated[
        Any,
        Doc(
            """
            事件负载。

            可为任意 JSON 可序列化值：Pydantic 模型、dict、list、字符串、数字等。
            **始终**序列化为 JSON：字符串会加引号（`"hello"` 在线路上为 `data: "hello"`）。

            与 `raw_data` 互斥。
            """
        ),
    ] = None
    raw_data: Annotated[
        str | None,
        Doc(
            """
            作为 `data:` 字段发送的原始字符串，**不**经 JSON 编码。

            适用于预格式化文本、HTML 片段、CSV 行等非 JSON 负载。
            字符串原样写入 `data:` 字段。

            与 `data` 互斥。
            """
        ),
    ] = None
    event: Annotated[
        str | None,
        AfterValidator(_check_event_single_line),
        Doc(
            """
            可选事件类型名。

            对应浏览器 `addEventListener(event, ...)`。省略时使用通用 `message` 事件。
            必须为单行。
            """
        ),
    ] = None
    id: Annotated[
        str | None,
        AfterValidator(_check_id_valid),
        Doc(
            """
            可选事件 ID。

            浏览器自动重连时会将其作为 `Last-Event-ID` 头发回。
            **必须为单行**且不得包含空字符（`\\0`）。
            """
        ),
    ] = None
    retry: Annotated[
        int | None,
        Field(ge=0),
        Doc(
            """
            可选重连等待时间，单位为**毫秒**。

            告知浏览器连接断开后等待多久再重连。须为非负整数。
            """
        ),
    ] = None
    comment: Annotated[
        str | None,
        Doc(
            """
            可选注释行。

            SSE 线路格式中以 `:` 开头的注释行会被 `EventSource` 客户端忽略。
            可用于 keep-alive 心跳，防止代理/负载均衡超时。
            """
        ),
    ] = None

    @model_validator(mode="after")
    def _check_data_exclusive(self) -> "ServerSentEvent":
        if self.data is not None and self.raw_data is not None:
            raise ValueError(
                "Cannot set both 'data' and 'raw_data' on the same "
                "ServerSentEvent. Use 'data' for JSON-serialized payloads "
                "or 'raw_data' for pre-formatted strings."
            )
        return self


def _split_sse_lines(value: str) -> list[str]:
    # 仅按 SSE 规范行终止符（\n、\r\n、\r）拆分，保留末尾空字符串
    return value.replace("\r\n", "\n").replace("\r", "\n").split("\n")


def format_sse_event(
    *,
    data_str: Annotated[
        str | None,
        Doc(
            """
            用作 `data:` 字段的预序列化数据字符串。
            """
        ),
    ] = None,
    event: Annotated[
        str | None,
        Doc(
            """
            可选事件类型名（`event:` 字段）。
            """
        ),
    ] = None,
    id: Annotated[
        str | None,
        Doc(
            """
            可选事件 ID（`id:` 字段）。
            """
        ),
    ] = None,
    retry: Annotated[
        int | None,
        Doc(
            """
            可选重连时间，毫秒（`retry:` 字段）。
            """
        ),
    ] = None,
    comment: Annotated[
        str | None,
        Doc(
            """
            可选注释行（`:` 前缀）。
            """
        ),
    ] = None,
) -> bytes:
    """从**预序列化**数据构建 SSE 线路格式字节。

    结果始终以 `\\n\\n`（事件终止符）结尾。
    """
    lines: list[str] = []

    if comment is not None:
        for line in _split_sse_lines(comment):
            lines.append(f": {line}")

    if event is not None:
        lines.append(f"event: {event}")

    if data_str is not None:
        for line in _split_sse_lines(data_str):
            lines.append(f"data: {line}")

    if id is not None:
        lines.append(f"id: {id}")

    if retry is not None:
        lines.append(f"retry: {retry}")

    lines.append("")
    lines.append("")
    return "\n".join(lines).encode("utf-8")


# SSE 规范建议的 keep-alive 注释
KEEPALIVE_COMMENT = b": ping\n\n"

# 生成器空闲时 keep-alive 心跳间隔（秒）。
# 私有但可导入，供测试 monkeypatch
_PING_INTERVAL: float = 15.0
