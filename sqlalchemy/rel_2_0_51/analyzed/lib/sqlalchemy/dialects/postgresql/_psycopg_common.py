# dialects/postgresql/_psycopg_common.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# mypy: ignore-errors
# psycopg/psycopg2 共享方言 mixin：类型 colspec、连接参数与两阶段提交

from __future__ import annotationsfrom __future__ import annotations

import decimal

from .array import ARRAY as PGARRAY
from .base import _DECIMAL_TYPES
from .base import _FLOAT_TYPES
from .base import _INT_TYPES
from .base import PGDialect
from .base import PGExecutionContext
from .hstore import HSTORE
from .pg_catalog import _SpaceVector
from .pg_catalog import INT2VECTOR
from .pg_catalog import OIDVECTOR
from ... import exc
from ... import types as sqltypes
from ... import util
from ...engine import processors

_server_side_id = util.counter()


# psycopg 系列 Numeric：按 OID 区分 float/decimal 结果处理器
class _PsycopgNumeric(sqltypes.Numeric):
    def bind_processor(self, dialect):
        return None

    def result_processor(self, dialect, coltype):
        if self.asdecimal:
            if coltype in _FLOAT_TYPES:
                return processors.to_decimal_processor_factory(
                    decimal.Decimal, self._effective_decimal_return_scale
                )
            elif coltype in _DECIMAL_TYPES or coltype in _INT_TYPES:
                # psycopg returns Decimal natively for 1700
                return None
            else:
                raise exc.InvalidRequestError(
                    "Unknown PG numeric type: %d" % coltype
                )
        else:
            if coltype in _FLOAT_TYPES:
                # psycopg returns float natively for 701
                return None
            elif coltype in _DECIMAL_TYPES or coltype in _INT_TYPES:
                return processors.to_float
            else:
                raise exc.InvalidRequestError(
                    "Unknown PG numeric type: %d" % coltype
                )


# psycopg Float 类型绑定
class _PsycopgFloat(_PsycopgNumeric):
    __visit_name__ = "float"


# HSTORE：有原生 hstore 时跳过自定义 bind/result 处理器
class _PsycopgHStore(HSTORE):
    def bind_processor(self, dialect):
        if dialect._has_native_hstore:
            return None
        else:
            return super().bind_processor(dialect)

    def result_processor(self, dialect, coltype):
        if dialect._has_native_hstore:
            return None
        else:
            return super().result_processor(dialect, coltype)


# ARRAY 启用 render_bind_cast 以生成 ::type[] 绑定
class _PsycopgARRAY(PGARRAY):
    render_bind_cast = True


# int2vector 结果处理器（空格分隔整数列表）
class _PsycopgINT2VECTOR(_SpaceVector, INT2VECTOR):
    pass


# oidvector 结果处理器
class _PsycopgOIDVECTOR(_SpaceVector, OIDVECTOR):
    pass


# psycopg 执行上下文：命名服务端游标 c_<id>_<counter>
class _PGExecutionContext_common_psycopg(PGExecutionContext):
    # 创建带唯一名称的服务端游标供流式读取
    def create_server_side_cursor(self):
        # use server-side cursors:
        # psycopg
        # https://www.psycopg.org/psycopg3/docs/advanced/cursors.html#server-side-cursors
        # psycopg2
        # https://www.psycopg.org/docs/usage.html#server-side-cursors
        ident = "c_%s_%s" % (hex(id(self))[2:], hex(_server_side_id())[2:])
        return self._dbapi_connection.cursor(ident)


