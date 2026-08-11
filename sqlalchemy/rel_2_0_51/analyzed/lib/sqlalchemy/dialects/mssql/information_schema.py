# dialects/mssql/information_schema.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# mypy: ignore-errors

# INFORMATION_SCHEMA / sys  catalog 表定义，供 MSSQL 反射查询

from ... import cast
from ... import Column
from ... import MetaData
from ... import Table
from ...ext.compiler import compiles
from ...sql import expression
from ...types import Boolean
from ...types import Integer
from ...types import Numeric
from ...types import NVARCHAR
from ...types import String
from ...types import TypeDecorator
from ...types import Unicode

# 反射用独立 MetaData，承载 information_schema 与 sys 视图
ischema = MetaData()


# 绑定值在 SQL Server 2005+ 上 CAST 为 Unicode，旧版直接透传
class CoerceUnicode(TypeDecorator):
    impl = Unicode
    cache_ok = True

    def bind_expression(self, bindvalue):
        return _cast_on_2005(bindvalue)


# 编译期按服务器版本决定是否对绑定值 CAST
class _cast_on_2005(expression.ColumnElement):
    def __init__(self, bindvalue):
        self.bindvalue = bindvalue


# 版本分支：低于 MS_2005 原样编译 bindvalue
@compiles(_cast_on_2005)
def _compile(element, compiler, **kw):
    from . import base

    if (
        compiler.dialect.server_version_info is None
        or compiler.dialect.server_version_info < base.MS_2005_VERSION
    ):
        return compiler.process(element.bindvalue, **kw)
    else:
        return compiler.process(cast(element.bindvalue, Unicode), **kw)


# INFORMATION_SCHEMA.SCHEMATA 模式列表
schemata = Table(
    "SCHEMATA",
    ischema,
    Column("CATALOG_NAME", CoerceUnicode, key="catalog_name"),
    Column("SCHEMA_NAME", CoerceUnicode, key="schema_name"),
    Column("SCHEMA_OWNER", CoerceUnicode, key="schema_owner"),
    schema="INFORMATION_SCHEMA",
)

# INFORMATION_SCHEMA.TABLES 基表/视图元数据
tables = Table(
    "TABLES",
    ischema,
    Column("TABLE_CATALOG", CoerceUnicode, key="table_catalog"),
    Column("TABLE_SCHEMA", CoerceUnicode, key="table_schema"),
    Column("TABLE_NAME", CoerceUnicode, key="table_name"),
    Column("TABLE_TYPE", CoerceUnicode, key="table_type"),
    schema="INFORMATION_SCHEMA",
)

# INFORMATION_SCHEMA.COLUMNS 列级信息（标准视图）
columns = Table(
    "COLUMNS",
    ischema,
    Column("TABLE_SCHEMA", CoerceUnicode, key="table_schema"),
    Column("TABLE_NAME", CoerceUnicode, key="table_name"),
    Column("COLUMN_NAME", CoerceUnicode, key="column_name"),
    Column("IS_NULLABLE", Integer, key="is_nullable"),
    Column("DATA_TYPE", String, key="data_type"),
    Column("ORDINAL_POSITION", Integer, key="ordinal_position"),
    Column(
        "CHARACTER_MAXIMUM_LENGTH", Integer, key="character_maximum_length"
    ),
    Column("NUMERIC_PRECISION", Integer, key="numeric_precision"),
    Column("NUMERIC_SCALE", Integer, key="numeric_scale"),
    Column("COLUMN_DEFAULT", Integer, key="column_default"),
    Column("COLLATION_NAME", String, key="collation_name"),
    schema="INFORMATION_SCHEMA",
)

# sys.columns 系统目录列定义
sys_columns = Table(
    "columns",
    ischema,
    Column("object_id", Integer),
    Column("name", CoerceUnicode),
    Column("column_id", Integer),
    Column("default_object_id", Integer),
    Column("user_type_id", Integer),
    Column("is_nullable", Integer),
    Column("ordinal_position", Integer),
    Column("max_length", Integer),
    Column("precision", Integer),
    Column("scale", Integer),
    Column("collation_name", String),
    schema="sys",
)

