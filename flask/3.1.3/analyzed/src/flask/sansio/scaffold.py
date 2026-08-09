from __future__ import annotations

import importlib.util
import os
import pathlib
import sys
import typing as t
from collections import defaultdict
from functools import update_wrapper

from jinja2 import BaseLoader
from jinja2 import FileSystemLoader
from werkzeug.exceptions import default_exceptions
from werkzeug.exceptions import HTTPException
from werkzeug.utils import cached_property

from .. import typing as ft
from ..helpers import get_root_path
from ..templating import _default_template_ctx_processor

if t.TYPE_CHECKING:  # pragma: no cover
    from click import Group

# a singleton sentinel value for parameter defaults
_sentinel = object()

F = t.TypeVar("F", bound=t.Callable[..., t.Any])
T_after_request = t.TypeVar("T_after_request", bound=ft.AfterRequestCallable[t.Any])
T_before_request = t.TypeVar("T_before_request", bound=ft.BeforeRequestCallable)
T_error_handler = t.TypeVar("T_error_handler", bound=ft.ErrorHandlerCallable)
T_teardown = t.TypeVar("T_teardown", bound=ft.TeardownCallable)
T_template_context_processor = t.TypeVar(
    "T_template_context_processor", bound=ft.TemplateContextProcessorCallable
)
T_url_defaults = t.TypeVar("T_url_defaults", bound=ft.URLDefaultCallable)
T_url_value_preprocessor = t.TypeVar(
    "T_url_value_preprocessor", bound=ft.URLValuePreprocessorCallable
)
T_route = t.TypeVar("T_route", bound=ft.RouteCallable)


def setupmethod(f: F) -> F:
    f_name = f.__name__

    def wrapper_func(self: Scaffold, *args: t.Any, **kwargs: t.Any) -> t.Any:
        self._check_setup_finished(f_name)
        return f(self, *args, **kwargs)

    return t.cast(F, update_wrapper(wrapper_func, f))


