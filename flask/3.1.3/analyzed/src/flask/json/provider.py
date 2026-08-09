from __future__ import annotations

import dataclasses
import decimal
import json
import typing as t
import uuid
import weakref
from datetime import date

from werkzeug.http import http_date

if t.TYPE_CHECKING:  # pragma: no cover
    from werkzeug.sansio.response import Response

    from ..sansio.app import App


class JSONProvider:
    """为应用提供标准 JSON 操作集。子类可定制 JSON 行为或使用不同的 JSON 库。

    要为特定库实现 provider，子类化此基类并至少实现
    :meth:`dumps` 和 :meth:`loads`，其他方法有默认实现。

    要使用不同的 provider，可子类化 ``Flask`` 并将
    :attr:`~flask.Flask.json_provider_class` 设为 provider 类，
    或将 :attr:`app.json <flask.Flask.json>` 设为该类的实例。

    :param app: 应用实例，将以 :class:`weakref.proxy` 形式存储在
        :attr:`_app` 属性上。

    .. versionadded:: 2.2
    """

    def __init__(self, app: App) -> None:
        self._app: App = weakref.proxy(app)

    def dumps(self, obj: t.Any, **kwargs: t.Any) -> str:
        """将数据序列化为 JSON 字符串。

        :param obj: 待序列化的数据。
        :param kwargs: 可传递给底层 JSON 库。
        """
        raise NotImplementedError

    def dump(self, obj: t.Any, fp: t.IO[str], **kwargs: t.Any) -> None:
        """将数据序列化为 JSON 并写入文件。

        :param obj: 待序列化的数据。
        :param fp: 以文本模式打开的可写文件，应使用 UTF-8 编码。
        :param kwargs: 可传递给底层 JSON 库。
        """
        fp.write(self.dumps(obj, **kwargs))

    def loads(self, s: str | bytes, **kwargs: t.Any) -> t.Any:
        """将 JSON 反序列化为 Python 对象。

        :param s: 文本或 UTF-8 字节串。
        :param kwargs: 可传递给底层 JSON 库。
        """
        raise NotImplementedError

    def load(self, fp: t.IO[t.AnyStr], **kwargs: t.Any) -> t.Any:
        """从文件读取并反序列化 JSON。

        :param fp: 以文本或 UTF-8 字节模式打开的可读文件。
        :param kwargs: 可传递给底层 JSON 库。
        """
        return self.loads(fp.read(), **kwargs)

    def _prepare_response_obj(
        self, args: tuple[t.Any, ...], kwargs: dict[str, t.Any]
    ) -> t.Any:
        """将 jsonify/response 的位置或关键字参数规范为单个待序列化对象。"""
        if args and kwargs:
            raise TypeError("app.json.response() takes either args or kwargs, not both")

        if not args and not kwargs:
            return None

        if len(args) == 1:
            return args[0]

        return args or kwargs

    def response(self, *args: t.Any, **kwargs: t.Any) -> Response:
        """将给定参数序列化为 JSON，并返回 ``application/json``
        mimetype 的 :class:`~flask.Response` 对象。

        :func:`~flask.json.jsonify` 会为当前应用调用此方法。

        只能传入位置参数或关键字参数，不能同时使用。
        若不传参数，则序列化 ``None``。

        :param args: 单个待序列化值，或多个值组成的列表。
        :param kwargs: 作为 dict 序列化。
        """
        obj = self._prepare_response_obj(args, kwargs)
        return self._app.response_class(self.dumps(obj), mimetype="application/json")


def _default(o: t.Any) -> t.Any:
    """标准 JSON 编码器不认识的类型的默认转换函数。"""
    if isinstance(o, date):
        return http_date(o)

    if isinstance(o, (decimal.Decimal, uuid.UUID)):
        return str(o)

    if dataclasses and dataclasses.is_dataclass(o):
        return dataclasses.asdict(o)  # type: ignore[arg-type]

    if hasattr(o, "__html__"):
        return str(o.__html__())

    raise TypeError(f"Object of type {type(o).__name__} is not JSON serializable")


class DefaultJSONProvider(JSONProvider):
    """使用 Python 内置 :mod:`json` 库提供 JSON 操作。
    额外支持以下类型的序列化：

    -   :class:`datetime.datetime` 和 :class:`datetime.date` 序列化为
        :rfc:`822` 字符串（与 HTTP 日期格式相同）。
    -   :class:`uuid.UUID` 序列化为字符串。
    -   :class:`dataclasses.dataclass` 传递给 :func:`dataclasses.asdict`。
    -   :class:`~markupsafe.Markup`（或任何带 ``__html__`` 方法的对象）
        调用 ``__html__`` 获取字符串。
    """

    default: t.Callable[[t.Any], t.Any] = staticmethod(_default)  # type: ignore[assignment]
    """对 :meth:`json.dumps` 无法序列化的对象应用此函数。
    应返回有效 JSON 类型，或抛出 ``TypeError``。
    """

    ensure_ascii = True
    """将非 ASCII 字符替换为转义序列。某些客户端兼容性更好，
    禁用后可提升性能和减小体积。
    """

    sort_keys = True
    """对序列化的 dict 按键排序。某些缓存场景有用，
    禁用后可提升性能。启用时键须均为字符串，排序前不会转换。
    """

    compact: bool | None = None
    """若为 ``True``，或非调试模式下的 ``None``，:meth:`response`
    输出不添加缩进、换行或空格。若为 ``False`` 或调试模式下的
    ``None``，则使用非紧凑格式。
    """

    mimetype = "application/json"
    """:meth:`response` 中设置的 mimetype。"""

    def dumps(self, obj: t.Any, **kwargs: t.Any) -> str:
        """将数据序列化为 JSON 字符串。

        关键字参数传递给 :func:`json.dumps`，部分默认值来自
        :attr:`default`、:attr:`ensure_ascii` 和 :attr:`sort_keys`。

        :param obj: 待序列化的数据。
        :param kwargs: 传递给 :func:`json.dumps`。
        """
        kwargs.setdefault("default", self.default)
        kwargs.setdefault("ensure_ascii", self.ensure_ascii)
        kwargs.setdefault("sort_keys", self.sort_keys)
        return json.dumps(obj, **kwargs)

    def loads(self, s: str | bytes, **kwargs: t.Any) -> t.Any:
        """从字符串或字节反序列化 JSON。

        :param s: 文本或 UTF-8 字节串。
        :param kwargs: 传递给 :func:`json.loads`。
        """
        return json.loads(s, **kwargs)

    def response(self, *args: t.Any, **kwargs: t.Any) -> Response:
        """将给定参数序列化为 JSON 并返回 :class:`~flask.Response`。
        响应 mimetype 为 "application/json"，可通过 :attr:`mimetype` 修改。

        若 :attr:`compact` 为 ``False`` 或启用调试模式，输出会格式化以便阅读。

        只能传入位置参数或关键字参数，不能同时使用。
        若不传参数，则序列化 ``None``。

        :param args: 单个待序列化值，或多个值组成的列表。
        :param kwargs: 作为 dict 序列化。
        """
        obj = self._prepare_response_obj(args, kwargs)
        dump_args: dict[str, t.Any] = {}

        if (self.compact is None and self._app.debug) or self.compact is False:
            dump_args.setdefault("indent", 2)
        else:
            dump_args.setdefault("separators", (",", ":"))

        return self._app.response_class(
            f"{self.dumps(obj, **dump_args)}\n", mimetype=self.mimetype
        )
