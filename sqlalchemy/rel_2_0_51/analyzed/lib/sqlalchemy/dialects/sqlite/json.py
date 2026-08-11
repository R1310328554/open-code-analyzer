# dialects/sqlite/json.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# mypy: ignore-errors

# SQLite JSON1：JSON_EXTRACT + JSON_QUOTE

from ... import types as sqltypes


# SQLite JSON 类型（依赖 JSON1 扩展）
class JSON(sqltypes.JSON):
    """SQLite JSON type.

    SQLite supports JSON as of version 3.9 through its JSON1_ extension. Note
    that JSON1_ is a
    `loadable extension <https://www.sqlite.org/loadext.html>`_ and as such
    may not be available, or may require run-time loading.

    :class:`_sqlite.JSON` is used automatically whenever the base
    :class:`_types.JSON` datatype is used against a SQLite backend.

    .. seealso::

        :class:`_types.JSON` - main documentation for the generic
        cross-platform JSON datatype.

    The :class:`_sqlite.JSON` type supports persistence of JSON values
    as well as the core index operations provided by :class:`_types.JSON`
    datatype, by adapting the operations to render the ``JSON_EXTRACT``
    function wrapped in the ``JSON_QUOTE`` function at the database level.
    Extracted values are quoted in order to ensure that the results are
    always JSON string values.


    .. versionadded:: 1.3


    .. _JSON1: https://www.sqlite.org/json1.html

    """


# Note: these objects currently match exactly those of MySQL, however since
# these are not generalizable to all JSON implementations, remain separately
# implemented for each dialect.
# JSON 索引/路径 bind 格式化 mixin
class _FormatTypeMixin:
    # 格式化索引路径
    # 拼接 JSONPath
    def _format_value(self, value):
        raise NotImplementedError()

    # bind 前 _format_value
    def bind_processor(self, dialect):
        super_proc = self.string_bind_processor(dialect)

        def process(value):
            value = self._format_value(value)
            if super_proc:
                value = super_proc(value)
            return value

        return process

    # 字面量同样格式化
    def literal_processor(self, dialect):
        super_proc = self.string_literal_processor(dialect)

        def process(value):
            value = self._format_value(value)
            if super_proc:
                value = super_proc(value)
            return value

        return process


# 索引键 -> $."key" 或 $[n]
class JSONIndexType(_FormatTypeMixin, sqltypes.JSON.JSONIndexType):
    def _format_value(self, value):
        if isinstance(value, int):
            value = "$[%s]" % value
        else:
            value = '$."%s"' % value
        return value


# 路径元组 -> $.a.b[n]
class JSONPathType(_FormatTypeMixin, sqltypes.JSON.JSONPathType):
    def _format_value(self, value):
        return "$%s" % (
            "".join(
                [
                    "[%s]" % elem if isinstance(elem, int) else '."%s"' % elem
                    for elem in value
                ]
            )
        )
