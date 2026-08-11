"""
django.urls.exceptions — URL 解析与反向解析异常。

Resolver404 表示无匹配路由；NoReverseMatch 表示 reverse 失败。
"""

from django.http import Http404from django.http import Http404


# 路径无法匹配任何 URL 模式
class Resolver404(Http404):
    pass


# reverse() 找不到匹配的命名路由或参数不匹配
class NoReverseMatch(Exception):
    pass
