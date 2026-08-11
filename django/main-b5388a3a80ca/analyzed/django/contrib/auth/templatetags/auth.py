"""
django.contrib.auth.templatetags.auth — Admin 密码哈希展示标签。

在只读界面安全渲染已存储密码的算法摘要，不暴露明文。
"""
from django.contrib.auth.hashers import UNUSABLE_PASSWORD_PREFIX, identify_hasher
from django.template import Library
from django.utils.html import format_html, format_html_join
from django.utils.translation import gettext

register = Library()


# 将密码字段值格式化为 HTML：未设置、无效哈希或 hasher.safe_summary
@register.simple_tag
def render_password_as_hash(value):
    if not value or value.startswith(UNUSABLE_PASSWORD_PREFIX):
        return format_html("<p><strong>{}</strong></p>", gettext("No password set."))
    try:
        hasher = identify_hasher(value)
        hashed_summary = hasher.safe_summary(value)
    except ValueError:
        return format_html(
            "<p><strong>{}</strong></p>",
            gettext("Invalid password format or unknown hashing algorithm."),
        )
    items = [(gettext(key), val) for key, val in hashed_summary.items()]
    return format_html(
        "<p>{}</p>",
        format_html_join(" ", "<strong>{}</strong>: <bdi>{}</bdi>", items),
    )
