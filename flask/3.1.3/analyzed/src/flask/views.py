from __future__ import annotations

import typing as t

from . import typing as ft
from .globals import current_app
from .globals import request

F = t.TypeVar("F", bound=t.Callable[..., t.Any])

http_method_funcs = frozenset(
    ["get", "post", "head", "options", "delete", "put", "trace", "patch"]
)


class View:
    """子类化此类并重写 :meth:`dispatch_request` 以创建通用基于类的视图。
    调用 :meth:`as_view` 可生成视图函数：该函数会实例化类并调用
    ``dispatch_request``，传入 URL 变量。

    详见 :doc:`views`。

    .. code-block:: python

        class Hello(View):
            init_every_request = False

            def dispatch_request(self, name):
                return f"Hello, {name}!"

        app.add_url_rule(
            "/hello/<name>", view_func=Hello.as_view("hello")
        )

    在类上设置 :attr:`methods` 可更改视图接受的 HTTP 方法。

    在类上设置 :attr:`decorators` 可将装饰器列表应用于生成的视图函数。
    注意：直接装饰在类上的装饰器不会应用到生成的视图函数！

    除非需要在 ``self`` 上存储请求级数据，否则将
    :attr:`init_every_request` 设为 ``False`` 以提高效率。
    """

    #: 此视图注册的 HTTP 方法。默认与 ``route`` 和
    #: ``add_url_rule`` 相同（``["GET", "HEAD", "OPTIONS"]``）。
    methods: t.ClassVar[t.Collection[str] | None] = None

    #: 是否自动处理 ``OPTIONS`` 方法。
    #: 默认与 ``route`` 和 ``add_url_rule`` 相同（``True``）。
    provide_automatic_options: t.ClassVar[bool | None] = None

    #: 按顺序应用于生成视图函数的装饰器列表。
    #: 注意 ``@decorator`` 语法自下而上应用，因此列表中第一个装饰器
    #: 位于最底层。
    #:
    #: .. versionadded:: 0.8
    decorators: t.ClassVar[list[t.Callable[..., t.Any]]] = []

    #: 默认每个请求创建此类的新实例。
    #: 若子类设为 ``False``，则所有请求共用同一实例。
    #:
    #: 单实例更高效，尤其在 ``__init__`` 中有复杂初始化时。
    #: 但 ``self`` 上不再能安全地跨请求存储数据，应改用
    #: :data:`~flask.g`。
    #:
    #: .. versionadded:: 2.2
    init_every_request: t.ClassVar[bool] = True

    def dispatch_request(self) -> ft.ResponseReturnValue:
        """实际的视图函数逻辑。子类必须重写此方法并返回有效响应。
        URL 规则中的变量以关键字参数传入。
        """
        raise NotImplementedError()

    @classmethod
    def as_view(
        cls, name: str, *class_args: t.Any, **class_kwargs: t.Any
    ) -> ft.RouteCallable:
        """将类转换为可注册为路由的视图函数。

        默认情况下，生成的视图会为每个请求创建视图类的新实例并调用
        :meth:`dispatch_request`。若视图类将
        :attr:`init_every_request` 设为 ``False``，则所有请求共用同一实例。

        除 ``name`` 外，传入此方法的其他参数均转发给视图类的
        ``__init__`` 方法。

        .. versionchanged:: 2.2
            新增 ``init_every_request`` 类属性。
        """
        if cls.init_every_request:

            def view(**kwargs: t.Any) -> ft.ResponseReturnValue:
                self = view.view_class(  # type: ignore[attr-defined]
                    *class_args, **class_kwargs
                )
                return current_app.ensure_sync(self.dispatch_request)(**kwargs)  # type: ignore[no-any-return]

        else:
            self = cls(*class_args, **class_kwargs)  # pyright: ignore

            def view(**kwargs: t.Any) -> ft.ResponseReturnValue:
                return current_app.ensure_sync(self.dispatch_request)(**kwargs)  # type: ignore[no-any-return]

        if cls.decorators:
            view.__name__ = name
            view.__module__ = cls.__module__
            for decorator in cls.decorators:
                view = decorator(view)

        # 将视图类附加到视图函数，原因有二：
        # 1. 便于识别基于类的视图来源；
        # 2. 用于实例化视图类，测试时可替换为其他类以便调试。
        view.view_class = cls  # type: ignore
        view.__name__ = name
        view.__doc__ = cls.__doc__
        view.__module__ = cls.__module__
        view.methods = cls.methods  # type: ignore
        view.provide_automatic_options = cls.provide_automatic_options  # type: ignore
        return view


class MethodView(View):
    """将请求方法分派到对应的实例方法。
    例如实现 ``get`` 方法即可处理 ``GET`` 请求。

    适用于定义 REST API。

    :attr:`methods` 会根据类上定义的方法自动设置。

    详见 :doc:`views`。

    .. code-block:: python

        class CounterAPI(MethodView):
            def get(self):
                return str(session.get("counter", 0))

            def post(self):
                session["counter"] = session.get("counter", 0) + 1
                return redirect(url_for("counter"))

        app.add_url_rule(
            "/counter", view_func=CounterAPI.as_view("counter")
        )
    """

    def __init_subclass__(cls, **kwargs: t.Any) -> None:
        super().__init_subclass__(**kwargs)

        if "methods" not in cls.__dict__:
            methods = set()

            for base in cls.__bases__:
                if getattr(base, "methods", None):
                    methods.update(base.methods)  # type: ignore[attr-defined]

            for key in http_method_funcs:
                if hasattr(cls, key):
                    methods.add(key.upper())

            if methods:
                cls.methods = methods

    def dispatch_request(self, **kwargs: t.Any) -> ft.ResponseReturnValue:
        meth = getattr(self, request.method.lower(), None)

        # 若为 HEAD 请求且没有对应处理器，则回退到 GET。
        if meth is None and request.method == "HEAD":
            meth = getattr(self, "get", None)

        assert meth is not None, f"Unimplemented method {request.method!r}"
        return current_app.ensure_sync(meth)(**kwargs)  # type: ignore[no-any-return]