class Scaffold:
    """
        :class:`~flask.Flask` 与 :class:`~flask.blueprints.Blueprint` 共享的通用行为。
        
        :param import_name: 定义此对象的模块导入名，通常用 :attr:`__name__`。
        :param static_folder: 要提供的静态文件文件夹路径。设置后会添加静态路由。
        :param static_url_path: 静态路由的 URL 前缀。
        :param template_folder: 包含模板文件的文件夹路径，用于渲染。设置后会添加 Jinja 加载器。
        :param root_path: 静态、模板和资源文件相对的根路径。通常自动检测，无需设置。
        
        .. versionadded:: 2.0
    """

    cli: Group
    name: str
    _static_folder: str | None = None
    _static_url_path: str | None = None

    def __init__(
        self,
        import_name: str,
        static_folder: str | os.PathLike[str] | None = None,
        static_url_path: str | None = None,
        template_folder: str | os.PathLike[str] | None = None,
        root_path: str | None = None,
    ):
        #: 此对象所属的包或模块名称；构造后请勿修改。
        #: to. Do not change this once it is set by the constructor.
        self.import_name = import_name

        self.static_folder = static_folder
        self.static_url_path = static_url_path

        #: 模板文件夹路径，相对于 :attr:`root_path`；``None`` 表示不添加模板。, relative to
        #: :attr:`root_path`, to add to the template loader. ``None`` if
        #: templates should not be added.
        self.template_folder = template_folder

        if root_path is None:
            root_path = get_root_path(self.import_name)

        #: 文件系统上包的绝对路径，用于查找包内资源。 Used to look
        #: up resources contained in the package.
        self.root_path = root_path

        #: 端点名到视图函数的映射。
        #:
        #: 注册视图函数请使用 :meth:`route` 装饰器。
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.view_functions: dict[str, ft.RouteCallable] = {}

        #: 已注册错误处理器，格式 ``{scope: {code: {class: handler}}}``。, in the format
        #: ``{scope: {code: {class: handler}}}``. The ``scope`` key is
        #: the name of a blueprint the handlers are active for, or
        #: ``None`` for all requests. The ``code`` key is the HTTP
        #: status code for ``HTTPException``, or ``None`` for
        #: other exceptions. The innermost dictionary maps exception
        #: classes to handler functions.
        #:
        #: 注册错误处理器请使用 :meth:`errorhandler` 装饰器。
        #: decorator.
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.error_handler_spec: dict[
            ft.AppOrBlueprintKey,
            dict[int | None, dict[type[Exception], ft.ErrorHandlerCallable]],
        ] = defaultdict(lambda: defaultdict(dict))

        #: 每个请求开始时调用的函数，格式 ``{scope: [functions]}``。
        #: each request, in the format ``{scope: [functions]}``. The
        #: ``scope`` key is the name of a blueprint the functions are
        #: active for, or ``None`` for all requests.
        #:
        #: 注册函数请使用 :meth:`before_request` 装饰器。
        #: decorator.
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.before_request_funcs: dict[
            ft.AppOrBlueprintKey, list[ft.BeforeRequestCallable]
        ] = defaultdict(list)

        #: 每个请求结束时调用的函数，格式 ``{scope: [functions]}``。
        #: request, in the format ``{scope: [functions]}``. The
        #: ``scope`` key is the name of a blueprint the functions are
        #: active for, or ``None`` for all requests.
        #:
        #: 注册函数请使用 :meth:`after_request` 装饰器。
        #: decorator.
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.after_request_funcs: dict[
            ft.AppOrBlueprintKey, list[ft.AfterRequestCallable[t.Any]]
        ] = defaultdict(list)

        #: 每个请求结束时调用的函数，格式 ``{scope: [functions]}``。
        #: 即使发生异常也在每个请求结束时调用。, in the format
        #: ``{scope: [functions]}``. The ``scope`` key is the name of a
        #: blueprint the functions are active for, or ``None`` for all
        #: requests.
        #:
        #: 注册函数请使用 :meth:`teardown_request` 装饰器。
        #: decorator.
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.teardown_request_funcs: dict[
            ft.AppOrBlueprintKey, list[ft.TeardownCallable]
        ] = defaultdict(list)

        #: 渲染模板时传递额外上下文的函数。
        #: values when rendering templates, in the format
        #: ``{scope: [functions]}``. The ``scope`` key is the name of a
        #: blueprint the functions are active for, or ``None`` for all
        #: requests.
        #:
        #: 注册函数请使用 :meth:`context_processor` 装饰器。
        #: decorator.
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.template_context_processors: dict[
            ft.AppOrBlueprintKey, list[ft.TemplateContextProcessorCallable]
        ] = defaultdict(list, {None: [_default_template_ctx_processor]})

        #: A data structure of functions to call to modify the keyword
        #: 修改传给视图函数的关键字参数。, in the format
        #: ``{scope: [functions]}``. The ``scope`` key is the name of a
        #: blueprint the functions are active for, or ``None`` for all
        #: requests.
        #:
        #: To register a function, use the
        #: :meth:`url_value_preprocessor` 装饰器。
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.url_value_preprocessors: dict[
            ft.AppOrBlueprintKey,
            list[ft.URLValuePreprocessorCallable],
        ] = defaultdict(list)

        #: A data structure of functions to call to modify the keyword
        #: 生成 URL 时修改关键字参数。, in the format
        #: ``{scope: [functions]}``. The ``scope`` key is the name of a
        #: blueprint the functions are active for, or ``None`` for all
        #: requests.
        #:
        #: 注册函数请使用 :meth:`url_defaults` 装饰器。
        #: decorator.
        #:
        #: 此数据结构为内部实现，请勿直接修改。 It should not be modified
        #: directly and its format may change at any time.
        self.url_default_functions: dict[
            ft.AppOrBlueprintKey, list[ft.URLDefaultCallable]
        ] = defaultdict(list)

    def __repr__(self) -> str:
        return f"<{type(self).__name__} {self.name!r}>"

    def _check_setup_finished(self, f_name: str) -> None:
        raise NotImplementedError

    @property
    def static_folder(self) -> str | None:
        """
            已配置静态文件夹的绝对路径。未设置时为 ``None``。
        """
        if self._static_folder is not None:
            return os.path.join(self.root_path, self._static_folder)
        else:
            return None

    @static_folder.setter
    def static_folder(self, value: str | os.PathLike[str] | None) -> None:
        if value is not None:
            value = os.fspath(value).rstrip(r"\/")

        self._static_folder = value

    @property
    def has_static_folder(self) -> bool:
        """
            若设置了 :attr:`static_folder` 则为 ``True``。
            
            .. versionadded:: 0.5
        """
        return self.static_folder is not None

    @property
    def static_url_path(self) -> str | None:
        """
            静态路由可访问的 URL 前缀。
            若初始化时未配置，从 :attr:`static_folder` 推导。
        """
        if self._static_url_path is not None:
            return self._static_url_path

        if self.static_folder is not None:
            basename = os.path.basename(self.static_folder)
            return f"/{basename}".rstrip("/")

        return None

    @static_url_path.setter
    def static_url_path(self, value: str | None) -> None:
        if value is not None:
            value = value.rstrip("/")

        self._static_url_path = value

    @cached_property
    def jinja_loader(self) -> BaseLoader | None:
        """
            此对象模板的 Jinja 加载器。默认若设置了 :attr:`template_folder`，
            使用指向该文件夹的 :class:`jinja2.loaders.FileSystemLoader`。
            
            .. versionadded:: 0.5
        """
        if self.template_folder is not None:
            return FileSystemLoader(os.path.join(self.root_path, self.template_folder))
        else:
            return None

    def _method_route(
        self,
        method: str,
        rule: str,
        options: dict[str, t.Any],
    ) -> t.Callable[[T_route], T_route]:
        if "methods" in options:
            raise TypeError("Use the 'route' decorator to use the 'methods' argument.")

        return self.route(rule, methods=[method], **options)

    @setupmethod
    def get(self, rule: str, **options: t.Any) -> t.Callable[[T_route], T_route]:
        """
            :meth:`route` 的快捷方式，``methods=["GET"]``。
            
            .. versionadded:: 2.0
        """
        return self._method_route("GET", rule, options)

    @setupmethod
    def post(self, rule: str, **options: t.Any) -> t.Callable[[T_route], T_route]:
        """
            :meth:`route` 的快捷方式，``methods=["POST"]``。
            
            .. versionadded:: 2.0
        """
        return self._method_route("POST", rule, options)

    @setupmethod
    def put(self, rule: str, **options: t.Any) -> t.Callable[[T_route], T_route]:
        """
            :meth:`route` 的快捷方式，``methods=["PUT"]``。
            
            .. versionadded:: 2.0
        """
        return self._method_route("PUT", rule, options)

    @setupmethod
    def delete(self, rule: str, **options: t.Any) -> t.Callable[[T_route], T_route]:
        """
            :meth:`route` 的快捷方式，``methods=["DELETE"]``。
            
            .. versionadded:: 2.0
        """
        return self._method_route("DELETE", rule, options)

    @setupmethod
    def patch(self, rule: str, **options: t.Any) -> t.Callable[[T_route], T_route]:
        """
            :meth:`route` 的快捷方式，``methods=["PATCH"]``。
            
            .. versionadded:: 2.0
        """
        return self._method_route("PATCH", rule, options)

    @setupmethod
    def route(self, rule: str, **options: t.Any) -> t.Callable[[T_route], T_route]:
        """
            装饰视图函数，以给定 URL 规则和选项注册。调用 :meth:`add_url_rule`，实现细节见其说明。
            
            .. code-block:: python
            
                @app.route("/")
                def index():
                    return "Hello, World!"
            
            见 :ref:`url-route-registrations`。
            
            未传 ``endpoint`` 时端点名默认为视图函数名。
            ``methods`` 默认为 ``["GET"]``，自动添加 ``HEAD`` 和 ``OPTIONS``。
            
            :param rule: URL 规则字符串。
            :param options: 传给 :class:`~werkzeug.routing.Rule` 的额外选项。
        """

        def decorator(f: T_route) -> T_route:
            endpoint = options.pop("endpoint", None)
            self.add_url_rule(rule, endpoint, f, **options)
            return f

        return decorator

    @setupmethod
    def add_url_rule(
        self,
        rule: str,
        endpoint: str | None = None,
        view_func: ft.RouteCallable | None = None,
        provide_automatic_options: bool | None = None,
        **options: t.Any,
    ) -> None:
        """
            注册路由规则和构建 URL 的规则。:meth:`route` 装饰器是传入 ``view_func`` 的快捷方式。
            
            未传 ``endpoint`` 时默认为视图函数名；端点已注册会报错。
            ``methods`` 默认为 ``["GET"]``，自动添加 ``HEAD`` 和 ``OPTIONS``。
            
            :param rule: URL 规则字符串。
            :param endpoint: 与规则和视图函数关联的端点名，用于路由和 :func:`url_for`。
                默认为 ``view_func.__name__``。
            :param view_func: 与端点名关联的视图函数。
            :param provide_automatic_options: 添加 ``OPTIONS`` 方法并自动响应 OPTIONS 请求。
            :param options: 传给 :class:`~werkzeug.routing.Rule` 的额外选项。
        """
        raise NotImplementedError

    @setupmethod
    def endpoint(self, endpoint: str) -> t.Callable[[F], F]:
        """
            装饰视图函数，为给定端点注册。用于未通过 :meth:`add_url_rule` 提供 ``view_func`` 的规则。
            
            :param endpoint: 与视图函数关联的端点名。
        """

        def decorator(f: F) -> F:
            self.view_functions[endpoint] = f
            return f

        return decorator

    @setupmethod
    def before_request(self, f: T_before_request) -> T_before_request:
        """
            注册在每个请求之前运行的函数，例如打开数据库连接或从 session 加载用户。
            
            函数无参数调用。若返回非 ``None`` 值，视为视图返回值并停止后续请求处理。
            
            可用于应用和蓝图。在应用上对每个请求执行；在蓝图上仅对蓝图处理的请求执行。
            要对每个请求执行请用 :meth:`.Blueprint.before_app_request`。
        """
        self.before_request_funcs.setdefault(None, []).append(f)
        return f

    @setupmethod
    def after_request(self, f: T_after_request) -> T_after_request:
        """
            注册在此对象每个请求之后运行的函数。接收响应对象，须返回响应对象。
            
            若函数抛出异常，剩余 ``after_request`` 不会调用；关闭资源请用 :meth:`teardown_request`。
            
            可用于应用和蓝图。要对每个请求执行请用 :meth:`.Blueprint.after_app_request`。
        """
        self.after_request_funcs.setdefault(None, []).append(f)
        return f

    @setupmethod
    def teardown_request(self, f: T_teardown) -> T_teardown:
        """
            注册在请求上下文弹出时调用的函数，通常在每个请求结束时，测试时也可手动压入。
            
            因未处理异常调用时会传入错误对象。若注册了 :meth:`errorhandler` 则由其处理，拆卸函数不会收到。
            
            拆卸函数须避免抛出异常；返回值被忽略。
            
            要对每个请求执行请用 :meth:`.Blueprint.teardown_app_request`。
        """
        self.teardown_request_funcs.setdefault(None, []).append(f)
        return f

    @setupmethod
    def context_processor(
        self,
        f: T_template_context_processor,
    ) -> T_template_context_processor:
        """
            注册模板上下文处理器，在渲染模板前运行，返回字典的键作为模板变量。
            
            在应用上作用于每个模板；在蓝图上仅作用于蓝图视图渲染的模板。
            要对每个模板生效请用 :meth:`.Blueprint.app_context_processor`。
        """
        self.template_context_processors[None].append(f)
        return f

    @setupmethod
    def url_value_preprocessor(
        self,
        f: T_url_value_preprocessor,
    ) -> T_url_value_preprocessor:
        """
            为应用中所有视图函数注册 URL 值预处理器，在 :meth:`before_request` 之前调用。
            
            可在传给视图前修改 URL 捕获的值，例如弹出公共语言代码放入 ``g``。
            
            传入端点名和 values 字典，返回值被忽略。
            
            要对每个请求生效请用 :meth:`.Blueprint.app_url_value_preprocessor`。
        """
        self.url_value_preprocessors[None].append(f)
        return f

    @setupmethod
    def url_defaults(self, f: T_url_defaults) -> T_url_defaults:
        """
            应用中所有视图函数的 URL 默认值回调。传入端点名和 values，就地更新 values。
            
            要对每个请求生效请用 :meth:`.Blueprint.app_url_defaults`。
        """
        self.url_default_functions[None].append(f)
        return f

    @setupmethod
    def errorhandler(
        self, code_or_exception: type[Exception] | int
    ) -> t.Callable[[T_error_handler], T_error_handler]:
        """
            按状态码或异常类注册错误处理函数。
            
            示例::
            
                @app.errorhandler(404)
                def page_not_found(error):
                    return 'This page does not exist', 404
            
            也可注册任意异常::
            
                @app.errorhandler(DatabaseError)
                def special_exception_handler(error):
                    return 'Database connection failed', 500
            
            要对每个请求生效请用 :meth:`.Blueprint.app_errorhandler`。
            
            .. versionadded:: 0.7
                请用 :meth:`register_error_handler` 而非直接修改 :attr:`error_handler_spec`。
            
            :param code_or_exception: 处理器的状态码（整数）或任意异常。
        """

        def decorator(f: T_error_handler) -> T_error_handler:
            self.register_error_handler(code_or_exception, f)
            return f

        return decorator

    @setupmethod
    def register_error_handler(
        self,
        code_or_exception: type[Exception] | int,
        f: ft.ErrorHandlerCallable,
    ) -> None:
        """
            :meth:`errorhandler` 装饰器的替代绑定方式，非装饰器用法更直接。
            
            .. versionadded:: 0.7
        """
        exc_class, code = self._get_exc_class_and_code(code_or_exception)
        self.error_handler_spec[None][code][exc_class] = f

    @staticmethod
    def _get_exc_class_and_code(
        exc_class_or_code: type[Exception] | int,
    ) -> tuple[type[Exception], int | None]:
        """
            获取正在处理的异常类。HTTP 状态码或 ``HTTPException`` 子类时返回异常类和状态码。
            
            :param exc_class_or_code: 任意异常类或 HTTP 状态码（整数）。
        """
        exc_class: type[Exception]

        if isinstance(exc_class_or_code, int):
            try:
                exc_class = default_exceptions[exc_class_or_code]
            except KeyError:
                raise ValueError(
                    f"'{exc_class_or_code}' is not a recognized HTTP"
                    " error code. Use a subclass of HTTPException with"
                    " that code instead."
                ) from None
        else:
            exc_class = exc_class_or_code

        if isinstance(exc_class, Exception):
            raise TypeError(
                f"{exc_class!r} is an instance, not a class. Handlers"
                " can only be registered for Exception classes or HTTP"
                " error codes."
            )

        if not issubclass(exc_class, Exception):
            raise ValueError(
                f"'{exc_class.__name__}' is not a subclass of Exception."
                " Handlers can only be registered for Exception classes"
                " or HTTP error codes."
            )

        if issubclass(exc_class, HTTPException):
            return exc_class, exc_class.code
        else:
            return exc_class, None


