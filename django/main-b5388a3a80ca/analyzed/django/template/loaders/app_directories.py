"""
django.template.loaders.app_directories — 从各 INSTALLED_APPS 的 templates/ 加载。

复用 FilesystemLoader，目录由 get_app_template_dirs 提供。
"""

"""
Wrapper for loading templates from "templates" directories in INSTALLED_APPS
packages.
"""

from django.template.utils import get_app_template_dirs

from .filesystem import Loader as FilesystemLoader


# 应用目录加载器：get_dirs 返回各 app 下的 templates 路径
class Loader(FilesystemLoader):
    def get_dirs(self):
        return get_app_template_dirs("templates")
