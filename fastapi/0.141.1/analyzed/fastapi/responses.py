import importlib
from typing import Any, Protocol, cast

from fastapi.exceptions import FastAPIDeprecationWarning
from fastapi.sse import EventSourceResponse as EventSourceResponse  # noqa
from starlette.responses import FileResponse as FileResponse  # noqa
from starlette.responses import HTMLResponse as HTMLResponse  # noqa
from starlette.responses import JSONResponse as JSONResponse  # noqa
from starlette.responses import PlainTextResponse as PlainTextResponse  # noqa
from starlette.responses import RedirectResponse as RedirectResponse  # noqa
from starlette.responses import Response as Response  # noqa
from starlette.responses import StreamingResponse as StreamingResponse  # noqa
from typing_extensions import deprecated


class _UjsonModule(Protocol):
    def dumps(self, __obj: Any, *, ensure_ascii: bool = ...) -> str: ...


class _OrjsonModule(Protocol):
    OPT_NON_STR_KEYS: int
    OPT_SERIALIZE_NUMPY: int

    def dumps(self, __obj: Any, *, option: int = ...) -> bytes: ...


try:
    ujson = cast(_UjsonModule, importlib.import_module("ujson"))
except ModuleNotFoundError:  # pragma: nocover
    ujson = None  # type: ignore[assignment]


try:
    orjson = cast(_OrjsonModule, importlib.import_module("orjson"))
except ModuleNotFoundError:  # pragma: nocover
    orjson = None  # type: ignore[assignment]


@deprecated(
    "UJSONResponse is deprecated, FastAPI now serializes data directly to JSON "
    "bytes via Pydantic when a return type or response model is set, which is "
    "faster and doesn't need a custom response class. Read more in the FastAPI "
    "docs: https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model "
    "and https://fastapi.tiangolo.com/tutorial/response-model/",
    category=FastAPIDeprecationWarning,
    stacklevel=2,
)
class UJSONResponse(JSONResponse):
    """使用 ujson 库将数据序列化为 JSON 的响应类。

    **已弃用**：`UJSONResponse` 已弃用。当设置了返回类型或响应模型时，
    FastAPI 现通过 Pydantic 直接将数据序列化为 JSON 字节，速度更快且无需自定义响应类。

    详见
    [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model)
    与
    [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。

    **注意**：FastAPI 不包含 `ujson`，需单独安装，例如 `pip install ujson`。
    """

    def render(self, content: Any) -> bytes:
        assert ujson is not None, "ujson must be installed to use UJSONResponse"
        return ujson.dumps(content, ensure_ascii=False).encode("utf-8")


@deprecated(
    "ORJSONResponse is deprecated, FastAPI now serializes data directly to JSON "
    "bytes via Pydantic when a return type or response model is set, which is "
    "faster and doesn't need a custom response class. Read more in the FastAPI "
    "docs: https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model "
    "and https://fastapi.tiangolo.com/tutorial/response-model/",
    category=FastAPIDeprecationWarning,
    stacklevel=2,
)
class ORJSONResponse(JSONResponse):
    """使用 orjson 库将数据序列化为 JSON 的响应类。

    **已弃用**：`ORJSONResponse` 已弃用。当设置了返回类型或响应模型时，
    FastAPI 现通过 Pydantic 直接将数据序列化为 JSON 字节，速度更快且无需自定义响应类。

    详见
    [FastAPI 自定义响应文档](https://fastapi.tiangolo.com/advanced/custom-response/#orjson-or-response-model)
    与
    [FastAPI 响应模型文档](https://fastapi.tiangolo.com/tutorial/response-model/)。

    **注意**：FastAPI 不包含 `orjson`，需单独安装，例如 `pip install orjson`。
    """

    def render(self, content: Any) -> bytes:
        assert orjson is not None, "orjson must be installed to use ORJSONResponse"
        return orjson.dumps(
            content, option=orjson.OPT_NON_STR_KEYS | orjson.OPT_SERIALIZE_NUMPY
        )