def _endpoint_from_view_func(view_func: ft.RouteCallable) -> str:
    """
        返回给定函数默认端点的内部辅助函数，始终为函数名。
    """
    assert view_func is not None, "expected view func if endpoint is not provided."
    return view_func.__name__


def _find_package_path(import_name: str) -> str:
    """
        查找包含包或模块的路径。
    """
    root_mod_name, _, _ = import_name.partition(".")

    try:
        root_spec = importlib.util.find_spec(root_mod_name)

        if root_spec is None:
            raise ValueError("not found")
    except (ImportError, ValueError):
        # ImportError：机制报告不存在
        # ValueError:
        # 模块
        # 模块
        #    - we raised `ValueError` due to `root_spec` being `None`
        return os.getcwd()

    if root_spec.submodule_search_locations:
        if root_spec.origin is None or root_spec.origin == "namespace":
            # 命名空间包
            package_spec = importlib.util.find_spec(import_name)

            if package_spec is not None and package_spec.submodule_search_locations:
                # 模块
                package_path = pathlib.Path(
                    os.path.commonpath(package_spec.submodule_search_locations)
                )
                search_location = next(
                    location
                    for location in root_spec.submodule_search_locations
                    if package_path.is_relative_to(location)
                )
            else:
                # Pick the first path.
                search_location = root_spec.submodule_search_locations[0]

            return os.path.dirname(search_location)
        else:
            # 含 __init__.py 的包
            return os.path.dirname(os.path.dirname(root_spec.origin))
    else:
        # 模块
        return os.path.dirname(root_spec.origin)  # type: ignore[type-var, return-value]


def find_package(import_name: str) -> tuple[str | None, str]:
    """
        查找包安装前缀及导入路径。
        
        前缀是包含标准目录层次（lib、bin 等）的目录。若包未安装到
        系统（:attr:`sys.prefix`）或虚拟环境（``site-packages``），返回 ``None``。
        
        路径是 :attr:`sys.path` 中包含该包的条目。未安装时假设从当前工作目录导入。
    """
    package_path = _find_package_path(import_name)
    py_prefix = os.path.abspath(sys.prefix)

    # 安装到系统
    if pathlib.PurePath(package_path).is_relative_to(py_prefix):
        return py_prefix, package_path

    site_parent, site_folder = os.path.split(package_path)

    # 安装到虚拟环境
    if site_folder.lower() == "site-packages":
        parent, folder = os.path.split(site_parent)

        # Windows (prefix/lib/site-packages)
        if folder.lower() == "lib":
            return parent, package_path

        # Unix (prefix/lib/pythonX.Y/site-packages)
        if os.path.basename(parent).lower() == "lib":
            return os.path.dirname(parent), package_path

        # something else (prefix/site-packages)
        return site_parent, package_path

    # 未安装
    return None, package_path
