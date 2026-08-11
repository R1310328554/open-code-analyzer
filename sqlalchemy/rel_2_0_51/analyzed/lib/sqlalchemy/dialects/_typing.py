# dialects/_typing.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# 方言层 ON CONFLICT / upsert 相关 TypeAlias（PostgreSQL 等复用约定）

from __future__ import annotations

from typing import Any
from typing import Iterable
from typing import Mapping
from typing import Optional
from typing import Union

from ..sql import roles
from ..sql.base import ColumnCollection
from ..sql.schema import Column
from ..sql.schema import ColumnCollectionConstraint
from ..sql.schema import Index

# ON CONFLICT 目标约束：名称、Constraint、Index 或 None
_OnConflictConstraintT = Union[str, ColumnCollectionConstraint, Index, None]
# ON CONFLICT 索引列元素：Column、列名字符串或 DDL 角色
_OnConflictIndexElementsT = Optional[
    Iterable[Union[Column[Any], str, roles.DDLConstraintColumnRole]]
]
# 部分唯一索引冲突子句的 WHERE 谓词
_OnConflictIndexWhereT = Optional[roles.WhereHavingRole]
# ON CONFLICT DO UPDATE 的 SET 映射或 ColumnCollection
_OnConflictSetT = Optional[
    Union[Mapping[Any, Any], ColumnCollection[Any, Any]]
]
# ON CONFLICT DO UPDATE 附加 WHERE 条件
_OnConflictWhereT = Optional[roles.WhereHavingRole]
