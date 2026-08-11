"""
django.views.generic.base — CBV 基类与核心 mixin。

提供 View、ContextMixin、TemplateView、RedirectView 等基础组件。
"""

import loggingimport logging
from inspect import iscoroutinefunction, markcoroutinefunction
from urllib.parse import urlparse

from django.core.exceptions import ImproperlyConfigured
from django.http import (
    HttpResponse,
    HttpResponseGone,
    HttpResponseNotAllowed,
    HttpResponsePermanentRedirect,
    HttpResponseRedirect,
)
from django.template.response import TemplateResponse
from django.urls import reverse
from django.utils.decorators import classonlymethod
from django.utils.functional import classproperty
from django.utils.log import log_response

logger = logging.getLogger("django.request")


# 默认将 get_context_data 关键字参数作为模板上下文
class ContextMixin:
    """
    A default context mixin that passes the keyword arguments received by
    get_context_data() as the template context.
    """

    extra_context = None

    # 合并 extra_context 并注入 view 引用
    def get_context_data(self, **kwargs):
        kwargs.setdefault("view", self)
        if self.extra_context is not None:
            kwargs.update(self.extra_context)
        return kwargs


# 所有视图的极简父类，实现按 HTTP 方法分发
class View:
    """
    Intentionally simple parent class for all views. Only implements
    dispatch-by-method and simple sanity checking.
    """

    http_method_names = [
        "get",
        "post",
        "put",
        "patch",
        "delete",
        "head",
        "options",
        "trace",
    ]

    # URLconf 构造时接收额外关键字参数
    def __init__(self, **kwargs):
        """
        Constructor. Called in the URLconf; can contain helpful extra
        keyword arguments, and other things.
        """
        # Go through keyword arguments, and either save their values to our
        # instance, or raise an error.
        for key, value in kwargs.items():
            setattr(self, key, value)

    @classproperty
    # 类属性：HTTP 处理器是否全部为协程
    def view_is_async(cls):
        handlers = [
            getattr(cls, method)
            for method in cls.http_method_names
            if (method != "options" and hasattr(cls, method))
        ]
        if not handlers:
            return False
        is_async = iscoroutinefunction(handlers[0])
        if not all(iscoroutinefunction(h) == is_async for h in handlers[1:]):
            raise ImproperlyConfigured(
                f"{cls.__qualname__} HTTP handlers must either be all sync or all "
                "async."
            )
        return is_async

    @classonlymethod
    # 类方法：返回 URLconf 可用的视图可调用对象
    def as_view(cls, **initkwargs):
        """Main entry point for a request-response process."""
        for key in initkwargs:
            if key in cls.http_method_names:
                raise TypeError(
                    "The method name %s is not accepted as a keyword argument "
                    "to %s()." % (key, cls.__name__)
                )
            if not hasattr(cls, key):
                raise TypeError(
                    "%s() received an invalid keyword %r. as_view "
                    "only accepts arguments that are already "
                    "attributes of the class." % (cls.__name__, key)
                )

        def view(request, *args, **kwargs):
            self = cls(**initkwargs)
            self.setup(request, *args, **kwargs)
            if not hasattr(self, "request"):
                raise AttributeError(
                    "%s instance has no 'request' attribute. Did you override "
                    "setup() and forget to call super()?" % cls.__name__
                )
            return self.dispatch(request, *args, **kwargs)

        view.view_class = cls
        view.view_initkwargs = initkwargs

        # __name__ and __qualname__ are intentionally left unchanged as
        # view_class should be used to robustly determine the name of the view
        # instead.
        view.__doc__ = cls.__doc__
        view.__module__ = cls.__module__
        view.__annotations__ = cls.dispatch.__annotations__
        # Copy possible attributes set by decorators, e.g. @csrf_exempt, from
        # the dispatch method.
        view.__dict__.update(cls.dispatch.__dict__)

        # Mark the callback if the view class is async.
        if cls.view_is_async:
            markcoroutinefunction(view)

        return view

    # 初始化 request/args/kwargs 等共享属性
    def setup(self, request, *args, **kwargs):
        """Initialize attributes shared by all view methods."""
        if hasattr(self, "get") and not hasattr(self, "head"):
            self.head = self.get
        self.request = request
        self.args = args
        self.kwargs = kwargs

    # 按 request.method 分发到对应处理器
    def dispatch(self, request, *args, **kwargs):
        # Try to dispatch to the right method; if a method doesn't exist,
        # defer to the error handler. Also defer to the error handler if the
        # request method isn't on the approved list.
        method = request.method.lower()
        if method in self.http_method_names:
            handler = getattr(self, method, self.http_method_not_allowed)
        else:
            handler = self.http_method_not_allowed
        return handler(request, *args, **kwargs)

    # 不允许的 HTTP 方法返回 405
    def http_method_not_allowed(self, request, *args, **kwargs):
        response = HttpResponseNotAllowed(self._allowed_methods())
        log_response(
            "Method Not Allowed (%s): %s",
            request.method,
            request.path,
            response=response,
            request=request,
        )

        if self.view_is_async:

            async def func():
                return response

            return func()
        else:
            return response

    # 响应 OPTIONS 预检，返回 Allow 头
    # OPTIONS 委托给 get
    def options(self, request, *args, **kwargs):
        """Handle responding to requests for the OPTIONS HTTP verb."""
        response = HttpResponse()
        response.headers["Allow"] = ", ".join(self._allowed_methods())
        response.headers["Content-Length"] = "0"

        if self.view_is_async:

            async def func():
                return response

            return func()
        else:
            return response

    # 返回类上已实现的 HTTP 方法名列表
    def _allowed_methods(self):
        return [m.upper() for m in self.http_method_names if hasattr(self, m)]


