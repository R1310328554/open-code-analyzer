# sql/roles.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# SQL 角色标记：约束 ClauseElement 在语句中的合法使用位置

from __future__ import annotations

from typing import Any
from typing import Generic
from typing import Optional
from typing import TYPE_CHECKING
from typing import TypeVar

from .. import util
from ..util.typing import Literal

if TYPE_CHECKING:
    from ._typing import _PropagateAttrsType
    from .elements import Label
    from .selectable import _SelectIterable
    from .selectable import FromClause
    from .selectable import Subquery

_T = TypeVar("_T", bound=Any)
_T_co = TypeVar("_T_co", bound=Any, covariant=True)


# SQL 语句结构中的角色基类（1.4+ 类型安全）
class SQLRole:
    """Define a "role" within a SQL statement structure.

    Classes within SQL Core participate within SQLRole hierarchies in order
    to more accurately indicate where they may be used within SQL statements
    of all types.

    .. versionadded:: 1.4

    """

    __slots__ = ()
    allows_lambda = False
    uses_inspection = False


# 需 inspect() 后参与 coercion 的角色 mixin
class UsesInspection:
    __slots__ = ()
    _post_inspect: Literal[None] = None
    uses_inspection = True


# 允许 lambda 延迟求值的角色 mixin
class AllowsLambdaRole:
    __slots__ = ()
    allows_lambda = True


# 参与 SQL 编译缓存键计算的角色
class HasCacheKeyRole(SQLRole):
    __slots__ = ()
    _role_name = "Cacheable Core or ORM object"


# 可执行语句选项（如 with_loader_criteria）角色
class ExecutableOptionRole(SQLRole):
    __slots__ = ()
    _role_name = "ExecutionOption Core or ORM object"


# 字面量值参数角色
class LiteralValueRole(SQLRole):
    __slots__ = ()
    _role_name = "Literal Python value"


# 列/表达式参数角色
class ColumnArgumentRole(SQLRole):
    __slots__ = ()
    _role_name = "Column expression"


# 列参数或字符串键名角色
class ColumnArgumentOrKeyRole(ColumnArgumentRole):
    __slots__ = ()
    _role_name = "Column expression or string key"


# 字符串按 plain column 解析的角色
class StrAsPlainColumnRole(ColumnArgumentRole):
    __slots__ = ()
    _role_name = "Column expression or string key"


# 列列表（SELECT/GROUP BY 等）角色
class ColumnListRole(SQLRole):
    """Elements suitable for forming comma separated lists of expressions."""

    __slots__ = ()


# 字符串标签/名称角色
class StringRole(SQLRole):
    """mixin indicating a role that results in strings"""

    __slots__ = ()


# 需截断长度的标签字符串角色
class TruncatedLabelRole(StringRole, SQLRole):
    __slots__ = ()
    _role_name = "String SQL identifier"


# SELECT 列子句实体/表达式角色
class ColumnsClauseRole(AllowsLambdaRole, UsesInspection, ColumnListRole):
    __slots__ = ()
    _role_name = (
        "Column expression, FROM clause, or other columns clause element"
    )

    @property
    def _select_iterable(self) -> _SelectIterable:
        raise NotImplementedError()


# 带返回类型的列子句角色
class TypedColumnsClauseRole(Generic[_T_co], SQLRole):
    """element-typed form of ColumnsClauseRole"""

    __slots__ = ()


# LIMIT/OFFSET 参数角色
class LimitOffsetRole(SQLRole):
    __slots__ = ()
    _role_name = "LIMIT / OFFSET expression"


# GROUP BY / ORDER BY 列列表角色
class ByOfRole(ColumnListRole):
    __slots__ = ()
    _role_name = "GROUP BY / OF / etc. expression"


# GROUP BY 表达式角色
class GroupByRole(AllowsLambdaRole, UsesInspection, ByOfRole):
    __slots__ = ()
    # note there's a special case right now where you can pass a whole
    # ORM entity to group_by() and it splits out.   we may not want to keep
    # this around

    _role_name = "GROUP BY expression"


# ORDER BY 表达式角色
class OrderByRole(AllowsLambdaRole, ByOfRole):
    __slots__ = ()
    _role_name = "ORDER BY expression"


# 语句结构片段（JOIN/WHERE 等）角色
class StructuralRole(SQLRole):
    __slots__ = ()


# 语句级编译选项角色
class StatementOptionRole(StructuralRole):
    __slots__ = ()
    _role_name = "statement sub-expression element"


# JOIN ON 条件角色
class OnClauseRole(AllowsLambdaRole, StructuralRole):
    __slots__ = ()
    _role_name = (
        "ON clause, typically a SQL expression or "
        "ORM relationship attribute"
    )


# WHERE/HAVING 谓词角色
class WhereHavingRole(OnClauseRole):
    __slots__ = ()
    _role_name = "SQL expression for WHERE/HAVING role"


