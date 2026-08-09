"""FastAPI 路由层：APIRoute、APIRouter、请求处理与前端静态文件路由。"""

import contextlib
import copy
import email.message
import errno
import functools
import inspect
import json
import os
import stat
import threading
import types
import warnings
from collections.abc import (
    AsyncIterator,
    Awaitable,
    Callable,
    Collection,
    Coroutine,
    Generator,
    Iterator,
    Mapping,
    Sequence,
)
from contextlib import (
    AbstractAsyncContextManager,
    AbstractContextManager,
    AsyncExitStack,
    asynccontextmanager,
)
from contextvars import ContextVar
from dataclasses import dataclass, field
from enum import Enum, IntEnum
from typing import (
    Annotated,
    Any,
    Literal,
    Protocol,
    TypeVar,
    cast,
)

import anyio
from annotated_doc import Doc
from anyio.abc import ObjectReceiveStream
from fastapi import params
from fastapi._compat import (
    ModelField,
    Undefined,
    lenient_issubclass,
)
from fastapi.datastructures import Default, DefaultPlaceholder
from fastapi.dependencies.models import (
    Dependant,
    _is_async_gen_callable,
    _is_coroutine_callable,
    _is_gen_callable,
)
from fastapi.dependencies.utils import (
    SolvedDependency,
    _get_body_field,
    _get_flat_body_params,
    _should_embed_body_fields,
    get_dependant,
    get_parameterless_sub_dependant,
    get_stream_item_type,
    get_typed_return_annotation,
    solve_dependencies,
)
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import (
    EndpointContext,
    FastAPIError,
    RequestValidationError,
    ResponseValidationError,
    WebSocketRequestValidationError,
)
from fastapi.sse import (
    _PING_INTERVAL,
    KEEPALIVE_COMMENT,
    EventSourceResponse,
    ServerSentEvent,
    format_sse_event,
)
from fastapi.types import DecoratedCallable, IncEx
from fastapi.utils import (
    create_model_field,
    generate_unique_id,
    get_value_or_default,
    is_body_allowed_for_status_code,
)
from starlette import routing
from starlette._exception_handler import wrap_app_handling_exceptions
from starlette._utils import get_route_path, is_async_callable
from starlette.concurrency import iterate_in_threadpool, run_in_threadpool
from starlette.datastructures import URL, FormData, URLPath
from starlette.exceptions import HTTPException
from starlette.requests import Request
from starlette.responses import (
    JSONResponse,
    PlainTextResponse,
    RedirectResponse,
    Response,
    StreamingResponse,
)
from starlette.routing import (
    BaseRoute,
    Match,
    NoMatchFound,
    compile_path,
    get_name,
)
from starlette.routing import Mount as Mount  # noqa
from starlette.staticfiles import StaticFiles
from starlette.types import AppType, ASGIApp, Lifespan, Receive, Scope, Send
from starlette.websockets import WebSocket
from typing_extensions import deprecated


# 修改自 starlette.routing.request_response，加入依赖的 AsyncExitStack
def request_response(
    func: Callable[[Request], Awaitable[Response] | Response],
) -> ASGIApp:
    """
    接收函数或 coroutine `func(request) -> response`，返回 ASGI 应用。
    """
    f: Callable[[Request], Awaitable[Response]] = (
        func  # type: ignore[assignment]
        if is_async_callable(func)
        else functools.partial(run_in_threadpool, func)  # type: ignore[call-arg]
    )  # ty: ignore[invalid-assignment]

    async def app(scope: Scope, receive: Receive, send: Send) -> None:
        request = Request(scope, receive, send)

        async def app(scope: Scope, receive: Receive, send: Send) -> None:
            # 开始定制
            response_awaited = False
            async with AsyncExitStack() as request_stack:
                scope["fastapi_inner_astack"] = request_stack
                async with AsyncExitStack() as function_stack:
                    scope["fastapi_function_astack"] = function_stack
                    response = await f(request)
                await response(scope, receive, send)
                # 继续定制
                response_awaited = True
            if not response_awaited:
                raise FastAPIError(
                    "Response not awaited. There's a high chance that the "
                    "application code is raising an exception and a dependency with yield "
                    "has a block with a bare except, or a block with except Exception, "
                    "and is not raising the exception again. 详见 "
                    "docs: https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-with-yield/#dependencies-with-yield-and-except"
                )

        # 与 Starlette 相同
        await wrap_app_handling_exceptions(app, request)(scope, receive, send)

    return app


# 修改自 starlette.routing.websocket_session，加入依赖的 AsyncExitStack
def websocket_session(
    func: Callable[[WebSocket], Awaitable[None]],
) -> ASGIApp:
    """
    接收 coroutine `func(session)`，返回 ASGI 应用。
    """
    # assert asyncio.iscoroutinefunction(func), "WebSocket endpoints must be async"

    async def app(scope: Scope, receive: Receive, send: Send) -> None:
        session = WebSocket(scope, receive=receive, send=send)

        async def app(scope: Scope, receive: Receive, send: Send) -> None:
            async with AsyncExitStack() as request_stack:
                scope["fastapi_inner_astack"] = request_stack
                async with AsyncExitStack() as function_stack:
                    scope["fastapi_function_astack"] = function_stack
                    await func(session)

        # 与 Starlette 相同
        await wrap_app_handling_exceptions(app, session)(scope, receive, send)

    return app


_T = TypeVar("_T")


# 从 starlette.routing 复制，避免导入私有符号
class _AsyncLiftContextManager(AbstractAsyncContextManager[_T]):
    """
    将同步上下文管理器包装为异步。

    从 Starlette 复制，避免导入私有符号。
    """

    def __init__(self, cm: AbstractContextManager[_T]) -> None:
        self._cm = cm

    async def __aenter__(self) -> _T:
        return self._cm.__enter__()

    async def __aexit__(
        self,
        exc_type: type[BaseException] | None,
        exc_value: BaseException | None,
        traceback: types.TracebackType | None,
    ) -> bool | None:
        return self._cm.__exit__(exc_type, exc_value, traceback)


# 从 starlette.routing 复制，避免导入私有符号
def _wrap_gen_lifespan_context(
    lifespan_context: Callable[[Any], Generator[Any, Any, Any]],
) -> Callable[[Any], AbstractAsyncContextManager[Any]]:
    """
    将基于生成器的 lifespan 上下文包装为异步上下文管理器。

    从 Starlette 复制，避免导入私有符号。
    """
    cmgr = contextlib.contextmanager(lifespan_context)

    @functools.wraps(cmgr)
    def wrapper(app: Any) -> _AsyncLiftContextManager[Any]:
        return _AsyncLiftContextManager(cmgr(app))

    return wrapper


def _merge_lifespan_context(
    original_context: Lifespan[Any], nested_context: Lifespan[Any]
) -> Lifespan[Any]:
    @asynccontextmanager
    async def merged_lifespan(
        app: AppType,
    ) -> AsyncIterator[Mapping[str, Any] | None]:
        async with original_context(app) as maybe_original_state:
            async with nested_context(app) as maybe_nested_state:
                if maybe_nested_state is None and maybe_original_state is None:
                    yield None  # old ASGI compatibility
                else:
                    yield {**(maybe_nested_state or {}), **(maybe_original_state or {})}

    return merged_lifespan  # type: ignore[return-value]  # ty: ignore[invalid-return-type]


class _DefaultLifespan:
    """
    运行 on_startup 与 on_shutdown 处理器的默认 lifespan 上下文管理器。

    这是 Starlette 中已移除的 _DefaultLifespan 类的副本。
    FastAPI 保留它以维持与 on_startup/on_shutdown 事件处理器的向后兼容。

    参考：https://github.com/Kludex/starlette/pull/3117
    """

    def __init__(self, router: "APIRouter") -> None:
        self._router = router

    async def __aenter__(self) -> None:
        await self._router._startup()

    async def __aexit__(self, *exc_info: object) -> None:
        await self._router._shutdown()

    def __call__(self: _T, app: object) -> _T:
        return self


# 缓存端点上下文，避免每次请求重复提取
_endpoint_context_cache: dict[int, EndpointContext] = {}


def _extract_endpoint_context(func: Any) -> EndpointContext:
    """带缓存地提取端点上下文，避免重复文件 I/O。"""
    func_id = id(func)

    if func_id in _endpoint_context_cache:
        return _endpoint_context_cache[func_id]

    try:
        ctx: EndpointContext = {}

        if (source_file := inspect.getsourcefile(func)) is not None:
            ctx["file"] = source_file
        if (line_number := inspect.getsourcelines(func)[1]) is not None:
            ctx["line"] = line_number
        if (func_name := getattr(func, "__name__", None)) is not None:
            ctx["function"] = func_name
    except Exception:
        ctx = EndpointContext()

    _endpoint_context_cache[func_id] = ctx
    return ctx


async def serialize_response(
    *,
    field: ModelField | None = None,
    response_content: Any,
    include: IncEx | None = None,
    exclude: IncEx | None = None,
    by_alias: bool = True,
    exclude_unset: bool = False,
    exclude_defaults: bool = False,
    exclude_none: bool = False,
    is_coroutine: bool = True,
    endpoint_ctx: EndpointContext | None = None,
    dump_json: bool = False,
) -> Any:
    if field:
        if is_coroutine:
            value, errors = field.validate(response_content, {}, loc=("response",))
        else:
            value, errors = await run_in_threadpool(
                field.validate, response_content, {}, loc=("response",)
            )
        if errors:
            ctx = endpoint_ctx or EndpointContext()
            raise ResponseValidationError(
                errors=errors,
                body=response_content,
                endpoint_ctx=ctx,
            )
        serializer = field.serialize_json if dump_json else field.serialize
        return serializer(
            value,
            include=include,
            exclude=exclude,
            by_alias=by_alias,
            exclude_unset=exclude_unset,
            exclude_defaults=exclude_defaults,
            exclude_none=exclude_none,
        )

    else:
        return jsonable_encoder(response_content)


async def run_endpoint_function(
    *, dependant: Dependant, values: dict[str, Any], is_coroutine: bool
) -> Any:
    # 仅由 get_request_handler 调用。拆成独立函数便于分析端点性能（内层函数较难 profiling）。
    assert dependant.call is not None, "dependant.call must be a function"

    if is_coroutine:
        return await dependant.call(**values)
    else:
        return await run_in_threadpool(dependant.call, **values)


def _build_response_args(
    *, status_code: int | None, solved_result: Any
) -> dict[str, Any]:
    response_args: dict[str, Any] = {
        "background": solved_result.background_tasks,
    }
    # 若设置了 status_code 则使用，否则用响应类默认值（重定向为 307）
    current_status_code = (
        status_code if status_code else solved_result.response.status_code
    )
    if current_status_code is not None:
        response_args["status_code"] = current_status_code
    if solved_result.response.status_code:
        response_args["status_code"] = solved_result.response.status_code
    return response_args