# 使用 TemplateResponse 渲染模板的 mixin
class TemplateResponseMixin:
    """A mixin that can be used to render a template."""

    template_name = None
    template_engine = None
    response_class = TemplateResponse
    content_type = None

    # 用 response_class 渲染模板并返回响应
    def render_to_response(self, context, **response_kwargs):
        """
        Return a response, using the `response_class` for this view, with a
        template rendered with the given context.

        Pass response_kwargs to the constructor of the response class.
        """
        response_kwargs.setdefault("content_type", self.content_type)
        return self.response_class(
            request=self.request,
            template=self.get_template_names(),
            context=context,
            using=self.template_engine,
            **response_kwargs,
        )

    # 返回模板名列表（需定义 template_name 或重写此方法）
    def get_template_names(self):
        """
        Return a list of template names to be used for the request. Must return
        a list. May not be called if render_to_response() is overridden.
        """
        if self.template_name is None:
            raise ImproperlyConfigured(
                "TemplateResponseMixin requires either a definition of "
                "'template_name' or an implementation of 'get_template_names()'"
            )
        else:
            return [self.template_name]


# 渲染模板，将 URL 捕获参数传入上下文
class TemplateView(TemplateResponseMixin, ContextMixin, View):
    """
    Render a template. Pass keyword arguments from the URLconf to the context.
    """

    # GET 请求：收集上下文并渲染模板
    # 执行重定向或返回 410 Gone
    def get(self, request, *args, **kwargs):
        context = self.get_context_data(**kwargs)
        return self.render_to_response(context)


# 对 GET 等请求执行重定向
class RedirectView(View):
    """Provide a redirect on any GET request."""

    permanent = False
    url = None
    pattern_name = None
    query_string = False
    preserve_request = False

    # 根据 url/pattern_name 计算重定向目标 URL
    def get_redirect_url(self, *args, **kwargs):
        """
        Return the URL redirect to. Keyword arguments from the URL pattern
        match generating the redirect request are provided as kwargs to this
        method.
        """
        if self.url:
            url = self.url % kwargs
        elif self.pattern_name:
            url = reverse(self.pattern_name, args=args, kwargs=kwargs)
        else:
            return None

        args = self.request.META.get("QUERY_STRING", "")
        if args and self.query_string:
            if urlparse(url).query:
                url = f"{url}&{args}"
            else:
                url = f"{url}?{args}"
        return url

    def get(self, request, *args, **kwargs):
        url = self.get_redirect_url(*args, **kwargs)
        if url:
            if self.permanent:
                return HttpResponsePermanentRedirect(
                    url, preserve_request=self.preserve_request
                )
            else:
                return HttpResponseRedirect(url, preserve_request=self.preserve_request)
        else:
            response = HttpResponseGone()
            log_response("Gone: %s", request.path, response=response, request=request)
            return response

    # HEAD 委托给 get
    def head(self, request, *args, **kwargs):
        return self.get(request, *args, **kwargs)

    # POST 委托给 get（重定向视图常见模式）
    def post(self, request, *args, **kwargs):
        return self.get(request, *args, **kwargs)

    def options(self, request, *args, **kwargs):
        return self.get(request, *args, **kwargs)

    # DELETE 委托给 get
    def delete(self, request, *args, **kwargs):
        return self.get(request, *args, **kwargs)

    # PUT 委托给 get
    def put(self, request, *args, **kwargs):
        return self.get(request, *args, **kwargs)

    # PATCH 委托给 get
    def patch(self, request, *args, **kwargs):
        return self.get(request, *args, **kwargs)
