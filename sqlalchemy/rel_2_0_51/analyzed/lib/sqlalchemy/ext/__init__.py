# ext/__init__.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

# sqlalchemy.ext 包：预加载 ext 子模块

from .. import util as _sa_util

# 延迟加载 ext 下所有子包
_sa_util.preloaded.import_prefix("sqlalchemy.ext")
