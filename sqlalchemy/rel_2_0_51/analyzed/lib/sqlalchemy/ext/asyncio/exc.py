# ext/asyncio/exc.py
# Copyright (C) 2020-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

# asyncio 扩展专用 InvalidRequestError 子类

from ... import exc


# API 结果与 async 不兼容
class AsyncMethodRequired(exc.InvalidRequestError):
    """an API can't be used because its result would not be
    compatible with async"""


# StartableContext 尚未 start
class AsyncContextNotStarted(exc.InvalidRequestError):
    """a startable context manager has not been started."""


# StartableContext 已启动
class AsyncContextAlreadyStarted(exc.InvalidRequestError):
    """a startable context manager is already started."""
