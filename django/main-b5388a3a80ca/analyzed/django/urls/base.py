"""
django.urls.base — URL 解析、反向解析与线程局部 URLconf。

resolve/reverse 核心实现；set_urlconf 支持 per-request 路由表切换。
"""

from urllib.parse import unquote, urlencode, urlsplit, urlunsplitfrom urllib.parse import unquote, urlencode, urlsplit, urlunsplit

from asgiref.local import Local

from django.http import QueryDict
from django.utils.functional import lazy
from django.utils.translation import override

from .exceptions import NoReverseMatch, Resolver404
from .resolvers import _get_cached_resolver, get_ns_resolver, get_resolver
from .utils import get_callable

# SCRIPT_NAME prefixes for each thread are stored here. If there's no entry for
# the current thread (which is the only one we ever access), it is assumed to
# be empty.
# 线程局部 SCRIPT_NAME 前缀
_prefixes = Local()

# Overridden URLconfs for each thread are stored here.
# 线程/async 任务局部 URLconf 覆盖
_urlconfs = Local()


# 解析路径为 ResolverMatch
def resolve(path, urlconf=None):
    if urlconf is None:
        urlconf = get_urlconf()
    return get_resolver(urlconf).resolve(path)


# 按 viewname/namespace 反向生成 URL，支持 query/fragment
def reverse(
    viewname,
    urlconf=None,
    args=None,
    kwargs=None,
    current_app=None,
    *,
    query=None,
    fragment=None,
):
    if urlconf is None:
        urlconf = get_urlconf()
    resolver = get_resolver(urlconf)
    args = args or []
    kwargs = kwargs or {}

    prefix = get_script_prefix()

    if not isinstance(viewname, str):
        view = viewname
    else:
        *path, view = viewname.split(":")

        if current_app:
            current_path = current_app.split(":")
            current_path.reverse()
        else:
            current_path = None

        resolved_path = []
        ns_pattern = ""
        ns_converters = {}
        for ns in path:
            current_ns = current_path.pop() if current_path else None
            # Lookup the name to see if it could be an app identifier.
            try:
                app_list = resolver.app_dict[ns]
                # Yes! Path part matches an app in the current Resolver.
                if current_ns and current_ns in app_list:
                    # If we are reversing for a particular app, use that
                    # namespace.
                    ns = current_ns
                elif ns not in app_list:
                    # The name isn't shared by one of the instances (i.e.,
                    # the default) so pick the first instance as the default.
                    ns = app_list[0]
            except KeyError:
                pass

            if ns != current_ns:
                current_path = None

            try:
                extra, resolver = resolver.namespace_dict[ns]
                resolved_path.append(ns)
                ns_pattern += extra
                ns_converters.update(resolver.pattern.converters)
            except KeyError as key:
                if resolved_path:
                    raise NoReverseMatch(
                        "%s is not a registered namespace inside '%s'"
                        % (key, ":".join(resolved_path))
                    )
                else:
                    raise NoReverseMatch("%s is not a registered namespace" % key)
        if ns_pattern:
            resolver = get_ns_resolver(
                ns_pattern, resolver, tuple(ns_converters.items())
            )

    resolved_url = resolver._reverse_with_prefix(view, prefix, *args, **kwargs)
    if query is not None:
        if isinstance(query, QueryDict):
            query_string = query.urlencode()
        else:
            query_string = urlencode(query, doseq=True)
        if query_string:
            resolved_url += "?" + query_string
    if fragment is not None:
        resolved_url += "#" + fragment
    return resolved_url


# 延迟求值的 reverse
reverse_lazy = lazy(reverse, str)


# 清空 resolver 与 get_callable 缓存
def clear_url_caches():
    get_callable.cache_clear()
    _get_cached_resolver.cache_clear()
    get_ns_resolver.cache_clear()


# 设置当前线程 SCRIPT_NAME 前缀
def set_script_prefix(prefix):
    """
    Set the script prefix for the current thread.
    """
    if not prefix.endswith("/"):
        prefix += "/"
    _prefixes.value = prefix


# 读取当前 SCRIPT_NAME，默认 '/'
def get_script_prefix():
    """
    Return the currently active script prefix. Useful for client code that
    wishes to construct their own URLs manually (although accessing the request
    instance is normally going to be a lot cleaner).
    """
    return getattr(_prefixes, "value", "/")


# 清除线程局部 SCRIPT 前缀
def clear_script_prefix():
    """
    Unset the script prefix for the current thread.
    """
    try:
        del _prefixes.value
    except AttributeError:
        pass


# 覆盖或恢复当前 URLconf 模块
def set_urlconf(urlconf_name):
    """
    Set the URLconf for the current thread or asyncio task (overriding the
    default one in settings). If urlconf_name is None, revert back to the
    default.
    """
    if urlconf_name:
        _urlconfs.value = urlconf_name
    else:
        if hasattr(_urlconfs, "value"):
            del _urlconfs.value


# 读取被覆盖的 URLconf 名称
def get_urlconf(default=None):
    """
    Return the root URLconf to use for the current thread or asyncio task if it
    has been changed from the default one.
    """
    return getattr(_urlconfs, "value", default)


# 路径可解析则返回 ResolverMatch，否则 False
def is_valid_path(path, urlconf=None):
    """
    Return the ResolverMatch if the given path resolves against the default URL
    resolver, False otherwise. This is a convenience method to make working
    with "is this a match?" cases easier, avoiding try...except blocks.
    """
    try:
        return resolve(path, urlconf)
    except Resolver404:
        return False


# 在指定语言下尝试反向翻译 URL
def translate_url(url, lang_code):
    """
    Given a URL (absolute or relative), try to get its translated version in
    the `lang_code` language (either by i18n_patterns or by translated regex).
    Return the original URL if no translated version is found.
    """
    parsed = urlsplit(url)
    try:
        # URL may be encoded.
        match = resolve(unquote(parsed.path))
    except Resolver404:
        pass
    else:
        to_be_reversed = (
            "%s:%s" % (match.namespace, match.url_name)
            if match.namespace
            else match.url_name
        )
        with override(lang_code):
            try:
                url = reverse(to_be_reversed, args=match.args, kwargs=match.kwargs)
            except NoReverseMatch:
                pass
            else:
                url = urlunsplit(
                    (parsed.scheme, parsed.netloc, url, parsed.query, parsed.fragment)
                )
    return url
