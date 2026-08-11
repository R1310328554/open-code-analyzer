# ext/asyncio/__init__.py
# Copyright (C) 2020-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

# asyncio 扩展公开 API：引擎、连接、结果与会话

from .engine import async_engine_from_config as async_engine_from_config
# 异步连接
from .engine import AsyncConnection as AsyncConnection
# 异步引擎
from .engine import AsyncEngine as AsyncEngine
# 异步事务
from .engine import AsyncTransaction as AsyncTransaction
# 创建异步引擎
from .engine import create_async_engine as create_async_engine
from .engine import create_async_pool_from_url as create_async_pool_from_url
from .result import AsyncMappingResult as AsyncMappingResult
# 流式/服务端游标结果
from .result import AsyncResult as AsyncResult
from .result import AsyncScalarResult as AsyncScalarResult
from .result import AsyncTupleResult as AsyncTupleResult
from .scoping import async_scoped_session as async_scoped_session
from .session import async_object_session as async_object_session
from .session import async_session as async_session
from .session import async_sessionmaker as async_sessionmaker
from .session import AsyncAttrs as AsyncAttrs
# 异步 ORM 会话
from .session import AsyncSession as AsyncSession
from .session import AsyncSessionTransaction as AsyncSessionTransaction
# 关闭所有 AsyncSession
from .session import close_all_sessions as close_all_sessions
