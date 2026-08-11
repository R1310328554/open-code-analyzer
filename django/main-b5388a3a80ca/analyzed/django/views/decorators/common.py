"""
django.views.decorators.common — 通用中间件相关装饰器。

提供 no_append_slash 等 CommonMiddleware 行为控制。
"""

from functools import wrapsfrom functools import wraps
from inspect import iscoroutinefunction


# 标记视图不受 APPEND_SLASH 重定向影响
def no_append_slash(view_func):
    """
    Mark a view function as excluded from CommonMiddleware's APPEND_SLASH
    redirection.
    """

    # view_func.should_append_slash = False would also work, but decorators are
    # nicer if they don't have side effects, so return a new function.

    if iscoroutinefunction(view_func):

        async def _view_wrapper(request, *args, **kwargs):
            return await view_func(request, *args, **kwargs)

    else:

        def _view_wrapper(request, *args, **kwargs):
            return view_func(request, *args, **kwargs)

    _view_wrapper.should_append_slash = False

    return wraps(view_func)(_view_wrapper)
