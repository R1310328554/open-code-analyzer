# dialects/postgresql/operators.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# mypy: ignore-errors
# PostgreSQL 专用 SQL 运算符：JSON/JSONB/HSTORE/ARRAY/RANGE

from ...sql import operatorsfrom ...sql import operators

# JSON/HSTORE 下标运算符优先级
_getitem_precedence = operators._PRECEDENCE[operators.json_getitem_op]
_eq_precedence = operators._PRECEDENCE[operators.eq]

# JSON/JSONB 文本提取 ->>
# JSON + JSONB
ASTEXT = operators.custom_op(
    "->>",
    precedence=_getitem_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
)

# JSONPath 文本提取 #>>
JSONPATH_ASTEXT = operators.custom_op(
    "#>>",
    precedence=_getitem_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
)

# 键存在 ?
# JSONB + HSTORE
HAS_KEY = operators.custom_op(
    "?",
    precedence=_eq_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
    is_comparison=True,
)

# 包含全部键 ?&
HAS_ALL = operators.custom_op(
    "?&",
    precedence=_eq_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
    is_comparison=True,
)

# 包含任一键 ?|
HAS_ANY = operators.custom_op(
    "?|",
    precedence=_eq_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
    is_comparison=True,
)

# JSONB 删除路径 #-
# JSONB
DELETE_PATH = operators.custom_op(
    "#-",
    precedence=_getitem_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
)

# JSONPath 存在 @?
PATH_EXISTS = operators.custom_op(
    "@?",
    precedence=_eq_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
    is_comparison=True,
)

# JSONPath 匹配 @@
PATH_MATCH = operators.custom_op(
    "@@",
    precedence=_eq_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
    is_comparison=True,
)

# 包含 @>
# JSONB + ARRAY + HSTORE + RANGE
CONTAINS = operators.custom_op(
    "@>",
    precedence=_eq_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
    is_comparison=True,
)

# 被包含 <@
CONTAINED_BY = operators.custom_op(
    "<@",
    precedence=_eq_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
    is_comparison=True,
)

# 重叠 &&
# ARRAY + RANGE
OVERLAP = operators.custom_op(
    "&&",
    precedence=_eq_precedence,
    is_comparison=True,
)

# range 严格左 <<
# RANGE
STRICTLY_LEFT_OF = operators.custom_op(
    "<<", precedence=_eq_precedence, is_comparison=True
)

# range 严格右 >>
STRICTLY_RIGHT_OF = operators.custom_op(
    ">>", precedence=_eq_precedence, is_comparison=True
)

# 不向右延伸 &<
NOT_EXTEND_RIGHT_OF = operators.custom_op(
    "&<", precedence=_eq_precedence, is_comparison=True
)

# 不向左延伸 &>
NOT_EXTEND_LEFT_OF = operators.custom_op(
    "&>", precedence=_eq_precedence, is_comparison=True
)

# range 相邻 -|-
ADJACENT_TO = operators.custom_op(
    "-|-", precedence=_eq_precedence, is_comparison=True
)

# HSTORE 键访问 ->
# HSTORE
GETITEM = operators.custom_op(
    "->",
    precedence=_getitem_precedence,
    natural_self_precedent=True,
    eager_grouping=True,
)
