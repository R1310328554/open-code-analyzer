from __future__ import annotations

import importlib.metadata
import typing as t
from contextlib import contextmanager
from contextlib import ExitStack
from copy import copy
from types import TracebackType
from urllib.parse import urlsplit

import werkzeug.test
from click.testing import CliRunner
from click.testing import Result
from werkzeug.test import Client
from werkzeug.wrappers import Request as BaseRequest

from .cli import ScriptInfo
from .sessions import SessionMixin

if t.TYPE_CHECKING:  # pragma: no cover
    from _typeshed.wsgi import WSGIEnvironment
    from werkzeug.test import TestResponse

    from .app import Flask


class EnvironBuilder(werkzeug.test.EnvironBuilder):
    """:class:`~werkzeug.test.EnvironBuilder` 的子类，默认值来自应用配置。

    :param app: 用于配置环境的 Flask 应用。
    :param path: 请求的 URL 路径。
    :param base_url: 应用服务的基 URL，``path`` 相对于此。未提供时由
        :data:`PREFERRED_URL_SCHEME`、``subdomain``、
        :data:`SERVER_NAME` 和 :data:`APPLICATION_ROOT` 构建。
    :param subdomain: 追加到 :data:`SERVER_NAME` 的子域名。
    :param url_scheme: 替代 :data:`PREFERRED_URL_SCHEME` 的协议。
    :param json: 若提供，序列化为 JSON 并作为 ``data`` 传入，
        同时将 ``content_type`` 默认为 ``application/json``。
    :param args: 传递给 :class:`~werkzeug.test.EnvironBuilder` 的其他位置参数。
    :param kwargs: 传递给 :class:`~werkzeug.test.EnvironBuilder` 的其他关键字参数。
    """

    def __init__(
        self,
        app: Flask,
        path: str = "/",
        base_url: str | None = None,
        subdomain: str | None = None,
        url_scheme: str | None = None,
        *args: t.Any,
        **kwargs: t.Any,
    ) -> None:
        assert not (base_url or subdomain or url_scheme) or (
            base_url is not None
        ) != bool(subdomain or url_scheme), (
            'Cannot pass "subdomain" or "url_scheme" with "base_url".'
        )

        if base_url is None:
            http_host = app.config.get("SERVER_NAME") or "localhost"
            app_root = app.config["APPLICATION_ROOT"]

            if subdomain:
                http_host = f"{subdomain}.{http_host}"

            if url_scheme is None:
                url_scheme = app.config["PREFERRED_URL_SCHEME"]

            url = urlsplit(path)
            base_url = (
                f"{url.scheme or url_scheme}://{url.netloc or http_host}"
                f"/{app_root.lstrip('/')}"
            )
            path = url.path

            if url.query:
                path = f"{path}?{url.query}"

        self.app = app
        super().__init__(path, base_url, *args, **kwargs)

    def json_dumps(self, obj: t.Any, **kwargs: t.Any) -> str:
        """将 ``obj`` 序列化为 JSON 格式字符串。

        序列化配置与此 EnvironBuilder 的 ``app`` 关联。
        """
        return self.app.json.dumps(obj, **kwargs)


_werkzeug_version = ""


def _get_werkzeug_version() -> str:
    """延迟获取 Werkzeug 版本号，用于 User-Agent 头。"""
    global _werkzeug_version

    if not _werkzeug_version:
        _werkzeug_version = importlib.metadata.version("werkzeug")

    return _werkzeug_version