def get_request_handler(
    dependant: Dependant,
    body_field: ModelField | None = None,
    status_code: int | None = None,
    response_class: type[Response] | DefaultPlaceholder = Default(JSONResponse),
    response_field: ModelField | None = None,
    response_model_include: IncEx | None = None,
    response_model_exclude: IncEx | None = None,
    response_model_by_alias: bool = True,
    response_model_exclude_unset: bool = False,
    response_model_exclude_defaults: bool = False,
    response_model_exclude_none: bool = False,
    dependency_overrides_provider: Any | None = None,
    embed_body_fields: bool = False,
    strict_content_type: bool | DefaultPlaceholder = Default(True),
    stream_item_field: ModelField | None = None,
    is_json_stream: bool = False,
) -> Callable[[Request], Coroutine[Any, Any, Response]]:
    assert dependant.call is not None, "dependant.call must be a function"
    is_coroutine = _is_coroutine_callable(dependant.call)
    is_body_form = body_field and isinstance(body_field.field_info, params.Form)
    if isinstance(response_class, DefaultPlaceholder):
        actual_response_class: type[Response] = response_class.value
    else:
        actual_response_class = response_class
    is_sse_stream = lenient_issubclass(actual_response_class, EventSourceResponse)
    if isinstance(strict_content_type, DefaultPlaceholder):
        actual_strict_content_type: bool = strict_content_type.value
    else:
        actual_strict_content_type = strict_content_type

    async def app(request: Request) -> Response:
        response: Response | None = None
        file_stack = request.scope.get("fastapi_middleware_astack")
        assert isinstance(file_stack, AsyncExitStack), (
            "fastapi_middleware_astack not found in request scope"
        )

        # 提取端点上下文用于错误消息
        endpoint_ctx = (
            _extract_endpoint_context(dependant.call)
            if dependant.call
            else EndpointContext()
        )

        if dependant.path:
            # 挂载子应用时包含 mount 路径前缀
            mount_path = request.scope.get("root_path", "").rstrip("/")
            endpoint_ctx["path"] = f"{request.method} {mount_path}{dependant.path}"

        # 读取 body 并自动关闭文件
        try:
            body: Any = None
            if body_field:
                if is_body_form:
                    body = await request.form()
                    file_stack.push_async_callback(body.close)
                else:
                    body_bytes = await request.body()
                    if body_bytes:
                        json_body: Any = Undefined
                        content_type_value = request.headers.get("content-type")
                        if not content_type_value:
                            if not actual_strict_content_type:
                                json_body = await request.json()
                        else:
                            message = email.message.Message()
                            message["content-type"] = content_type_value
                            if message.get_content_maintype() == "application":
                                subtype = message.get_content_subtype()
                                if subtype == "json" or subtype.endswith("+json"):
                                    json_body = await request.json()
                        if json_body != Undefined:
                            body = json_body
                        else:
                            body = body_bytes
        except json.JSONDecodeError as e:
            validation_error = RequestValidationError(
                [
                    {
                        "type": "json_invalid",
                        "loc": ("body", e.pos),
                        "msg": "JSON decode error",
                        "input": {},
                        "ctx": {"error": e.msg},
                    }
                ],
                body=e.doc,
                endpoint_ctx=endpoint_ctx,
            )
            raise validation_error from e
        except HTTPException:
            # 中间件抛出 HTTPException 时应再次抛出
            raise
        except Exception as e:
            http_error = HTTPException(
                status_code=400, detail="There was an error parsing the body"
            )
            raise http_error from e

        # 解析依赖并运行路径操作函数，自动关闭依赖
        errors: list[Any] = []
        async_exit_stack = request.scope.get("fastapi_inner_astack")
        assert isinstance(async_exit_stack, AsyncExitStack), (
            "fastapi_inner_astack not found in request scope"
        )
        solved_result = await solve_dependencies(
            request=request,
            dependant=dependant,
            body=cast(dict[str, Any] | FormData | bytes | None, body),
            dependency_overrides_provider=dependency_overrides_provider,
            async_exit_stack=async_exit_stack,
            embed_body_fields=embed_body_fields,
        )
        errors = solved_result.errors
        assert dependant.call  # For types
        if not errors:
            # 流式项（JSONL/SSE）共享序列化器：若设置了 stream_item_field 则校验，再序列化为 JSON 字节。
            def _serialize_data(data: Any) -> bytes:
                if stream_item_field:
                    value, errors_ = stream_item_field.validate(
                        data, {}, loc=("response",)
                    )
                    if errors_:
                        ctx = endpoint_ctx or EndpointContext()
                        raise ResponseValidationError(
                            errors=errors_,
                            body=data,
                            endpoint_ctx=ctx,
                        )
                    return stream_item_field.serialize_json(
                        value,
                        include=response_model_include,
                        exclude=response_model_exclude,
                        by_alias=response_model_by_alias,
                        exclude_unset=response_model_exclude_unset,
                        exclude_defaults=response_model_exclude_defaults,
                        exclude_none=response_model_exclude_none,
                    )
                else:
                    data = jsonable_encoder(data)
                    return json.dumps(data).encode("utf-8")

            if is_sse_stream:
                # 生成器端点：以 Server-Sent Events 流式输出
                gen = dependant.call(**solved_result.values)

                def _serialize_sse_item(item: Any) -> bytes:
                    if isinstance(item, ServerSentEvent):
                        # User controls the event structure.
                        # Serialize the data payload if present.
                        # For ServerSentEvent items we skip stream_item_field
                        # validation (the user may mix types intentionally).
                        if item.raw_data is not None:
                            data_str: str | None = item.raw_data
                        elif item.data is not None:
                            if hasattr(item.data, "model_dump_json"):
                                data_str = item.data.model_dump_json()
                            else:
                                data_str = json.dumps(jsonable_encoder(item.data))
                        else:
                            data_str = None
                        return format_sse_event(
                            data_str=data_str,
                            event=item.event,
                            id=item.id,
                            retry=item.retry,
                            comment=item.comment,
                        )
                    else:
                        # Plain object: validate + serialize via
                        # stream_item_field (if set) and wrap in data field
                        return format_sse_event(
                            data_str=_serialize_data(item).decode("utf-8")
                        )

                if _is_async_gen_callable(dependant.call):
                    sse_aiter: AsyncIterator[Any] = gen.__aiter__()
                else:
                    sse_aiter = iterate_in_threadpool(gen)

                @asynccontextmanager
                async def _sse_producer_cm() -> AsyncIterator[
                    ObjectReceiveStream[bytes]
                ]:
                    # Use a memory stream to decouple generator iteration
                    # from the keepalive timer. A producer task pulls items
                    # from the generator independently, so
                    # `anyio.fail_after` never wraps the generator's
                    # `__anext__` directly - avoiding CancelledError that
                    # would finalize the generator and also working for sync
                    # generators running in a thread pool.
                    #
                    # This context manager is entered on the request-scoped
                    # AsyncExitStack so its __aexit__ (which cancels the
                    # task group) is called by the exit stack after the
                    # streaming response completes — not by async generator
                    # finalization via GeneratorExit.
                    # Ref: https://peps.python.org/pep-0789/
                    send_stream, receive_stream = anyio.create_memory_object_stream[
                        bytes
                    ](max_buffer_size=1)

                    async def _producer() -> None:
                        async with send_stream:
                            async for raw_item in sse_aiter:
                                await send_stream.send(_serialize_sse_item(raw_item))

                    send_keepalive, receive_keepalive = (
                        anyio.create_memory_object_stream[bytes](max_buffer_size=1)
                    )

                    async def _keepalive_inserter() -> None:
                        """从生产者读取并转发到输出，
                        超时时插入 keepalive 注释。"""
                        async with send_keepalive, receive_stream:
                            try:
                                while True:
                                    try:
                                        with anyio.fail_after(_PING_INTERVAL):
                                            data = await receive_stream.receive()
                                        await send_keepalive.send(data)
                                    except TimeoutError:
                                        await send_keepalive.send(KEEPALIVE_COMMENT)
                            except anyio.EndOfStream:
                                pass

                    async with anyio.create_task_group() as tg:
                        tg.start_soon(_producer)
                        tg.start_soon(_keepalive_inserter)
                        yield receive_keepalive
                        tg.cancel_scope.cancel()

                # Enter the SSE context manager on the request-scoped
                # exit stack. The stack outlives the streaming response,
                # so __aexit__ runs via proper structured teardown, not
                # via GeneratorExit thrown into an async generator.
                sse_receive_stream = await async_exit_stack.enter_async_context(
                    _sse_producer_cm()
                )
                # Ensure the receive stream is closed when the exit stack
                # unwinds, preventing ResourceWarning from __del__.
                async_exit_stack.push_async_callback(sse_receive_stream.aclose)

                async def _sse_with_checkpoints(
                    stream: ObjectReceiveStream[bytes],
                ) -> AsyncIterator[bytes]:
                    async for data in stream:
                        yield data
                        # Guarantee a checkpoint so cancellation can be
                        # delivered even when the producer is faster than
                        # the consumer and receive() never suspends.
                        await anyio.sleep(0)

                sse_stream_content: AsyncIterator[bytes] | Iterator[bytes] = (
                    _sse_with_checkpoints(sse_receive_stream)
                )

                response_args = _build_response_args(
                    status_code=status_code, solved_result=solved_result
                )
                response = StreamingResponse(
                    sse_stream_content,
                    media_type="text/event-stream",
                    **response_args,
                )
                response.headers["Cache-Control"] = "no-cache"
                # For Nginx proxies to not buffer server sent events
                response.headers["X-Accel-Buffering"] = "no"
                response.headers.raw.extend(solved_result.response.headers.raw)
            elif is_json_stream:
                # Generator endpoint: stream as JSONL
                gen = dependant.call(**solved_result.values)

                def _serialize_item(item: Any) -> bytes:
                    return _serialize_data(item) + b"\n"

                if _is_async_gen_callable(dependant.call):

                    async def _async_stream_jsonl() -> AsyncIterator[bytes]:
                        async for item in gen:
                            yield _serialize_item(item)
                            # To allow for cancellation to trigger
                            # Ref: https://github.com/fastapi/fastapi/issues/14680
                            await anyio.sleep(0)

                    jsonl_stream_content: AsyncIterator[bytes] | Iterator[bytes] = (
                        _async_stream_jsonl()
                    )
                else:

                    def _sync_stream_jsonl() -> Iterator[bytes]:
                        for item in gen:  # ty: ignore[not-iterable]
                            yield _serialize_item(item)

                    jsonl_stream_content = _sync_stream_jsonl()

                response_args = _build_response_args(
                    status_code=status_code, solved_result=solved_result
                )
                response = StreamingResponse(
                    jsonl_stream_content,
                    media_type="application/jsonl",
                    **response_args,
                )
                response.headers.raw.extend(solved_result.response.headers.raw)
            elif _is_async_gen_callable(dependant.call) or _is_gen_callable(
                dependant.call
            ):
                # Raw streaming with explicit response_class (e.g. StreamingResponse)
                gen = dependant.call(**solved_result.values)
                if _is_async_gen_callable(dependant.call):

                    async def _async_stream_raw(
                        async_gen: AsyncIterator[Any],
                    ) -> AsyncIterator[Any]:
                        async for chunk in async_gen:
                            yield chunk
                            # To allow for cancellation to trigger
                            # Ref: https://github.com/fastapi/fastapi/issues/14680
                            await anyio.sleep(0)

                    gen = _async_stream_raw(gen)
                response_args = _build_response_args(
                    status_code=status_code, solved_result=solved_result
                )
                response = actual_response_class(content=gen, **response_args)
                response.headers.raw.extend(solved_result.response.headers.raw)
            else:
                raw_response = await run_endpoint_function(
                    dependant=dependant,
                    values=solved_result.values,
                    is_coroutine=is_coroutine,
                )
                if isinstance(raw_response, Response):
                    if raw_response.background is None:
                        raw_response.background = solved_result.background_tasks
                    response = raw_response
                else:
                    response_args = _build_response_args(
                        status_code=status_code, solved_result=solved_result
                    )
                    # Use the fast path (dump_json) when no custom response
                    # class was set and a response field with a TypeAdapter
                    # exists. Serializes directly to JSON bytes via Pydantic's
                    # Rust core, skipping the intermediate Python dict +
                    # json.dumps() step.
                    use_dump_json = response_field is not None and isinstance(
                        response_class, DefaultPlaceholder
                    )
                    content = await serialize_response(
                        field=response_field,
                        response_content=raw_response,
                        include=response_model_include,
                        exclude=response_model_exclude,
                        by_alias=response_model_by_alias,
                        exclude_unset=response_model_exclude_unset,
                        exclude_defaults=response_model_exclude_defaults,
                        exclude_none=response_model_exclude_none,
                        is_coroutine=is_coroutine,
                        endpoint_ctx=endpoint_ctx,
                        dump_json=use_dump_json,
                    )
                    if use_dump_json:
                        response = Response(
                            content=content,
                            media_type="application/json",
                            **response_args,
                        )
                    else:
                        response = actual_response_class(content, **response_args)
                    if not is_body_allowed_for_status_code(response.status_code):
                        response.body = b""
                    response.headers.raw.extend(solved_result.response.headers.raw)
        if errors:
            validation_error = RequestValidationError(
                errors, body=body, endpoint_ctx=endpoint_ctx
            )
            raise validation_error

        # Return response
        assert response
        return response

    return app


def get_websocket_app(
    dependant: Dependant,
    dependency_overrides_provider: Any | None = None,
    embed_body_fields: bool = False,
) -> Callable[[WebSocket], Coroutine[Any, Any, Any]]:
    async def app(websocket: WebSocket) -> None:
        endpoint_ctx = (
            _extract_endpoint_context(dependant.call)
            if dependant.call
            else EndpointContext()
        )
        if dependant.path:
            # 挂载子应用时包含 mount 路径前缀
            mount_path = websocket.scope.get("root_path", "").rstrip("/")
            endpoint_ctx["path"] = f"WS {mount_path}{dependant.path}"
        async_exit_stack = websocket.scope.get("fastapi_inner_astack")
        assert isinstance(async_exit_stack, AsyncExitStack), (
            "fastapi_inner_astack not found in request scope"
        )
        solved_result = await solve_dependencies(
            request=websocket,
            dependant=dependant,
            dependency_overrides_provider=dependency_overrides_provider,
            async_exit_stack=async_exit_stack,
            embed_body_fields=embed_body_fields,
        )
        if solved_result.errors:
            raise WebSocketRequestValidationError(
                solved_result.errors,
                endpoint_ctx=endpoint_ctx,
            )
        assert dependant.call is not None, "dependant.call must be a function"
        await dependant.call(**solved_result.values)

    return app


