"""
django.conf.urls 兼容层：导出默认 HTTP 错误处理器与 include。

handler400/403/404/500 分别绑定 django.views.defaults 中的
bad_request、permission_denied、page_not_found、server_error；
include 从 django.urls 再导出，便于旧式 from django.conf.urls import include 写法。
"""
from django.urls import include
from django.views import defaults

__all__ = ["handler400", "handler403", "handler404", "handler500", "include"]

# 400 错误默认视图
handler400 = defaults.bad_request
# 403 权限拒绝默认视图
handler403 = defaults.permission_denied
# 404 页面未找到默认视图
handler404 = defaults.page_not_found
# 500 服务器错误默认视图
handler500 = defaults.server_error
