# future/__init__.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

"""2.0 API features.

this module is legacy as 2.0 APIs are now standard.

"""

# SQLAlchemy 2.0 过渡命名空间（已弃用）：重导出 Engine/Connection/select
"""2.0 API features.

this module is legacy as 2.0 APIs are now standard.

"""

# SQLAlchemy 2.0 过渡命名空间（已弃用）：重导出 Engine/Connection/select
"""2.0 API features.

this module is legacy as 2.0 APIs are now standard.

"""

# 2.0 风格 Connection
# 2.0 风格 Connection
from .engine import Connection as Connection
# 2.0 风格 create_engine
# 2.0 风格 create_engine
from .engine import create_engine as create_engine
# 2.0 风格 Engine
# 2.0 风格 Engine
from .engine import Engine as Engine
# 2.0 风格 select() 构造器
# 2.0 风格 select() 构造器
from ..sql._selectable_constructors import select as select
