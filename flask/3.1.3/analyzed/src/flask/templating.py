from __future__ import annotations

import typing as t

from jinja2 import BaseLoader
from jinja2 import Environment as BaseEnvironment
from jinja2 import Template
from jinja2 import TemplateNotFound

from .globals import _cv_app
from .globals import _cv_request
from .globals import current_app
from .globals import request
from .helpers import stream_with_context
from .signals import before_render_template
from .signals import template_rendered

if t.TYPE_CHECKING:  # pragma: no cover
    from .app import Flask
    from .sansio.app import App
    from .sansio.scaffold import Scaffold


def _default_template_ctx_processor() -> dict[str, t.Any]:
    """默认模板上下文处理器。将 ``request`` 和 ``g`` 代理替换为具体对象以加速访问。
    """
    appctx = _cv_app.get(None)
    reqctx = _cv_request.get(None)
    rv: dict[str, t.Any] = {}
    if appctx is not None:
        rv["g"] = appctx.g
    if reqctx is not None:
        rv["request"] = reqctx.request
        # session 代理无法替换，访问时会得到 RequestContext.session，
        # 并设置 session.accessed。
    return rv


class Environment(BaseEnvironment):
    """行为类似常规 Jinja 环境，但额外了解 Flask 蓝图机制，
    可在必要时为引用的模板名添加蓝图前缀。
    """

    def __init__(self, app: App, **options: t.Any) -> None:
        if "loader" not in options:
            options["loader"] = app.create_global_jinja_loader()
        BaseEnvironment.__init__(self, **options)
        self.app = app


class DispatchingJinjaLoader(BaseLoader):
    """在应用及所有蓝图文件夹中查找模板的加载器。
    """

    def __init__(self, app: App) -> None:
        self.app = app

    def get_source(
        self, environment: BaseEnvironment, template: str
    ) -> tuple[str, str | None, t.Callable[[], bool] | None]:
        if self.app.config["EXPLAIN_TEMPLATE_LOADING"]:
            return self._get_source_explained(environment, template)
        return self._get_source_fast(environment, template)

    def _get_source_explained(
        self, environment: BaseEnvironment, template: str
    ) -> tuple[str, str | None, t.Callable[[], bool] | None]:
        """启用 EXPLAIN_TEMPLATE_LOADING 时，记录每次加载尝试并返回结果。"""
        attempts = []
        rv: tuple[str, str | None, t.Callable[[], bool] | None] | None
        trv: None | (tuple[str, str | None, t.Callable[[], bool] | None]) = None

        for srcobj, loader in self._iter_loaders(template):
            try:
                rv = loader.get_source(environment, template)
                if trv is None:
                    trv = rv
            except TemplateNotFound:
                rv = None
            attempts.append((loader, srcobj, rv))

        from .debughelpers import explain_template_loading_attempts

        explain_template_loading_attempts(self.app, template, attempts)

        if trv is not None:
            return trv
        raise TemplateNotFound(template)

    def _get_source_fast(
        self, environment: BaseEnvironment, template: str
    ) -> tuple[str, str | None, t.Callable[[], bool] | None]:
        """快速路径：找到第一个匹配的加载器即返回。"""
        for _srcobj, loader in self._iter_loaders(template):
            try:
                return loader.get_source(environment, template)
            except TemplateNotFound:
                continue
        raise TemplateNotFound(template)

    def _iter_loaders(self, template: str) -> t.Iterator[tuple[Scaffold, BaseLoader]]:
        """按蓝图优先顺序迭代应用及蓝图的模板加载器。"""
        loader = self.app.jinja_loader
        if loader is not None:
            yield self.app, loader

        for blueprint in self.app.iter_blueprints():
            loader = blueprint.jinja_loader
            if loader is not None:
                yield blueprint, loader

    def list_templates(self) -> list[str]:
        result = set()
        loader = self.app.jinja_loader
        if loader is not None:
            result.update(loader.list_templates())

        for blueprint in self.app.iter_blueprints():
            loader = blueprint.jinja_loader
            if loader is not None:
                for template in loader.list_templates():
                    result.add(template)

        return list(result)


def _render(app: Flask, template: Template, context: dict[str, t.Any]) -> str:
    """渲染模板并发送 before/after 信号。"""
    app.update_template_context(context)
    before_render_template.send(
        app, _async_wrapper=app.ensure_sync, template=template, context=context
    )
    rv = template.render(context)
    template_rendered.send(
        app, _async_wrapper=app.ensure_sync, template=template, context=context
    )
    return rv


def render_template(
    template_name_or_list: str | Template | list[str | Template],
    **context: t.Any,
) -> str:
    """按名称渲染模板并传入给定上下文。

    :param template_name_or_list: 要渲染的模板名。若为列表，渲染第一个存在的模板。
    :param context: 在模板中可用的变量。
    """
    app = current_app._get_current_object()  # type: ignore[attr-defined]
    template = app.jinja_env.get_or_select_template(template_name_or_list)
    return _render(app, template, context)


def render_template_string(source: str, **context: t.Any) -> str:
    """从给定源字符串渲染模板并传入上下文。

    :param source: 要渲染的模板源代码。
    :param context: 在模板中可用的变量。
    """
    app = current_app._get_current_object()  # type: ignore[attr-defined]
    template = app.jinja_env.from_string(source)
    return _render(app, template, context)


def _stream(
    app: Flask, template: Template, context: dict[str, t.Any]
) -> t.Iterator[str]:
    """流式渲染模板，生成前发送信号，生成后发送 template_rendered。"""
    app.update_template_context(context)
    before_render_template.send(
        app, _async_wrapper=app.ensure_sync, template=template, context=context
    )

    def generate() -> t.Iterator[str]:
        yield from template.generate(context)
        template_rendered.send(
            app, _async_wrapper=app.ensure_sync, template=template, context=context
        )

    rv = generate()

    # 若有活动请求上下文，在生成过程中保持上下文。
    if request:
        rv = stream_with_context(rv)

    return rv


def stream_template(
    template_name_or_list: str | Template | list[str | Template],
    **context: t.Any,
) -> t.Iterator[str]:
    """按名称流式渲染模板，返回字符串迭代器，可用作视图的流式响应。

    :param template_name_or_list: 要渲染的模板名。若为列表，渲染第一个存在的模板。
    :param context: 在模板中可用的变量。

    .. versionadded:: 2.2
    """
    app = current_app._get_current_object()  # type: ignore[attr-defined]
    template = app.jinja_env.get_or_select_template(template_name_or_list)
    return _stream(app, template, context)


def stream_template_string(source: str, **context: t.Any) -> t.Iterator[str]:
    """从给定源字符串流式渲染模板，返回字符串迭代器，可用作视图的流式响应。

    :param source: 要渲染的模板源代码。
    :param context: 在模板中可用的变量。

    .. versionadded:: 2.2
    """
    app = current_app._get_current_object()  # type: ignore[attr-defined]
    template = app.jinja_env.from_string(source)
    return _stream(app, template, context)
