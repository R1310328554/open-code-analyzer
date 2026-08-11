from urllib.parse import urlparse
from urllib.request import url2pathname

from asgiref.sync import sync_to_async

from django.conf import settings
from django.contrib.staticfiles import utils
from django.contrib.staticfiles.views import serve
from django.core.handlers.asgi import ASGIHandler
from django.core.handlers.exception import response_for_exception
from django.core.handlers.wsgi import WSGIHandler, get_path_info
from django.http import Http404


# WSGI/ASGI 静态文件处理器共用方法
class StaticFilesHandlerMixin:
    """
    Common methods used by WSGI and ASGI handlers.
    """

    # May be used to differentiate between handler types (e.g. in a
    # request_finished signal)
    handles_files = True

    # 中间件已由 application 加载，此处无需重复
    def load_middleware(self):
        # Middleware are already loaded for self.application; no need to reload
        # them for self.
        pass

    # 校验设置并返回 STATIC_URL
    def get_base_url(self):
        utils.check_settings()
        return settings.STATIC_URL

    # 判断请求路径是否应由静态文件处理器处理
    def _should_handle(self, path):
        """
        Check if the path should be handled. Ignore the path if:
        * the host is provided as part of the base_url
        * the request's path isn't under the media path (or equal)
        """
        return path.startswith(self.base_url.path) and not self.base_url.netloc

    # 将 URL 转为磁盘上的相对文件路径
    def file_path(self, url):
        """
        Return the relative path to the media file on disk for the given URL.
        """
        relative_url = url.removeprefix(self.base_url.path)
        return url2pathname(relative_url)

    # 以 insecure 模式调用 views.serve 提供文件
    def serve(self, request):
        """Serve the request path."""
        return serve(request, self.file_path(request.path), insecure=True)

    # 同步提供静态文件，404 时转为异常响应
    def get_response(self, request):
        try:
            return self.serve(request)
        except Http404 as e:
            return response_for_exception(request, e)

    # 异步包装 serve 与异常处理
    # 异步响应并修复非 async 的 FileResponse 流
    async def get_response_async(self, request):
        try:
            return await sync_to_async(self.serve, thread_sensitive=False)(request)
        except Http404 as e:
            return await sync_to_async(response_for_exception, thread_sensitive=True)(
                request, e
            )


# WSGI 中间件 — 拦截 STATIC_URL 下的请求并服务静态文件
class StaticFilesHandler(StaticFilesHandlerMixin, WSGIHandler):
    """
    WSGI middleware that intercepts calls to the static files directory, as
    defined by the STATIC_URL setting, and serves those files.
    """

    # 包装内层 application 并解析 base_url
    # 保存内层 ASGI 应用并解析 STATIC_URL
    def __init__(self, application):
        self.application = application
        self.base_url = urlparse(self.get_base_url())
        super().__init__()

    # 非静态路径交给内层 app，否则由 WSGIHandler 处理
    def __call__(self, environ, start_response):
        if not self._should_handle(get_path_info(environ)):
            return self.application(environ, start_response)
        return super().__call__(environ, start_response)


# ASGI 包装器 — 拦截 HTTP 静态请求并交给 Django 静态视图
class ASGIStaticFilesHandler(StaticFilesHandlerMixin, ASGIHandler):
    """
    ASGI application which wraps another and intercepts requests for static
    files, passing them off to Django's static file serving.
    """

    def __init__(self, application):
        self.application = application
        self.base_url = urlparse(self.get_base_url())

    # HTTP 且路径匹配时由 super 服务，否则转发内层 app
    async def __call__(self, scope, receive, send):
        # Only even look at HTTP requests
        if scope["type"] == "http" and self._should_handle(scope["path"]):
            # Serve static content
            # (the one thing super() doesn't do is __call__, apparently)
            return await super().__call__(scope, receive, send)
        # Hand off to the main app
        return await self.application(scope, receive, send)

    async def get_response_async(self, request):
        response = await super().get_response_async(request)
        response._resource_closers.append(request.close)
        # FileResponse is not async compatible.
        if response.streaming and not response.is_async:
            _iterator = response.streaming_content

            async def awrapper():
                for part in await sync_to_async(list)(_iterator):
                    yield part

            response.streaming_content = awrapper()
        return response
