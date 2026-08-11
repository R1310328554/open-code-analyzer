# 遍历 sqlalchemy 包命名空间下所有子模块（pkgutil.walk_packages）
import pkgutil

import sqlalchemy

list(pkgutil.walk_packages(sqlalchemy.__path__, sqlalchemy.__name__ + "."))