# 通用 SQL 表达式元素角色
class ExpressionElementRole(TypedColumnsClauseRole[_T_co]):
    # note when using generics for ExpressionElementRole,
    # the generic type needs to be in
    # sqlalchemy.sql.coercions._impl_lookup mapping also.
    # these are set up for basic types like int, bool, str, float
    # right now

    __slots__ = ()
    _role_name = "SQL expression element"

    def label(self, name: Optional[str]) -> Label[_T]:
        raise NotImplementedError()


# 常量表达式（True_/False_/Null）角色
class ConstExprRole(ExpressionElementRole[_T]):
    __slots__ = ()
    _role_name = "Constant True/False/None expression"


# 带 label 的列表达式角色
class LabeledColumnExprRole(ExpressionElementRole[_T]):
    __slots__ = ()


# 二元表达式角色
class BinaryElementRole(ExpressionElementRole[_T]):
    __slots__ = ()
    _role_name = "SQL expression element or literal value"


# IN / VALUES / 子查询集合角色
class InElementRole(SQLRole):
    __slots__ = ()
    _role_name = (
        "IN expression list, SELECT construct, or bound parameter object"
    )


# JOIN 右表/目标角色
class JoinTargetRole(AllowsLambdaRole, UsesInspection, StructuralRole):
    __slots__ = ()
    _role_name = (
        "Join target, typically a FROM expression, or ORM "
        "relationship attribute"
    )


# FROM 子句表/子查询角色
class FromClauseRole(ColumnsClauseRole, JoinTargetRole):
    __slots__ = ()
    _role_name = "FROM expression, such as a Table or alias() object"

    _is_subquery = False

    named_with_column: bool


# 严格 FROM 子句（不可匿名化）角色
class StrictFromClauseRole(FromClauseRole):
    __slots__ = ()
    # does not allow text() or select() objects


# 可匿名化为 Alias 的 FROM 角色
class AnonymizedFromClauseRole(StrictFromClauseRole):
    __slots__ = ()

    if TYPE_CHECKING:

        def _anonymous_fromclause(
            self, *, name: Optional[str] = None, flat: bool = False
        ) -> FromClause: ...


# 返回行集的子句角色
class ReturnsRowsRole(SQLRole):
    __slots__ = ()
    _role_name = (
        "Row returning expression such as a SELECT, a FROM clause, or an "
        "INSERT/UPDATE/DELETE with RETURNING"
    )


# 完整 SQL 语句角色
class StatementRole(SQLRole):
    __slots__ = ()
    _role_name = "Executable SQL or text() construct"

    if TYPE_CHECKING:

        @util.memoized_property
        def _propagate_attrs(self) -> _PropagateAttrsType: ...

    else:
        _propagate_attrs = util.EMPTY_DICT


# SELECT 类语句角色
class SelectStatementRole(StatementRole, ReturnsRowsRole):
    __slots__ = ()
    _role_name = "SELECT construct or equivalent text() construct"

    def subquery(self) -> Subquery:
        raise NotImplementedError(
            "All SelectStatementRole objects should implement a "
            ".subquery() method."
        )


# 可附加 CTE 的语句角色
class HasCTERole(ReturnsRowsRole):
    __slots__ = ()


# CTE 对象本身角色
class IsCTERole(SQLRole):
    __slots__ = ()
    _role_name = "CTE object"


# UNION 等复合 SELECT 成员角色
class CompoundElementRole(AllowsLambdaRole, SQLRole):
    """SELECT statements inside a CompoundSelect, e.g. UNION, EXTRACT, etc."""

    __slots__ = ()
    _role_name = (
        "SELECT construct for inclusion in a UNION or other set construct"
    )


# TODO: are we using this?
# INSERT/UPDATE/DELETE 语句角色
class DMLRole(StatementRole):
    __slots__ = ()


# DML 目标表角色
class DMLTableRole(FromClauseRole):
    __slots__ = ()
    _role_name = "subject table for an INSERT, UPDATE or DELETE"


# DML SET/VALUES 列表达式角色
class DMLColumnRole(SQLRole):
    __slots__ = ()
    _role_name = "SET/VALUES column expression or string key"


# 嵌入 DML 的 SELECT 子句角色
class DMLSelectRole(SQLRole):
    """A SELECT statement embedded in DML, typically INSERT from SELECT"""

    __slots__ = ()
    _role_name = "SELECT statement or equivalent textual object"


# DDL 语句角色
class DDLRole(StatementRole):
    __slots__ = ()


# DDL 约束中的 SQL 表达式角色
class DDLExpressionRole(StructuralRole):
    __slots__ = ()
    _role_name = "SQL expression element for DDL constraint"


# DDL 约束列名/列表达式角色
class DDLConstraintColumnRole(SQLRole):
    __slots__ = ()
    _role_name = "String column name or column expression for DDL constraint"


# 外键 DDL 被引用列角色
class DDLReferredColumnRole(DDLConstraintColumnRole):
    __slots__ = ()
    _role_name = (
        "String column name or Column object for DDL foreign key constraint"
    )