# psycopg/psycopg2 方言公共基类：多主机 URL、AUTOCOMMIT 与 TPC
class _PGDialect_common_psycopg(PGDialect):
    supports_statement_cache = True
    supports_server_side_cursors = True

    default_paramstyle = "pyformat"

    _has_native_hstore = True

    colspecs = util.update_copy(
        PGDialect.colspecs,
        {
            sqltypes.Numeric: _PsycopgNumeric,
            sqltypes.Float: _PsycopgFloat,
            HSTORE: _PsycopgHStore,
            sqltypes.ARRAY: _PsycopgARRAY,
            INT2VECTOR: _PsycopgINT2VECTOR,
            OIDVECTOR: _PsycopgOIDVECTOR,
        },
    )

    def __init__(
        self,
        client_encoding=None,
        use_native_hstore=True,
        **kwargs,
    ):
        PGDialect.__init__(self, **kwargs)
        if not use_native_hstore:
            self._has_native_hstore = False
        self.use_native_hstore = use_native_hstore
        self.client_encoding = client_encoding

    # URL 转关键字参数；多 host 合并为逗号分隔；无参数时 dsn=''
    def create_connect_args(self, url):
        opts = url.translate_connect_args(username="user", database="dbname")

        multihosts, multiports = self._split_multihost_from_url(url)

        if opts or url.query:
            if not opts:
                opts = {}
            if "port" in opts:
                opts["port"] = int(opts["port"])
            opts.update(url.query)

            if multihosts:
                opts["host"] = ",".join(multihosts)
                comma_ports = ",".join(str(p) if p else "" for p in multiports)
                if comma_ports:
                    opts["port"] = comma_ports
            return ([], opts)
        else:
            # no connection arguments whatsoever; psycopg2.connect()
            # requires that "dsn" be present as a blank string.
            return ([""], opts)

    # 含 AUTOCOMMIT 的隔离级别列表
    def get_isolation_level_values(self, dbapi_connection):
        return (
            "AUTOCOMMIT",
            "READ COMMITTED",
            "READ UNCOMMITTED",
            "REPEATABLE READ",
            "SERIALIZABLE",
        )

    # 设置连接 deferrable 属性
    def set_deferrable(self, connection, value):
        connection.deferrable = value

    def get_deferrable(self, connection):
        return connection.deferrable

    def _do_autocommit(self, connection, value):
        connection.autocommit = value

    def detect_autocommit_setting(self, dbapi_connection):
        return bool(dbapi_connection.autocommit)

    # 临时 autocommit 执行 SELECT 1 探活
    def do_ping(self, dbapi_connection):
        before_autocommit = dbapi_connection.autocommit

        if not before_autocommit:
            dbapi_connection.autocommit = True
        cursor = dbapi_connection.cursor()
        try:
            cursor.execute(self._dialect_specific_select_one)
        finally:
            cursor.close()
            if not before_autocommit and not dbapi_connection.closed:
                dbapi_connection.autocommit = before_autocommit

        return True

    # 两阶段提交：tpc_begin
    def do_begin_twophase(self, connection, xid):
        connection.connection.tpc_begin(xid)

    def do_prepare_twophase(self, connection, xid):
        connection.connection.tpc_prepare()

    def _do_twophase(self, dbapi_conn, operation, xid, recover=False):
        if recover:
            if not self._twophase_idle_check(dbapi_conn):
                dbapi_conn.rollback()
            operation(xid)
        else:
            operation()

    def _twophase_idle_check(self, dbapi_conn):
        raise NotImplementedError

    def do_rollback_twophase(
        self, connection, xid, is_prepared=True, recover=False
    ):
        dbapi_conn = connection.connection.dbapi_connection
        self._do_twophase(
            dbapi_conn, dbapi_conn.tpc_rollback, xid, recover=recover
        )

    def do_commit_twophase(
        self, connection, xid, is_prepared=True, recover=False
    ):
        dbapi_conn = connection.connection.dbapi_connection
        self._do_twophase(
            dbapi_conn, dbapi_conn.tpc_commit, xid, recover=recover
        )

    # 恢复未完成的两阶段事务 xid 列表
    def do_recover_twophase(self, connection):
        return [str(row) for row in connection.connection.tpc_recover()]