# sys.types 用户与系统类型映射
sys_types = Table(
    "types",
    ischema,
    Column("name", CoerceUnicode, key="name"),
    Column("system_type_id", Integer, key="system_type_id"),
    Column("user_type_id", Integer, key="user_type_id"),
    Column("schema_id", Integer, key="schema_id"),
    Column("max_length", Integer, key="max_length"),
    Column("precision", Integer, key="precision"),
    Column("scale", Integer, key="scale"),
    Column("collation_name", CoerceUnicode, key="collation_name"),
    Column("is_nullable", Boolean, key="is_nullable"),
    Column("is_user_defined", Boolean, key="is_user_defined"),
    Column("is_assembly_type", Boolean, key="is_assembly_type"),
    Column("default_object_id", Integer, key="default_object_id"),
    Column("rule_object_id", Integer, key="rule_object_id"),
    Column("is_table_type", Boolean, key="is_table_type"),
    schema="sys",
)

# INFORMATION_SCHEMA.TABLE_CONSTRAINTS
constraints = Table(
    "TABLE_CONSTRAINTS",
    ischema,
    Column("TABLE_SCHEMA", CoerceUnicode, key="table_schema"),
    Column("TABLE_NAME", CoerceUnicode, key="table_name"),
    Column("CONSTRAINT_NAME", CoerceUnicode, key="constraint_name"),
    Column("CONSTRAINT_TYPE", CoerceUnicode, key="constraint_type"),
    schema="INFORMATION_SCHEMA",
)

# sys.default_constraints 列默认值
sys_default_constraints = Table(
    "default_constraints",
    ischema,
    Column("object_id", Integer),
    Column("name", CoerceUnicode),
    Column("schema_id", Integer),
    Column("parent_column_id", Integer),
    Column("definition", CoerceUnicode),
    schema="sys",
)

# INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE
column_constraints = Table(
    "CONSTRAINT_COLUMN_USAGE",
    ischema,
    Column("TABLE_SCHEMA", CoerceUnicode, key="table_schema"),
    Column("TABLE_NAME", CoerceUnicode, key="table_name"),
    Column("COLUMN_NAME", CoerceUnicode, key="column_name"),
    Column("CONSTRAINT_NAME", CoerceUnicode, key="constraint_name"),
    schema="INFORMATION_SCHEMA",
)

# INFORMATION_SCHEMA.KEY_COLUMN_USAGE 主外键列
key_constraints = Table(
    "KEY_COLUMN_USAGE",
    ischema,
    Column("TABLE_SCHEMA", CoerceUnicode, key="table_schema"),
    Column("TABLE_NAME", CoerceUnicode, key="table_name"),
    Column("COLUMN_NAME", CoerceUnicode, key="column_name"),
    Column("CONSTRAINT_NAME", CoerceUnicode, key="constraint_name"),
    Column("CONSTRAINT_SCHEMA", CoerceUnicode, key="constraint_schema"),
    Column("ORDINAL_POSITION", Integer, key="ordinal_position"),
    schema="INFORMATION_SCHEMA",
)

# INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS 外键规则
ref_constraints = Table(
    "REFERENTIAL_CONSTRAINTS",
    ischema,
    Column("CONSTRAINT_CATALOG", CoerceUnicode, key="constraint_catalog"),
    Column("CONSTRAINT_SCHEMA", CoerceUnicode, key="constraint_schema"),
    Column("CONSTRAINT_NAME", CoerceUnicode, key="constraint_name"),
    # TODO: is CATLOG misspelled ?
    Column(
        "UNIQUE_CONSTRAINT_CATLOG",
        CoerceUnicode,
        key="unique_constraint_catalog",
    ),
    Column(
        "UNIQUE_CONSTRAINT_SCHEMA",
        CoerceUnicode,
        key="unique_constraint_schema",
    ),
    Column(
        "UNIQUE_CONSTRAINT_NAME", CoerceUnicode, key="unique_constraint_name"
    ),
    Column("MATCH_OPTION", String, key="match_option"),
    Column("UPDATE_RULE", String, key="update_rule"),
    Column("DELETE_RULE", String, key="delete_rule"),
    schema="INFORMATION_SCHEMA",
)

