"""
django.conf.urls.i18n — 国际化 URL 前缀与语言切换路由。

i18n_patterns() 为 URL 模式添加语言代码前缀；urlpatterns 提供 setlang 视图。
"""
import functools

from django.conf import settings
from django.urls import LocalePrefixPattern, URLResolver, get_resolver, path
from django.views.i18n import set_language


# 为内部 URL 列表包裹 LocalePrefixPattern，仅在根 URLconf 中使用
def i18n_patterns(*urls, prefix_default_language=True):
    """
    Add the language code prefix to every URL pattern within this function.
    This may only be used in the root URLconf, not in an included URLconf.
    """
    if not settings.USE_I18N:
        return list(urls)
    return [
        URLResolver(
            LocalePrefixPattern(prefix_default_language=prefix_default_language),
            list(urls),
        )
    ]


@functools.cache
# 检测 URLconf 是否使用 i18n_patterns 及默认语言是否带前缀
def is_language_prefix_patterns_used(urlconf):
    """
    Return a tuple of two booleans: (
        `True` if i18n_patterns() (LocalePrefixPattern) is used in the URLconf,
        `True` if the default language should be prefixed
    )
    """
    for url_pattern in get_resolver(urlconf).url_patterns:
        if isinstance(url_pattern.pattern, LocalePrefixPattern):
            return True, url_pattern.pattern.prefix_default_language
    return False, False


# 内置语言切换端点（POST 至 set_language 视图）
urlpatterns = [
    path("setlang/", set_language, name="set_language"),
]