class APIWebSocketRoute(routing.WebSocketRoute):
    def __init__(
        self,
        path: str,
        endpoint: Callable[..., Any],
        *,
        name: str | None = None,
        dependencies: Sequence[params.Depends] | None = None,
        dependency_overrides_provider: Any | None = None,
    ) -> None:
        self.path = path
        self.endpoint = endpoint
        self.name = get_name(endpoint) if name is None else name
        self.dependencies = list(dependencies or [])
        self.path_regex, self.path_format, self.param_convertors = compile_path(path)
        (
            self.dependant,
            _,
            self._embed_body_fields,
        ) = _build_dependant_with_parameterless_dependencies(
            path=self.path_format,
            call=self.endpoint,
            dependencies=self.dependencies,
        )
        self.app = websocket_session(
            get_websocket_app(
                dependant=self.dependant,
                dependency_overrides_provider=dependency_overrides_provider,
                embed_body_fields=self._embed_body_fields,
            )
        )

    def matches(self, scope: Scope) -> tuple[Match, Scope]:
        match, child_scope = super().matches(scope)
        if match != Match.NONE:
            child_scope["route"] = self
        return match, child_scope


_FASTAPI_SCOPE_KEY = "fastapi"
_FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY = "effective_route_context"
_FASTAPI_FRONTEND_PATH_KEY = "frontend_path"
_FASTAPI_FRONTEND_SPECIFICITY_KEY = "frontend_specificity"
_FASTAPI_INCLUDED_ROUTER_KEY = "included_router"
_effective_route_context_var: ContextVar[Any | None] = ContextVar(
    "fastapi_effective_route_context", default=None
)
_SCOPE_MISSING = object()


def _frontend_dependency_endpoint() -> None:
    pass  # pragma: no cover


def _build_dependant_with_parameterless_dependencies(
    *,
    path: str,
    call: Callable[..., Any],
    dependencies: Sequence[params.Depends],
) -> tuple[Dependant, list[ModelField], bool]:
    dependant = get_dependant(path=path, call=call, scope="function")
    for depends in dependencies[::-1]:
        dependant.dependencies.insert(
            0,
            get_parameterless_sub_dependant(depends=depends, path=path),
        )
    body_params = _get_flat_body_params(dependant)
    embed_body_fields = _should_embed_body_fields(body_params)
    return dependant, body_params, embed_body_fields


class _RouteWithPath(Protocol):
    path: str


def _get_fastapi_scope(scope: Scope) -> dict[str, Any]:
    fastapi_scope = scope.setdefault(_FASTAPI_SCOPE_KEY, {})
    assert isinstance(fastapi_scope, dict)
    return fastapi_scope


def _update_scope(scope: Scope, child_scope: Scope) -> None:
    fastapi_child_scope = child_scope.get(_FASTAPI_SCOPE_KEY)
    for key, value in child_scope.items():
        if key != _FASTAPI_SCOPE_KEY:
            scope[key] = value
    if isinstance(fastapi_child_scope, dict):
        _get_fastapi_scope(scope).update(fastapi_child_scope)


def _get_scope_effective_route_context(scope: Scope) -> Any | None:
    return scope.get(_FASTAPI_SCOPE_KEY, {}).get(_FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY)


def _get_scope_included_router(scope: Scope) -> Any | None:
    return scope.get(_FASTAPI_SCOPE_KEY, {}).get(_FASTAPI_INCLUDED_ROUTER_KEY)


def _frontend_scope_specificity(scope: Scope) -> int | None:
    specificity = scope.get(_FASTAPI_SCOPE_KEY, {}).get(
        _FASTAPI_FRONTEND_SPECIFICITY_KEY
    )
    if isinstance(specificity, int):
        return specificity
    return None


def _restore_fastapi_scope_key(scope: Scope, key: str, previous: Any) -> None:
    fastapi_scope = scope.get(_FASTAPI_SCOPE_KEY)
    if not isinstance(fastapi_scope, dict):
        return
    if previous is _SCOPE_MISSING:
        fastapi_scope.pop(key, None)
    else:
        fastapi_scope[key] = previous


class _APIRouteLike(Protocol):
    path: str
    endpoint: Callable[..., Any]
    stream_item_type: Any | None
    response_model: Any
    summary: str | None
    response_description: str
    deprecated: bool | None
    operation_id: str | None
    response_model_include: IncEx | None
    response_model_exclude: IncEx | None
    response_model_by_alias: bool
    response_model_exclude_unset: bool
    response_model_exclude_defaults: bool
    response_model_exclude_none: bool
    include_in_schema: bool
    response_class: type[Response] | DefaultPlaceholder
    dependency_overrides_provider: Any | None
    callbacks: list[BaseRoute] | None
    openapi_extra: dict[str, Any] | None
    generate_unique_id_function: Callable[[Any], str] | DefaultPlaceholder
    strict_content_type: bool | DefaultPlaceholder
    tags: list[str | Enum]
    responses: dict[int | str, dict[str, Any]]
    name: str
    path_regex: Any
    path_format: str
    param_convertors: dict[str, Any]
    methods: set[str]
    unique_id: str
    status_code: int | None
    response_field: ModelField | None
    stream_item_field: ModelField | None
    dependencies: list[params.Depends]
    description: str
    response_fields: dict[int | str, ModelField]
    dependant: Dependant
    _embed_body_fields: bool
    body_field: ModelField | None
    is_sse_stream: bool
    is_json_stream: bool


def _populate_api_route_state(
    route: _APIRouteLike,
    path: str,
    endpoint: Callable[..., Any],
    *,
    response_model: Any = Default(None),
    status_code: int | None = None,
    tags: list[str | Enum] | None = None,
    dependencies: Sequence[params.Depends] | None = None,
    summary: str | None = None,
    description: str | None = None,
    response_description: str = "Successful Response",
    responses: dict[int | str, dict[str, Any]] | None = None,
    deprecated: bool | None = None,
    name: str | None = None,
    methods: set[str] | list[str] | None = None,
    operation_id: str | None = None,
    response_model_include: IncEx | None = None,
    response_model_exclude: IncEx | None = None,
    response_model_by_alias: bool = True,
    response_model_exclude_unset: bool = False,
    response_model_exclude_defaults: bool = False,
    response_model_exclude_none: bool = False,
    include_in_schema: bool = True,
    response_class: type[Response] | DefaultPlaceholder = Default(JSONResponse),
    dependency_overrides_provider: Any | None = None,
    callbacks: list[BaseRoute] | None = None,
    openapi_extra: dict[str, Any] | None = None,
    generate_unique_id_function: Callable[[Any], str] | DefaultPlaceholder = Default(
        generate_unique_id
    ),
    strict_content_type: bool | DefaultPlaceholder = Default(True),
    stream_item_type: Any | None = None,
) -> None:
    route.path = path
    route.endpoint = endpoint
    route.stream_item_type = stream_item_type
    route.summary = summary
    route.response_description = response_description
    route.deprecated = deprecated
    route.operation_id = operation_id
    route.response_model_include = response_model_include
    route.response_model_exclude = response_model_exclude
    route.response_model_by_alias = response_model_by_alias
    route.response_model_exclude_unset = response_model_exclude_unset
    route.response_model_exclude_defaults = response_model_exclude_defaults
    route.response_model_exclude_none = response_model_exclude_none
    route.include_in_schema = include_in_schema
    route.response_class = response_class
    route.dependency_overrides_provider = dependency_overrides_provider
    route.callbacks = callbacks
    route.openapi_extra = openapi_extra
    route.generate_unique_id_function = generate_unique_id_function
    route.strict_content_type = strict_content_type
    route.tags = tags or []
    route.responses = responses or {}
    route.name = get_name(endpoint) if name is None else name
    route.path_regex, route.path_format, route.param_convertors = compile_path(path)
    if methods is None:
        methods = ["GET"]
    route.methods = {method.upper() for method in methods}
    if isinstance(generate_unique_id_function, DefaultPlaceholder):
        current_generate_unique_id: Callable[[Any], str] = (
            generate_unique_id_function.value
        )
    else:
        current_generate_unique_id = generate_unique_id_function
    route.unique_id = route.operation_id or current_generate_unique_id(route)
    # normalize enums e.g. http.HTTPStatus
    if isinstance(status_code, IntEnum):
        status_code = int(status_code)
    route.status_code = status_code
    route.dependencies = list(dependencies or [])
    route.description = description or inspect.cleandoc(route.endpoint.__doc__ or "")
    # if a "form feed" character (page break) is found in the description text,
    # truncate description text to the content preceding the first "form feed"
    route.description = route.description.split("\f")[0].strip()
    response_fields = {}
    for additional_status_code, response in route.responses.items():
        assert isinstance(response, dict), "An additional response must be a dict"
        model = response.get("model")
        if model:
            assert is_body_allowed_for_status_code(additional_status_code), (
                f"Status code {additional_status_code} must not have a response body"
            )
            response_name = f"Response_{additional_status_code}_{route.unique_id}"
            response_field = create_model_field(
                name=response_name, type_=model, mode="serialization"
            )
            response_fields[additional_status_code] = response_field
    if response_fields:
        route.response_fields = response_fields
    else:
        route.response_fields = {}

    assert callable(endpoint), "An endpoint must be a callable"
    (
        route.dependant,
        body_params,
        route._embed_body_fields,
    ) = _build_dependant_with_parameterless_dependencies(
        path=route.path_format,
        call=route.endpoint,
        dependencies=route.dependencies,
    )
    route.body_field = _get_body_field(
        body_params=body_params,
        name=route.unique_id,
        embed_body_fields=route._embed_body_fields,
    )
    # Detect generator endpoints that should stream as JSONL or SSE
    is_generator = _is_async_gen_callable(route.dependant.call) or _is_gen_callable(
        route.dependant.call
    )
    route.is_sse_stream = is_generator and lenient_issubclass(
        response_class, EventSourceResponse
    )
    route.is_json_stream = is_generator and isinstance(
        response_class, DefaultPlaceholder
    )
    if isinstance(response_model, DefaultPlaceholder):
        return_annotation = get_typed_return_annotation(endpoint)
        if lenient_issubclass(return_annotation, Response):
            response_model = None
        else:
            stream_item = get_stream_item_type(return_annotation)
            if stream_item is not None and is_generator:
                # Extract item type for JSONL or SSE streaming for
                # generator endpoints when response_class is
                # DefaultPlaceholder (JSONL) or EventSourceResponse (SSE).
                # ServerSentEvent is excluded: it's a transport
                # wrapper, not a data model, so it shouldn't feed
                # into validation or OpenAPI schema generation.
                if (
                    isinstance(response_class, DefaultPlaceholder)
                    or lenient_issubclass(response_class, EventSourceResponse)
                ) and not lenient_issubclass(stream_item, ServerSentEvent):
                    route.stream_item_type = stream_item
                response_model = None
            else:
                response_model = return_annotation
    route.response_model = response_model
    if route.response_model:
        assert is_body_allowed_for_status_code(status_code), (
            f"Status code {status_code} must not have a response body"
        )
        response_name = "Response_" + route.unique_id
        route.response_field = create_model_field(
            name=response_name,
            type_=route.response_model,
            mode="serialization",
        )
    else:
        route.response_field = None
    if route.stream_item_type:
        stream_item_name = "StreamItem_" + route.unique_id
        route.stream_item_field = create_model_field(
            name=stream_item_name,
            type_=route.stream_item_type,
            mode="serialization",
        )
    else:
        route.stream_item_field = None


