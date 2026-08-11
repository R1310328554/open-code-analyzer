"""
django.template.backends.utils — 非 DTL 后端的 CSRF 辅助。

提供 lazy 包装的 csrf_input 隐藏域与 csrf_token 字符串。
"""

from django.middleware.csrf import get_tokenfrom django.middleware.csrf import get_token
from django.utils.functional import lazy
from django.utils.html import format_html
from django.utils.safestring import SafeString


# 生成 CSRF 隐藏 input 标签 HTML
def csrf_input(request):
    return format_html(
        '<input type="hidden" name="csrfmiddlewaretoken" value="{}">',
        get_token(request),
    )


# 延迟求值的 csrf_input，用于模板 context
csrf_input_lazy = lazy(csrf_input, SafeString, str)
# 延迟求值的 CSRF token 字符串
csrf_token_lazy = lazy(get_token, str)
