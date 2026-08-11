"""
django.template.loaders.filesystem — 从文件系统目录加载模板。

使用 safe_join 防止路径穿越，按 engine.dirs 或构造参数搜索。
"""

"""
Wrapper for loading templates from the filesystem.
"""

from django.core.exceptions import SuspiciousFileOperation
from django.template import Origin, TemplateDoesNotExist
from django.utils._os import safe_join

from .base import Loader as BaseLoader


# 文件系统加载器：open 读取 Origin.name 对应文件
class Loader(BaseLoader):
    def __init__(self, engine, dirs=None):
        super().__init__(engine)
        self.dirs = dirs

    def get_dirs(self):
        return self.dirs if self.dirs is not None else self.engine.dirs

    def get_contents(self, origin):
        try:
            with open(origin.name, encoding=self.engine.file_charset) as fp:
                return fp.read()
        except FileNotFoundError:
            raise TemplateDoesNotExist(origin)

    def get_template_sources(self, template_name):
        """
        Return an Origin object pointing to an absolute path in each directory
        in template_dirs. For security reasons, if a path doesn't lie inside
        one of the template_dirs it is excluded from the result set.
        """
        for template_dir in self.get_dirs():
            try:
                name = safe_join(template_dir, template_name)
            except SuspiciousFileOperation:
                # The joined path was located outside of this template_dir
                # (it might be inside another one, so this isn't fatal).
                continue

            yield Origin(
                name=name,
                template_name=template_name,
                loader=self,
            )
