from __future__ import annotations

import logging
import os
import sys
import typing as t
from datetime import timedelta
from itertools import chain

from werkzeug.exceptions import Aborter
from werkzeug.exceptions import BadRequest
from werkzeug.exceptions import BadRequestKeyError
from werkzeug.routing import BuildError
from werkzeug.routing import Map
from werkzeug.routing import Rule
from werkzeug.sansio.response import Response
from werkzeug.utils import cached_property
from werkzeug.utils import redirect as _wz_redirect

from .. import typing as ft
from ..config import Config
from ..config import ConfigAttribute
from ..ctx import _AppCtxGlobals
from ..helpers import _split_blueprint_path
from ..helpers import get_debug_flag
from ..json.provider import DefaultJSONProvider
from ..json.provider import JSONProvider
from ..logging import create_logger
from ..templating import DispatchingJinjaLoader
from ..templating import Environment
from .scaffold import _endpoint_from_view_func
from .scaffold import find_package
from .scaffold import Scaffold
from .scaffold import setupmethod

if t.TYPE_CHECKING:  # pragma: no cover
    from werkzeug.wrappers import Response as BaseResponse

    from ..testing import FlaskClient
    from ..testing import FlaskCliRunner
    from .blueprints import Blueprint

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


class App(Scaffold):
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

    #: 赋给 :attr:`aborter` 的类，由 :meth:`create_aborter` 创建。, created by
    #: :meth:`create_aborter`. That object is called by
    #: :func:`flask.abort` 抛出 HTTP 错误，也可直接调用。, and can be
    #: called directly as well.
    #:
    #: 默认为 :class:`werkzeug.exceptions.Aborter`。
    #:
    #: .. versionadded:: 2.2
    aborter_class = Aborter

    #: 用于 Jinja 环境的类。
    #:
    #: .. versionadded:: 0.11
    jinja_environment = Environment

    #: 用于 :data:`~flask.g` 实例的类。
    #:
    #: 自定义类的典型用例：
    #:
    #: 1. 在 flask.g 上存储任意属性。
    #: 2. 添加惰性每请求数据库连接器的属性。
    #: 3. 意外属性返回 None 而非 AttributeError。
    #: 4. 设置意外属性时抛出异常，实现受控的 flask.g。, a "controlled" flask.g.
    #:
    #: In Flask 0.9 this property was called `request_globals_class` but it
    #: was changed in 0.10 to :attr:`app_ctx_globals_class` because the
    #: flask.g object is now application context scoped.
    #:
    #: .. versionadded:: 0.10
    app_ctx_globals_class = _AppCtxGlobals

    #: 用于此应用 ``config`` 属性的类。
    #: 默认为 :class:`~flask.Config`。
    #:
    #: 自定义类的典型用例：
    #:
    #: 1. 某些配置项的默认值。
    #: 2. 除键外还可通过属性访问配置值。
    #:
    #: .. versionadded:: 0.11
    config_class = Config

    #: 测试标志。设为 ``True`` 启用 Flask 扩展的测试模式。  Set this to ``True`` to enable the test mode of
    #: Flask extensions (and in the future probably also Flask itself).
    #: For example this might activate test helpers that have an
    #: additional runtime cost which should not be enabled by default.
    #:
    #: If this is enabled and PROPAGATE_EXCEPTIONS is not changed from the
    #: default it's implicitly enabled.
    #:
    #: This attribute can also be configured from the config with the
    #: ``TESTING`` configuration key.  Defaults to ``False``.
    testing = ConfigAttribute[bool]("TESTING")

    #: If a secret key is set, cryptographic components can use this to
    #: sign cookies and other things. Set this to a complex random value
    #: when you want to use the secure cookie for instance.
    #:
    #: This attribute can also be configured from the config with the
    #: :data:`SECRET_KEY` configuration key. Defaults to ``None``.
    secret_key = ConfigAttribute[t.Union[str, bytes, None]]("SECRET_KEY")

    #: A :class:`~datetime.timedelta` which is used to set the expiration
    #: date of a permanent session.  The default is 31 days which makes a
    #: permanent session survive for roughly one month.
    #:
    #: This attribute can also be configured from the config with the
    #: ``PERMANENT_SESSION_LIFETIME`` configuration key.  Defaults to
    #: ``timedelta(days=31)``
    permanent_session_lifetime = ConfigAttribute[timedelta](
        "PERMANENT_SESSION_LIFETIME",
        get_converter=_make_timedelta,  # type: ignore[arg-type]
    )

    json_provider_class: type[JSONProvider] = DefaultJSONProvider
    """A subclass of :class:`~flask.json.provider.JSONProvider`. An
    instance is created and assigned to :attr:`app.json` when creating
    the app.

    The default, :class:`~flask.json.provider.DefaultJSONProvider`, uses
    Python's built-in :mod:`json` library. A different provider can use
    a different JSON library.

    .. versionadded:: 2.2
    """

    #: Options that are passed to the Jinja environment in
    #: :meth:`create_jinja_environment`. Changing these options after
    #: the environment is created (accessing :attr:`jinja_env`) will
    #: have no effect.
    #:
    #: .. versionchanged:: 1.1.0
    #:     This is a ``dict`` instead of an ``ImmutableDict`` to allow
    #:     easier configuration.
    #:
    jinja_options: dict[str, t.Any] = {}

    #: The rule object to use for URL rules created.  This is used by
    #: :meth:`add_url_rule`.  Defaults to :class:`werkzeug.routing.Rule`.
    #:
    #: .. versionadded:: 0.7
    url_rule_class = Rule

    #: The map object to use for storing the URL rules and routing
    #: configuration parameters. Defaults to :class:`werkzeug.routing.Map`.
    #:
    #: .. versionadded:: 1.1.0
    url_map_class = Map

    #: The :meth:`test_client` method creates an instance of this test
    #: client class. Defaults to :class:`~flask.testing.FlaskClient`.
    #:
    #: .. versionadded:: 0.7
    test_client_class: type[FlaskClient] | None = None

    #: The :class:`~click.testing.CliRunner` subclass, by default
    #: :class:`~flask.testing.FlaskCliRunner` that is used by
    #: :meth:`test_cli_runner`. Its ``__init__`` method should take a
    #: Flask app object as the first argument.
    #:
    #: .. versionadded:: 1.0
    test_cli_runner_class: type[FlaskCliRunner] | None = None

    default_config: dict[str, t.Any]
    response_class: type[Response]

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
    ) -> None:
        super().__init__(
            import_name=import_name,
            static_folder=static_folder,
            static_url_path=static_url_path,
            template_folder=template_folder,
            root_path=root_path,
        )

        if instance_path is None:
            instance_path = self.auto_find_instance_path()
        elif not os.path.isabs(instance_path):
            raise ValueError(
                "If an instance path is provided it must be absolute."
                " A relative path was given instead."
            )

        #: Holds the path to the instance folder.
        #:
        #: .. versionadded:: 0.8
        self.instance_path = instance_path

        #: The configuration dictionary as :class:`Config`.  This behaves
        #: exactly like a regular dictionary but supports additional methods
        #: to load a config from files.
        self.config = self.make_config(instance_relative_config)

        #: An instance of :attr:`aborter_class` created by
        #: :meth:`make_aborter`. This is called by :func:`flask.abort`
        #: to raise HTTP errors, and can be called directly as well.
        #:
        #: .. versionadded:: 2.2
        #:     Moved from ``flask.abort``, which calls this object.
        self.aborter = self.make_aborter()

        self.json: JSONProvider = self.json_provider_class(self)
        """Provides access to JSON methods. Functions in ``flask.json``
        will call methods on this provider when the application context
        is active. Used for handling JSON requests and responses.

        An instance of :attr:`json_provider_class`. Can be customized by
        changing that attribute on a subclass, or by assigning to this
        attribute afterwards.

        The default, :class:`~flask.json.provider.DefaultJSONProvider`,
        uses Python's built-in :mod:`json` library. A different provider
        can use a different JSON library.

        .. versionadded:: 2.2
        """

        #: A list of functions that are called by
        #: :meth:`handle_url_build_error` when :meth:`.url_for` raises a
        #: :exc:`~werkzeug.routing.BuildError`. Each function is called
        #: with ``error``, ``endpoint`` and ``values``. If a function
        #: returns ``None`` or raises a ``BuildError``, it is skipped.
        #: Otherwise, its return value is returned by ``url_for``.
        #:
        #: .. versionadded:: 0.9
        self.url_build_error_handlers: list[
            t.Callable[[Exception, str, dict[str, t.Any]], str]
        ] = []

        #: A list of functions that are called when the application context
        #: is destroyed.  Since the application context is also torn down
        #: if the request ends this is the place to store code that disconnects
        #: from databases.
        #:
        #: .. versionadded:: 0.9
        self.teardown_appcontext_funcs: list[ft.TeardownCallable] = []

        #: A list of shell context processor functions that should be run
        #: when a shell context is created.
        #:
        #: .. versionadded:: 0.11
        self.shell_context_processors: list[ft.ShellContextProcessorCallable] = []

        #: Maps registered blueprint names to blueprint objects. The
        #: dict retains the order the blueprints were registered in.
        #: Blueprints can be registered multiple times, this dict does
        #: not track how often they were attached.
        #:
        #: .. versionadded:: 0.7
        self.blueprints: dict[str, Blueprint] = {}

        #: a place where extensions can store application specific state.  For
        #: example this is where an extension could store database engines and
        #: similar things.
        #:
        # 模块
        #: case of a "Flask-Foo" extension in `flask_foo`, the key would be
        #: ``'foo'``.
        #:
        #: .. versionadded:: 0.7
        self.extensions: dict[str, t.Any] = {}

        #: The :class:`~werkzeug.routing.Map` for this instance.  You can use
        #: this to change the routing converters after the class was created
        #: but before any routes are connected.  Example::
        #:
        #:    from werkzeug.routing import BaseConverter
        #:
        #:    class ListConverter(BaseConverter):
        #:        def to_python(self, value):
        #:            return value.split(',')
        #:        def to_url(self, values):
        #:            return ','.join(super(ListConverter, self).to_url(value)
        #:                            for value in values)
        #:
        #:    app = Flask(__name__)
        #:    app.url_map.converters['list'] = ListConverter
        self.url_map = self.url_map_class(host_matching=host_matching)

        self.subdomain_matching = subdomain_matching

        # tracks internally if the application already handled at least one
        # request.
        self._got_first_request = False

    def _check_setup_finished(self, f_name: str) -> None:
        if self._got_first_request:
            raise AssertionError(
                f"The setup method '{f_name}' can no longer be called"
                " on the application. It has already handled its first"
                " request, any changes will not be applied"
                " consistently.\n"
                "Make sure all imports, decorators, functions, etc."
                " needed to set up the application are done before"
                " running it."
            )

    @cached_property
    def name(self) -> str:
        """
            应用名称。通常为导入名；若导入名为 main 则从运行文件猜测。
            Flask 需要应用名称时用作显示名，可设置和覆盖。
            
            .. versionadded:: 0.8
        """
        if self.import_name == "__main__":
            fn: str | None = getattr(sys.modules["__main__"], "__file__", None)
            if fn is None:
                return "__main__"
            return os.path.splitext(os.path.basename(fn))[0]
        return self.import_name

    @cached_property
    def logger(self) -> logging.Logger:
        """
            应用的 :class:`logging.Logger`，用于 Flask 内部日志及应用日志。
            
            每个请求不会创建新日志器，配置一次即可。
            
            .. versionadded:: 0.3
        """
        return create_logger(self)

    @cached_property
    def jinja_env(self) -> Environment:
        """
            应用的 Jinja 环境，在首次访问时惰性创建。
        """
        return self.create_jinja_environment()

    def create_jinja_environment(self) -> Environment:
        raise NotImplementedError()

    def make_config(self, instance_relative: bool = False) -> Config:
        """
            用于创建 :attr:`config` 的工厂方法。默认创建 :class:`~flask.Config` 实例。
            
            测试时可用此方法创建不同配置的实例。
            
            :param instance_relative: 是否使用相对实例路径加载配置。
        """
        root_path = self.root_path
        if instance_relative:
            root_path = self.instance_path
        defaults = dict(self.default_config)
        defaults["DEBUG"] = get_debug_flag()
        return self.config_class(root_path, defaults)

    def make_aborter(self) -> Aborter:
        """
            创建 :attr:`aborter` 对象的工厂方法。
        """
        return self.aborter_class()

    def auto_find_instance_path(self) -> str:
        """
            尝试定位实例路径，若未配置则调用。
        """
        prefix, package_path = find_package(self.import_name)
        if prefix is None:
            return os.path.join(package_path, "instance")
        return os.path.join(prefix, "var", f"{self.name}-instance")

    def create_global_jinja_loader(self) -> DispatchingJinjaLoader:
        """
            创建用于从应用和蓝图加载模板的 Jinja 加载器。
        """
        return DispatchingJinjaLoader(self)

    def select_jinja_autoescape(self, filename: str | None) -> bool:
        """
            根据模板文件名选择是否自动转义。默认对 ``.html``、``.htm``、
            ``.xml``、``.xhtml`` 启用自动转义。
            
            .. versionadded:: 0.5
        """
        if filename is None:
            return True
        return filename.endswith((".html", ".htm", ".xml", ".xhtml", ".svg"))

    @property
    def debug(self) -> bool:
        """
            是否启用调试模式。
        """
        return self.config["DEBUG"]  # type: ignore[no-any-return]

    @debug.setter
    def debug(self, value: bool) -> None:
        self.config["DEBUG"] = value

        if self.config["TEMPLATES_AUTO_RELOAD"] is None:
            self.jinja_env.auto_reload = value

    @setupmethod
    def register_blueprint(self, blueprint: Blueprint, **options: t.Any) -> None:
        """
            在应用上注册蓝图。注册蓝图上定义的所有视图和回调。
            
            :param blueprint: 要注册的蓝图。
            :param url_prefix: 注册时覆盖蓝图的 URL 前缀。
            :param subdomain: 注册时覆盖蓝图的子域名。
            :param url_defaults: 注册时覆盖蓝图的 URL 默认值。
            :param options: 额外选项，转发给蓝图的 :meth:`~flask.Blueprint.register`。
            
            .. versionchanged:: 2.0.1
                ``name`` 选项可更改注册名称。
            
            .. versionadded:: 0.7
        """
        blueprint.register(self, options)

    def iter_blueprints(self) -> t.ValuesView[Blueprint]:
        """
            返回按注册顺序排列的蓝图名称元组。
        """
        return self.blueprints.values()

    @setupmethod
    def add_url_rule(
        self,
        rule: str,
        endpoint: str | None = None,
        view_func: ft.RouteCallable | None = None,
        provide_automatic_options: bool | None = None,
        **options: t.Any,
    ) -> None:
        if endpoint is None:
            endpoint = _endpoint_from_view_func(view_func)  # type: ignore
        options["endpoint"] = endpoint
        methods = options.pop("methods", None)

        # if the methods are not given and the view_func object knows its
        # methods we can use that instead.  If neither exists, we go with
        # a tuple of only ``GET`` as default.
        if methods is None:
            methods = getattr(view_func, "methods", None) or ("GET",)
        if isinstance(methods, str):
            raise TypeError(
                "Allowed methods must be a list of strings, for"
                ' example: @app.route(..., methods=["POST"])'
            )
        methods = {item.upper() for item in methods}

        # Methods that should always be added
        required_methods: set[str] = set(getattr(view_func, "required_methods", ()))

        # starting with Flask 0.8 the view_func object can disable and
        # force-enable the automatic options handling.
        if provide_automatic_options is None:
            provide_automatic_options = getattr(
                view_func, "provide_automatic_options", None
            )

        if provide_automatic_options is None:
            if "OPTIONS" not in methods and self.config["PROVIDE_AUTOMATIC_OPTIONS"]:
                provide_automatic_options = True
                required_methods.add("OPTIONS")
            else:
                provide_automatic_options = False

        # Add the required methods now.
        methods |= required_methods

        rule_obj = self.url_rule_class(rule, methods=methods, **options)
        rule_obj.provide_automatic_options = provide_automatic_options  # type: ignore[attr-defined]

        self.url_map.add(rule_obj)
        if view_func is not None:
            old_func = self.view_functions.get(endpoint)
            if old_func is not None and old_func != view_func:
                raise AssertionError(
                    "View function mapping is overwriting an existing"
                    f" endpoint function: {endpoint}"
                )
            self.view_functions[endpoint] = view_func

    @setupmethod
    def template_filter(
        self, name: str | None = None
    ) -> t.Callable[[T_template_filter], T_template_filter]:
        """
            注册模板过滤器。
            
            :param name: 过滤器可选名称，默认使用函数名。
        """

        def decorator(f: T_template_filter) -> T_template_filter:
            self.add_template_filter(f, name=name)
            return f

        return decorator

    @setupmethod
    def add_template_filter(
        self, f: ft.TemplateFilterCallable, name: str | None = None
    ) -> None:
        """
            注册模板过滤器，等价于 :meth:`template_filter` 装饰器。
            
            :param name: 过滤器可选名称。
        """
        self.jinja_env.filters[name or f.__name__] = f

    @setupmethod
    def template_test(
        self, name: str | None = None
    ) -> t.Callable[[T_template_test], T_template_test]:
        """
            注册模板测试。
            
            .. versionadded:: 0.10
            
            :param name: 测试可选名称。
        """

        def decorator(f: T_template_test) -> T_template_test:
            self.add_template_test(f, name=name)
            return f

        return decorator

    @setupmethod
    def add_template_test(
        self, f: ft.TemplateTestCallable, name: str | None = None
    ) -> None:
        """
            注册模板测试，等价于 :meth:`template_test`。
            
            .. versionadded:: 0.10
            
            :param name: 测试可选名称。
        """
        self.jinja_env.tests[name or f.__name__] = f

    @setupmethod
    def template_global(
        self, name: str | None = None
    ) -> t.Callable[[T_template_global], T_template_global]:
        """
            注册模板全局变量。
            
            .. versionadded:: 0.10
            
            :param name: 全局变量可选名称。
        """

        def decorator(f: T_template_global) -> T_template_global:
            self.add_template_global(f, name=name)
            return f

        return decorator

    @setupmethod
    def add_template_global(
        self, f: ft.TemplateGlobalCallable, name: str | None = None
    ) -> None:
        """
            注册模板全局变量，等价于 :meth:`template_global`。
            
            .. versionadded:: 0.10
            
            :param name: 全局变量可选名称。
        """
        self.jinja_env.globals[name or f.__name__] = f

    @setupmethod
    def teardown_appcontext(self, f: T_teardown) -> T_teardown:
        """
            注册在应用上下文弹出时调用的函数。
        """
        self.teardown_appcontext_funcs.append(f)
        return f

    @setupmethod
    def shell_context_processor(
        self, f: T_shell_context_processor
    ) -> T_shell_context_processor:
        """
            注册 shell 上下文处理器，``flask shell`` 命令使用。
        """
        self.shell_context_processors.append(f)
        return f

    def _find_error_handler(
        self, e: Exception, blueprints: list[str]
    ) -> ft.ErrorHandlerCallable | None:
        """
            查找给定异常的错误处理器。
        """
        exc_class, code = self._get_exc_class_and_code(type(e))
        names = (*blueprints, None)

        for c in (code, None) if code is not None else (None,):
            for name in names:
                handler_map = self.error_handler_spec[name][c]

                if not handler_map:
                    continue

                for cls in exc_class.__mro__:
                    handler = handler_map.get(cls)

                    if handler is not None:
                        return handler
        return None

    def trap_http_exception(self, e: Exception) -> bool:
        """
            是否应捕获给定 HTTP 异常。
        """
        if self.config["TRAP_HTTP_EXCEPTIONS"]:
            return True

        trap_bad_request = self.config["TRAP_BAD_REQUEST_ERRORS"]

        # if unset, trap key errors in debug mode
        if (
            trap_bad_request is None
            and self.debug
            and isinstance(e, BadRequestKeyError)
        ):
            return True

        if trap_bad_request:
            return isinstance(e, BadRequest)

        return False

    def should_ignore_error(self, error: BaseException | None) -> bool:
        """
            是否应忽略给定错误（不传播、不记录）。
        """
        return False

    def redirect(self, location: str, code: int = 302) -> BaseResponse:
        """
            创建重定向响应。包装 :func:`werkzeug.utils.redirect`，
            使用 :attr:`response_class`。
            
            :param location: 重定向 URL。
            :param code: 状态码。
            
            .. versionadded:: 2.2
        """
        return _wz_redirect(
            location,
            code=code,
            Response=self.response_class,  # type: ignore[arg-type]
        )

    def inject_url_defaults(self, endpoint: str, values: dict[str, t.Any]) -> None:
        """
            将 URL 默认值注入端点。
        """
        names: t.Iterable[str | None] = (None,)

        # url_for may be called outside a request context, parse the
        # passed endpoint instead of using request.blueprints.
        if "." in endpoint:
            names = chain(
                names, reversed(_split_blueprint_path(endpoint.rpartition(".")[0]))
            )

        for name in names:
            if name in self.url_default_functions:
                for func in self.url_default_functions[name]:
                    func(endpoint, values)

    def handle_url_build_error(
        self, error: BuildError, endpoint: str, values: dict[str, t.Any]
    ) -> str:
        """
            处理 :meth:`url_for` 构建 URL 时的错误。
        """
        for handler in self.url_build_error_handlers:
            try:
                rv = handler(error, endpoint, values)
            except BuildError as e:
                # make error available outside except block
                error = e
            else:
                if rv is not None:
                    return rv

        # Re-raise if called with an active exception, otherwise raise
        # the passed in exception.
        if error is sys.exc_info()[1]:
            raise

        raise error
