# engine/strategies.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

"""Deprecated mock engine strategy used by Alembic."""

# 已弃用的 Alembic mock 引擎策略别名

from __future__ import annotations

from .mock import MockConnection  # noqa


# 历史兼容：MockConnection 类引用
class MockEngineStrategy:
    MockConnection = MockConnection
