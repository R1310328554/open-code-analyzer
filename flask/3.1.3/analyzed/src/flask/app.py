from __future__ import annotations

import collections.abc as cabc
import os
import sys
import typing as t
import weakref
from datetime import timedelta
from inspect import iscoroutinefunction
from itertools import chain
from types import TracebackType
from urllib.parse import quote as _url_quote

import click
from werkzeug.datastructures import Headers
from werkzeug.datastructures import ImmutableDict
from werkzeug.exceptions import BadRequestKeyError
from werkzeug.exceptions import HTTPException
from werkzeug.exceptions import InternalServerError
from werkzeug.routing import BuildError
from werkzeug.routing import MapAdapter
from werkzeug.routing import RequestRedirect
from werkzeug.routing import RoutingException
from werkzeug.routing import Rule
from werkzeug.serving import is_running_from_reloader
from werkzeug.wrappers import Response as BaseResponse
from werkzeug.wsgi import get_host

from . import cli
from . import typing as ft
from .ctx import AppContext
from .ctx import RequestContext
from .globals import _cv_app
from .globals import _cv_request
from .globals import current_app
from .globals import g
from .globals import request
from .globals import request_ctx
from .globals import session
from .helpers import get_debug_flag
from .helpers import get_flashed_messages
from .helpers import get_load_dotenv
from .helpers import send_from_directory
from .sansio.app import App
from .sansio.scaffold import _sentinel
from .sessions import SecureCookieSessionInterface
from .sessions import SessionInterface
from .signals import appcontext_tearing_down
from .signals import got_request_exception
from .signals import request_finished
from .signals import request_started
from .signals import request_tearing_down
from .templating import Environment
from .wrappers import Request
from .wrappers import Response

if t.TYPE_CHECKING:  # pragma: no cover
    from _typeshed.wsgi import StartResponse
    from _typeshed.wsgi import WSGIEnvironment

    from .testing import FlaskClient
    from .testing import FlaskCliRunner
    from .typing import HeadersValue

T_shell_context_processor = t.TypeVar(
    "T_shell_context_processor", bound=ft.ShellContextProcessorCallable
)
T_teardown = t.TypeVar("T_teardown", bound=ft.TeardownCallable)
T_template_filter = t.TypeVar("T_template_filter", bound=ft.TemplateFilterCallable)
T_template_global = t.TypeVar("T_template_global", bound=ft.TemplateGlobalCallable)
T_template_test = t.TypeVar("T_template_test", bound=ft.TemplateTestCallable)


def _make_timedelta(value: timedelta | int | None) -> timedelta | None:
    if value is None or isinstance(value, timedelta):
        return value

    return timedelta(seconds=value)


