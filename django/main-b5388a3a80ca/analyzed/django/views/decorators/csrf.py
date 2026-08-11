"""
django.views.decorators.csrf — CSRF 保护装饰器。

提供 csrf_protect、requires_csrf_token、ensure_csrf_cookie、csrf_exempt。
"""

from functools import wrapsfrom functools import wraps
from inspect import iscoroutinefunction

from django.middleware.csrf import CsrfViewMiddleware, get_token
from django.utils.decorators import decorator_from_middleware

# 与 CsrfViewMiddleware 等效的 per-view CSRF 保护
csrf_protect = decorator_from_middleware(CsrfViewMiddleware)
csrf_protect.__name__ = "csrf_protect"
csrf_protect.__doc__ = """
This decorator adds CSRF protection in exactly the same way as
CsrfViewMiddleware, but it can be used on a per view basis. Using both, or
using the decorator multiple times, is harmless and efficient.
"""


# 仅确保 csrf_token 可用，不拒绝请求
class _EnsureCsrfToken(CsrfViewMiddleware):
    # Behave like CsrfViewMiddleware but don't reject requests or log warnings.
    # 覆盖拒绝逻辑，始终放行
    def _reject(self, request, reason):
        return None


# 模板需要 {% csrf_token %} 但不强制 CSRF 校验
requires_csrf_token = decorator_from_middleware(_EnsureCsrfToken)
requires_csrf_token.__name__ = "requires_csrf_token"
requires_csrf_token.__doc__ = """
Use this decorator on views that need a correct csrf_token available to
RequestContext, but without the CSRF protection that csrf_protect
enforces.
"""


# 强制在响应中设置 CSRF cookie
class _EnsureCsrfCookie(CsrfViewMiddleware):
    def _reject(self, request, reason):
        return None

    # 调用 get_token 确保 process_response 发送 cookie
    def process_view(self, request, callback, callback_args, callback_kwargs):
        retval = super().process_view(request, callback, callback_args, callback_kwargs)
        # Force process_response to send the cookie
        get_token(request)
        return retval


# 无论是否使用 csrf_token 标签都设置 CSRF cookie
ensure_csrf_cookie = decorator_from_middleware(_EnsureCsrfCookie)
ensure_csrf_cookie.__name__ = "ensure_csrf_cookie"
ensure_csrf_cookie.__doc__ = """
Use this decorator to ensure that a view sets a CSRF cookie, whether or not it
uses the csrf_token template tag, or the CsrfViewMiddleware is used.
"""


# 标记视图豁免 CSRF 校验
def csrf_exempt(view_func):
    """Mark a view function as being exempt from the CSRF view protection."""

    # view_func.csrf_exempt = True would also work, but decorators are nicer
    # if they don't have side effects, so return a new function.

    if iscoroutinefunction(view_func):

        async def _view_wrapper(request, *args, **kwargs):
            return await view_func(request, *args, **kwargs)

    else:

        def _view_wrapper(request, *args, **kwargs):
            return view_func(request, *args, **kwargs)

    _view_wrapper.csrf_exempt = True

    return wraps(view_func)(_view_wrapper)
