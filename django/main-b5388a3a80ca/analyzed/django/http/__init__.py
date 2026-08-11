"""
django.http — HTTP 请求/响应、Cookie 与 QueryDict 公共导出。

聚合 request、response、cookie 子模块的核心类型与异常。
"""

from django.http.cookie import SimpleCookie, parse_cookiefrom django.http.cookie import SimpleCookie, parse_cookie
from django.http.request import (
    HttpHeaders,
    HttpRequest,
    QueryDict,
    RawPostDataException,
    UnreadablePostError,
)
from django.http.response import (
    BadHeaderError,
    FileResponse,
    Http404,
    HttpResponse,
    HttpResponseBadRequest,
    HttpResponseBase,
    HttpResponseForbidden,
    HttpResponseGone,
    HttpResponseNotAllowed,
    HttpResponseNotFound,
    HttpResponseNotModified,
    HttpResponsePermanentRedirect,
    HttpResponseRedirect,
    HttpResponseServerError,
    JsonResponse,
    StreamingHttpResponse,
)

__all__ = [
    "SimpleCookie",
    "parse_cookie",
    "HttpHeaders",
    "HttpRequest",
    "QueryDict",
    "RawPostDataException",
    "UnreadablePostError",
    "HttpResponse",
    "HttpResponseBase",
    "StreamingHttpResponse",
    "HttpResponseRedirect",
    "HttpResponsePermanentRedirect",
    "HttpResponseNotModified",
    "HttpResponseBadRequest",
    "HttpResponseForbidden",
    "HttpResponseNotFound",
    "HttpResponseNotAllowed",
    "HttpResponseGone",
    "HttpResponseServerError",
    "Http404",
    "BadHeaderError",
    "JsonResponse",
    "FileResponse",
]
