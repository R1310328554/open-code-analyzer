"""
django.contrib.flatpages.middleware — 404 回退中间件。

响应为 404 时尝试按 request.path_info 渲染匹配的 FlatPage。
"""
from django.conf import settings
from django.contrib.flatpages.views import flatpage
from django.http import Http404
from django.middleware import MiddlewareMixin


# 404 回退：调用 flatpage 视图，失败则返回原响应
class FlatpageFallbackMiddleware(MiddlewareMixin):
    # 非 404 直接透传；Http404 吞掉，DEBUG 下其他异常上抛
    def process_response(self, request, response):
        if response.status_code != 404:
            return response  # No need to check for a flatpage for non-404 responses.
        try:
            return flatpage(request, request.path_info)
        # Return the original response if any errors happened. Because this
        # is a middleware, we can't assume the errors will be caught elsewhere.
        except Http404:
            return response
        except Exception:
            if settings.DEBUG:
                raise
            return response
