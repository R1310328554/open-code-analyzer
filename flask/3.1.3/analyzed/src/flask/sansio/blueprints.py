from __future__ import annotations

import os
import typing as t
from collections import defaultdict
from functools import update_wrapper

from .. import typing as ft
from .scaffold import _endpoint_from_view_func
from .scaffold import _sentinel
from .scaffold import Scaffold
from .scaffold import setupmethod

if t.TYPE_CHECKING:  # pragma: no cover
    from .app import App

DeferredSetupFunction = t.Callable[["BlueprintSetupState"], None]
T_after_request = t.TypeVar("T_after_request", bound=ft.AfterRequestCallable[t.Any])
T_before_request = t.TypeVar("T_before_request", bound=ft.BeforeRequestCallable)
T_error_handler = t.TypeVar("T_error_handler", bound=ft.ErrorHandlerCallable)
T_teardown = t.TypeVar("T_teardown", bound=ft.TeardownCallable)
T_template_context_processor = t.TypeVar(
    "T_template_context_processor", bound=ft.TemplateContextProcessorCallable
)
T_template_filter = t.TypeVar("T_template_filter", bound=ft.TemplateFilterCallable)
T_template_global = t.TypeVar("T_template_global", bound=ft.TemplateGlobalCallable)
T_template_test = t.TypeVar("T_template_test", bound=ft.TemplateTestCallable)
T_url_defaults = t.TypeVar("T_url_defaults", bound=ft.URLDefaultCallable)
T_url_value_preprocessor = t.TypeVar(
    "T_url_value_preprocessor", bound=ft.URLValuePreprocessorCallable
)


class BlueprintSetupState:
    """
        将蓝图注册到应用时的临时持有对象。
        由 :meth:`~flask.Blueprint.make_setup_state` 创建，
        随后传给所有注册回调函数。
    """

    def __init__(
        self,
        blueprint: Blueprint,
        app: App,
        options: t.Any,
        first_registration: bool,
    ) -> None:
        #: 当前应用的引用
        self.app = app

        #: 创建此设置状态的蓝图的引用。
        self.blueprint = blueprint

        #: 传给 :meth:`~flask.Flask.register_blueprint` 的所有选项。
        #: :meth:`~flask.Flask.register_blueprint` method.
        self.options = options

        #: 蓝图可多次注册；此属性判断蓝图是否曾注册过。 with the
        #: application and not everything wants to be registered
        #: multiple times on it, this attribute can be used to figure
        #: out if the blueprint was registered in the past already.
        self.first_registration = first_registration

        subdomain = self.options.get("subdomain")
        if subdomain is None:
            subdomain = self.blueprint.subdomain

        #: 蓝图生效的子域名，否则为 ``None``。, ``None``
        #: otherwise.
        self.subdomain = subdomain

        url_prefix = self.options.get("url_prefix")
        if url_prefix is None:
            url_prefix = self.blueprint.url_prefix
        #: 蓝图上定义的所有 URL 使用的前缀。
        #: blueprint.
        self.url_prefix = url_prefix

        self.name = self.options.get("name", blueprint.name)
        self.name_prefix = self.options.get("name_prefix", "")

        #: 添加到蓝图上每个 URL 的默认参数字典。
        #: URL that was defined with the blueprint.
        self.url_defaults = dict(self.blueprint.url_values_defaults)
        self.url_defaults.update(self.options.get("url_defaults", ()))

    def add_url_rule(
        self,
        rule: str,
        endpoint: str | None = None,
        view_func: ft.RouteCallable | None = None,
        **options: t.Any,
    ) -> None:
        """
            向应用注册规则（及可选视图函数）的辅助方法。
            端点名自动加上蓝图名称前缀。
        """
        if self.url_prefix is not None:
            if rule:
                rule = "/".join((self.url_prefix.rstrip("/"), rule.lstrip("/")))
            else:
                rule = self.url_prefix
        options.setdefault("subdomain", self.subdomain)
        if endpoint is None:
            endpoint = _endpoint_from_view_func(view_func)  # type: ignore
        defaults = self.url_defaults
        if "defaults" in options:
            defaults = dict(defaults, **options.pop("defaults"))

        self.app.add_url_rule(
            rule,
            f"{self.name_prefix}.{self.name}.{endpoint}".lstrip("."),
            view_func,
            defaults=defaults,
            **options,
        )


