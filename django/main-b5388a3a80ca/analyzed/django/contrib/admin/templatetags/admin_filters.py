"""
django.contrib.admin.templatetags.admin_filters — changelist 与删除确认页展示过滤器。

将对象值格式化为 admin 可读文本，并截断渲染关联对象的无序列表。
"""
from django import template
from django.contrib.admin.options import EMPTY_VALUE_STRING
from django.contrib.admin.utils import display_for_value
from django.template.defaultfilters import _walk_items, stringfilter
from django.utils.html import conditional_escape
from django.utils.safestring import mark_safe
from django.utils.translation import ngettext

register = template.Library()


# 将字符串值格式化为 admin 列表/详情中的显示文本
@register.filter
@stringfilter
def to_object_display_value(value):
    return display_for_value(str(value), EMPTY_VALUE_STRING)


# 渲染无序列表，超出 max_items 时追加「还有 N 个对象」提示
@register.filter(is_safe=True, needs_autoescape=True)
def truncated_unordered_list(value, max_items, autoescape=True):
    """
    Render an unordered list, showing at most ``max_items`` items and a
    "...and N more objects." item at the end.

    Usage::

        {{ deleted_objects|truncated_unordered_list:100 }}
    """

    has_unlimited_items = max_items is None
    if not has_unlimited_items:
        max_items = int(max_items)
        if max_items <= 0:
            return mark_safe("")

    if autoescape:
        escaper = conditional_escape
    else:

        def escaper(x):
            return x

    item_count = 0

    def list_formatter(item_list, tabs=1):
        nonlocal item_count
        indent = "\t" * tabs
        output = []
        for item, children in _walk_items(item_list):
            sublist = ""
            item_count += 1
            should_display_item = has_unlimited_items or 0 < item_count <= max_items
            if children:
                sublist = "\n%s<ul>\n%s\n%s</ul>\n%s" % (
                    indent,
                    list_formatter(children, tabs + 1),
                    indent,
                    indent,
                )

            if should_display_item:
                output.append("%s<li>%s%s</li>" % (indent, escaper(item), sublist))

        return "\n".join(output)

    rendered_object_list = list_formatter(value)
    remaining_objects_message = ""

    if not has_unlimited_items and item_count > max_items:
        remaining_object_count = item_count - max_items
        remaining_objects_message = "\n\t<li>%s</li>" % (
            ngettext(
                "…and %(count)d more object.",
                "…and %(count)d more objects.",
                remaining_object_count,
            )
            % {"count": remaining_object_count}
        )

    return mark_safe(rendered_object_list + remaining_objects_message)
