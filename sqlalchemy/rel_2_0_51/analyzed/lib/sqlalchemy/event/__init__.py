# event/__init__.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php

# SQLAlchemy 事件子系统公开 API 重导出

from __future__ import annotations

# listen / listens_for / remove / contains 与符号常量
from .api import CANCEL as CANCEL
from .api import contains as contains
from .api import listen as listen
from .api import listens_for as listens_for
from .api import NO_RETVAL as NO_RETVAL
from .api import remove as remove
# 内部 dispatch 属性实现
from .attr import _InstanceLevelDispatch as _InstanceLevelDispatch
from .attr import RefCollection as RefCollection
# Events 与 dispatcher 基类
from .base import _Dispatch as _Dispatch
from .base import _DispatchCommon as _DispatchCommon
from .base import dispatcher as dispatcher
from .base import Events as Events
# 旧版事件签名兼容
from .legacy import _legacy_signature as _legacy_signature
from .legacy import _omit_standard_example as _omit_standard_example
# 监听器注册键与 EventTarget
from .registry import _EventKey as _EventKey
from .registry import _ListenerFnType as _ListenerFnType
from .registry import EventTarget as EventTarget
