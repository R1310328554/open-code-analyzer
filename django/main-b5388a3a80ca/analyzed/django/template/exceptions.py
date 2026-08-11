"""
django.template.exceptions — 模板后端通用的异常类型。

DTL 内部也复用这些异常；DTL 专属异常不应添加于此。
"""

"""
This module contains generic exceptions used by template backends. Although,
due to historical reasons, the Django template language also internally uses
these exceptions, other exceptions specific to the DTL should not be added
here.
"""


# 模板不存在：可携带 backend、tried 来源链与 chain 嵌套异常
class TemplateDoesNotExist(Exception):
    """
    The exception used when a template does not exist. Optional arguments:

    backend
        The template backend class used when raising this exception.

    tried
        A list of sources that were tried when finding the template. This
        is formatted as a list of tuples containing (origin, status), where
        origin is an Origin object or duck type and status is a string with the
        reason the template wasn't found.

    chain
        A list of intermediate TemplateDoesNotExist exceptions. This is used to
        encapsulate multiple exceptions when loading templates from multiple
        engines.
    """

    def __init__(self, msg, tried=None, backend=None, chain=None):
        self.backend = backend
        if tried is None:
            tried = []
        self.tried = tried
        if chain is None:
            chain = []
        self.chain = chain
        super().__init__(msg)


# 模板解析或渲染阶段的语法错误
class TemplateSyntaxError(Exception):
    """
    The exception used for syntax errors during parsing or rendering.
    """

    pass
