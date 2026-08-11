# connectors/__init__.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php


# 连接器包：跨多种数据库后端复用的 DBAPI 方言 mixin 基类

from ..engine.interfaces import Dialect


# 连接器方言 mixin 基类；当前主要供 pyodbc 系列驱动继承
class Connector(Dialect):
    """Base class for dialect mixins, for DBAPIs that work
    across entirely different database backends.

    Currently the only such mixin is pyodbc.

    """
