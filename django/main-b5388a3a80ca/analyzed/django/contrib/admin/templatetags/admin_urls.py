"""
django.contrib.admin.templatetags.admin_urls — 管理后台 URL 相关模板过滤器与标签。

提供 admin URL 命名、对象引用编码，以及在跳转时保留 changelist 筛选参数。
"""
from urllib.parse import parse_qsl, unquote, urlsplit, urlunsplit

from django import template
from django.contrib.admin.utils import quote
from django.urls import Resolver404, get_script_prefix, resolve
from django.utils.http import urlencode

register = template.Library()


# 生成 admin 命名空间下的 URL 名称（admin:app_model_action）
@register.filter
def admin_urlname(value, arg):
    return "admin:%s_%s_%s" % (value.app_label, value.model_name, arg)


# 对主键等对象标识符做 admin 安全的 URL 引用
@register.filter
def admin_urlquote(value):
    return quote(value)


# 合并 changelist 保留筛选、弹窗与 to_field 等查询参数到目标 URL
@register.simple_tag(takes_context=True)
def add_preserved_filters(context, url, popup=False, to_field=None):
    opts = context.get("opts")
    preserved_filters = context.get("preserved_filters")
    preserved_qsl = context.get("preserved_qsl")

    parsed_url = list(urlsplit(url))
    parsed_qs = dict(parse_qsl(parsed_url[3]))
    merged_qs = {}

    if preserved_qsl:
        merged_qs.update(preserved_qsl)

    if opts and preserved_filters:
        preserved_filters = dict(parse_qsl(preserved_filters))

        match_url = "/%s" % unquote(url).partition(get_script_prefix())[2]
        try:
            match = resolve(match_url)
        except Resolver404:
            pass
        else:
            current_url = "%s:%s" % (match.app_name, match.url_name)
            changelist_url = "admin:%s_%s_changelist" % (
                opts.app_label,
                opts.model_name,
            )
            if (
                changelist_url == current_url
                and "_changelist_filters" in preserved_filters
            ):
                preserved_filters = dict(
                    parse_qsl(preserved_filters["_changelist_filters"])
                )

        merged_qs.update(preserved_filters)

    if popup:
        from django.contrib.admin.options import IS_POPUP_VAR

        merged_qs[IS_POPUP_VAR] = 1
    if to_field:
        from django.contrib.admin.options import TO_FIELD_VAR

        merged_qs[TO_FIELD_VAR] = to_field

    merged_qs.update(parsed_qs)

    parsed_url[3] = urlencode(merged_qs)
    return urlunsplit(parsed_url)