class FlaskClient(Client):
    """行为类似常规 Werkzeug 测试客户端，但了解 Flask 上下文，
    可将请求上下文的清理推迟到 ``with`` 块结束。用法参见
    :class:`werkzeug.test.Client`。

    .. versionchanged:: 0.12
       ``app.test_client()`` 包含预设默认环境，可在实例化后通过
       ``client.environ_base`` 修改。

    基本用法见 :doc:`/testing` 章节。
    """

    application: Flask

    def __init__(self, *args: t.Any, **kwargs: t.Any) -> None:
        super().__init__(*args, **kwargs)
        self.preserve_context = False
        self._new_contexts: list[t.ContextManager[t.Any]] = []
        self._context_stack = ExitStack()
        self.environ_base = {
            "REMOTE_ADDR": "127.0.0.1",
            "HTTP_USER_AGENT": f"Werkzeug/{_get_werkzeug_version()}",
        }

    @contextmanager
    def session_transaction(
        self, *args: t.Any, **kwargs: t.Any
    ) -> t.Iterator[SessionMixin]:
        """与 ``with`` 语句配合使用，打开会话事务。
        可用于修改测试客户端使用的会话，``with`` 块结束后会话会写回。

        ::

            with client.session_transaction() as session:
                session['value'] = 42

        内部通过临时测试请求上下文实现，由于会话处理可能依赖请求变量，
        此函数接受与 :meth:`~flask.Flask.test_request_context` 相同的参数并直接转发。

        """
        if self._cookies is None:
            raise TypeError(
                "Cookies are disabled. Create a client with 'use_cookies=True'."
            )

        app = self.application
        ctx = app.test_request_context(*args, **kwargs)
        self._add_cookies_to_wsgi(ctx.request.environ)

        with ctx:
            sess = app.session_interface.open_session(app, ctx.request)

        if sess is None:
            raise RuntimeError("Session backend did not open a session.")

        yield sess
        resp = app.response_class()

        if app.session_interface.is_null_session(sess):
            return

        with ctx:
            app.session_interface.save_session(app, sess, resp)

        self._update_cookies_from_response(
            ctx.request.host.partition(":")[0],
            ctx.request.path,
            resp.headers.getlist("Set-Cookie"),
        )

    def _copy_environ(self, other: WSGIEnvironment) -> WSGIEnvironment:
        out = {**self.environ_base, **other}

        if self.preserve_context:
            out["werkzeug.debug.preserve_context"] = self._new_contexts.append

        return out

    def _request_from_builder_args(
        self, args: tuple[t.Any, ...], kwargs: dict[str, t.Any]
    ) -> BaseRequest:
        kwargs["environ_base"] = self._copy_environ(kwargs.get("environ_base", {}))
        builder = EnvironBuilder(self.application, *args, **kwargs)

        try:
            return builder.get_request()
        finally:
            builder.close()

    def open(
        self,
        *args: t.Any,
        buffered: bool = False,
        follow_redirects: bool = False,
        **kwargs: t.Any,
    ) -> TestResponse:
        if args and isinstance(
            args[0], (werkzeug.test.EnvironBuilder, dict, BaseRequest)
        ):
            if isinstance(args[0], werkzeug.test.EnvironBuilder):
                builder = copy(args[0])
                builder.environ_base = self._copy_environ(builder.environ_base or {})  # type: ignore[arg-type]
                request = builder.get_request()
            elif isinstance(args[0], dict):
                request = EnvironBuilder.from_environ(
                    args[0], app=self.application, environ_base=self._copy_environ({})
                ).get_request()
            else:
                # isinstance(args[0], BaseRequest)
                request = copy(args[0])
                request.environ = self._copy_environ(request.environ)
        else:
            # request is None
            request = self._request_from_builder_args(args, kwargs)

        # 清除先前保留的上下文，防止跨重定向或同一块内多次请求时上下文泄漏。
        self._context_stack.close()

        response = super().open(
            request,
            buffered=buffered,
            follow_redirects=follow_redirects,
        )
        response.json_module = self.application.json  # type: ignore[assignment]

        # 重新压入请求期间保留的上下文。
        for cm in self._new_contexts:
            self._context_stack.enter_context(cm)

        self._new_contexts.clear()
        return response

    def __enter__(self) -> FlaskClient:
        if self.preserve_context:
            raise RuntimeError("Cannot nest client invocations")
        self.preserve_context = True
        return self

    def __exit__(
        self,
        exc_type: type | None,
        exc_value: BaseException | None,
        tb: TracebackType | None,
    ) -> None:
        self.preserve_context = False
        self._context_stack.close()


class FlaskCliRunner(CliRunner):
    """用于测试 Flask 应用 CLI 命令的 :class:`~click.testing.CliRunner`。
    通常通过 :meth:`~flask.Flask.test_cli_runner` 创建。示例见 :ref:`testing-cli`。
    """

    def __init__(self, app: Flask, **kwargs: t.Any) -> None:
        self.app = app
        super().__init__(**kwargs)

    def invoke(  # type: ignore
        self, cli: t.Any = None, args: t.Any = None, **kwargs: t.Any
    ) -> Result:
        """在隔离环境中调用 CLI 命令。完整文档参见
        :meth:`CliRunner.invoke <click.testing.CliRunner.invoke>`。
        示例见 :ref:`testing-cli`。

        若未提供 ``obj`` 参数，则传入知道如何加载被测 Flask 应用的
        :class:`~flask.cli.ScriptInfo` 实例。

        :param cli: 要调用的命令对象。默认为应用的
            :attr:`~flask.app.Flask.cli` 组。
        :param args: 调用命令的参数列表。

        :return: :class:`~click.testing.Result` 对象。
        """
        if cli is None:
            cli = self.app.cli

        if "obj" not in kwargs:
            kwargs["obj"] = ScriptInfo(create_app=lambda: self.app)

        return super().invoke(cli, args, **kwargs)
