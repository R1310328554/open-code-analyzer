"""
带标签的 JSON
~~~~~~~~~~~~~

用于无损序列化非标准 JSON 类型的紧凑表示。
:class:`~flask.sessions.SecureCookieSessionInterface` 用此序列化
会话数据，也可在其他场景使用，并可通过扩展支持更多类型。

.. autoclass:: TaggedJSONSerializer
    :members:

.. autoclass:: JSONTag
    :members:

以下示例为 :class:`~collections.OrderedDict` 添加支持。
JSON 中 dict 无序，因此将条目序列化为 ``[key, value]`` 对列表。
子类化 :class:`JSONTag` 并使用新键 ``' od'`` 标识该类型。
会话序列化器先处理 dict，因此将新标签插入顺序前端，
因为 ``OrderedDict`` 须在 ``dict`` 之前处理。

.. code-block:: python

    from flask.json.tag import JSONTag

    class TagOrderedDict(JSONTag):
        __slots__ = ('serializer',)
        key = ' od'

        def check(self, value):
            return isinstance(value, OrderedDict)

        def to_json(self, value):
            return [[k, self.serializer.tag(v)] for k, v in iteritems(value)]

        def to_python(self, value):
            return OrderedDict(value)

    app.session_interface.serializer.register(TagOrderedDict, index=0)
"""

from __future__ import annotations

import typing as t
from base64 import b64decode
from base64 import b64encode
from datetime import datetime
from uuid import UUID

from markupsafe import Markup
from werkzeug.http import http_date
from werkzeug.http import parse_date

from ..json import dumps
from ..json import loads


class JSONTag:
    """为 :class:`TaggedJSONSerializer` 定义类型标签的基类。"""

    __slots__ = ("serializer",)

    #: 标记序列化对象的标签。若为空，此标签仅作为标注过程中的中间步骤。
    key: str = ""

    def __init__(self, serializer: TaggedJSONSerializer) -> None:
        """为给定序列化器创建标签处理器。"""
        self.serializer = serializer

    def check(self, value: t.Any) -> bool:
        """检查给定值是否应由本标签处理。"""
        raise NotImplementedError

    def to_json(self, value: t.Any) -> t.Any:
        """将 Python 对象转换为有效 JSON 类型，标签稍后添加。"""
        raise NotImplementedError

    def to_python(self, value: t.Any) -> t.Any:
        """将 JSON 表示还原为正确类型，标签此时已移除。"""
        raise NotImplementedError

    def tag(self, value: t.Any) -> dict[str, t.Any]:
        """将值转换为有效 JSON 类型并包裹标签结构。"""
        return {self.key: self.to_json(value)}


class TagDict(JSONTag):
    """为仅含一个键且该键匹配已注册标签的 dict 打标签。

    内部将 dict 键后缀 ``__``，反序列化时移除后缀。
    """

    __slots__ = ()
    key = " di"

    def check(self, value: t.Any) -> bool:
        return (
            isinstance(value, dict)
            and len(value) == 1
            and next(iter(value)) in self.serializer.tags
        )

    def to_json(self, value: t.Any) -> t.Any:
        key = next(iter(value))
        return {f"{key}__": self.serializer.tag(value[key])}

    def to_python(self, value: t.Any) -> t.Any:
        key = next(iter(value))
        return {key[:-2]: value[key]}


class PassDict(JSONTag):
    """普通 dict 的透传标签（无独立 key）。"""

    __slots__ = ()

    def check(self, value: t.Any) -> bool:
        return isinstance(value, dict)

    def to_json(self, value: t.Any) -> t.Any:
        # JSON 对象键须为字符串，此处无需为键打标签。
        return {k: self.serializer.tag(v) for k, v in value.items()}

    tag = to_json


class TagTuple(JSONTag):
    __slots__ = ()
    key = " t"

    def check(self, value: t.Any) -> bool:
        return isinstance(value, tuple)

    def to_json(self, value: t.Any) -> t.Any:
        return [self.serializer.tag(item) for item in value]

    def to_python(self, value: t.Any) -> t.Any:
        return tuple(value)


class PassList(JSONTag):
    """普通 list 的透传标签（无独立 key）。"""

    __slots__ = ()

    def check(self, value: t.Any) -> bool:
        return isinstance(value, list)

    def to_json(self, value: t.Any) -> t.Any:
        return [self.serializer.tag(item) for item in value]

    tag = to_json


