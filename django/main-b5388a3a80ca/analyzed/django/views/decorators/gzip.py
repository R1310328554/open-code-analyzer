"""
django.views.decorators.gzip — GZip 压缩装饰器。

客户端支持时对响应进行 gzip 压缩。
"""

from django.middleware.gzip import GZipMiddlewarefrom django.middleware.gzip import GZipMiddleware
from django.utils.decorators import decorator_from_middleware

# 对支持 gzip 的客户端压缩页面响应
gzip_page = decorator_from_middleware(GZipMiddleware)
gzip_page.__doc__ = "Decorator for views that gzips pages if the client supports it."