class Flask(App):
    """
        flask 对象实现 WSGI 应用并作为中心对象。传入应用模块或包的名称。
        创建后作为视图函数、URL 规则、模板配置等的中心注册表。
        
        包名用于解析包内资源或模块所在目录中的资源，取决于 package 参数
        解析为真正的 Python 包（含 :file:`__init__.py` 的文件夹）
        还是标准模块（仅 ``.py`` 文件）。
        
        资源加载详情见 :func:`open_resource`。
        
        通常在主模块或包的 :file:`__init__.py` 中创建 :class:`Flask` 实例::
        
            from flask import Flask
            app = Flask(__name__)
        
        .. admonition:: 关于第一个参数
        
            第一个参数让 Flask 知道哪些属于您的应用。此名称用于在文件系统上查找资源、
            供扩展改善调试信息等。
        
            因此提供什么很重要。单模块时 `__name__` 总是正确值。
            使用包时通常建议硬编码包名。
        
            例如应用在 :file:`yourapplication/app.py` 中定义，
            应使用以下两种方式之一创建::
        
                app = Flask('yourapplication')
                app = Flask(__name__.split('.')[0])
        
            为何？使用 `__name__` 应用仍可工作，因为资源查找机制。
            但调试会更困难。某些扩展会基于导入名做假设。
            例如 Flask-SQLAlchemy 在调试模式下查找触发 SQL 查询的代码。
            若导入名未正确设置，调试信息会丢失。
            （例如只捕获 `yourapplication.app` 而非
            `yourapplication.views.frontend` 中的 SQL 查询）
        
        .. versionadded:: 0.7
           新增 `static_url_path`、`static_folder` 和 `template_folder` 参数。
        
        .. versionadded:: 0.8
           新增 `instance_path` 和 `instance_relative_config` 参数。
        
        .. versionadded:: 0.11
           新增 `root_path` 参数。
        
        .. versionadded:: 1.0
           新增 ``host_matching`` 和 ``static_host`` 参数。
        
        .. versionadded:: 1.0
           新增 ``subdomain_matching`` 参数。子域名匹配现须手动启用。
           设置 :data:`SERVER_NAME` 不再隐式启用。
        
        :param import_name: 应用包名称。
        :param static_url_path: 指定 Web 上静态文件的不同路径。默认为 `static_folder` 文件夹名称。
        :param static_folder: 在 ``static_url_path`` 提供服务的静态文件文件夹。
            相对于应用 ``root_path`` 或绝对路径。默认为 ``'static'``。
        :param static_host: 添加静态路由时使用的主机。默认为 None。
            配置 ``static_folder`` 且 ``host_matching=True`` 时必须设置。
        :param host_matching: 设置 ``url_map.host_matching`` 属性。默认为 False。
        :param subdomain_matching: 匹配路由时考虑相对于 :data:`SERVER_NAME` 的子域名。默认为 False。
        :param template_folder: 应用使用的模板文件夹。默认为应用根路径下的 ``'templates'`` 文件夹。
        :param instance_path: 应用的替代实例路径。默认假设包或模块旁的 ``'instance'`` 文件夹为实例路径。
        :param instance_relative_config: 若为 ``True``，加载配置的相对文件名相对于实例路径而非应用根路径。
        :param root_path: 应用文件根路径。仅在无法自动检测时手动设置，例如命名空间包。
    """

    default_config = ImmutableDict(
        {
            "DEBUG": None,
            "TESTING": False,
            "PROPAGATE_EXCEPTIONS": None,
            "SECRET_KEY": None,
            "SECRET_KEY_FALLBACKS": None,
            "PERMANENT_SESSION_LIFETIME": timedelta(days=31),
            "USE_X_SENDFILE": False,
            "TRUSTED_HOSTS": None,
            "SERVER_NAME": None,
            "APPLICATION_ROOT": "/",
            "SESSION_COOKIE_NAME": "session",
            "SESSION_COOKIE_DOMAIN": None,
            "SESSION_COOKIE_PATH": None,
            "SESSION_COOKIE_HTTPONLY": True,
            "SESSION_COOKIE_SECURE": False,
            "SESSION_COOKIE_PARTITIONED": False,
            "SESSION_COOKIE_SAMESITE": None,
            "SESSION_REFRESH_EACH_REQUEST": True,
            "MAX_CONTENT_LENGTH": None,
            "MAX_FORM_MEMORY_SIZE": 500_000,
            "MAX_FORM_PARTS": 1_000,
            "SEND_FILE_MAX_AGE_DEFAULT": None,
            "TRAP_BAD_REQUEST_ERRORS": None,
            "TRAP_HTTP_EXCEPTIONS": False,
            "EXPLAIN_TEMPLATE_LOADING": False,
            "PREFERRED_URL_SCHEME": "http",
            "TEMPLATES_AUTO_RELOAD": None,
            "MAX_COOKIE_SIZE": 4093,
            "PROVIDE_AUTOMATIC_OPTIONS": True,
        }
    )

    #: The class that is used for request objects.  See :class:`~flask.Request`
    #: for more information.
    request_class: type[Request] = Request

    #: The class that is used for response objects.  See
    #: :class:`~flask.Response` for more information.
    response_class: type[Response] = Response

    #: the session interface to use.  By default an instance of
    #: :class:`~flask.sessions.SecureCookieSessionInterface` is used here.
    #:
    #: .. versionadded:: 0.8
    session_interface: SessionInterface = SecureCookieSessionInterface()

    def __init__(
        self,
        import_name: str,
        static_url_path: str | None = None,
        static_folder: str | os.PathLike[str] | None = "static",
        static_host: str | None = None,
        host_matching: bool = False,
        subdomain_matching: bool = False,
        template_folder: str | os.PathLike[str] | None = "templates",
        instance_path: str | None = None,
        instance_relative_config: bool = False,
        root_path: str | None = None,
    ):
        super().__init__(
            import_name=import_name,
            static_url_path=static_url_path,
            static_folder=static_folder,
            static_host=static_host,
            host_matching=host_matching,
            subdomain_matching=subdomain_matching,
            template_folder=template_folder,
            instance_path=instance_path,
            instance_relative_config=instance_relative_config,
            root_path=root_path,
        )

        #: 用于注册 CLI 命令的 Click 命令组 for this
        #: object. The commands are available from the ``flask`` command
        #: once the application has been discovered and blueprints have
        #: been registered.
        self.cli = cli.AppGroup()

        # Set the name of the Click group in case someone wants to add
        # the app's commands to another CLI tool.
        self.cli.name = self.name

        # Add a static route using the provided static_url_path, static_host,
        # and static_folder if there is a configured static_folder.
        # Note we do this without checking if static_folder exists.
        # For one, it might be created while the server is running (e.g. during
        # development). Also, Google App Engine stores static files somewhere
        if self.has_static_folder:
            assert bool(static_host) == host_matching, (
                "Invalid static_host/host_matching combination"
            )
            # Use a weakref to avoid creating a reference cycle between the app
            # and the view function (see #3761).
            self_ref = weakref.ref(self)
            self.add_url_rule(
                f"{self.static_url_path}/<path:filename>",
                endpoint="static",
                host=static_host,
                view_func=lambda **kw: self_ref().send_static_file(**kw),  # type: ignore
            )

    def get_send_file_max_age(self, filename: str | None) -> int | None:
        """
            供 :func:`send_file` 在未显式传入 ``max_age`` 时根据文件路径确定缓存值。
            
            默认返回配置 :data:`SEND_FILE_MAX_AGE_DEFAULT`，默认为 ``None``，
            让浏览器使用条件请求而非定时缓存。
            
            .. versionchanged:: 2.0
                默认配置由 12 小时改为 ``None``。
            
            .. versionadded:: 0.9
        """
        value = current_app.config["SEND_FILE_MAX_AGE_DEFAULT"]

        if value is None:
            return None

        if isinstance(value, timedelta):
            return int(value.total_seconds())

        return value  # type: ignore[no-any-return]

    def send_static_file(self, filename: str) -> Response:
        """
            从 :attr:`static_folder` 提供静态文件的视图函数。
            若设置了 :attr:`static_folder`，会在 :attr:`static_url_path` 自动注册路由。
            
            .. versionadded:: 0.5
        """
        if not self.has_static_folder:
            raise RuntimeError("'static_folder' must be set to serve static_files.")

        # send_file only knows to call get_send_file_max_age on the app,
        # call it here so it works for blueprints too.
        max_age = self.get_send_file_max_age(filename)
        return send_from_directory(
            t.cast(str, self.static_folder), filename, max_age=max_age
        )

    def open_resource(
        self, resource: str, mode: str = "rb", encoding: str | None = None
    ) -> t.IO[t.AnyStr]:
        """
            以只读方式打开相对于 :attr:`root_path` 的资源文件。
            
            :param resource: 相对于 :attr:`root_path` 的资源路径。
            :param mode: 打开模式，仅支持 ``"r"``（或 ``"rt"``）和 ``"rb"``。
            :param encoding: 文本模式编码，二进制模式忽略。
            
            .. versionchanged:: 3.1
                新增 ``encoding`` 参数。
        """
        if mode not in {"r", "rt", "rb"}:
            raise ValueError("Resources can only be opened for reading.")

        path = os.path.join(self.root_path, resource)

        if mode == "rb":
            return open(path, mode)  # pyright: ignore

        return open(path, mode, encoding=encoding)

    def open_instance_resource(
        self, resource: str, mode: str = "rb", encoding: str | None = "utf-8"
    ) -> t.IO[t.AnyStr]:
        """
            以只读方式打开相对于 :attr:`instance_path` 的资源文件。
            
            :param resource: 相对于 :attr:`instance_path` 的资源路径。
            :param mode: 打开模式。
            :param encoding: 文本模式编码。
            
            .. versionchanged:: 3.1
                新增 ``encoding`` 参数。
        """
        path = os.path.join(self.instance_path, resource)

        if "b" in mode:
            return open(path, mode)

        return open(path, mode, encoding=encoding)

    def create_jinja_environment(self) -> Environment:
        """
            创建并配置 Jinja 环境。
        """
        options = dict(self.jinja_options)

        if "autoescape" not in options:
            options["autoescape"] = self.select_jinja_autoescape

        if "auto_reload" not in options:
            auto_reload = self.config["TEMPLATES_AUTO_RELOAD"]

            if auto_reload is None:
                auto_reload = self.debug

            options["auto_reload"] = auto_reload

        rv = self.jinja_environment(self, **options)
        rv.globals.update(
            url_for=self.url_for,
            get_flashed_messages=get_flashed_messages,
            config=self.config,
            # request, session and g are normally added with the
            # context processor for efficiency reasons but for imported
            # templates we also want the proxies in there.
            request=request,
            session=session,
            g=g,
        )
        rv.policies["json.dumps_function"] = self.json.dumps
        return rv

    def create_url_adapter(self, request: Request | None) -> MapAdapter | None:
        """
            为给定请求创建 URL 适配器，无请求时创建应用适配器。
            
            :param request: 请求对象，或 ``None`` 创建应用级适配器。
        """
        if request is not None:
            if (trusted_hosts := self.config["TRUSTED_HOSTS"]) is not None:
                request.trusted_hosts = trusted_hosts

            # Check trusted_hosts here until bind_to_environ does.
            request.host = get_host(request.environ, request.trusted_hosts)  # pyright: ignore
            subdomain = None
            server_name = self.config["SERVER_NAME"]

            if self.url_map.host_matching:
                # Don't pass SERVER_NAME, otherwise it's used and the actual
                # host is ignored, which breaks host matching.
                server_name = None
            elif not self.subdomain_matching:
                # Werkzeug doesn't implement subdomain matching yet. Until then,
                # disable it by forcing the current subdomain to the default, or
                # the empty string.
                subdomain = self.url_map.default_subdomain or ""

            return self.url_map.bind_to_environ(
                request.environ, server_name=server_name, subdomain=subdomain
            )

        # Need at least SERVER_NAME to match/build outside a request.
        if self.config["SERVER_NAME"] is not None:
            return self.url_map.bind(
                self.config["SERVER_NAME"],
                script_name=self.config["APPLICATION_ROOT"],
                url_scheme=self.config["PREFERRED_URL_SCHEME"],
            )

        return None

    def raise_routing_exception(self, request: Request) -> t.NoReturn:
        """
            重新抛出请求的路由异常。
        """
        if (
            not self.debug
            or not isinstance(request.routing_exception, RequestRedirect)
            or request.routing_exception.code in {307, 308}
            or request.method in {"GET", "HEAD", "OPTIONS"}
        ):
            raise request.routing_exception  # type: ignore[misc]

        from .debughelpers import FormDataRoutingRedirect

        raise FormDataRoutingRedirect(request)

    def update_template_context(self, context: dict[str, t.Any]) -> None:
        """
            用上下文处理器更新模板上下文。
        """
        names: t.Iterable[str | None] = (None,)

        # A template may be rendered outside a request context.
        if request:
            names = chain(names, reversed(request.blueprints))

        # The values passed to render_template take precedence. Keep a
        # copy to re-apply after all context functions.
        orig_ctx = context.copy()

        for name in names:
            if name in self.template_context_processors:
                for func in self.template_context_processors[name]:
                    context.update(self.ensure_sync(func)())

        context.update(orig_ctx)

    def make_shell_context(self) -> dict[str, t.Any]:
        """
            返回 ``flask shell`` 使用的默认 shell 上下文字典。
        """
        rv = {"app": self, "g": g}
        for processor in self.shell_context_processors:
            rv.update(processor())
        return rv

    def run(
        self,
        host: str | None = None,
        port: int | None = None,
        debug: bool | None = None,
        load_dotenv: bool = True,
        **options: t.Any,
    ) -> None:
        """
            运行本地开发服务器。勿在生产环境使用，应使用 WSGI 服务器。
            
            若安装了 python-dotenv，会加载 .env 和 .flaskenv 中的环境变量。
            
            :param host: 监听主机，默认 ``'127.0.0.1'`` 或环境变量 ``FLASK_RUN_HOST``。
            :param port: 监听端口，默认 5000 或 ``FLASK_RUN_PORT``。
            :param debug: 是否启用调试，默认使用 :attr:`debug`。
            :param load_dotenv: 是否加载 dotenv 文件。
            :param options: 传给 Werkzeug ``run_simple`` 的选项。
            
            .. versionchanged:: 1.0
                若安装了 python-dotenv，默认加载 .env 和 .flaskenv。
        """
        # Ignore this call so that it doesn't start another server if
        # the 'flask run' command is used.
        if os.environ.get("FLASK_RUN_FROM_CLI") == "true":
            if not is_running_from_reloader():
                click.secho(
                    " * Ignoring a call to 'app.run()' that would block"
                    " the current 'flask' CLI command.\n"
                    "   Only call 'app.run()' in an 'if __name__ =="
                    ' "__main__"\' guard.',
                    fg="red",
                )

            return

        if get_load_dotenv(load_dotenv):
            cli.load_dotenv()

            # if set, env var overrides existing value
            if "FLASK_DEBUG" in os.environ:
                self.debug = get_debug_flag()

        # debug passed to method overrides all other sources
        if debug is not None:
            self.debug = bool(debug)

        server_name = self.config.get("SERVER_NAME")
        sn_host = sn_port = None

        if server_name:
            sn_host, _, sn_port = server_name.partition(":")

        if not host:
            if sn_host:
                host = sn_host
            else:
                host = "127.0.0.1"

        if port or port == 0:
            port = int(port)
        elif sn_port:
            port = int(sn_port)
        else:
            port = 5000

        options.setdefault("use_reloader", self.debug)
        options.setdefault("use_debugger", self.debug)
        options.setdefault("threaded", True)

        cli.show_server_banner(self.debug, self.name)

        from werkzeug.serving import run_simple

        try:
            run_simple(t.cast(str, host), port, self, **options)
        finally:
            # reset the first request information if the development server
            # reset normally.  This makes it possible to restart the server
            # without reloader and that stuff from an interactive shell.
            self._got_first_request = False

    def test_client(self, use_cookies: bool = True, **kwargs: t.Any) -> FlaskClient:
        """
            创建测试客户端，用于在测试中模拟请求。
            
            见 :doc:`/testing`。
            
            :param use_cookies: 是否在客户端间持久化 cookie。
            :param kwargs: 传给测试客户端构造器的参数。
        """
        cls = self.test_client_class
        if cls is None:
            from .testing import FlaskClient as cls
        return cls(  # type: ignore
            self, self.response_class, use_cookies=use_cookies, **kwargs
        )

    def test_cli_runner(self, **kwargs: t.Any) -> FlaskCliRunner:
        """
            创建 CLI 运行器，用于测试 Click 命令。
        """
        cls = self.test_cli_runner_class

        if cls is None:
            from .testing import FlaskCliRunner as cls

        return cls(self, **kwargs)  # type: ignore

    def handle_http_exception(
        self, e: HTTPException
    ) -> HTTPException | ft.ResponseReturnValue:
        """
            处理 HTTP 异常，返回响应或重新抛出。
        """
        # Proxy exceptions don't have error codes.  We want to always return
        # those unchanged as errors
        if e.code is None:
            return e

        # RoutingExceptions are used internally to trigger routing
        # actions, such as slash redirects raising RequestRedirect. They
        # are not raised or handled in user code.
        if isinstance(e, RoutingException):
            return e

        handler = self._find_error_handler(e, request.blueprints)
        if handler is None:
            return e
        return self.ensure_sync(handler)(e)  # type: ignore[no-any-return]

    def handle_user_exception(
        self, e: Exception
    ) -> HTTPException | ft.ResponseReturnValue:
        """
            处理用户代码抛出的异常。
        """
        if isinstance(e, BadRequestKeyError) and (
            self.debug or self.config["TRAP_BAD_REQUEST_ERRORS"]
        ):
            e.show_exception = True

        if isinstance(e, HTTPException) and not self.trap_http_exception(e):
            return self.handle_http_exception(e)

        handler = self._find_error_handler(e, request.blueprints)

        if handler is None:
            raise

        return self.ensure_sync(handler)(e)  # type: ignore[no-any-return]

    def handle_exception(self, e: Exception) -> Response:
        """
            处理未捕获异常，返回 500 响应。
        """
        exc_info = sys.exc_info()
        got_request_exception.send(self, _async_wrapper=self.ensure_sync, exception=e)
        propagate = self.config["PROPAGATE_EXCEPTIONS"]

        if propagate is None:
            propagate = self.testing or self.debug

        if propagate:
            # Re-raise if called with an active exception, otherwise
            # raise the passed in exception.
            if exc_info[1] is e:
                raise

            raise e

        self.log_exception(exc_info)
        server_error: InternalServerError | ft.ResponseReturnValue
        server_error = InternalServerError(original_exception=e)
        handler = self._find_error_handler(server_error, request.blueprints)

        if handler is not None:
            server_error = self.ensure_sync(handler)(server_error)

        return self.finalize_request(server_error, from_error_handler=True)

    def log_exception(
        self,
        exc_info: (tuple[type, BaseException, TracebackType] | tuple[None, None, None]),
    ) -> None:
        """
            将异常记录到 :attr:`logger`。
        """
        self.logger.error(
            f"Exception on {request.path} [{request.method}]", exc_info=exc_info
        )

    def dispatch_request(self) -> ft.ResponseReturnValue:
        """
            调度请求到视图函数并返回结果。
        """
        req = request_ctx.request
        if req.routing_exception is not None:
            self.raise_routing_exception(req)
        rule: Rule = req.url_rule  # type: ignore[assignment]
        # if we provide automatic options for this URL and the
        # request came with the OPTIONS method, reply automatically
        if (
            getattr(rule, "provide_automatic_options", False)
            and req.method == "OPTIONS"
        ):
            return self.make_default_options_response()
        # otherwise dispatch to the handler for that endpoint
        view_args: dict[str, t.Any] = req.view_args  # type: ignore[assignment]
        return self.ensure_sync(self.view_functions[rule.endpoint])(**view_args)  # type: ignore[no-any-return]

    def full_dispatch_request(self) -> Response:
        """
            完整请求分发，含预处理和错误处理。
        """
        self._got_first_request = True

        try:
            request_started.send(self, _async_wrapper=self.ensure_sync)
            rv = self.preprocess_request()
            if rv is None:
                rv = self.dispatch_request()
        except Exception as e:
            rv = self.handle_user_exception(e)
        return self.finalize_request(rv)

    def finalize_request(
        self,
        rv: ft.ResponseReturnValue | HTTPException,
        from_error_handler: bool = False,
    ) -> Response:
        """
            将视图返回值转换为响应对象。
        """
        response = self.make_response(rv)
        try:
            response = self.process_response(response)
            request_finished.send(
                self, _async_wrapper=self.ensure_sync, response=response
            )
        except Exception:
            if not from_error_handler:
                raise
            self.logger.exception(
                "Request finalizing failed with an error while handling an error"
            )
        return response

    def make_default_options_response(self) -> Response:
        """
            为 OPTIONS 请求创建默认响应。
        """
        adapter = request_ctx.url_adapter
        methods = adapter.allowed_methods()  # type: ignore[union-attr]
        rv = self.response_class()
        rv.allow.update(methods)
        return rv

    def ensure_sync(self, func: t.Callable[..., t.Any]) -> t.Callable[..., t.Any]:
        """
            确保函数以同步方式调用，包装异步函数。
        """
        if iscoroutinefunction(func):
            return self.async_to_sync(func)

        return func

    def async_to_sync(
        self, func: t.Callable[..., t.Coroutine[t.Any, t.Any, t.Any]]
    ) -> t.Callable[..., t.Any]:
        """
            将异步函数转为同步可调用对象。
        """
        try:
            from asgiref.sync import async_to_sync as asgiref_async_to_sync
        except ImportError:
            raise RuntimeError(
                "Install Flask with the 'async' extra in order to use async views."
            ) from None

        return asgiref_async_to_sync(func)

    def url_for(
        self,
        /,
        endpoint: str,
        *,
        _anchor: str | None = None,
        _method: str | None = None,
        _scheme: str | None = None,
        _external: bool | None = None,
        **values: t.Any,
    ) -> str:
        """
            根据端点和参数生成 URL。
            
            :param endpoint: 端点名，以 ``.`` 开头时使用当前蓝图名。
            :param _anchor: URL 片段（``#anchor``）。
            :param _method: 生成该方法的 URL。
            :param _scheme: 外部 URL 的 scheme。
            :param _external: 是否要求外部 URL。
            :param values: URL 变量值及查询参数。
        """
        req_ctx = _cv_request.get(None)

        if req_ctx is not None:
            url_adapter = req_ctx.url_adapter
            blueprint_name = req_ctx.request.blueprint

            # If the endpoint starts with "." and the request matches a
            # blueprint, the endpoint is relative to the blueprint.
            if endpoint[:1] == ".":
                if blueprint_name is not None:
                    endpoint = f"{blueprint_name}{endpoint}"
                else:
                    endpoint = endpoint[1:]

            # When in a request, generate a URL without scheme and
            # domain by default, unless a scheme is given.
            if _external is None:
                _external = _scheme is not None
        else:
            app_ctx = _cv_app.get(None)

            # If called by helpers.url_for, an app context is active,
            # use its url_adapter. Otherwise, app.url_for was called
            # directly, build an adapter.
            if app_ctx is not None:
                url_adapter = app_ctx.url_adapter
            else:
                url_adapter = self.create_url_adapter(None)

            if url_adapter is None:
                raise RuntimeError(
                    "Unable to build URLs outside an active request"
                    " without 'SERVER_NAME' configured. Also configure"
                    " 'APPLICATION_ROOT' and 'PREFERRED_URL_SCHEME' as"
                    " needed."
                )

            # When outside a request, generate a URL with scheme and
            # domain by default.
            if _external is None:
                _external = True

        # It is an error to set _scheme when _external=False, in order
        # to avoid accidental insecure URLs.
        if _scheme is not None and not _external:
            raise ValueError("When specifying '_scheme', '_external' must be True.")

        self.inject_url_defaults(endpoint, values)

        try:
            rv = url_adapter.build(  # type: ignore[union-attr]
                endpoint,
                values,
                method=_method,
                url_scheme=_scheme,
                force_external=_external,
            )
        except BuildError as error:
            values.update(
                _anchor=_anchor, _method=_method, _scheme=_scheme, _external=_external
            )
            return self.handle_url_build_error(error, endpoint, values)

        if _anchor is not None:
            _anchor = _url_quote(_anchor, safe="%!#$&'()*+,/:;=?@")
            rv = f"{rv}#{_anchor}"

        return rv

    def make_response(self, rv: ft.ResponseReturnValue) -> Response:
        """
            将视图返回值转换为响应对象。
        """

        status: int | None = None
        headers: HeadersValue | None = None

        # unpack tuple returns
        if isinstance(rv, tuple):
            len_rv = len(rv)

            # a 3-tuple is unpacked directly
            if len_rv == 3:
                rv, status, headers = rv  # type: ignore[misc]
            # decide if a 2-tuple has status or headers
            elif len_rv == 2:
                if isinstance(rv[1], (Headers, dict, tuple, list)):
                    rv, headers = rv  # pyright: ignore
                else:
                    rv, status = rv  # type: ignore[assignment,misc]
            # other sized tuples are not allowed
            else:
                raise TypeError(
                    "The view function did not return a valid response tuple."
                    " The tuple must have the form (body, status, headers),"
                    " (body, status), or (body, headers)."
                )

        # the body must not be None
        if rv is None:
            raise TypeError(
                f"The view function for {request.endpoint!r} did not"
                " return a valid response. The function either returned"
                " None or ended without a return statement."
            )

        # make sure the body is an instance of the response class
        if not isinstance(rv, self.response_class):
            if isinstance(rv, (str, bytes, bytearray)) or isinstance(rv, cabc.Iterator):
                # let the response class set the status and headers instead of
                # waiting to do it manually, so that the class can handle any
                # special logic
                rv = self.response_class(
                    rv,  # pyright: ignore
                    status=status,
                    headers=headers,  # type: ignore[arg-type]
                )
                status = headers = None
            elif isinstance(rv, (dict, list)):
                rv = self.json.response(rv)
            elif isinstance(rv, BaseResponse) or callable(rv):
                # evaluate a WSGI callable, or coerce a different response
                # class to the correct type
                try:
                    rv = self.response_class.force_type(
                        rv,  # type: ignore[arg-type]
                        request.environ,
                    )
                except TypeError as e:
                    raise TypeError(
                        f"{e}\nThe view function did not return a valid"
                        " response. The return type must be a string,"
                        " dict, list, tuple with headers or status,"
                        " Response instance, or WSGI callable, but it"
                        f" was a {type(rv).__name__}."
                    ).with_traceback(sys.exc_info()[2]) from None
            else:
                raise TypeError(
                    "The view function did not return a valid"
                    " response. The return type must be a string,"
                    " dict, list, tuple with headers or status,"
                    " Response instance, or WSGI callable, but it was a"
                    f" {type(rv).__name__}."
                )

        rv = t.cast(Response, rv)
        # prefer the status if it was provided
        if status is not None:
            if isinstance(status, (str, bytes, bytearray)):
                rv.status = status
            else:
                rv.status_code = status

        # extend existing headers with provided headers
        if headers:
            rv.headers.update(headers)

        return rv

    def preprocess_request(self) -> ft.ResponseReturnValue | None:
        """
            在请求分发前运行 ``before_request`` 函数。
        """
        names = (None, *reversed(request.blueprints))

        for name in names:
            if name in self.url_value_preprocessors:
                for url_func in self.url_value_preprocessors[name]:
                    url_func(request.endpoint, request.view_args)

        for name in names:
            if name in self.before_request_funcs:
                for before_func in self.before_request_funcs[name]:
                    rv = self.ensure_sync(before_func)()

                    if rv is not None:
                        return rv  # type: ignore[no-any-return]

        return None

    def process_response(self, response: Response) -> Response:
        """
            运行 ``after_request`` 函数处理响应。
        """
        ctx = request_ctx._get_current_object()  # type: ignore[attr-defined]

        for func in ctx._after_request_functions:
            response = self.ensure_sync(func)(response)

        for name in chain(request.blueprints, (None,)):
            if name in self.after_request_funcs:
                for func in reversed(self.after_request_funcs[name]):
                    response = self.ensure_sync(func)(response)

        if not self.session_interface.is_null_session(ctx._session):
            self.session_interface.save_session(self, ctx._session, response)

        return response

    def do_teardown_request(
        self,
        exc: BaseException | None = _sentinel,  # type: ignore[assignment]
    ) -> None:
        """
            执行 ``teardown_request`` 函数。
        """
        if exc is _sentinel:
            exc = sys.exc_info()[1]

        for name in chain(request.blueprints, (None,)):
            if name in self.teardown_request_funcs:
                for func in reversed(self.teardown_request_funcs[name]):
                    self.ensure_sync(func)(exc)

        request_tearing_down.send(self, _async_wrapper=self.ensure_sync, exc=exc)

    def do_teardown_appcontext(
        self,
        exc: BaseException | None = _sentinel,  # type: ignore[assignment]
    ) -> None:
        """
            执行 ``teardown_appcontext`` 函数。
        """
        if exc is _sentinel:
            exc = sys.exc_info()[1]

        for func in reversed(self.teardown_appcontext_funcs):
            self.ensure_sync(func)(exc)

        appcontext_tearing_down.send(self, _async_wrapper=self.ensure_sync, exc=exc)

    def app_context(self) -> AppContext:
        """
            创建应用上下文，用作 ``with`` 块。
        """
        return AppContext(self)

    def request_context(self, environ: WSGIEnvironment) -> RequestContext:
        """
            从 WSGI 环境创建请求上下文。
        """
        return RequestContext(self, environ)

    def test_request_context(self, *args: t.Any, **kwargs: t.Any) -> RequestContext:
        """
            创建测试请求上下文，用于测试中模拟请求。
            
            :param path: 请求路径。
            :param kwargs: 其他 WSGI 环境变量。
        """
        from .testing import EnvironBuilder

        builder = EnvironBuilder(self, *args, **kwargs)

        try:
            return self.request_context(builder.get_environ())
        finally:
            builder.close()

    def wsgi_app(
        self, environ: WSGIEnvironment, start_response: StartResponse
    ) -> cabc.Iterable[bytes]:
        """
            实际 WSGI 应用，调用 :meth:`full_dispatch_request`。
            可包装以添加 WSGI 中间件。
            
            .. versionadded:: 0.7
        """
        ctx = self.request_context(environ)
        error: BaseException | None = None
        try:
            try:
                ctx.push()
                response = self.full_dispatch_request()
            except Exception as e:
                error = e
                response = self.handle_exception(e)
            except:
                error = sys.exc_info()[1]
                raise
            return response(environ, start_response)
        finally:
            if "werkzeug.debug.preserve_context" in environ:
                environ["werkzeug.debug.preserve_context"](_cv_app.get())
                environ["werkzeug.debug.preserve_context"](_cv_request.get())

            if error is not None and self.should_ignore_error(error):
                error = None

            ctx.pop(error)

    def __call__(
        self, environ: WSGIEnvironment, start_response: StartResponse
    ) -> cabc.Iterable[bytes]:
        """
            WSGI 服务器调用的入口点，委托给 :meth:`wsgi_app`。
        """
        return self.wsgi_app(environ, start_response)
