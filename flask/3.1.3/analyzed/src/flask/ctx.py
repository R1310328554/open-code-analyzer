from __future__ import annotations

import contextvars
import sys
import typing as t
from functools import update_wrapper
from types import TracebackType

from werkzeug.exceptions import HTTPException

from . import typing as ft
from .globals import _cv_app
from .globals import _cv_request
from .signals import appcontext_popped
from .signals import appcontext_pushed

if t.TYPE_CHECKING:  # pragma: no cover
    from _typeshed.wsgi import WSGIEnvironment

    from .app import Flask
    from .sessions import SessionMixin
    from .wrappers import Request


# 用作参数默认值的单例哨兵值
_sentinel = object()


class _AppCtxGlobals:
    """
        普通对象，用作应用上下文中存储数据的命名空间。
        
        创建应用上下文时会自动创建此对象，并通过 :data:`g` 代理对外暴露。
    """

    # 定义属性方法让 mypy 知道这是命名空间对象
    # 可拥有任意属性。

    def __getattr__(self, name: str) -> t.Any:
        try:
            return self.__dict__[name]
        except KeyError:
            raise AttributeError(name) from None

    def __setattr__(self, name: str, value: t.Any) -> None:
        self.__dict__[name] = value

    def __delattr__(self, name: str) -> None:
        try:
            del self.__dict__[name]
        except KeyError:
            raise AttributeError(name) from None

    def get(self, name: str, default: t.Any | None = None) -> t.Any:
        """
            按名称获取属性，若不存在则返回默认值。行为类似 :meth:`dict.get`。
            
            :param name: 要获取的属性名。
            :param default: 属性不存在时返回的值。
        """
        return self.__dict__.get(name, default)

    def pop(self, name: str, default: t.Any = _sentinel) -> t.Any:
        """
            按名称获取并移除属性。行为类似 :meth:`dict.pop`。
            
            :param name: 要弹出的属性名。
            :param default: 属性不存在时返回的值，而非抛出 ``KeyError``。
        """
        if default is _sentinel:
            return self.__dict__.pop(name)
        else:
            return self.__dict__.pop(name, default)

    def setdefault(self, name: str, default: t.Any = None) -> t.Any:
        """
            若属性存在则返回其值，否则设置并返回默认值。
            行为类似 :meth:`dict.setdefault`。
            
            :param name: 要获取的属性名。
            :param default: 属性不存在时设置并返回的值。
        """
        return self.__dict__.setdefault(name, default)

    def __contains__(self, item: str) -> bool:
        return item in self.__dict__

    def __iter__(self) -> t.Iterator[str]:
        return iter(self.__dict__)

    def __repr__(self) -> str:
        ctx = _cv_app.get(None)
        if ctx is not None:
            return f"<flask.g of '{ctx.app.name}'>"
        return object.__repr__(self)


def after_this_request(
    f: ft.AfterRequestCallable[t.Any],
) -> ft.AfterRequestCallable[t.Any]:
    """
        在当前请求结束后执行函数，常用于修改响应对象。
        函数接收响应对象，须返回同一对象或新对象。
    """
    ctx = _cv_request.get(None)

    if ctx is None:
        raise RuntimeError(
            "'after_this_request' can only be used when a request"
            " context is active, such as in a view function."
        )

    ctx._after_request_functions.append(f)
    return f


F = t.TypeVar("F", bound=t.Callable[..., t.Any])


def copy_current_request_context(f: F) -> F:
    """
        装饰函数以保留当前请求上下文的辅助函数，适用于 greenlet 场景。
    """
    ctx = _cv_request.get(None)

    if ctx is None:
        raise RuntimeError(
            "'copy_current_request_context' can only be used when a"
            " request context is active, such as in a view function."
        )

    ctx = ctx.copy()

    def wrapper(*args: t.Any, **kwargs: t.Any) -> t.Any:
        with ctx:
            return ctx.app.ensure_sync(f)(*args, **kwargs)

    return update_wrapper(wrapper, f)  # type: ignore[return-value]


def has_request_context() -> bool:
    """
        若代码需要检测请求上下文是否存在，可使用此函数。
    """
    return _cv_request.get(None) is not None


def has_app_context() -> bool:
    """
        与 :func:`has_request_context` 类似，但针对应用上下文。
    """
    return _cv_app.get(None) is not None


