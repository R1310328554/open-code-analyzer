"""
django.contrib.admindocs.middleware — 为 admindocs 书签let 提供 X-View 响应头。
"""
from django.conf import settings
from django.core.exceptions import ImproperlyConfigured
from django.http import HttpResponse
from django.middleware import MiddlewareMixin

from .utils import get_view_name


# 对内网或 staff 的 HEAD 请求附加 X-View，标识处理该页的视图函数
class XViewMiddleware(MiddlewareMixin):
    """
    Add an X-View header to internal HEAD requests.
    """

    # 满足条件时短路返回带 X-View 头的空 HttpResponse
    def process_view(self, request, view_func, view_args, view_kwargs):
        """
        If the request method is HEAD and either the IP is internal or the
        user is a logged-in staff member, return a response with an x-view
        header indicating the view function. This is used to lookup the view
        function for an arbitrary page.
        """
        if not hasattr(request, "user"):
            raise ImproperlyConfigured(
                "The XView middleware requires authentication middleware to "
                "be installed. Edit your MIDDLEWARE setting to insert "
                "'django.contrib.auth.middleware.AuthenticationMiddleware'."
            )
        if request.method == "HEAD" and (
            request.META.get("REMOTE_ADDR") in settings.INTERNAL_IPS
            or (request.user.is_active and request.user.is_staff)
        ):
            response = HttpResponse()
            response.headers["X-View"] = get_view_name(view_func)
            return response