class APIRoute(routing.Route):
    stream_item_type: Any | None
    response_model: Any
    summary: str | None
    response_description: str
    deprecated: bool | None
    operation_id: str | None
    response_model_include: IncEx | None
    response_model_exclude: IncEx | None
    response_model_by_alias: bool
    response_model_exclude_unset: bool
    response_model_exclude_defaults: bool
    response_model_exclude_none: bool
    include_in_schema: bool
    response_class: type[Response] | DefaultPlaceholder
    dependency_overrides_provider: Any | None
    callbacks: list[BaseRoute] | None
    openapi_extra: dict[str, Any] | None
    generate_unique_id_function: Callable[[Any], str] | DefaultPlaceholder
    strict_content_type: bool | DefaultPlaceholder
    tags: list[str | Enum]
    responses: dict[int | str, dict[str, Any]]
    unique_id: str
    status_code: int | None
    response_field: ModelField | None
    stream_item_field: ModelField | None
    dependencies: list[params.Depends]
    description: str
    response_fields: dict[int | str, ModelField]
    dependant: Dependant
    _embed_body_fields: bool
    body_field: ModelField | None
    is_sse_stream: bool
    is_json_stream: bool

    def __init__(
        self,
        path: str,
        endpoint: Callable[..., Any],
        *,
        response_model: Any = Default(None),
        status_code: int | None = None,
        tags: list[str | Enum] | None = None,
        dependencies: Sequence[params.Depends] | None = None,
        summary: str | None = None,
        description: str | None = None,
        response_description: str = "Successful Response",
        responses: dict[int | str, dict[str, Any]] | None = None,
        deprecated: bool | None = None,
        name: str | None = None,
        methods: set[str] | list[str] | None = None,
        operation_id: str | None = None,
        response_model_include: IncEx | None = None,
        response_model_exclude: IncEx | None = None,
        response_model_by_alias: bool = True,
        response_model_exclude_unset: bool = False,
        response_model_exclude_defaults: bool = False,
        response_model_exclude_none: bool = False,
        include_in_schema: bool = True,
        response_class: type[Response] | DefaultPlaceholder = Default(JSONResponse),
        dependency_overrides_provider: Any | None = None,
        callbacks: list[BaseRoute] | None = None,
        openapi_extra: dict[str, Any] | None = None,
        generate_unique_id_function: Callable[["APIRoute"], str]
        | DefaultPlaceholder = Default(generate_unique_id),
        strict_content_type: bool | DefaultPlaceholder = Default(True),
    ) -> None:
        _populate_api_route_state(
            cast(_APIRouteLike, self),
            path,
            endpoint,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            name=name,
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
            dependency_overrides_provider=dependency_overrides_provider,
            callbacks=callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=generate_unique_id_function,
            strict_content_type=strict_content_type,
        )
        self.app = request_response(self.get_route_handler())

    def get_route_handler(self) -> Callable[[Request], Coroutine[Any, Any, Response]]:
        route = cast(_APIRouteLike, self)
        # TODO: Replace or deprecate this no-scope hook so included-route
        # effective context can be passed explicitly instead of via ContextVar.
        effective_context = _effective_route_context_var.get()
        if effective_context is not None and effective_context.original_route is self:
            route = cast(_APIRouteLike, effective_context)
        return get_request_handler(
            dependant=route.dependant,
            body_field=route.body_field,
            status_code=route.status_code,
            response_class=route.response_class,
            response_field=route.response_field,
            response_model_include=route.response_model_include,
            response_model_exclude=route.response_model_exclude,
            response_model_by_alias=route.response_model_by_alias,
            response_model_exclude_unset=route.response_model_exclude_unset,
            response_model_exclude_defaults=route.response_model_exclude_defaults,
            response_model_exclude_none=route.response_model_exclude_none,
            dependency_overrides_provider=route.dependency_overrides_provider,
            embed_body_fields=route._embed_body_fields,
            strict_content_type=route.strict_content_type,
            stream_item_field=route.stream_item_field,
            is_json_stream=route.is_json_stream,
        )

    def matches(self, scope: Scope) -> tuple[Match, Scope]:
        effective_context = _get_scope_effective_route_context(scope)
        if effective_context is not None and effective_context.original_route is self:
            match, child_scope = effective_context.matches(scope)
        else:
            match, child_scope = super().matches(scope)
        if match != Match.NONE:
            child_scope["route"] = self
        return match, child_scope

    async def handle(self, scope: Scope, receive: Receive, send: Send) -> None:
        effective_context = _get_scope_effective_route_context(scope)
        if effective_context is not None and effective_context.original_route is self:
            methods = effective_context.methods
            if methods and scope["method"] not in methods:
                headers = {"Allow": ", ".join(methods)}
                if "app" in scope:
                    raise HTTPException(status_code=405, headers=headers)
                response = PlainTextResponse(
                    "Method Not Allowed", status_code=405, headers=headers
                )
                await response(scope, receive, send)
                return
            token = _effective_route_context_var.set(effective_context)
            try:
                app = request_response(self.get_route_handler())
            finally:
                _effective_route_context_var.reset(token)
            await app(scope, receive, send)
            return
        await super().handle(scope, receive, send)


@dataclass
class _RouterIncludeContext:
    included_router: "APIRouter"
    prefix: str = ""
    tags: list[str | Enum] = field(default_factory=list)
    dependencies: list[params.Depends] = field(default_factory=list)
    default_response_class: type[Response] | DefaultPlaceholder = field(
        default_factory=lambda: Default(JSONResponse)
    )
    responses: dict[int | str, dict[str, Any]] = field(default_factory=dict)
    callbacks: list[BaseRoute] = field(default_factory=list)
    deprecated: bool | None = None
    include_in_schema: bool = True
    generate_unique_id_function: Callable[[APIRoute], str] | DefaultPlaceholder = field(
        default_factory=lambda: Default(generate_unique_id)
    )
    strict_content_type: bool | DefaultPlaceholder = field(
        default_factory=lambda: Default(True)
    )
    dependency_overrides_provider: Any | None = None

    @classmethod
    def for_include(
        cls,
        *,
        parent_router: "APIRouter",
        included_router: "APIRouter",
        prefix: str = "",
        tags: list[str | Enum] | None = None,
        dependencies: Sequence[params.Depends] | None = None,
        default_response_class: type[Response] | DefaultPlaceholder = Default(
            JSONResponse
        ),
        responses: dict[int | str, dict[str, Any]] | None = None,
        callbacks: list[BaseRoute] | None = None,
        deprecated: bool | None = None,
        include_in_schema: bool = True,
        generate_unique_id_function: Callable[[APIRoute], str]
        | DefaultPlaceholder = Default(generate_unique_id),
    ) -> "_RouterIncludeContext":
        return cls(
            included_router=included_router,
            prefix=parent_router.prefix + prefix,
            tags=[*parent_router.tags, *(tags or [])],
            dependencies=[*parent_router.dependencies, *(dependencies or [])],
            default_response_class=get_value_or_default(
                default_response_class, parent_router.default_response_class
            ),
            responses={**parent_router.responses, **(responses or {})},
            callbacks=[*parent_router.callbacks, *(callbacks or [])],
            deprecated=deprecated or parent_router.deprecated,
            include_in_schema=parent_router.include_in_schema and include_in_schema,
            generate_unique_id_function=get_value_or_default(
                generate_unique_id_function, parent_router.generate_unique_id_function
            ),
            strict_content_type=parent_router.strict_content_type,
            dependency_overrides_provider=parent_router.dependency_overrides_provider,
        )

    def combine(
        self, child_context: "_RouterIncludeContext"
    ) -> "_RouterIncludeContext":
        return _RouterIncludeContext(
            included_router=child_context.included_router,
            prefix=self.prefix + child_context.prefix,
            tags=[*self.tags, *child_context.tags],
            dependencies=[*self.dependencies, *child_context.dependencies],
            default_response_class=get_value_or_default(
                child_context.default_response_class, self.default_response_class
            ),
            responses={**self.responses, **child_context.responses},
            callbacks=[*self.callbacks, *child_context.callbacks],
            deprecated=self.deprecated or child_context.deprecated,
            include_in_schema=self.include_in_schema
            and child_context.include_in_schema,
            generate_unique_id_function=get_value_or_default(
                child_context.generate_unique_id_function,
                self.generate_unique_id_function,
            ),
            strict_content_type=get_value_or_default(
                child_context.strict_content_type, self.strict_content_type
            ),
            dependency_overrides_provider=self.dependency_overrides_provider,
        )

    def path_for(self, route: _RouteWithPath) -> str:
        return self.prefix + route.path


@dataclass
class _EffectiveRouteContext:
    original_route: BaseRoute
    starlette_route: BaseRoute | None = None
    frontend_prefix: str = ""
    path: str = ""
    endpoint: Callable[..., Any] | None = None
    stream_item_type: Any | None = None
    response_model: Any = None
    summary: str | None = None
    response_description: str = "Successful Response"
    deprecated: bool | None = None
    operation_id: str | None = None
    response_model_include: IncEx | None = None
    response_model_exclude: IncEx | None = None
    response_model_by_alias: bool = True
    response_model_exclude_unset: bool = False
    response_model_exclude_defaults: bool = False
    response_model_exclude_none: bool = False
    include_in_schema: bool = True
    response_class: type[Response] | DefaultPlaceholder = field(
        default_factory=lambda: Default(JSONResponse)
    )
    dependency_overrides_provider: Any | None = None
    callbacks: list[BaseRoute] | None = None
    openapi_extra: dict[str, Any] | None = None
    generate_unique_id_function: Callable[[Any], str] | DefaultPlaceholder = field(
        default_factory=lambda: Default(generate_unique_id)
    )
    strict_content_type: bool | DefaultPlaceholder = field(
        default_factory=lambda: Default(True)
    )
    tags: list[str | Enum] = field(default_factory=list)
    responses: dict[int | str, dict[str, Any]] = field(default_factory=dict)
    name: str = ""
    path_regex: Any = None
    path_format: str = ""
    param_convertors: dict[str, Any] = field(default_factory=dict)
    methods: set[str] = field(default_factory=set)
    unique_id: str = ""
    status_code: int | None = None
    response_field: ModelField | None = None
    stream_item_field: ModelField | None = None
    dependencies: list[params.Depends] = field(default_factory=list)
    description: str = ""
    response_fields: dict[int | str, ModelField] = field(default_factory=dict)
    dependant: Dependant | None = None
    _embed_body_fields: bool = False
    body_field: ModelField | None = None
    is_sse_stream: bool = False
    is_json_stream: bool = False

    @classmethod
    def from_api_route(
        cls,
        *,
        original_route: APIRoute,
        include_context: _RouterIncludeContext,
    ) -> "_EffectiveRouteContext":
        route = cast(_APIRouteLike, original_route)
        context = cls(original_route=original_route)
        _populate_api_route_state(
            cast(_APIRouteLike, context),
            include_context.path_for(original_route),
            route.endpoint,
            response_model=route.response_model,
            status_code=route.status_code,
            tags=[*include_context.tags, *route.tags],
            dependencies=[*include_context.dependencies, *route.dependencies],
            summary=route.summary,
            description=route.description,
            response_description=route.response_description,
            responses={**include_context.responses, **route.responses},
            deprecated=route.deprecated or include_context.deprecated,
            methods=route.methods,
            operation_id=route.operation_id,
            response_model_include=route.response_model_include,
            response_model_exclude=route.response_model_exclude,
            response_model_by_alias=route.response_model_by_alias,
            response_model_exclude_unset=route.response_model_exclude_unset,
            response_model_exclude_defaults=route.response_model_exclude_defaults,
            response_model_exclude_none=route.response_model_exclude_none,
            include_in_schema=route.include_in_schema
            and include_context.include_in_schema,
            response_class=get_value_or_default(
                route.response_class,
                include_context.included_router.default_response_class,
                include_context.default_response_class,
            ),
            name=route.name,
            dependency_overrides_provider=include_context.dependency_overrides_provider,
            callbacks=[*include_context.callbacks, *(route.callbacks or [])],
            openapi_extra=route.openapi_extra,
            generate_unique_id_function=get_value_or_default(
                route.generate_unique_id_function,
                include_context.included_router.generate_unique_id_function,
                include_context.generate_unique_id_function,
            ),
            strict_content_type=get_value_or_default(
                route.strict_content_type,
                include_context.included_router.strict_content_type,
                include_context.strict_content_type,
            ),
            stream_item_type=route.stream_item_type,
        )
        return context

    @classmethod
    def from_frontend_route_group(
        cls,
        *,
        original_route: "_FrontendRouteGroup",
        include_context: _RouterIncludeContext,
    ) -> "_EffectiveRouteContext":
        dependencies = [*include_context.dependencies, *original_route.dependencies]
        context = cls(
            original_route=original_route,
            frontend_prefix=include_context.prefix,
            dependencies=dependencies,
            dependency_overrides_provider=include_context.dependency_overrides_provider,
        )
        (
            context.dependant,
            _,
            context._embed_body_fields,
        ) = _build_dependant_with_parameterless_dependencies(
            path="",
            call=_frontend_dependency_endpoint,
            dependencies=dependencies,
        )
        return context

    def matches(self, scope: Scope) -> tuple[Match, Scope]:
        if isinstance(self.original_route, _FrontendRouteGroup):
            return self.original_route.matches_with_prefix(scope, self.frontend_prefix)
        if not isinstance(self.original_route, APIRoute):
            assert self.starlette_route is not None
            return self.starlette_route.matches(scope)
        if scope["type"] != "http":
            return Match.NONE, {}
        route_path = get_route_path(scope)
        match = self.path_regex.match(route_path)
        if not match:
            return Match.NONE, {}
        matched_params = match.groupdict()
        for key, value in matched_params.items():
            matched_params[key] = self.param_convertors[key].convert(value)
        path_params = dict(scope.get("path_params", {}))
        path_params.update(matched_params)
        child_scope = {"endpoint": self.endpoint, "path_params": path_params}
        methods = self.methods
        if methods and scope["method"] not in methods:
            return Match.PARTIAL, child_scope
        return Match.FULL, child_scope

    def url_path_for(self, name: str, /, **path_params: Any) -> Any:
        if not isinstance(self.original_route, APIRoute):
            assert self.starlette_route is not None
            return self.starlette_route.url_path_for(name, **path_params)
        seen_params = set(path_params.keys())
        param_convertors = self.param_convertors
        expected_params = set(param_convertors.keys())
        if name != self.name or seen_params != expected_params:
            raise routing.NoMatchFound(name, path_params)
        path, remaining_params = routing.replace_params(
            self.path_format, param_convertors, path_params
        )
        assert not remaining_params
        return URLPath(path=path, protocol="http")