class AppContext:
    """
        应用上下文包含应用专属信息。每个请求开始时若尚无活动上下文，
        会创建并压入应用上下文。
    """

    def __init__(self, app: Flask) -> None:
        self.app = app
        self.url_adapter = app.create_url_adapter(None)
        self.g: _AppCtxGlobals = app.app_ctx_globals_class()
        self._cv_tokens: list[contextvars.Token[AppContext]] = []

    def push(self) -> None:
        """
            将应用上下文绑定到当前上下文。
        """
        self._cv_tokens.append(_cv_app.set(self))
        appcontext_pushed.send(self.app, _async_wrapper=self.app.ensure_sync)

    def pop(self, exc: BaseException | None = _sentinel) -> None:  # type: ignore
        """
            弹出应用上下文。
        """
        try:
            if len(self._cv_tokens) == 1:
                if exc is _sentinel:
                    exc = sys.exc_info()[1]
                self.app.do_teardown_appcontext(exc)
        finally:
            ctx = _cv_app.get()
            _cv_app.reset(self._cv_tokens.pop())

        if ctx is not self:
            raise AssertionError(
                f"Popped wrong app context. ({ctx!r} instead of {self!r})"
            )

        appcontext_popped.send(self.app, _async_wrapper=self.app.ensure_sync)

    def __enter__(self) -> AppContext:
        self.push()
        return self

    def __exit__(
        self,
        exc_type: type | None,
        exc_value: BaseException | None,
        tb: TracebackType | None,
    ) -> None:
        self.pop(exc_value)


class RequestContext:
    """
        请求上下文包含每个请求的专属信息。Flask 应用在请求开始时创建并压入，
        请求结束时弹出。
    """

    def __init__(
        self,
        app: Flask,
        environ: WSGIEnvironment,
        request: Request | None = None,
        session: SessionMixin | None = None,
    ) -> None:
        self.app = app
        if request is None:
            request = app.request_class(environ)
            request.json_module = app.json
        self.request: Request = request
        self.url_adapter = None
        try:
            self.url_adapter = app.create_url_adapter(self.request)
        except HTTPException as e:
            self.request.routing_exception = e
        self.flashes: list[tuple[str, str]] | None = None
        self._session: SessionMixin | None = session
        # Functions that should be executed after the request on the response
        # object.  These will be called before the regular "after_request"
        # functions.
        self._after_request_functions: list[ft.AfterRequestCallable[t.Any]] = []

        self._cv_tokens: list[
            tuple[contextvars.Token[RequestContext], AppContext | None]
        ] = []

    def copy(self) -> RequestContext:
        """
            创建使用同一请求对象的请求上下文副本。
        """
        return self.__class__(
            self.app,
            environ=self.request.environ,
            request=self.request,
            session=self._session,
        )

    def match_request(self) -> None:
        """
            子类可重写以介入请求匹配过程。
        """
        try:
            result = self.url_adapter.match(return_rule=True)  # type: ignore
            self.request.url_rule, self.request.view_args = result  # type: ignore
        except HTTPException as e:
            self.request.routing_exception = e

    @property
    def session(self) -> SessionMixin:
        """
            与此请求关联的 session 数据。上下文压入前不可用。
        """
        assert self._session is not None, "The session has not yet been opened."
        self._session.accessed = True
        return self._session

    def push(self) -> None:
        # Before we push the request context we have to ensure that there
        # is an application context.
        app_ctx = _cv_app.get(None)

        if app_ctx is None or app_ctx.app is not self.app:
            app_ctx = self.app.app_context()
            app_ctx.push()
        else:
            app_ctx = None

        self._cv_tokens.append((_cv_request.set(self), app_ctx))

        # Open the session at the moment that the request context is available.
        # This allows a custom open_session method to use the request context.
        # 仅在首次压入请求时打开新 session
        # 否则 stream_with_context 会丢失 session。
        if self._session is None:
            session_interface = self.app.session_interface
            self._session = session_interface.open_session(self.app, self.request)

            if self._session is None:
                self._session = session_interface.make_null_session(self.app)

        # Match the request URL after loading the session, so that the
        # session is available in custom URL converters.
        if self.url_adapter is not None:
            self.match_request()

    def pop(self, exc: BaseException | None = _sentinel) -> None:  # type: ignore
        """
            弹出请求上下文并解绑。同时触发 teardown_request 注册的函数。
        """
        clear_request = len(self._cv_tokens) == 1

        try:
            if clear_request:
                if exc is _sentinel:
                    exc = sys.exc_info()[1]
                self.app.do_teardown_request(exc)

                request_close = getattr(self.request, "close", None)
                if request_close is not None:
                    request_close()
        finally:
            ctx = _cv_request.get()
            token, app_ctx = self._cv_tokens.pop()
            _cv_request.reset(token)

            # get rid of circular dependencies at the end of the request
            # so that we don't require the GC to be active.
            if clear_request:
                ctx.request.environ["werkzeug.request"] = None

            if app_ctx is not None:
                app_ctx.pop(exc)

            if ctx is not self:
                raise AssertionError(
                    f"Popped wrong request context. ({ctx!r} instead of {self!r})"
                )

    def __enter__(self) -> RequestContext:
        self.push()
        return self

    def __exit__(
        self,
        exc_type: type | None,
        exc_value: BaseException | None,
        tb: TracebackType | None,
    ) -> None:
        self.pop(exc_value)

    def __repr__(self) -> str:
        return (
            f"<{type(self).__name__} {self.request.url!r}"
            f" [{self.request.method}] of {self.app.name}>"
        )
