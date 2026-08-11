# dialects/mysql/mariadb.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

# MariaDB 专用方言基类、INET 类型与驱动 loader

from __future__ import annotationsfrom __future__ import annotations

from typing import Any

from .base import MariaDBIdentifierPreparer
from .base import MySQLDialect
from .base import MySQLIdentifierPreparer
from .base import MySQLTypeCompiler
from ...sql import sqltypes


# MariaDB INET4（IPv4）列类型
class INET4(sqltypes.TypeEngine[str]):
    """INET4 column type for MariaDB

    .. versionadded:: 2.0.37
    """

    __visit_name__ = "INET4"


# MariaDB INET6（IPv6）列类型
class INET6(sqltypes.TypeEngine[str]):
    """INET6 column type for MariaDB

    .. versionadded:: 2.0.37
    """

    __visit_name__ = "INET6"


# MariaDB 类型编译：INET4/INET6 DDL
class MariaDBTypeCompiler(MySQLTypeCompiler):
    def visit_INET4(self, type_: INET4, **kwargs: Any) -> str:
        return "INET4"

    def visit_INET6(self, type_: INET6, **kwargs: Any) -> str:
        return "INET6"


# MariaDB 方言标志：is_mariadb、专用 preparer 与 type compiler
class MariaDBDialect(MySQLDialect):
    is_mariadb = True
    supports_statement_cache = True
    name = "mariadb"
    preparer: type[MySQLIdentifierPreparer] = MariaDBIdentifierPreparer
    type_compiler_cls = MariaDBTypeCompiler


# 按驱动名动态组合 MariaDBDialect 与具体 driver 方言
def loader(driver: str) -> type[MariaDBDialect]:
    dialect_mod = __import__(
        "sqlalchemy.dialects.mysql.%s" % driver
    ).dialects.mysql

    driver_mod = getattr(dialect_mod, driver)
    if hasattr(driver_mod, "mariadb_dialect"):
        driver_cls = driver_mod.mariadb_dialect
        return driver_cls  # type: ignore[no-any-return]
    else:
        driver_cls = driver_mod.dialect

        return type(
            "MariaDBDialect_%s" % driver,
            (
                MariaDBDialect,
                driver_cls,
            ),
            {"supports_statement_cache": True},
        )
