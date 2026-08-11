# sql/_orm_types.py
# Copyright (C) 2022-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

"""ORM types that need to present specifically for **documentation only** of
the Executable.execution_options() method, which includes options that
are meaningful to the ORM.

"""

# ORM execution_options 文档用 Literal 类型别名

from __future__ import annotations

from ..util.typing import Literal

# UPDATE/DELETE 后会话同步策略
SynchronizeSessionArgument = Literal[False, "auto", "evaluate", "fetch"]
# ORM DML 执行策略（bulk/raw/orm）
DMLStrategyArgument = Literal["bulk", "raw", "orm", "auto"]
