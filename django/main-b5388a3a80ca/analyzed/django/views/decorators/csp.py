"""
django.views.decorators.csp — Content-Security-Policy 装饰器。

为单个视图覆盖 CSP 或 CSP-Report-Only 响应头。
"""

from functools import wrapsfrom functools import wraps
from inspect import iscoroutinefunction


# CSP 覆盖装饰器工厂，将配置写入响应属性
def _make_csp_decorator(config_attr_name, config_attr_value):
    """General CSP override decorator factory."""

    if not isinstance(config_attr_value, dict):
        raise TypeError("CSP config should be a mapping.")

    def decorator(view_func):
        if iscoroutinefunction(view_func):

            @wraps(view_func)
            async def _wrapped_async_view(request, *args, **kwargs):
                response = await view_func(request, *args, **kwargs)
                setattr(response, config_attr_name, config_attr_value)
                return response

            return _wrapped_async_view

        @wraps(view_func)
        def _wrapped_sync_view(request, *args, **kwargs):
            response = view_func(request, *args, **kwargs)
            setattr(response, config_attr_name, config_attr_value)
            return response

        return _wrapped_sync_view

    return decorator


# 覆盖 Content-Security-Policy 头
def csp_override(config):
    """Override the Content-Security-Policy header for a view."""
    return _make_csp_decorator("_csp_config", config)


# 覆盖 Content-Security-Policy-Report-Only 头
def csp_report_only_override(config):
    """Override the Content-Security-Policy-Report-Only header for a view."""
    return _make_csp_decorator("_csp_ro_config", config)
