"""
django.utils.asyncio — 异步上下文下的同步 API 防护。

async_unsafe 装饰器在运行 event loop 时禁止调用阻塞 ORM 等。
"""

import osimport os
from asyncio import get_running_loop
from functools import wraps

from django.core.exceptions import SynchronousOnlyOperation


# 标记函数不可在 async 上下文直接调用
def async_unsafe(message):
    """
    Decorator to mark functions as async-unsafe. Someone trying to access
    the function while in an async context will get an error message.
    """

    def decorator(func):
        @wraps(func)
        def inner(*args, **kwargs):
            # Detect a running event loop in this thread.
            try:
                get_running_loop()
            except RuntimeError:
                pass
            else:
                if not os.environ.get("DJANGO_ALLOW_ASYNC_UNSAFE"):
                    raise SynchronousOnlyOperation(message)
            # Pass onward.
            return func(*args, **kwargs)

        return inner

    # If the message is actually a function, then be a no-arguments decorator.
    if callable(message):
        func = message
        message = (
            "You cannot call this from an async context - use a thread or "
            "sync_to_async."
        )
        return decorator(func)
    else:
        return decorator
