"""
django.template.loader — 跨引擎加载与渲染模板的便捷 API。

get_template/select_template/render_to_string 封装 TEMPLATES 配置。
"""

from . import enginesfrom . import engines
from .exceptions import TemplateDoesNotExist


# 按名称加载模板；遍历引擎并聚合 TemplateDoesNotExist 链
def get_template(template_name, using=None):
    """
    Load and return a template for the given name.

    Raise TemplateDoesNotExist if no such template exists.
    """
    chain = []
    engines = _engine_list(using)
    for engine in engines:
        try:
            return engine.get_template(template_name)
        except TemplateDoesNotExist as e:
            chain.append(e)

    raise TemplateDoesNotExist(template_name, chain=chain)


# 按列表顺序尝试多个模板名，返回首个命中
def select_template(template_name_list, using=None):
    """
    Load and return a template for one of the given names.

    Try names in order and return the first template found.

    Raise TemplateDoesNotExist if no such template exists.
    """
    if isinstance(template_name_list, str):
        raise TypeError(
            "select_template() takes an iterable of template names but got a "
            "string: %r. Use get_template() if you want to load a single "
            "template by name." % template_name_list
        )

    chain = []
    engines = _engine_list(using)
    for template_name in template_name_list:
        for engine in engines:
            try:
                return engine.get_template(template_name)
            except TemplateDoesNotExist as e:
                chain.append(e)

    if template_name_list:
        raise TemplateDoesNotExist(", ".join(template_name_list), chain=chain)
    else:
        raise TemplateDoesNotExist("No template names provided")


# 加载模板并用 context/request 渲染为字符串
def render_to_string(template_name, context=None, request=None, using=None):
    """
    Load a template and render it with a context. Return a string.

    template_name may be a string or a list of strings.
    """
    if isinstance(template_name, (list, tuple)):
        template = select_template(template_name, using=using)
    else:
        template = get_template(template_name, using=using)
    return template.render(context, request)


# 返回全部或指定别名的模板引擎列表
def _engine_list(using=None):
    return engines.all() if using is None else [engines[using]]