# INFORMATION_SCHEMA.VIEWS 视图定义文本
views = Table(
    "VIEWS",
    ischema,
    Column("TABLE_CATALOG", CoerceUnicode, key="table_catalog"),
    Column("TABLE_SCHEMA", CoerceUnicode, key="table_schema"),
    Column("TABLE_NAME", CoerceUnicode, key="table_name"),
    Column("VIEW_DEFINITION", CoerceUnicode, key="view_definition"),
    Column("CHECK_OPTION", String, key="check_option"),
    Column("IS_UPDATABLE", String, key="is_updatable"),
    schema="INFORMATION_SCHEMA",
)

# sys.computed_columns 计算列 persisted 定义
computed_columns = Table(
    "computed_columns",
    ischema,
    Column("object_id", Integer),
    Column("name", CoerceUnicode),
    Column("column_id", Integer),
    Column("is_computed", Boolean),
    Column("is_persisted", Boolean),
    Column("definition", CoerceUnicode),
    schema="sys",
)

# INFORMATION_SCHEMA.SEQUENCES T-SQL SEQUENCE
sequences = Table(
    "SEQUENCES",
    ischema,
    Column("SEQUENCE_CATALOG", CoerceUnicode, key="sequence_catalog"),
    Column("SEQUENCE_SCHEMA", CoerceUnicode, key="sequence_schema"),
    Column("SEQUENCE_NAME", CoerceUnicode, key="sequence_name"),
    schema="INFORMATION_SCHEMA",
)


# identity_columns 中 sql_variant 数值列 CAST 为 Numeric(38,0)
class NumericSqlVariant(TypeDecorator):
    r"""This type casts sql_variant columns in the identity_columns view
    to numeric. This is required because:

    * pyodbc does not support sql_variant
    * pymssql under python 2 return the byte representation of the number,
      int 1 is returned as "\x01\x00\x00\x00". On python 3 it returns the
      correct value as string.
    """

    impl = Unicode
    cache_ok = True

    def column_expression(self, colexpr):
        return cast(colexpr, Numeric(38, 0))


# sys.identity_columns IDENTITY 种子/增量/最后值
identity_columns = Table(
    "identity_columns",
    ischema,
    Column("object_id", Integer),
    Column("name", CoerceUnicode),
    Column("column_id", Integer),
    Column("is_identity", Boolean),
    Column("seed_value", NumericSqlVariant),
    Column("increment_value", NumericSqlVariant),
    Column("last_value", NumericSqlVariant),
    Column("is_not_for_replication", Boolean),
    schema="sys",
)


# extended_properties.value 从 sql_variant CAST 为 NVARCHAR
class NVarcharSqlVariant(TypeDecorator):
    """This type casts sql_variant columns in the extended_properties view
    to nvarchar. This is required because pyodbc does not support sql_variant
    """

    impl = Unicode
    cache_ok = True

    def column_expression(self, colexpr):
        return cast(colexpr, NVARCHAR)


# sys.extended_properties MS_Description 等扩展属性
extended_properties = Table(
    "extended_properties",
    ischema,
    Column("class", Integer),  # TINYINT
    Column("class_desc", CoerceUnicode),
    Column("major_id", Integer),
    Column("minor_id", Integer),
    Column("name", CoerceUnicode),
    Column("value", NVarcharSqlVariant),
    schema="sys",
)
