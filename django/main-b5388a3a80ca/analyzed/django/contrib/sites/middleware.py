from django.middleware import MiddlewareMixin

from .shortcuts import get_current_site


# 中间件：在 request 上附加 site 属性（当前站点对象）
class CurrentSiteMiddleware(MiddlewareMixin):
    """
    Middleware that sets `site` attribute to request object.
    """

    # 调用 get_current_site 解析站点并赋值 request.site
    def process_request(self, request):
        request.site = get_current_site(request)
