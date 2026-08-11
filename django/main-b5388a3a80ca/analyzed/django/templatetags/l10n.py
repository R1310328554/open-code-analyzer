"""
django.templatetags.l10n — 模板本地化标签与过滤器。

控制 context.use_l10n，强制数值/日期按 locale 或原始格式渲染。
"""

from django.template import Library, Node, TemplateSyntaxErrorfrom django.template import Library, Node, TemplateSyntaxError
from django.utils import formats

# 注册 l10n 模板标签库
register = Library()


# 过滤器：强制本地化渲染
@register.filter(is_safe=False)
def localize(value):
    """
    Force a value to be rendered as a localized value.
    """
    return str(formats.localize(value, use_l10n=True))


# 过滤器：强制非本地化（原始）渲染
@register.filter(is_safe=False)
def unlocalize(value):
    """
    Force a value to be rendered as a non-localized value.
    """
    return str(formats.localize(value, use_l10n=False))


# {% localize on/off %}：块内切换 use_l10n
class LocalizeNode(Node):
    def __init__(self, nodelist, use_l10n):
        self.nodelist = nodelist
        self.use_l10n = use_l10n

    def __repr__(self):
        return "<%s>" % self.__class__.__name__

    def render(self, context):
        old_setting = context.use_l10n
        context.use_l10n = self.use_l10n
        output = self.nodelist.render(context)
        context.use_l10n = old_setting
        return output


# 标签：localize/endlocalize 块
@register.tag("localize")
def localize_tag(parser, token):
    """
    Force or prevents localization of values.

    Sample usage::

        {% localize off %}
            var pi = {{ 3.1415 }};
        {% endlocalize %}
    """
    use_l10n = None
    bits = list(token.split_contents())
    if len(bits) == 1:
        use_l10n = True
    elif len(bits) > 2 or bits[1] not in ("on", "off"):
        raise TemplateSyntaxError("%r argument should be 'on' or 'off'" % bits[0])
    else:
        use_l10n = bits[1] == "on"
    nodelist = parser.parse(("endlocalize",))
    parser.delete_first_token()
    return LocalizeNode(nodelist, use_l10n)