@dataclass(frozen=True)
class RouteContext:
    route: BaseRoute
    _route_context: _EffectiveRouteContext | None = field(default=None, repr=False)

    @property
    def original_route(self) -> BaseRoute:
        if self._route_context is not None:
            return self._route_context.original_route
        return self.route

    @property
    def _effective_route(self) -> BaseRoute | _EffectiveRouteContext:
        if self._route_context is not None:
            return self._route_context
        return self.route

    @property
    def path(self) -> str | None:
        return getattr(self._effective_route, "path", None)

    @property
    def path_format(self) -> str | None:
        return getattr(self._effective_route, "path_format", None)

    @property
    def name(self) -> str | None:
        return getattr(self._effective_route, "name", None)

    @property
    def methods(self) -> set[str] | None:
        return getattr(self._effective_route, "methods", None)

    @property
    def endpoint(self) -> Callable[..., Any] | None:
        return getattr(self._effective_route, "endpoint", None)

    def __getattr__(self, name: str) -> Any:
        return getattr(self._effective_route, name)


@dataclass
class _IncludedRouter(BaseRoute):
    original_router: "APIRouter"
    include_context: _RouterIncludeContext
    _effective_routes_lock: Any = field(
        default_factory=threading.Lock, repr=False, compare=False
    )
    _effective_candidates: list["_EffectiveRouteContext | _IncludedRouter"] = field(
        default_factory=list
    )
    _effective_candidates_version: int | None = None
    _effective_low_priority_routes: list["_EffectiveRouteContext"] = field(
        default_factory=list
    )
    _effective_low_priority_routes_version: int | None = None

    def effective_candidates(self) -> list["_EffectiveRouteContext | _IncludedRouter"]:
        routes_version = self.original_router._get_routes_version()
        if routes_version == self._effective_candidates_version:
            return self._effective_candidates
        with self._effective_routes_lock:
            routes_version = self.original_router._get_routes_version()
            if routes_version == self._effective_candidates_version:
                return self._effective_candidates
            effective_candidates: list[_EffectiveRouteContext | _IncludedRouter] = []
            for route in self.original_router.routes:
                if isinstance(route, _IncludedRouter):
                    child_context = self.include_context.combine(route.include_context)
                    child_branch = _IncludedRouter(
                        original_router=route.original_router,
                        include_context=child_context,
                    )
                    effective_candidates.append(child_branch)
                    continue
                route_context = self._build_effective_context(route)
                if route_context is not None:
                    effective_candidates.append(route_context)
            self._effective_candidates = effective_candidates
            self._effective_candidates_version = routes_version
            return effective_candidates

    def effective_low_priority_routes(self) -> list["_EffectiveRouteContext"]:
        routes_version = self.original_router._get_routes_version()
        if routes_version == self._effective_low_priority_routes_version:
            return self._effective_low_priority_routes
        with self._effective_routes_lock:
            routes_version = self.original_router._get_routes_version()
            if routes_version == self._effective_low_priority_routes_version:
                return self._effective_low_priority_routes
            effective_low_priority_routes: list[_EffectiveRouteContext] = []
            for route in self.original_router._low_priority_routes:
                route_context = self._build_effective_context(route)
                if route_context is not None:
                    effective_low_priority_routes.append(route_context)
            for route in self.original_router.routes:
                if isinstance(route, _IncludedRouter):
                    child_context = self.include_context.combine(route.include_context)
                    child_branch = _IncludedRouter(
                        original_router=route.original_router,
                        include_context=child_context,
                    )
                    effective_low_priority_routes.extend(
                        child_branch.effective_low_priority_routes()
                    )
            self._effective_low_priority_routes = effective_low_priority_routes
            self._effective_low_priority_routes_version = routes_version
            return effective_low_priority_routes

    def _build_effective_context(
        self, route: BaseRoute
    ) -> _EffectiveRouteContext | None:
        if isinstance(route, APIRoute):
            return _EffectiveRouteContext.from_api_route(
                original_route=route,
                include_context=self.include_context,
            )
        if isinstance(route, _FrontendRouteGroup):
            return _EffectiveRouteContext.from_frontend_route_group(
                original_route=route,
                include_context=self.include_context,
            )
        if isinstance(route, routing.Route):
            starlette_route: BaseRoute = routing.Route(
                self.include_context.path_for(route),
                endpoint=route.endpoint,
                methods=list(route.methods or []),
                name=route.name,
                include_in_schema=route.include_in_schema,
            )
            return _EffectiveRouteContext(
                original_route=route,
                starlette_route=starlette_route,
            )
        if isinstance(route, APIWebSocketRoute):
            starlette_route = APIWebSocketRoute(
                self.include_context.path_for(route),
                endpoint=route.endpoint,
                name=route.name,
                dependencies=[*self.include_context.dependencies, *route.dependencies],
                dependency_overrides_provider=(
                    self.include_context.dependency_overrides_provider
                ),
            )
            return _EffectiveRouteContext(
                original_route=route,
                starlette_route=starlette_route,
            )
        if isinstance(route, routing.WebSocketRoute):
            starlette_route = routing.WebSocketRoute(
                self.include_context.path_for(route), route.endpoint, name=route.name
            )
            return _EffectiveRouteContext(
                original_route=route,
                starlette_route=starlette_route,
            )
        if isinstance(route, routing.Mount):
            starlette_route = copy.copy(route)
            starlette_route.path = self.include_context.path_for(route).rstrip("/")
            (
                starlette_route.path_regex,
                starlette_route.path_format,
                starlette_route.param_convertors,
            ) = compile_path(starlette_route.path + "/{path:path}")
            return _EffectiveRouteContext(
                original_route=route,
                starlette_route=starlette_route,
            )
        if isinstance(route, routing.Host):
            if self.include_context.prefix:
                prefixed_app: ASGIApp = routing.Router(
                    routes=[routing.Mount(self.include_context.prefix, app=route.app)]
                )
            else:
                prefixed_app = route.app
            starlette_route = routing.Host(
                route.host, app=prefixed_app, name=route.name
            )
            return _EffectiveRouteContext(
                original_route=route,
                starlette_route=starlette_route,
            )
        return None

    def _match(
        self, scope: Scope
    ) -> tuple[Match, Scope, BaseRoute | None, _EffectiveRouteContext | None]:
        partial: tuple[Scope, BaseRoute, _EffectiveRouteContext | None] | None = None
        for candidate in self.effective_candidates():
            if isinstance(candidate, _IncludedRouter):
                match, child_scope = candidate.matches(scope)
                route: BaseRoute = candidate
                route_context = None
            elif isinstance(candidate.original_route, APIRoute):
                route_context = candidate
                fastapi_scope = _get_fastapi_scope(scope)
                previous_context = fastapi_scope.get(
                    _FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY, _SCOPE_MISSING
                )
                fastapi_scope[_FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY] = route_context
                try:
                    match, child_scope = candidate.original_route.matches(scope)
                finally:
                    _restore_fastapi_scope_key(
                        scope, _FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY, previous_context
                    )
                route = candidate.original_route
            else:
                route_context = candidate
                match, child_scope = candidate.matches(scope)
                route = candidate.starlette_route or candidate.original_route
            if match == Match.FULL:
                return match, child_scope, route, route_context
            if match == Match.PARTIAL and partial is None:
                partial = (child_scope, route, route_context)
        if partial is not None:
            child_scope, route, route_context = partial
            return Match.PARTIAL, child_scope, route, route_context
        return Match.NONE, {}, None, None

    def matches(self, scope: Scope) -> tuple[Match, Scope]:
        fastapi_scope = _get_fastapi_scope(scope)
        previous_router = fastapi_scope.get(
            _FASTAPI_INCLUDED_ROUTER_KEY, _SCOPE_MISSING
        )
        fastapi_scope[_FASTAPI_INCLUDED_ROUTER_KEY] = self
        try:
            match, _ = self.original_router.matches(scope)
            return match, {}
        finally:
            _restore_fastapi_scope_key(
                scope, _FASTAPI_INCLUDED_ROUTER_KEY, previous_router
            )

    async def handle(self, scope: Scope, receive: Receive, send: Send) -> None:
        _get_fastapi_scope(scope)[_FASTAPI_INCLUDED_ROUTER_KEY] = self
        await self.original_router.handle(scope, receive, send)

    async def _handle_selected(
        self, scope: Scope, receive: Receive, send: Send
    ) -> None:
        match, child_scope, route, effective_context = self._match(scope)
        if match == Match.NONE or route is None:
            await self.original_router.default(scope, receive, send)
            return
        scope.update(child_scope)
        if isinstance(route, _IncludedRouter):
            await route.handle(scope, receive, send)
            return
        if effective_context is not None:
            _get_fastapi_scope(scope)[_FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY] = (
                effective_context
            )
            original_route = effective_context.original_route
            if isinstance(original_route, APIRoute):
                scope["route"] = original_route
                await original_route.handle(scope, receive, send)
                return
        await route.handle(scope, receive, send)

    def effective_route_contexts(self) -> Iterator[_EffectiveRouteContext]:
        for candidate in self.effective_candidates():
            if isinstance(candidate, _IncludedRouter):
                yield from candidate.effective_route_contexts()
            else:
                yield candidate

    def url_path_for(self, name: str, /, **path_params: Any) -> Any:
        for route_context in self.effective_route_contexts():
            try:
                return route_context.url_path_for(name, **path_params)
            except routing.NoMatchFound:
                pass
        raise routing.NoMatchFound(name, path_params)


def _iter_included_route_candidates(routes: Sequence[BaseRoute]) -> Iterator[BaseRoute]:
    for route, route_context in _iter_routes_with_context(routes):
        if route_context is not None and route_context.starlette_route is not None:
            yield route_context.starlette_route
        else:
            yield route


def iter_route_contexts(
    routes: Sequence[BaseRoute | RouteContext],
) -> Iterator[RouteContext]:
    for route in routes:
        if isinstance(route, RouteContext):
            yield route
            continue
        for original_route, route_context in _iter_routes_with_context([route]):
            if route_context is None:
                yield RouteContext(original_route)
            else:
                yield RouteContext(original_route, route_context)


def _iter_routes_with_context(
    routes: Sequence[BaseRoute],
) -> Iterator[tuple[BaseRoute, _EffectiveRouteContext | None]]:
    for route in routes:
        if isinstance(route, _IncludedRouter):
            for route_context in route.effective_route_contexts():
                yield route_context.original_route, route_context
        else:
            yield route, None


def _normalize_frontend_path(path: str) -> str:
    if not path:
        raise AssertionError("A frontend path cannot be empty")
    if not path.startswith("/"):
        raise AssertionError("A frontend path must start with '/'")
    if path != "/":
        path = path.rstrip("/")
    return path


def _join_frontend_paths(prefix: str, path: str) -> str:
    if not prefix:
        return path
    if path == "/":
        return prefix
    return prefix + path


def _frontend_path_specificity(path: str) -> int:
    if path == "/":
        return 0
    return len(path)


def _get_resolved_absolute_path(path: str | os.PathLike[str]) -> str:
    return os.path.realpath(os.fspath(path))