class Blueprint(Scaffold):
    """
        表示蓝图，即路由及其他应用相关函数的集合，可稍后注册到实际应用。
        
        蓝图允许在不预先拥有应用对象的情况下定义应用函数。
        使用与 :class:`~flask.Flask` 相同的装饰器，但将注册推迟到之后。
        
        用蓝图装饰函数会创建延迟函数，在蓝图注册到应用时
        以 :class:`~flask.blueprints.BlueprintSetupState` 调用。
        
        详见 :doc:`/blueprints`。
        
        :param name: 蓝图名称，会加在每个端点名前。
        :param import_name: 蓝图包的导入名，通常为 ``__name__``，用于定位 ``root_path``。
        :param static_folder: 蓝图静态路由提供的静态文件文件夹，相对于蓝图根路径。默认禁用。
        :param static_url_path: 提供静态文件的 URL，默认为 ``static_folder``。
            若蓝图无 ``url_prefix``，应用静态路由优先，蓝图静态文件不可访问。
        :param template_folder: 加入应用模板搜索路径的模板文件夹，相对于蓝图根路径。默认禁用。
            蓝图模板优先级低于应用 templates 文件夹。
        :param url_prefix: 加在蓝图所有 URL 前的前缀，使其与应用其余路由区分。
        :param subdomain: 蓝图路由默认匹配的子域名。
        :param url_defaults: 蓝图路由默认接收的参数字典。
        :param root_path: 默认根据 ``import_name`` 自动设置。自动检测失败时可手动指定。
        
        .. versionchanged:: 1.1.0
            蓝图有 ``cli`` 组注册嵌套 CLI 命令。
            ``cli_group`` 参数控制 ``flask`` 命令下组的名称。
        
        .. versionadded:: 0.7
    """

    _got_registered_once = False

    def __init__(
        self,
        name: str,
        import_name: str,
        static_folder: str | os.PathLike[str] | None = None,
        static_url_path: str | None = None,
        template_folder: str | os.PathLike[str] | None = None,
        url_prefix: str | None = None,
        subdomain: str | None = None,
        url_defaults: dict[str, t.Any] | None = None,
        root_path: str | None = None,
        cli_group: str | None = _sentinel,  # type: ignore[assignment]
    ):
        super().__init__(
            import_name=import_name,
            static_folder=static_folder,
            static_url_path=static_url_path,
            template_folder=template_folder,
            root_path=root_path,
        )

        if not name:
            raise ValueError("'name' may not be empty.")

        if "." in name:
            raise ValueError("'name' may not contain a dot '.' character.")

        self.name = name
        self.url_prefix = url_prefix
        self.subdomain = subdomain
        self.deferred_functions: list[DeferredSetupFunction] = []

        if url_defaults is None:
            url_defaults = {}

        self.url_values_defaults = url_defaults
        self.cli_group = cli_group
        self._blueprints: list[tuple[Blueprint, dict[str, t.Any]]] = []

    def _check_setup_finished(self, f_name: str) -> None:
        if self._got_registered_once:
            raise AssertionError(
                f"The setup method '{f_name}' can no longer be called on the blueprint"
                f" '{self.name}'. It has already been registered at least once, any"
                " changes will not be applied consistently.\n"
                "Make sure all imports, decorators, functions, etc. needed to set up"
                " the blueprint are done before registering it."
            )

    @setupmethod
    def record(self, func: DeferredSetupFunction) -> None:
        """
            注册在蓝图注册到应用时调用的函数。
            此函数接收 :meth:`make_setup_state` 返回的状态对象。
        """
        self.deferred_functions.append(func)

    @setupmethod
    def record_once(self, func: DeferredSetupFunction) -> None:
        """
            类似 :meth:`record`，但包装函数确保只调用一次。
            蓝图第二次注册时传入的函数不会被调用。
        """

        def wrapper(state: BlueprintSetupState) -> None:
            if state.first_registration:
                func(state)

        self.record(update_wrapper(wrapper, func))

    def make_setup_state(
        self, app: App, options: dict[str, t.Any], first_registration: bool = False
    ) -> BlueprintSetupState:
        """
            创建 :meth:`~flask.blueprints.BlueprintSetupState` 实例并传给注册回调。
            子类可重写以返回子类实例。
        """
        return BlueprintSetupState(self, app, options, first_registration)

    @setupmethod
    def register_blueprint(self, blueprint: Blueprint, **options: t.Any) -> None:
        """
            在此蓝图上注册 :class:`~flask.Blueprint`。关键字参数会覆盖蓝图默认值。
            
            .. versionchanged:: 2.0.1
                ``name`` 可更改注册名称，允许同一蓝图多次注册。
            
            .. versionadded:: 2.0
        """
        if blueprint is self:
            raise ValueError("Cannot register a blueprint on itself")
        self._blueprints.append((blueprint, options))

    def register(self, app: App, options: dict[str, t.Any]) -> None:
        """
            由 :meth:`Flask.register_blueprint` 调用，将蓝图上所有视图和回调注册到应用。
            创建 :class:`.BlueprintSetupState` 并调用每个 :meth:`record` 回调。
            
            :param app: 注册此蓝图的应用。
            :param options: 从 :meth:`~Flask.register_blueprint` 转发的关键字参数。
            
            .. versionchanged:: 2.3
                嵌套蓝图现在正确应用子域名。
            
            .. versionchanged:: 2.1
                以相同名称多次注册同一蓝图会报错。
            
            .. versionchanged:: 2.0.1
                嵌套蓝图以带点名称注册；``name`` 选项可更改注册名称。
        """
        name_prefix = options.get("name_prefix", "")
        self_name = options.get("name", self.name)
        name = f"{name_prefix}.{self_name}".lstrip(".")

        if name in app.blueprints:
            bp_desc = "this" if app.blueprints[name] is self else "a different"
            existing_at = f" '{name}'" if self_name != name else ""

            raise ValueError(
                f"The name '{self_name}' is already registered for"
                f" {bp_desc} blueprint{existing_at}. Use 'name=' to"
                f" provide a unique name."
            )

        first_bp_registration = not any(bp is self for bp in app.blueprints.values())
        first_name_registration = name not in app.blueprints

        app.blueprints[name] = self
        self._got_registered_once = True
        state = self.make_setup_state(app, options, first_bp_registration)

        if self.has_static_folder:
            state.add_url_rule(
                f"{self.static_url_path}/<path:filename>",
                view_func=self.send_static_file,  # type: ignore[attr-defined]
                endpoint="static",
            )

        # 将蓝图数据合并到父级。
        if first_bp_registration or first_name_registration:
            self._merge_blueprint_funcs(app, name)

        for deferred in self.deferred_functions:
            deferred(state)

        cli_resolved_group = options.get("cli_group", self.cli_group)

        if self.cli.commands:
            if cli_resolved_group is None:
                app.cli.commands.update(self.cli.commands)
            elif cli_resolved_group is _sentinel:
                self.cli.name = name
                app.cli.add_command(self.cli)
            else:
                self.cli.name = cli_resolved_group
                app.cli.add_command(self.cli)

        for blueprint, bp_options in self._blueprints:
            bp_options = bp_options.copy()
            bp_url_prefix = bp_options.get("url_prefix")
            bp_subdomain = bp_options.get("subdomain")

            if bp_subdomain is None:
                bp_subdomain = blueprint.subdomain

            if state.subdomain is not None and bp_subdomain is not None:
                bp_options["subdomain"] = bp_subdomain + "." + state.subdomain
            elif bp_subdomain is not None:
                bp_options["subdomain"] = bp_subdomain
            elif state.subdomain is not None:
                bp_options["subdomain"] = state.subdomain

            if bp_url_prefix is None:
                bp_url_prefix = blueprint.url_prefix

            if state.url_prefix is not None and bp_url_prefix is not None:
                bp_options["url_prefix"] = (
                    state.url_prefix.rstrip("/") + "/" + bp_url_prefix.lstrip("/")
                )
            elif bp_url_prefix is not None:
                bp_options["url_prefix"] = bp_url_prefix
            elif state.url_prefix is not None:
                bp_options["url_prefix"] = state.url_prefix

            bp_options["name_prefix"] = name
            blueprint.register(app, bp_options)

    def _merge_blueprint_funcs(self, app: App, name: str) -> None:
        def extend(
            bp_dict: dict[ft.AppOrBlueprintKey, list[t.Any]],
            parent_dict: dict[ft.AppOrBlueprintKey, list[t.Any]],
        ) -> None:
            for key, values in bp_dict.items():
                key = name if key is None else f"{name}.{key}"
                parent_dict[key].extend(values)

        for key, value in self.error_handler_spec.items():
            key = name if key is None else f"{name}.{key}"
            value = defaultdict(
                dict,
                {
                    code: {exc_class: func for exc_class, func in code_values.items()}
                    for code, code_values in value.items()
                },
            )
            app.error_handler_spec[key] = value

        for endpoint, func in self.view_functions.items():
            app.view_functions[endpoint] = func

        extend(self.before_request_funcs, app.before_request_funcs)
        extend(self.after_request_funcs, app.after_request_funcs)
        extend(
            self.teardown_request_funcs,
            app.teardown_request_funcs,
        )
        extend(self.url_default_functions, app.url_default_functions)
        extend(self.url_value_preprocessors, app.url_value_preprocessors)
        extend(self.template_context_processors, app.template_context_processors)

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
            向蓝图注册 URL 规则。完整说明见 :meth:`.Flask.add_url_rule`。
            URL 规则会加上蓝图的 URL 前缀；:func:`url_for` 使用的端点名会加上蓝图名称前缀。
        """
        if endpoint and "." in endpoint:
            raise ValueError("'endpoint' may not contain a dot '.' character.")

        if view_func and hasattr(view_func, "__name__") and "." in view_func.__name__:
            raise ValueError("'view_func' name may not contain a dot '.' character.")

        self.record(
            lambda s: s.add_url_rule(
                rule,
                endpoint,
                view_func,
                provide_automatic_options=provide_automatic_options,
                **options,
            )
        )

    @setupmethod
    def app_template_filter(
        self, name: str | None = None
    ) -> t.Callable[[T_template_filter], T_template_filter]:
        """
            注册模板过滤器，应用渲染的任何模板均可用。等价于 :meth:`.Flask.template_filter`。
            
            :param name: 过滤器可选名称，默认使用函数名。
        """

        def decorator(f: T_template_filter) -> T_template_filter:
            self.add_app_template_filter(f, name=name)
            return f

        return decorator

    @setupmethod
    def add_app_template_filter(
        self, f: ft.TemplateFilterCallable, name: str | None = None
    ) -> None:
        """
            注册模板过滤器，等价于 :meth:`app_template_filter` 装饰器及 :meth:`.Flask.add_template_filter`。
            
            :param name: 过滤器可选名称，默认使用函数名。
        """

        def register_template(state: BlueprintSetupState) -> None:
            state.app.jinja_env.filters[name or f.__name__] = f

        self.record_once(register_template)

    @setupmethod
    def app_template_test(
        self, name: str | None = None
    ) -> t.Callable[[T_template_test], T_template_test]:
        """
            注册模板测试，应用渲染的任何模板均可用。等价于 :meth:`.Flask.template_test`。
            
            .. versionadded:: 0.10
            
            :param name: 测试可选名称，默认使用函数名。
        """

        def decorator(f: T_template_test) -> T_template_test:
            self.add_app_template_test(f, name=name)
            return f

        return decorator

    @setupmethod
    def add_app_template_test(
        self, f: ft.TemplateTestCallable, name: str | None = None
    ) -> None:
        """
            注册模板测试，等价于 :meth:`app_template_test` 及 :meth:`.Flask.add_template_test`。
            
            .. versionadded:: 0.10
            
            :param name: 测试可选名称，默认使用函数名。
        """

        def register_template(state: BlueprintSetupState) -> None:
            state.app.jinja_env.tests[name or f.__name__] = f

        self.record_once(register_template)

    @setupmethod
    def app_template_global(
        self, name: str | None = None
    ) -> t.Callable[[T_template_global], T_template_global]:
        """
            注册模板全局变量，应用渲染的任何模板均可用。等价于 :meth:`.Flask.template_global`。
            
            .. versionadded:: 0.10
            
            :param name: 全局变量可选名称，默认使用函数名。
        """

        def decorator(f: T_template_global) -> T_template_global:
            self.add_app_template_global(f, name=name)
            return f

        return decorator

    @setupmethod
    def add_app_template_global(
        self, f: ft.TemplateGlobalCallable, name: str | None = None
    ) -> None:
        """
            注册模板全局变量，等价于 :meth:`app_template_global` 及 :meth:`.Flask.add_template_global`。
            
            .. versionadded:: 0.10
            
            :param name: 全局变量可选名称，默认使用函数名。
        """

        def register_template(state: BlueprintSetupState) -> None:
            state.app.jinja_env.globals[name or f.__name__] = f

        self.record_once(register_template)

    @setupmethod
    def before_app_request(self, f: T_before_request) -> T_before_request:
        """
            类似 :meth:`before_request`，但在每个请求之前执行，不限于蓝图处理的请求。
            等价于 :meth:`.Flask.before_request`。
        """
        self.record_once(
            lambda s: s.app.before_request_funcs.setdefault(None, []).append(f)
        )
        return f

    @setupmethod
    def after_app_request(self, f: T_after_request) -> T_after_request:
        """
            类似 :meth:`after_request`，但在每个请求之后执行。
            等价于 :meth:`.Flask.after_request`。
        """
        self.record_once(
            lambda s: s.app.after_request_funcs.setdefault(None, []).append(f)
        )
        return f

    @setupmethod
    def teardown_app_request(self, f: T_teardown) -> T_teardown:
        """
            类似 :meth:`teardown_request`，但在每个请求之后执行。
            等价于 :meth:`.Flask.teardown_request`。
        """
        self.record_once(
            lambda s: s.app.teardown_request_funcs.setdefault(None, []).append(f)
        )
        return f

    @setupmethod
    def app_context_processor(
        self, f: T_template_context_processor
    ) -> T_template_context_processor:
        """
            类似 :meth:`context_processor`，但作用于每个视图渲染的模板。
            等价于 :meth:`.Flask.context_processor`。
        """
        self.record_once(
            lambda s: s.app.template_context_processors.setdefault(None, []).append(f)
        )
        return f

    @setupmethod
    def app_errorhandler(
        self, code: type[Exception] | int
    ) -> t.Callable[[T_error_handler], T_error_handler]:
        """
            类似 :meth:`errorhandler`，但作用于每个请求。
            等价于 :meth:`.Flask.errorhandler`。
        """

        def decorator(f: T_error_handler) -> T_error_handler:
            def from_blueprint(state: BlueprintSetupState) -> None:
                state.app.errorhandler(code)(f)

            self.record_once(from_blueprint)
            return f

        return decorator

    @setupmethod
    def app_url_value_preprocessor(
        self, f: T_url_value_preprocessor
    ) -> T_url_value_preprocessor:
        """
            类似 :meth:`url_value_preprocessor`，但作用于每个请求。
            等价于 :meth:`.Flask.url_value_preprocessor`。
        """
        self.record_once(
            lambda s: s.app.url_value_preprocessors.setdefault(None, []).append(f)
        )
        return f

    @setupmethod
    def app_url_defaults(self, f: T_url_defaults) -> T_url_defaults:
        """
            类似 :meth:`url_defaults`，但作用于每个请求。
            等价于 :meth:`.Flask.url_defaults`。
        """
        self.record_once(
            lambda s: s.app.url_default_functions.setdefault(None, []).append(f)
        )
        return f
