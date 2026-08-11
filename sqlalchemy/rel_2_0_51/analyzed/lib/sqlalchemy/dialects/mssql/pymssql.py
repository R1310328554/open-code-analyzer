# dialects/mssql/pymssql.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# mypy: ignore-errors


"""
mssql+pymssql 方言：基于 FreeTDS 的 pymssql DBAPI 驱动适配。

.. dialect:: mssql+pymssql"""
.. dialect:: mssql+pymssql
    :name: pymssql
    :dbapi: pymssql
    :connectstring: mssql+pymssql://<username>:<password>@<freetds_name>/?charset=utf8

pymssql is a Python module that provides a Python DBAPI interface around
`FreeTDS <https://www.freetds.org/>`_.

.. versionchanged:: 2.0.5

    pymssql was restored to SQLAlchemy's continuous integration testing


"""  # noqa

import re

from .base import MSDialect
from .base import MSIdentifierPreparer
from ... import types as sqltypes
from ... import util
from ...engine import processors


# pymssql 专用 Numeric：非 asdecimal 时用 to_float 处理结果
class _MSNumeric_pymssql(sqltypes.Numeric):
    # 按 asdecimal 选择浮点或标准 Numeric 结果处理器
    def result_processor(self, dialect, type_):
        if not self.asdecimal:
            return processors.to_float
        else:
            return sqltypes.Numeric.result_processor(self, dialect, type_)


# pymssql 标识符预处理器：pyformat 但无需双写百分号
class MSIdentifierPreparer_pymssql(MSIdentifierPreparer):
    def __init__(self, dialect):
        super().__init__(dialect)
        # pymssql has the very unusual behavior that it uses pyformat
        # yet does not require that percent signs be doubled
        self._double_percents = False


# pymssql 方言主类：连接参数、版本检测与断连判定
class MSDialect_pymssql(MSDialect):
    supports_statement_cache = True
    supports_native_decimal = True
    supports_native_uuid = True
    driver = "pymssql"

    preparer = MSIdentifierPreparer_pymssql

    colspecs = util.update_copy(
        MSDialect.colspecs,
        {sqltypes.Numeric: _MSNumeric_pymssql, sqltypes.Float: sqltypes.Float},
    )

    @classmethod
    # 导入 pymssql；旧版补 Binary、低于 1.0 时告警
    def import_dbapi(cls):
        module = __import__("pymssql")
        # pymmsql < 2.1.1 doesn't have a Binary method.  we use string
        client_ver = tuple(int(x) for x in module.__version__.split("."))
        if client_ver < (2, 1, 1):
            # TODO: monkeypatching here is less than ideal
            module.Binary = lambda x: x if hasattr(x, "decode") else str(x)

        if client_ver < (1,):
            util.warn(
                "The pymssql dialect expects at least "
                "the 1.0 series of the pymssql DBAPI."
            )
        return module

    # 解析 @@version 字符串得到 (major, minor, build, revision)
    def _get_server_version_info(self, connection):
        vers = connection.exec_driver_sql("select @@version").scalar()
        m = re.match(r"Microsoft .*? - (\d+)\.(\d+)\.(\d+)\.(\d+)", vers)
        if m:
            return tuple(int(x) for x in m.group(1, 2, 3, 4))
        else:
            return None

    # 将 URL 转为 pymssql 关键字连接参数（host:port 合并）
    def create_connect_args(self, url):
        opts = url.translate_connect_args(username="user")
        opts.update(url.query)
        port = opts.pop("port", None)
        if port and "host" in opts:
            opts["host"] = "%s:%s" % (opts["host"], port)
        return ([], opts)

    # 根据 FreeTDS/连接错误消息判断是否为断连
    def is_disconnect(self, e, connection, cursor):
        for msg in (
            "Adaptive Server connection timed out",
            "Net-Lib error during Connection reset by peer",
            "message 20003",  # connection timeout
            "Error 10054",
            "Not connected to any MS SQL server",
            "Connection is closed",
            "message 20006",  # Write to the server failed
            "message 20017",  # Unexpected EOF from the server
            "message 20047",  # DBPROCESS is dead or not enabled
            "The server failed to resume the transaction",
        ):
            if msg in str(e):
                return True
        else:
            return False

    # 在基类隔离级别列表上追加 AUTOCOMMIT
    def get_isolation_level_values(self, dbapi_connection):
        return super().get_isolation_level_values(dbapi_connection) + [
            "AUTOCOMMIT"
        ]

    # AUTOCOMMIT 时直接 autocommit(True)，否则走基类
    def set_isolation_level(self, dbapi_connection, level):
        if level == "AUTOCOMMIT":
            dbapi_connection.autocommit(True)
        else:
            dbapi_connection.autocommit(False)
            super().set_isolation_level(dbapi_connection, level)


# 方言入口：供 create_engine 解析 mssql+pymssql
dialect = MSDialect_pymssql