def _resolve_frontend_check_dir(
    *,
    directory: str | os.PathLike[str],
    check_dir: bool | Literal["auto"],
) -> bool:
    if check_dir != "auto":
        return check_dir
    if os.environ.get("FASTAPI_ENV") != "development":
        return True
    if not os.path.isdir(directory):
        warnings.warn(
            f"Frontend directory '{directory}' does not exist. "
            f"Resolved absolute path: '{_get_resolved_absolute_path(directory)}'",
            stacklevel=3,
        )
    return False


class _FrontendStaticFiles(StaticFiles):
    def __init__(
        self,
        *,
        directory: str | os.PathLike[str],
        fallback: Literal["auto", "index.html", "404.html"] | None,
        check_dir: bool,
    ) -> None:
        self.fallback = fallback
        if check_dir and not os.path.isdir(directory):
            raise RuntimeError(
                f"Frontend directory '{directory}' does not exist. "
                f"Resolved absolute path: '{_get_resolved_absolute_path(directory)}'"
            )
        super().__init__(
            directory=directory,
            html=True,
            check_dir=check_dir,
            follow_symlink=False,
        )
        if check_dir and fallback in {"index.html", "404.html"}:
            self._check_fallback_file(fallback)

    def _check_fallback_file(self, fallback: str) -> None:
        _, stat_result = self.lookup_path(fallback)
        if stat_result is None or not stat.S_ISREG(stat_result.st_mode):
            raise RuntimeError(
                f"Frontend fallback file '{fallback}' does not exist in "
                f"directory '{self.directory}'. Resolved absolute directory: "
                f"'{self._get_resolved_directory()}'"
            )

    def _get_resolved_directory(self) -> str:
        assert self.directory is not None
        return _get_resolved_absolute_path(self.directory)

    def get_path(self, scope: Scope) -> str:
        path = _get_fastapi_scope(scope).get(_FASTAPI_FRONTEND_PATH_KEY, "")
        assert isinstance(path, str)
        return os.path.normpath(os.path.join(*path.split("/")))

    async def get_response_for_scope(self, scope: Scope) -> Response:
        if not self.config_checked:
            await self.check_config()
            self.config_checked = True
        return await self.get_response(self.get_path(scope), scope)

    async def get_response(self, path: str, scope: Scope) -> Response:
        if scope["method"] not in ("GET", "HEAD"):
            if await self._lookup_static_resource(path) is not None:
                raise HTTPException(status_code=405)
            raise HTTPException(status_code=404)

        static_resource = await self._lookup_static_resource(path)
        if static_resource is not None:
            full_path, stat_result, is_directory_index = static_resource
            if is_directory_index and not scope["path"].endswith("/"):
                url = URL(scope=scope)
                url = url.replace(path=url.path + "/")
                return RedirectResponse(url=url)
            return self.file_response(full_path, stat_result, scope)

        if self.fallback == "404.html" or (
            self.fallback == "auto" and self._fallback_file_exists("404.html")
        ):
            return await self._fallback_response("404.html", scope, status_code=404)

        if (
            self.fallback == "index.html"
            or (self.fallback == "auto" and self._fallback_file_exists("index.html"))
        ) and _is_frontend_navigation_request(scope):
            return await self._fallback_response("index.html", scope, status_code=200)

        raise HTTPException(status_code=404)

    async def _lookup_path(self, path: str) -> tuple[str, os.stat_result | None]:
        try:
            return await run_in_threadpool(self.lookup_path, path)
        except PermissionError:
            raise HTTPException(status_code=401) from None
        except OSError as exc:
            if exc.errno == errno.ENAMETOOLONG:
                raise HTTPException(status_code=404) from None
            raise exc
        except ValueError:
            raise HTTPException(status_code=404) from None

    async def _lookup_static_resource(
        self, path: str
    ) -> tuple[str, os.stat_result, bool] | None:
        full_path, stat_result = await self._lookup_path(path)
        if stat_result is None:
            return None
        if stat.S_ISREG(stat_result.st_mode):
            return full_path, stat_result, False
        if stat.S_ISDIR(stat_result.st_mode):
            index_path = os.path.join(path, "index.html")
            full_path, stat_result = await self._lookup_path(index_path)
            if stat_result is not None and stat.S_ISREG(stat_result.st_mode):
                return full_path, stat_result, True
        return None

    def _fallback_file_exists(self, fallback: str) -> bool:
        _, stat_result = self.lookup_path(fallback)
        return stat_result is not None and stat.S_ISREG(stat_result.st_mode)

    async def _fallback_response(
        self, fallback: str, scope: Scope, *, status_code: int
    ) -> Response:
        full_path, stat_result = await run_in_threadpool(self.lookup_path, fallback)
        if stat_result is None or not stat.S_ISREG(stat_result.st_mode):
            raise RuntimeError(
                f"Frontend fallback file '{fallback}' does not exist in "
                f"directory '{self.directory}'. Resolved absolute directory: "
                f"'{self._get_resolved_directory()}'"
            )
        return self.file_response(
            full_path, stat_result, scope, status_code=status_code
        )


def _iter_accept_media_types(accept: str) -> Iterator[tuple[str, float]]:
    for raw_value in accept.split(","):
        message = email.message.Message()
        message["content-type"] = raw_value.strip()
        q = message.get_param("q")
        quality = 1.0
        if isinstance(q, str):
            try:
                quality = float(q)
            except ValueError:
                pass
        yield (
            f"{message.get_content_maintype()}/{message.get_content_subtype()}",
            quality,
        )


def _is_frontend_navigation_request(scope: Scope) -> bool:
    request = Request(scope)
    for media_type, quality in _iter_accept_media_types(
        request.headers.get("accept", "")
    ):
        if media_type in {"text/html", "application/xhtml+xml"} and quality != 0:
            return True
    return False


class _FrontendRoute(BaseRoute):
    def __init__(
        self,
        path: str,
        *,
        directory: str | os.PathLike[str],
        fallback: Literal["auto", "index.html", "404.html"] | None = "auto",
        check_dir: bool,
    ) -> None:
        if fallback not in {"auto", "index.html", "404.html", None}:
            raise AssertionError(
                "fallback must be 'auto', 'index.html', '404.html', or None"
            )
        self.path = _normalize_frontend_path(path)
        self.methods = {"GET", "HEAD"}
        self.app = _FrontendStaticFiles(
            directory=directory, fallback=fallback, check_dir=check_dir
        )

    def matches(self, scope: Scope) -> tuple[Match, Scope]:
        return self.matches_with_path(scope, self.path)

    def matches_with_path(self, scope: Scope, path: str) -> tuple[Match, Scope]:
        if scope["type"] != "http":
            return Match.NONE, {}
        frontend_path = self._get_frontend_path(path, get_route_path(scope))
        if frontend_path is None:
            return Match.NONE, {}
        child_scope = {
            _FASTAPI_SCOPE_KEY: {
                _FASTAPI_FRONTEND_PATH_KEY: frontend_path,
                _FASTAPI_FRONTEND_SPECIFICITY_KEY: _frontend_path_specificity(path),
            }
        }
        if scope["method"] not in self.methods:
            return Match.PARTIAL, child_scope
        return Match.FULL, child_scope

    def _get_frontend_path(self, path: str, route_path: str) -> str | None:
        if path == "/":
            return route_path.lstrip("/")
        if route_path == path:
            return ""
        prefix = path + "/"
        if route_path.startswith(prefix):
            return route_path[len(prefix) :]
        return None

    async def handle(self, scope: Scope, receive: Receive, send: Send) -> None:
        response = await self.app.get_response_for_scope(scope)
        await response(scope, receive, send)

    def url_path_for(self, name: str, /, **path_params: Any) -> URLPath:
        raise NoMatchFound(name, path_params)


class _FrontendRouteGroup(BaseRoute):
    def __init__(
        self,
        *,
        dependencies: Sequence[params.Depends] | None = None,
        dependency_overrides_provider: Any | None = None,
    ) -> None:
        self.routes: list[_FrontendRoute] = []
        self.dependencies = list(dependencies or [])
        self.dependency_overrides_provider = dependency_overrides_provider
        (
            self.dependant,
            _,
            self._embed_body_fields,
        ) = _build_dependant_with_parameterless_dependencies(
            path="",
            call=_frontend_dependency_endpoint,
            dependencies=self.dependencies,
        )

    def add_frontend_route(
        self,
        path: str,
        *,
        directory: str | os.PathLike[str],
        fallback: Literal["auto", "index.html", "404.html"] | None = "auto",
        check_dir: bool,
    ) -> None:
        self.routes.append(
            _FrontendRoute(
                path,
                directory=directory,
                fallback=fallback,
                check_dir=check_dir,
            )
        )

    def matches(self, scope: Scope) -> tuple[Match, Scope]:
        match, child_scope, _ = self._match(scope, prefix="")
        return match, child_scope

    def matches_with_prefix(self, scope: Scope, prefix: str) -> tuple[Match, Scope]:
        match, child_scope, _ = self._match(scope, prefix=prefix)
        return match, child_scope

    def _match(
        self, scope: Scope, *, prefix: str
    ) -> tuple[Match, Scope, _FrontendRoute | None]:
        full: tuple[Scope, _FrontendRoute, int] | None = None
        partial: tuple[Scope, _FrontendRoute, int] | None = None
        for route in self.routes:
            path = _join_frontend_paths(prefix, route.path)
            match, child_scope = route.matches_with_path(scope, path)
            specificity = _frontend_path_specificity(path)
            if match == Match.FULL:
                if full is None or specificity > full[2]:
                    full = (child_scope, route, specificity)
            elif match == Match.PARTIAL:
                if partial is None or specificity > partial[2]:
                    partial = (child_scope, route, specificity)
        if full is not None:
            child_scope, route, _ = full
            return Match.FULL, child_scope, route
        if partial is not None:
            child_scope, route, _ = partial
            return Match.PARTIAL, child_scope, route
        return Match.NONE, {}, None

    async def handle(self, scope: Scope, receive: Receive, send: Send) -> None:
        effective_context = _get_scope_effective_route_context(scope)
        if (
            isinstance(effective_context, _EffectiveRouteContext)
            and effective_context.original_route is self
        ):
            prefix = effective_context.frontend_prefix
            dependant = effective_context.dependant
            dependency_overrides_provider = (
                effective_context.dependency_overrides_provider
            )
            embed_body_fields = effective_context._embed_body_fields
        else:
            prefix = ""
            dependant = self.dependant
            dependency_overrides_provider = self.dependency_overrides_provider
            embed_body_fields = self._embed_body_fields
        match, child_scope, route = self._match(scope, prefix=prefix)
        if match == Match.NONE or route is None:
            raise HTTPException(status_code=404)
        _update_scope(scope, child_scope)
        if match == Match.FULL and dependant and dependant.dependencies:
            async with self._solve_dependencies(
                scope,
                receive,
                send,
                dependant=dependant,
                dependency_overrides_provider=dependency_overrides_provider,
                embed_body_fields=embed_body_fields,
            ) as solved_result:
                response = await route.app.get_response_for_scope(scope)
                if response.background is None:
                    response.background = solved_result.background_tasks
                response.headers.raw.extend(solved_result.response.headers.raw)
                await response(scope, receive, send)
            return
        await route.handle(scope, receive, send)

    def url_path_for(self, name: str, /, **path_params: Any) -> URLPath:
        raise NoMatchFound(name, path_params)

    # TODO：可能移出 Route/Route Group（APIRoute 同理），
    #  ideally 作为 FastAPI 顶层逻辑，而非在 APIRoute 等处重复
    @asynccontextmanager
    async def _solve_dependencies(
        self,
        scope: Scope,
        receive: Receive,
        send: Send,
        *,
        dependant: Dependant,
        dependency_overrides_provider: Any | None,
        embed_body_fields: bool,
    ) -> AsyncIterator[SolvedDependency]:
        request = Request(scope, receive, send)
        previous_inner_astack = scope.get("fastapi_inner_astack", _SCOPE_MISSING)
        previous_function_astack = scope.get("fastapi_function_astack", _SCOPE_MISSING)
        try:
            async with AsyncExitStack() as request_stack:
                scope["fastapi_inner_astack"] = request_stack
                async with AsyncExitStack() as function_stack:
                    scope["fastapi_function_astack"] = function_stack
                    solved_result = await solve_dependencies(
                        request=request,
                        dependant=dependant,
                        dependency_overrides_provider=dependency_overrides_provider,
                        async_exit_stack=request_stack,
                        embed_body_fields=embed_body_fields,
                    )
                    if solved_result.errors:
                        raise RequestValidationError(solved_result.errors)
                    yield solved_result
        finally:
            if previous_inner_astack is _SCOPE_MISSING:
                scope.pop("fastapi_inner_astack", None)
            else:
                scope["fastapi_inner_astack"] = previous_inner_astack
            if previous_function_astack is _SCOPE_MISSING:
                scope.pop("fastapi_function_astack", None)
            else:
                scope["fastapi_function_astack"] = previous_function_astack


