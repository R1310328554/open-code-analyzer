"""
django.template.loaders.locmem — 从内存字典加载模板（测试用）。

templates_dict 键为模板名，值为源码字符串。
"""

"""
Wrapper for loading templates from a plain Python dict.
"""

from django.template import Origin, TemplateDoesNotExist

from .base import Loader as BaseLoader


# 内存字典加载器：get_contents 从 templates_dict 取值
class Loader(BaseLoader):
    def __init__(self, engine, templates_dict):
        self.templates_dict = templates_dict
        super().__init__(engine)

    def get_contents(self, origin):
        try:
            return self.templates_dict[origin.name]
        except KeyError:
            raise TemplateDoesNotExist(origin)

    def get_template_sources(self, template_name):
        yield Origin(
            name=template_name,
            template_name=template_name,
            loader=self,
        )
