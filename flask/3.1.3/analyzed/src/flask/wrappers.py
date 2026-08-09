from __future__ import annotations

import typing as t

from werkzeug.exceptions import BadRequest
from werkzeug.exceptions import HTTPException
from werkzeug.wrappers import Request as RequestBase
from werkzeug.wrappers import Response as ResponseBase

from . import json
from .globals import current_app
from .helpers import _split_blueprint_path

if t.TYPE_CHECKING:  # pragma: no cover
    from werkzeug.routing import Rule


class Request(RequestBase):
    """Flask 默认使用的请求对象，记录匹配的端点和视图参数。

    即 :class:`~flask.request`。若要替换请求对象，可子类化此类并将
    :attr:`~flask.Flask.request_class` 设为子类。

    请求对象是 :class:`~werkzeug.wrappers.Request` 的子类，
    提供 Werkzeug 定义的所有属性及若干 Flask 特有属性。
    """

    json_module: t.Any = json

    #: 匹配此请求的内部 URL 规则。可用于在 before/after 处理器中
    #: 检查该 URL 允许的方法（``request.url_rule.methods``）等。
    #: 若请求方法对 URL 规则无效，有效方法列表在
    #: ``routing_exception.valid_methods`` 中（Werkzeug 异常
    #: :exc:`~werkzeug.exceptions.MethodNotAllowed` 的属性），
    #: 因为请求从未在内部绑定。
    #:
    #: .. versionadded:: 0.6
    url_rule: Rule | None = None

    #: 匹配请求的视图参数字典。匹配过程中发生异常时为 ``None``。
    view_args: dict[str, t.Any] | None = None

    #: URL 匹配失败时，请求处理中抛出/将抛出的异常。
    #: 通常为 :exc:`~werkzeug.exceptions.NotFound` 或类似异常。
    routing_exception: HTTPException | None = None

    _max_content_length: int | None = None
    _max_form_memory_size: int | None = None
    _max_form_parts: int | None = None

    @property
    def max_content_length(self) -> int | None:
        """此请求期间将读取的最大字节数。超出限制时抛出 413
        :exc:`~werkzeug.exceptions.RequestEntityTooLarge` 错误。
        设为 ``None`` 时 Flask 应用层不强制限制。但若为 ``None`` 且请求
        无 ``Content-Length`` 头且 WSGI 服务器未表明会终止流，则不读取数据
        以避免无限流。

        每个请求默认使用 :data:`MAX_CONTENT_LENGTH` 配置（默认为 ``None``）。
        可在特定 ``request`` 上设置以仅对该视图生效，应根据应用或视图需求适当配置。

        .. versionchanged:: 3.1
            可按请求设置。

        .. versionchanged:: 0.6
            可通过 Flask 配置。
        """
        if self._max_content_length is not None:
            return self._max_content_length

        if not current_app:
            return super().max_content_length

        return current_app.config["MAX_CONTENT_LENGTH"]  # type: ignore[no-any-return]

    @max_content_length.setter
    def max_content_length(self, value: int | None) -> None:
        self._max_content_length = value

    @property
    def max_form_memory_size(self) -> int | None:
        """``multipart/form-data`` 请求体中非文件表单字段的最大字节数。
        超出限制时抛出 413 :exc:`~werkzeug.exceptions.RequestEntityTooLarge`。
        设为 ``None`` 时 Flask 应用层不强制限制。

        每个请求默认使用 :data:`MAX_FORM_MEMORY_SIZE` 配置（默认 ``500_000``）。
        可在特定 ``request`` 上设置以仅对该视图生效。

        .. versionchanged:: 3.1
            可通过 Flask 配置。
        """
        if self._max_form_memory_size is not None:
            return self._max_form_memory_size

        if not current_app:
            return super().max_form_memory_size

        return current_app.config["MAX_FORM_MEMORY_SIZE"]  # type: ignore[no-any-return]

    @max_form_memory_size.setter
    def max_form_memory_size(self, value: int | None) -> None:
        self._max_form_memory_size = value

    @property  # type: ignore[override]
    def max_form_parts(self) -> int | None:
        """``multipart/form-data`` 请求体中字段的最大数量。
        超出限制时抛出 413 :exc:`~werkzeug.exceptions.RequestEntityTooLarge`。
        设为 ``None`` 时 Flask 应用层不强制限制。

        每个请求默认使用 :data:`MAX_FORM_PARTS` 配置（默认 ``1_000``）。
        可在特定 ``request`` 上设置以仅对该视图生效。

        .. versionchanged:: 3.1
            可通过 Flask 配置。
        """
        if self._max_form_parts is not None:
            return self._max_form_parts

        if not current_app:
            return super().max_form_parts

        return current_app.config["MAX_FORM_PARTS"]  # type: ignore[no-any-return]

    @max_form_parts.setter
    def max_form_parts(self, value: int | None) -> None:
        self._max_form_parts = value

    @property
    def endpoint(self) -> str | None:
        """匹配请求 URL 的端点。

        匹配失败或尚未执行时为 ``None``。

        与 :attr:`view_args` 结合可重建相同或修改后的 URL。
        """
        if self.url_rule is not None:
            return self.url_rule.endpoint  # type: ignore[no-any-return]

        return None

    @property
    def blueprint(self) -> str | None:
        """当前蓝图的注册名称。

        端点不属于蓝图，或 URL 匹配失败/尚未执行时为 ``None``。

        不一定与创建蓝图时使用的名称相同，可能已嵌套或以不同名称注册。
        """
        endpoint = self.endpoint

        if endpoint is not None and "." in endpoint:
            return endpoint.rpartition(".")[0]

        return None

    @property
    def blueprints(self) -> list[str]:
        """从当前蓝图向上遍历父蓝图的所有注册名称。

        无当前蓝图或 URL 匹配失败时为空列表。

        .. versionadded:: 2.0.1
        """
        name = self.blueprint

        if name is None:
            return []

        return _split_blueprint_path(name)

    def _load_form_data(self) -> None:
        super()._load_form_data()

        # 调试模式下，将 files multidict 替换为在键错误时抛出不同错误的子类。
        if (
            current_app
            and current_app.debug
            and self.mimetype != "multipart/form-data"
            and not self.files
        ):
            from .debughelpers import attach_enctype_error_multidict

            attach_enctype_error_multidict(self)

    def on_json_loading_failed(self, e: ValueError | None) -> t.Any:
        try:
            return super().on_json_loading_failed(e)
        except BadRequest as ebr:
            if current_app and current_app.debug:
                raise

            raise BadRequest() from ebr


class Response(ResponseBase):
    """Flask 默认使用的响应对象。行为类似 Werkzeug 响应对象，
    但默认 mimetype 为 HTML。通常无需自行创建，
    :meth:`~flask.Flask.make_response` 会处理。

    若要替换响应对象，可子类化此类并将
    :attr:`~flask.Flask.response_class` 设为子类。

    .. versionchanged:: 1.0
        响应对象增加 JSON 支持，便于测试时以 JSON 获取测试客户端响应数据。

    .. versionchanged:: 1.0

        新增 :attr:`max_cookie_size`。
    """

    default_mimetype: str | None = "text/html"

    json_module = json

    autocorrect_location_header = False

    @property
    def max_cookie_size(self) -> int:  # type: ignore
        """:data:`MAX_COOKIE_SIZE` 配置键的只读视图。

        参见 Werkzeug 文档中的 :attr:`~werkzeug.wrappers.Response.max_cookie_size`。
        """
        if current_app:
            return current_app.config["MAX_COOKIE_SIZE"]  # type: ignore[no-any-return]

        # 不在应用上下文中时返回 Werkzeug 默认值
        return super().max_cookie_size
