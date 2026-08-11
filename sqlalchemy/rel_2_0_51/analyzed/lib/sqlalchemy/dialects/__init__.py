# dialects/__init__.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

# 方言插件注册：PluginLoader 按 dialect[+driver] 懒加载各后端

from __future__ import annotations

from typing import Any
from typing import Callable
from typing import Optional
from typing import Type
from typing import TYPE_CHECKING

from .. import util

if TYPE_CHECKING:
    from ..engine.interfaces import Dialect

__all__ = ("mssql", "mysql", "oracle", "postgresql", "sqlite")


# 默认方言导入器：解析 name 为 dialect.driver 并返回 dialect 类工厂
def _auto_fn(name: str) -> Optional[Callable[[], Type[Dialect]]]:
    """default dialect importer.

    plugs into the :class:`.PluginLoader`
    as a first-hit system.

    """
    if "." in name:
        dialect, driver = name.split(".")
    else:
        dialect = name
        driver = "base"

    try:
        if dialect == "mariadb":
            # it's "OK" for us to hardcode here since _auto_fn is already
            # hardcoded.   if mysql / mariadb etc were third party dialects
            # they would just publish all the entrypoints, which would actually
            # look much nicer.
            module: Any = __import__(
                "sqlalchemy.dialects.mysql.mariadb"
            ).dialects.mysql.mariadb
            return module.loader(driver)  # type: ignore
        else:
            module = __import__("sqlalchemy.dialects.%s" % (dialect,)).dialects
            module = getattr(module, dialect)
    except ImportError:
        return None

    if hasattr(module, driver):
        module = getattr(module, driver)
        return lambda: module.dialect
    else:
        return None


# sqlalchemy.dialects 入口点注册表，供 create_engine 解析 drivername
registry = util.PluginLoader("sqlalchemy.dialects", auto_fn=_auto_fn)

# sqlalchemy.plugins 通用插件加载器
plugins = util.PluginLoader("sqlalchemy.plugins")