class APIRouter(routing.Router):
    """
    `APIRouter` 类，用于分组*路径操作*，例如将应用拆分为多文件。
    随后可包含在 `FastAPI` 应用或另一个 `APIRouter` 中（最终包含在应用中）。

    详见
    [FastAPI 大型应用（多文件）文档](https://fastapi.tiangolo.com/tutorial/bigger-applications/)。

    ## 示例

    ```python
    from fastapi import APIRouter, FastAPI

    app = FastAPI()
    router = APIRouter()


    @router.get("/users/", tags=["users"])
    async def read_users():
        return [{"username": "Rick"}, {"username": "Morty"}]


    app.include_router(router)
    ```
    """

    def __init__(
        self,
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
            Sequence[params.Depends] | None,
            Doc(
                """
                应用于此路由所有*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 大型应用文档（include_router 自定义前缀/tags/响应/依赖）](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies)。
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
                """
            ),
        ] = Default(JSONResponse),
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
                OpenAPI callbacks that should apply to all *路径操作* in this
                router.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
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

                在 FastAPI 中通常使用*路径操作方法*，如 `router.get()`、`router.post()` 等。
                """
            ),
        ] = None,
        redirect_slashes: Annotated[
            bool,
            Doc(
                """
                客户端 URL 斜杠格式不一致时，是否检测并重定向。
                """
            ),
        ] = True,
        default: Annotated[
            ASGIApp | None,
            Doc(
                """
                此路由默认处理器，处理 404 Not Found。
                """
            ),
        ] = None,
        dependency_overrides_provider: Annotated[
            Any | None,
            Doc(
                """
                仅 FastAPI 内部用于依赖覆盖。

                通常无需使用，一般指向 `FastAPI` 应用对象。
                """
            ),
        ] = None,
        route_class: Annotated[
            type[APIRoute],
            Doc(
                """
                Custom route (*路径操作*) class to be used by this router.

                详见
                [FastAPI docs for Custom Request and APIRoute class](https://fastapi.tiangolo.com/how-to/custom-request-and-route/#custom-apiroute-class-in-a-router).
                """
            ),
        ] = APIRoute,
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
        # the generic to Lifespan[AppType] is the type of the top level application
        # which the router cannot know statically, so we use typing.Any
        lifespan: Annotated[
            Lifespan[Any] | None,
            Doc(
                """
                `Lifespan` 上下文管理器处理器，用单一上下文管理器替代 `startup`/`shutdown` 函数。

                详见
                [FastAPI docs for `lifespan`](https://fastapi.tiangolo.com/advanced/events/).
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此路由中所有*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此路由中所有*路径操作*包含在 OpenAPI 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 查询参数与字符串校验文档（从 OpenAPI 排除参数）](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi)。
                """
            ),
        ] = True,
        generate_unique_id_function: Annotated[
            Callable[[APIRoute], str],
            Doc(
                """
                自定义生成 OpenAPI 中*路径操作*唯一 ID 的函数。

                自动生成 API 客户端或 SDK 时尤其有用。

                详见
                [FastAPI 生成客户端文档（自定义 operation ID）](https://fastapi.tiangolo.com/advanced/generate-clients/#custom-generate-unique-id-function)。
                """
            ),
        ] = Default(generate_unique_id),
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
        ] = Default(True),
    ) -> None:
        # Determine the lifespan context to use
        if lifespan is None:
            # Use the default lifespan that runs on_startup/on_shutdown handlers
            lifespan_context: Lifespan[Any] = _DefaultLifespan(self)
        elif inspect.isasyncgenfunction(lifespan):
            lifespan_context = asynccontextmanager(lifespan)
        elif inspect.isgeneratorfunction(lifespan):
            lifespan_context = _wrap_gen_lifespan_context(lifespan)
        else:
            lifespan_context = lifespan
        self.lifespan_context = lifespan_context

        super().__init__(
            routes=routes,
            redirect_slashes=redirect_slashes,
            default=default,
            lifespan=lifespan_context,
        )
        if prefix:
            assert prefix.startswith("/"), "A path prefix must start with '/'"
            assert not prefix.endswith("/"), (
                "A path prefix must not end with '/', as the routes will start with '/'"
            )

        # Handle on_startup/on_shutdown locally since Starlette removed support
        # Ref: https://github.com/Kludex/starlette/pull/3117
        # TODO: deprecate this once the lifespan (or alternative) interface is improved
        self.on_startup: list[Callable[[], Any]] = (
            [] if on_startup is None else list(on_startup)
        )
        self.on_shutdown: list[Callable[[], Any]] = (
            [] if on_shutdown is None else list(on_shutdown)
        )

        self.prefix = prefix
        self.tags: list[str | Enum] = tags or []
        self.dependencies = list(dependencies or [])
        self.deprecated = deprecated
        self.include_in_schema = include_in_schema
        self.responses = responses or {}
        self.callbacks = callbacks or []
        self.dependency_overrides_provider = dependency_overrides_provider
        self.route_class = route_class
        self.default_response_class = default_response_class
        self.generate_unique_id_function = generate_unique_id_function
        self.strict_content_type = strict_content_type
        self._routes_version = 0
        self._low_priority_routes: list[BaseRoute] = []
        self._frontend_routes: _FrontendRouteGroup | None = None

    def _mark_routes_changed(self) -> None:
        self._routes_version += 1

    def _get_routes_version(self, seen: set[int] | None = None) -> int:
        if seen is None:
            seen = set()
        router_id = id(self)
        if router_id in seen:
            return self._routes_version
        seen.add(router_id)
        version = self._routes_version
        for route in self.routes:
            if isinstance(route, _IncludedRouter):
                version += route.original_router._get_routes_version(seen)
        return version

    def _contains_router(
        self, router: "APIRouter", seen: set[int] | None = None
    ) -> bool:
        if seen is None:
            seen = set()
        router_id = id(self)
        if router_id in seen:
            return False
        seen.add(router_id)
        for route in self.routes:
            if not isinstance(route, _IncludedRouter):
                continue
            if route.original_router is router:
                return True
            if route.original_router._contains_router(router, seen):
                return True
        return False

    def add_route(
        self,
        path: str,
        endpoint: Callable[[Request], Awaitable[Response] | Response],
        methods: Collection[str] | None = None,
        name: str | None = None,
        include_in_schema: bool = True,
    ) -> None:
        super().add_route(
            path,
            endpoint,
            methods=methods,
            name=name,
            include_in_schema=include_in_schema,
        )
        self._mark_routes_changed()

    def add_websocket_route(
        self,
        path: str,
        endpoint: Callable[[WebSocket], Awaitable[None]],
        name: str | None = None,
    ) -> None:
        super().add_websocket_route(path, endpoint, name=name)
        self._mark_routes_changed()

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
        from fastapi import APIRouter, FastAPI

        app = FastAPI()
        router = APIRouter()
        router.frontend("/", directory="dist")
        app.include_router(router)
        ```
        """
        check_dir = _resolve_frontend_check_dir(
            directory=directory, check_dir=check_dir
        )
        normalized_path = _normalize_frontend_path(path)
        if self._frontend_routes is None:
            self._frontend_routes = _FrontendRouteGroup(
                dependencies=self.dependencies,
                dependency_overrides_provider=self.dependency_overrides_provider,
            )
            self._low_priority_routes.append(self._frontend_routes)
        self._frontend_routes.add_frontend_route(
            _join_frontend_paths(self.prefix, normalized_path),
            directory=directory,
            fallback=fallback,
            check_dir=check_dir,
        )
        self._mark_routes_changed()

    async def app(self, scope: Scope, receive: Receive, send: Send) -> None:
        assert scope["type"] in ("http", "websocket", "lifespan")

        if "router" not in scope:
            scope["router"] = self

        if scope["type"] == "lifespan":
            await self.lifespan(scope, receive, send)
            return

        partial: tuple[BaseRoute, Scope] | None = None
        for route in self.routes:
            match, child_scope = route.matches(scope)
            if match == Match.FULL:
                scope.update(child_scope)
                await route.handle(scope, receive, send)
                return
            if match == Match.PARTIAL and partial is None:
                partial = (route, child_scope)

        if partial is not None:
            route, child_scope = partial
            scope.update(child_scope)
            await route.handle(scope, receive, send)
            return

        route_path = get_route_path(scope)
        if scope["type"] == "http" and self.redirect_slashes and route_path != "/":
            redirect_scope = dict(scope)
            if route_path.endswith("/"):
                redirect_scope["path"] = redirect_scope["path"].rstrip("/")
            else:
                redirect_scope["path"] = redirect_scope["path"] + "/"

            for route in self.routes:
                match, _ = route.matches(redirect_scope)
                if match != Match.NONE:
                    redirect_url = URL(scope=redirect_scope)
                    response = RedirectResponse(url=str(redirect_url))
                    await response(scope, receive, send)
                    return

        (
            low_priority_match,
            low_priority_scope,
            low_priority_route,
            low_priority_context,
        ) = self._match_low_priority(scope)
        if low_priority_match != Match.NONE and low_priority_route is not None:
            _update_scope(scope, low_priority_scope)
            if low_priority_context is not None:
                _get_fastapi_scope(scope)[_FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY] = (
                    low_priority_context
                )
                original_route = low_priority_context.original_route
                if isinstance(original_route, APIRoute):
                    scope["route"] = original_route
                    await original_route.handle(scope, receive, send)
                    return
            await low_priority_route.handle(scope, receive, send)
            return

        await self.default(scope, receive, send)

    async def handle(self, scope: Scope, receive: Receive, send: Send) -> None:
        included_router = _get_scope_included_router(scope)
        if (
            isinstance(included_router, _IncludedRouter)
            and included_router.original_router is self
        ):
            await included_router._handle_selected(scope, receive, send)
            return
        await self.app(scope, receive, send)

    def matches(self, scope: Scope) -> tuple[Match, Scope]:
        included_router = _get_scope_included_router(scope)
        if (
            isinstance(included_router, _IncludedRouter)
            and included_router.original_router is self
        ):
            match, child_scope, _, _ = included_router._match(scope)
            return match, child_scope
        return Match.NONE, {}

    def _iter_low_priority_routes(
        self,
    ) -> Iterator[BaseRoute | _EffectiveRouteContext]:
        yield from self._low_priority_routes
        for route in self.routes:
            if isinstance(route, _IncludedRouter):
                yield from route.effective_low_priority_routes()

    def _match_low_priority(
        self, scope: Scope
    ) -> tuple[Match, Scope, BaseRoute | None, _EffectiveRouteContext | None]:
        full: tuple[Scope, BaseRoute, _EffectiveRouteContext | None] | None = None
        partial: tuple[Scope, BaseRoute, _EffectiveRouteContext | None] | None = None
        for candidate in self._iter_low_priority_routes():
            route: BaseRoute
            if isinstance(candidate, _EffectiveRouteContext):
                route_context: _EffectiveRouteContext | None = candidate
                original_route = candidate.original_route
                if isinstance(original_route, APIRoute):
                    fastapi_scope = _get_fastapi_scope(scope)
                    previous_context = fastapi_scope.get(
                        _FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY, _SCOPE_MISSING
                    )
                    fastapi_scope[_FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY] = route_context
                    try:
                        match, child_scope = original_route.matches(scope)
                    finally:
                        _restore_fastapi_scope_key(
                            scope,
                            _FASTAPI_EFFECTIVE_ROUTE_CONTEXT_KEY,
                            previous_context,
                        )
                    route = original_route
                else:
                    match, child_scope = candidate.matches(scope)
                    route = candidate.starlette_route or original_route
            else:
                route_context = None
                match, child_scope = candidate.matches(scope)
                route = candidate
            if match == Match.FULL:
                if full is None or self._frontend_match_is_more_specific(
                    child_scope, full[0]
                ):
                    full = (child_scope, route, route_context)
            elif match == Match.PARTIAL:
                if partial is None or self._frontend_match_is_more_specific(
                    child_scope, partial[0]
                ):
                    partial = (child_scope, route, route_context)
        if full is not None:
            child_scope, route, route_context = full
            return Match.FULL, child_scope, route, route_context
        if partial is not None:
            child_scope, route, route_context = partial
            return Match.PARTIAL, child_scope, route, route_context
        return Match.NONE, {}, None, None

    def _frontend_match_is_more_specific(
        self, child_scope: Scope, previous_child_scope: Scope
    ) -> bool:
        specificity = _frontend_scope_specificity(child_scope)
        previous_specificity = _frontend_scope_specificity(previous_child_scope)
        if specificity is None or previous_specificity is None:
            return False
        return specificity > previous_specificity

    def route(
        self,
        path: str,
        methods: Collection[str] | None = None,
        name: str | None = None,
        include_in_schema: bool = True,
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_route(
                path,
                func,
                methods=methods,
                name=name,
                include_in_schema=include_in_schema,
            )
            return func

        return decorator

    def add_api_route(
        self,
        path: str,
        endpoint: Callable[..., Any],
        *,
        response_model: Any = Default(None),
        status_code: int | None = None,
        tags: list[str | Enum] | None = None,
        dependencies: Sequence[params.Depends] | None = None,
        summary: str | None = None,
        description: str | None = None,
        response_description: str = "Successful Response",
        responses: dict[int | str, dict[str, Any]] | None = None,
        deprecated: bool | None = None,
        methods: set[str] | list[str] | None = None,
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
        route_class_override: type[APIRoute] | None = None,
        callbacks: list[BaseRoute] | None = None,
        openapi_extra: dict[str, Any] | None = None,
        generate_unique_id_function: Callable[[APIRoute], str]
        | DefaultPlaceholder = Default(generate_unique_id),
        strict_content_type: bool | DefaultPlaceholder = Default(True),
    ) -> None:
        route_class = route_class_override or self.route_class
        responses = responses or {}
        combined_responses = {**self.responses, **responses}
        current_response_class = get_value_or_default(
            response_class, self.default_response_class
        )
        current_tags = self.tags.copy()
        if tags:
            current_tags.extend(tags)
        current_dependencies = self.dependencies.copy()
        if dependencies:
            current_dependencies.extend(dependencies)
        current_callbacks = self.callbacks.copy()
        if callbacks:
            current_callbacks.extend(callbacks)
        current_generate_unique_id = get_value_or_default(
            generate_unique_id_function, self.generate_unique_id_function
        )
        route = route_class(
            self.prefix + path,
            endpoint=endpoint,
            response_model=response_model,
            status_code=status_code,
            tags=current_tags,
            dependencies=current_dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=combined_responses,
            deprecated=deprecated or self.deprecated,
            methods=methods,
            operation_id=operation_id,
            response_model_include=response_model_include,
            response_model_exclude=response_model_exclude,
            response_model_by_alias=response_model_by_alias,
            response_model_exclude_unset=response_model_exclude_unset,
            response_model_exclude_defaults=response_model_exclude_defaults,
            response_model_exclude_none=response_model_exclude_none,
            include_in_schema=include_in_schema and self.include_in_schema,
            response_class=current_response_class,
            name=name,
            dependency_overrides_provider=self.dependency_overrides_provider,
            callbacks=current_callbacks,
            openapi_extra=openapi_extra,
            generate_unique_id_function=current_generate_unique_id,
            strict_content_type=get_value_or_default(
                strict_content_type, self.strict_content_type
            ),
        )
        self.routes.append(route)
        self._mark_routes_changed()

    def api_route(
        self,
        path: str,
        *,
        response_model: Any = Default(None),
        status_code: int | None = None,
        tags: list[str | Enum] | None = None,
        dependencies: Sequence[params.Depends] | None = None,
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
        callbacks: list[BaseRoute] | None = None,
        openapi_extra: dict[str, Any] | None = None,
        generate_unique_id_function: Callable[[APIRoute], str] = Default(
            generate_unique_id
        ),
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_api_route(
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
                callbacks=callbacks,
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
        dependencies: Sequence[params.Depends] | None = None,
    ) -> None:
        current_dependencies = self.dependencies.copy()
        if dependencies:
            current_dependencies.extend(dependencies)

        route = APIWebSocketRoute(
            self.prefix + path,
            endpoint=endpoint,
            name=name,
            dependencies=current_dependencies,
            dependency_overrides_provider=self.dependency_overrides_provider,
        )
        self.routes.append(route)
        self._mark_routes_changed()

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
            Sequence[params.Depends] | None,
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

        ## 示例

        ```python
        from fastapi import APIRouter, FastAPI, WebSocket

        app = FastAPI()
        router = APIRouter()

        @router.websocket("/ws")
        async def websocket_endpoint(websocket: WebSocket):
            await websocket.accept()
            while True:
                data = await websocket.receive_text()
                await websocket.send_text(f"Message text was: {data}")

        app.include_router(router)
        ```
        """

        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_api_websocket_route(
                path, func, name=name, dependencies=dependencies
            )
            return func

        return decorator

    def websocket_route(
        self, path: str, name: str | None = None
    ) -> Callable[[DecoratedCallable], DecoratedCallable]:
        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_websocket_route(path, func, name=name)
            return func

        return decorator

    def include_router(
        self,
        router: Annotated["APIRouter", Doc("要包含的 `APIRouter`。")],
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
            Sequence[params.Depends] | None,
            Doc(
                """
                应用于此路由所有*路径操作*的依赖列表（`Depends()`）。

                详见
                [FastAPI 大型应用文档（include_router 自定义前缀/tags/响应/依赖）](https://fastapi.tiangolo.com/tutorial/bigger-applications/#include-an-apirouter-with-a-custom-prefix-tags-responses-and-dependencies)。
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
                """
            ),
        ] = Default(JSONResponse),
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
                OpenAPI callbacks that should apply to all *路径操作* in this
                router.

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI OpenAPI 回调文档](https://fastapi.tiangolo.com/advanced/openapi-callbacks/)。
                """
            ),
        ] = None,
        deprecated: Annotated[
            bool | None,
            Doc(
                """
                将此路由中所有*路径操作*标记为已弃用。

                将写入生成的 OpenAPI（例如在 `/docs` 可见）。

                详见
                [FastAPI 路径操作配置文档](https://fastapi.tiangolo.com/tutorial/path-operation-configuration/)。
                """
            ),
        ] = None,
        include_in_schema: Annotated[
            bool,
            Doc(
                """
                是否将此路由中所有*路径操作*包含在 OpenAPI schema 中。

                影响生成的 OpenAPI（例如在 `/docs` 可见）。
                """
            ),
        ] = True,
        generate_unique_id_function: Annotated[
            Callable[[APIRoute], str],
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
        在当前 `APIRouter` 中包含另一个 `APIRouter`。

        详见
        [FastAPI docs for Bigger Applications](https://fastapi.tiangolo.com/tutorial/bigger-applications/).

        ## 示例

        ```python
        from fastapi import APIRouter, FastAPI

        app = FastAPI()
        internal_router = APIRouter()
        users_router = APIRouter()

        @users_router.get("/users/")
        def read_users():
            return [{"name": "Rick"}, {"name": "Morty"}]

        internal_router.include_router(users_router)
        app.include_router(internal_router)
        ```
        """
        assert self is not router, (
            "Cannot include the same APIRouter instance into itself. "
            "Did you mean to include a different router?"
        )
        assert not router._contains_router(self), (
            "Cannot include an APIRouter instance that already includes this router. "
            "Did you mean to include a different router?"
        )
        if prefix:
            assert prefix.startswith("/"), "A path prefix must start with '/'"
            assert not prefix.endswith("/"), (
                "A path prefix must not end with '/', as the routes will start with '/'"
            )
        else:
            for route, route_context in _iter_routes_with_context(router.routes):
                if route_context is None:
                    path = getattr(route, "path", None)
                    name = getattr(route, "name", "unknown")
                elif route_context.starlette_route is not None:
                    path = getattr(route_context.starlette_route, "path", None)
                    name = getattr(route_context.starlette_route, "name", "unknown")
                else:
                    path = route_context.path
                    name = route_context.name
                if path is not None and not path:
                    raise FastAPIError(
                        f"Prefix and path cannot be both empty (path operation: {name})"
                    )
        include_context = _RouterIncludeContext.for_include(
            parent_router=self,
            included_router=router,
            prefix=prefix,
            tags=tags,
            dependencies=dependencies,
            default_response_class=default_response_class,
            responses=responses,
            callbacks=callbacks,
            deprecated=deprecated,
            include_in_schema=include_in_schema,
            generate_unique_id_function=generate_unique_id_function,
        )
        self.routes.append(
            _IncludedRouter(original_router=router, include_context=include_context)
        )
        self._mark_routes_changed()
        for handler in router.on_startup:
            self.add_event_handler("startup", handler)
        for handler in router.on_shutdown:
            self.add_event_handler("shutdown", handler)
        self.lifespan_context = _merge_lifespan_context(
            self.lifespan_context,
            router.lifespan_context,
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI

        app = FastAPI()
        router = APIRouter()

        @router.get("/items/")
        def read_items():
            return [{"name": "Empanada"}, {"name": "Arepa"}]

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["GET"],
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()
        router = APIRouter()

        @router.put("/items/{item_id}")
        def replace_item(item_id: str, item: Item):
            return {"message": "Item replaced", "id": item_id}

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["PUT"],
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()
        router = APIRouter()

        @router.post("/items/")
        def create_item(item: Item):
            return {"message": "Item created"}

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["POST"],
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI

        app = FastAPI()
        router = APIRouter()

        @router.delete("/items/{item_id}")
        def delete_item(item_id: str):
            return {"message": "Item deleted"}

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["DELETE"],
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI

        app = FastAPI()
        router = APIRouter()

        @router.options("/items/")
        def get_item_options():
            return {"additions": ["Aji", "Guacamole"]}

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["OPTIONS"],
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()
        router = APIRouter()

        @router.head("/items/", status_code=204)
        def get_items_headers(response: Response):
            response.headers["X-Cat-Dog"] = "Alone in the world"

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["HEAD"],
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()
        router = APIRouter()

        @router.patch("/items/")
        def update_item(item: Item):
            return {"message": "Item updated in place"}

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["PATCH"],
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
            Sequence[params.Depends] | None,
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
            Callable[[APIRoute], str],
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
        from fastapi import APIRouter, FastAPI
        from pydantic import BaseModel

        class Item(BaseModel):
            name: str
            description: str | None = None

        app = FastAPI()
        router = APIRouter()

        @router.trace("/items/{item_id}")
        def trace_item(item_id: str):
            return None

        app.include_router(router)
        ```
        """
        return self.api_route(
            path=path,
            response_model=response_model,
            status_code=status_code,
            tags=tags,
            dependencies=dependencies,
            summary=summary,
            description=description,
            response_description=response_description,
            responses=responses,
            deprecated=deprecated,
            methods=["TRACE"],
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

    # TODO: remove this once the lifespan (or alternative) interface is improved
    async def _startup(self) -> None:
        """
        运行所有 `.on_startup` 事件处理器。

        Starlette 移除 on_startup/on_shutdown 支持后，
        此方法保留以维持向后兼容。

        参考：https://github.com/Kludex/starlette/pull/3117
        """
        for handler in self.on_startup:
            if is_async_callable(handler):
                await handler()
            else:
                handler()

    # TODO: remove this once the lifespan (or alternative) interface is improved
    async def _shutdown(self) -> None:
        """
        运行所有 `.on_shutdown` 事件处理器。

        Starlette 移除 on_startup/on_shutdown 支持后，
        此方法保留以维持向后兼容。

        参考：https://github.com/Kludex/starlette/pull/3117
        """
        for handler in self.on_shutdown:
            if is_async_callable(handler):
                await handler()
            else:
                handler()

    # TODO: remove this once the lifespan (or alternative) interface is improved
    def add_event_handler(
        self,
        event_type: str,
        func: Callable[[], Any],
    ) -> None:
        """
        添加 startup 或 shutdown 事件处理函数。

        Starlette 移除 on_startup/on_shutdown 支持后，
        此方法保留以维持向后兼容。

        参考：https://github.com/Kludex/starlette/pull/3117
        """
        assert event_type in ("startup", "shutdown")
        if event_type == "startup":
            self.on_startup.append(func)
        else:
            self.on_shutdown.append(func)

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
        向路由添加事件处理器。

        `on_event` 已弃用，请改用 `lifespan` 事件处理器。

        详见
        [FastAPI 生命周期事件文档（已弃用的替代方案）](https://fastapi.tiangolo.com/advanced/events/#alternative-events-deprecated)。
        """

        def decorator(func: DecoratedCallable) -> DecoratedCallable:
            self.add_event_handler(event_type, func)
            return func

        return decorator
