from __future__ import annotations

import json as _json
import typing as t

from ..globals import current_app
from .provider import _default

if t.TYPE_CHECKING:  # pragma: no cover
    from ..wrappers import Response


def dumps(obj: t.Any, **kwargs: t.Any) -> str:
    """将数据序列化为 JSON 字符串。

    若 :data:`~flask.current_app` 可用，则调用其
    :meth:`app.json.dumps() <flask.json.provider.JSONProvider.dumps>`
    方法，否则使用 :func:`json.dumps`。

    :param obj: 待序列化的数据。
    :param kwargs: 传递给 ``dumps`` 实现的参数。

    .. versionchanged:: 2.3
        移除了 ``app`` 参数。

    .. versionchanged:: 2.2
        调用 ``current_app.json.dumps``，允许应用覆盖行为。

    .. versionchanged:: 2.0.2
        支持 :class:`decimal.Decimal`，转换为字符串。

    .. versionchanged:: 2.0
        ``encoding`` 将在 Flask 2.1 中移除。

    .. versionchanged:: 1.0.3
        可直接传入 ``app``，无需应用上下文即可配置。
    """
    if current_app:
        return current_app.json.dumps(obj, **kwargs)

    kwargs.setdefault("default", _default)
    return _json.dumps(obj, **kwargs)


def dump(obj: t.Any, fp: t.IO[str], **kwargs: t.Any) -> None:
    """将数据序列化为 JSON 并写入文件。

    若 :data:`~flask.current_app` 可用，则调用其
    :meth:`app.json.dump() <flask.json.provider.JSONProvider.dump>`
    方法，否则使用 :func:`json.dump`。

    :param obj: 待序列化的数据。
    :param fp: 以文本模式打开的可写文件，应使用 UTF-8 编码以符合 JSON 规范。
    :param kwargs: 传递给 ``dump`` 实现的参数。

    .. versionchanged:: 2.3
        移除了 ``app`` 参数。

    .. versionchanged:: 2.2
        调用 ``current_app.json.dump``，允许应用覆盖行为。

    .. versionchanged:: 2.0
        写入二进制文件及 ``encoding`` 参数将在 Flask 2.1 中移除。
    """
    if current_app:
        current_app.json.dump(obj, fp, **kwargs)
    else:
        kwargs.setdefault("default", _default)
        _json.dump(obj, fp, **kwargs)


def loads(s: str | bytes, **kwargs: t.Any) -> t.Any:
    """将 JSON 反序列化为 Python 对象。

    若 :data:`~flask.current_app` 可用，则调用其
    :meth:`app.json.loads() <flask.json.provider.JSONProvider.loads>`
    方法，否则使用 :func:`json.loads`。

    :param s: 文本或 UTF-8 字节串。
    :param kwargs: 传递给 ``loads`` 实现的参数。

    .. versionchanged:: 2.3
        移除了 ``app`` 参数。

    .. versionchanged:: 2.2
        调用 ``current_app.json.loads``，允许应用覆盖行为。

    .. versionchanged:: 2.0
        ``encoding`` 将在 Flask 2.1 中移除。数据须为字符串或 UTF-8 字节。

    .. versionchanged:: 1.0.3
        可直接传入 ``app``，无需应用上下文即可配置。
    """
    if current_app:
        return current_app.json.loads(s, **kwargs)

    return _json.loads(s, **kwargs)


def load(fp: t.IO[t.AnyStr], **kwargs: t.Any) -> t.Any:
    """从文件读取并反序列化 JSON。

    若 :data:`~flask.current_app` 可用，则调用其
    :meth:`app.json.load() <flask.json.provider.JSONProvider.load>`
    方法，否则使用 :func:`json.load`。

    :param fp: 以文本或 UTF-8 字节模式打开的可读文件。
    :param kwargs: 传递给 ``load`` 实现的参数。

    .. versionchanged:: 2.3
        移除了 ``app`` 参数。

    .. versionchanged:: 2.2
        调用 ``current_app.json.load``，允许应用覆盖行为。

    .. versionchanged:: 2.2
        ``app`` 参数将在 Flask 2.3 中移除。

    .. versionchanged:: 2.0
        ``encoding`` 将在 Flask 2.1 中移除。文件须为文本模式，或二进制模式下的 UTF-8 字节。
    """
    if current_app:
        return current_app.json.load(fp, **kwargs)

    return _json.load(fp, **kwargs)


def jsonify(*args: t.Any, **kwargs: t.Any) -> Response:
    """将给定参数序列化为 JSON，并返回 ``application/json``
    mimetype 的 :class:`~flask.Response` 对象。视图返回的 dict 或 list
    会自动转换为 JSON 响应，无需显式调用此函数。

    需要活动的请求或应用上下文，并调用
    :meth:`app.json.response() <flask.json.provider.JSONProvider.response>`。

    调试模式下输出会带缩进以便阅读，也可由 provider 控制。

    只能传入位置参数或关键字参数，不能同时使用。
    若不传参数，则序列化 ``None``。

    :param args: 单个待序列化值，或多个值组成的列表。
    :param kwargs: 作为 dict 序列化。

    .. versionchanged:: 2.2
        调用 ``current_app.json.response``，允许应用覆盖行为。

    .. versionchanged:: 2.0.2
        支持 :class:`decimal.Decimal`，转换为字符串。

    .. versionchanged:: 0.11
        支持序列化顶层数组。这在古老浏览器中是安全风险。
        参见 :ref:`security-json`。

    .. versionadded:: 0.2
    """
    return current_app.json.response(*args, **kwargs)  # type: ignore[return-value]