class TagBytes(JSONTag):
    __slots__ = ()
    key = " b"

    def check(self, value: t.Any) -> bool:
        return isinstance(value, bytes)

    def to_json(self, value: t.Any) -> t.Any:
        return b64encode(value).decode("ascii")

    def to_python(self, value: t.Any) -> t.Any:
        return b64decode(value)


class TagMarkup(JSONTag):
    """序列化符合 :class:`~markupsafe.Markup` API（含 ``__html__`` 方法）
    的对象，结果为该方法返回值。反序列化始终得到
    :class:`~markupsafe.Markup` 实例。"""

    __slots__ = ()
    key = " m"

    def check(self, value: t.Any) -> bool:
        return callable(getattr(value, "__html__", None))

    def to_json(self, value: t.Any) -> t.Any:
        return str(value.__html__())

    def to_python(self, value: t.Any) -> t.Any:
        return Markup(value)


class TagUUID(JSONTag):
    __slots__ = ()
    key = " u"

    def check(self, value: t.Any) -> bool:
        return isinstance(value, UUID)

    def to_json(self, value: t.Any) -> t.Any:
        return value.hex

    def to_python(self, value: t.Any) -> t.Any:
        return UUID(value)


class TagDateTime(JSONTag):
    __slots__ = ()
    key = " d"

    def check(self, value: t.Any) -> bool:
        return isinstance(value, datetime)

    def to_json(self, value: t.Any) -> t.Any:
        return http_date(value)

    def to_python(self, value: t.Any) -> t.Any:
        return parse_date(value)


class TaggedJSONSerializer:
    """使用标签系统紧凑表示非 JSON 类型的序列化器。
    作为 :class:`itsdangerous.Serializer` 的中间序列化器传入。

    额外支持以下类型：

    * :class:`dict`
    * :class:`tuple`
    * :class:`bytes`
    * :class:`~markupsafe.Markup`
    * :class:`~uuid.UUID`
    * :class:`~datetime.datetime`
    """

    __slots__ = ("tags", "order")

    #: 创建序列化器时绑定的标签类，之后可通过 :meth:`~register` 添加。
    default_tags = [
        TagDict,
        PassDict,
        TagTuple,
        PassList,
        TagBytes,
        TagMarkup,
        TagUUID,
        TagDateTime,
    ]

    def __init__(self) -> None:
        self.tags: dict[str, JSONTag] = {}
        self.order: list[JSONTag] = []

        for cls in self.default_tags:
            self.register(cls)

    def register(
        self,
        tag_class: type[JSONTag],
        force: bool = False,
        index: int | None = None,
    ) -> None:
        """向此序列化器注册新标签。

        :param tag_class: 要注册的标签类，将以此序列化器实例化。
        :param force: 是否覆盖已有标签。默认 ``False`` 时若已存在则抛出
            :exc:`KeyError`。
        :param index: 在标签顺序中插入新标签的位置。当新标签是已有标签的
            特例时很有用。默认 ``None`` 表示追加到末尾。

        :raise KeyError: 标签键已注册且 ``force`` 不为真时。
        """
        tag = tag_class(self)
        key = tag.key

        if key:
            if not force and key in self.tags:
                raise KeyError(f"Tag '{key}' is already registered.")

            self.tags[key] = tag

        if index is None:
            self.order.append(tag)
        else:
            self.order.insert(index, tag)

    def tag(self, value: t.Any) -> t.Any:
        """必要时将值转换为带标签的表示。"""
        for tag in self.order:
            if tag.check(value):
                return tag.tag(value)

        return value

    def untag(self, value: dict[str, t.Any]) -> t.Any:
        """将带标签的表示还原为原始类型。"""
        if len(value) != 1:
            return value

        key = next(iter(value))

        if key not in self.tags:
            return value

        return self.tags[key].to_python(value[key])

    def _untag_scan(self, value: t.Any) -> t.Any:
        """递归扫描并还原所有嵌套结构中的标签。"""
        if isinstance(value, dict):
            # 递归还原每个条目
            value = {k: self._untag_scan(v) for k, v in value.items()}
            # 还原 dict 本身
            value = self.untag(value)
        elif isinstance(value, list):
            # 递归还原每个条目
            value = [self._untag_scan(item) for item in value]

        return value

    def dumps(self, value: t.Any) -> str:
        """为值打标签并序列化为紧凑 JSON 字符串。"""
        return dumps(self.tag(value), separators=(",", ":"))

    def loads(self, value: str) -> t.Any:
        """从 JSON 字符串加载数据并反序列化所有带标签的对象。"""
        return self._untag_scan(loads(value))
