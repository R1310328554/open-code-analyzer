"""
django.templatetags.static — 静态与媒体 URL 模板标签。

{% static %} 拼接 STATIC_URL；{% get_static_prefix %} 等读取设置前缀。
"""

from urllib.parse import quote, urljoinfrom urllib.parse import quote, urljoin

from django import template
from django.apps import apps
from django.utils.encoding import iri_to_uri
from django.utils.html import conditional_escape

# 注册 static 模板标签库
register = template.Library()


# 读取 settings 中 STATIC_URL/MEDIA_URL 等前缀
class PrefixNode(template.Node):
    def __repr__(self):
        return "<PrefixNode for %r>" % self.name

    def __init__(self, varname=None, name=None):
        if name is None:
            raise template.TemplateSyntaxError(
                "Prefix nodes must be given a name to return."
            )
        self.varname = varname
        self.name = name

    @classmethod
    def handle_token(cls, parser, token, name):
        """
        Class method to parse prefix node and return a Node.
        """
        # token.split_contents() isn't useful here because tags using this
        # method don't accept variable as arguments.
        tokens = token.contents.split()
        if len(tokens) > 1 and tokens[1] != "as":
            raise template.TemplateSyntaxError(
                "First argument in '%s' must be 'as'" % tokens[0]
            )
        if len(tokens) > 1:
            varname = tokens[2]
        else:
            varname = None
        return cls(varname, name)

    @classmethod
    def handle_simple(cls, name):
        try:
            from django.conf import settings
        except ImportError:
            prefix = ""
        else:
            prefix = iri_to_uri(getattr(settings, name, ""))
        return prefix

    def render(self, context):
        prefix = self.handle_simple(self.name)
        if self.varname is None:
            return prefix
        context[self.varname] = prefix
        return ""


# 标签：输出或绑定 STATIC_URL
@register.tag
def get_static_prefix(parser, token):
    """
    Populate a template variable with the static prefix,
    ``settings.STATIC_URL``.

    Usage::

        {% get_static_prefix [as varname] %}

    Examples::

        {% get_static_prefix %}
        {% get_static_prefix as static_prefix %}
    """
    return PrefixNode.handle_token(parser, token, "STATIC_URL")


# 标签：输出或绑定 MEDIA_URL
@register.tag
def get_media_prefix(parser, token):
    """
    Populate a template variable with the media prefix,
    ``settings.MEDIA_URL``.

    Usage::

        {% get_media_prefix [as varname] %}

    Examples::

        {% get_media_prefix %}
        {% get_media_prefix as media_prefix %}
    """
    return PrefixNode.handle_token(parser, token, "MEDIA_URL")


# {% static path %}：拼接静态资源绝对 URL
class StaticNode(template.Node):
    child_nodelists = ()

    def __init__(self, varname=None, path=None):
        if path is None:
            raise template.TemplateSyntaxError(
                "Static template nodes must be given a path to return."
            )
        self.path = path
        self.varname = varname

    def __repr__(self):
        return (
            f"{self.__class__.__name__}(varname={self.varname!r}, path={self.path!r})"
        )

    def url(self, context):
        path = self.path.resolve(context)
        return self.handle_simple(path)

    def render(self, context):
        url = self.url(context)
        if context.autoescape:
            url = conditional_escape(url)
        if self.varname is None:
            return url
        context[self.varname] = url
        return ""

    @classmethod
    def handle_simple(cls, path):
        if apps.is_installed("django.contrib.staticfiles"):
            from django.contrib.staticfiles.storage import staticfiles_storage

            return staticfiles_storage.url(path)
        else:
            return urljoin(PrefixNode.handle_simple("STATIC_URL"), quote(path))

    @classmethod
    def handle_token(cls, parser, token):
        """
        Class method to parse prefix node and return a Node.
        """
        bits = token.split_contents()

        if len(bits) < 2:
            raise template.TemplateSyntaxError(
                "'%s' takes at least one argument (path to file)" % bits[0]
            )

        path = parser.compile_filter(bits[1])

        if len(bits) >= 2 and bits[-2] == "as":
            varname = bits[3]
        else:
            varname = None

        return cls(varname, path)


# 标签：static 入口
@register.tag("static")
def do_static(parser, token):
    """
    Join the given path with the STATIC_URL setting.

    Usage::

        {% static path [as varname] %}

    Examples::

        {% static "myapp/css/base.css" %}
        {% static variable_with_path %}
        {% static "myapp/css/base.css" as admin_base_css %}
        {% static variable_with_path as varname %}
    """
    return StaticNode.handle_token(parser, token)


# Python API：给定相对路径返回静态 URL
def static(path):
    """
    Given a relative path to a static asset, return the absolute path to the
    asset.
    """
    return StaticNode.handle_simple(path)
