"""
django.template.backends.base — 模板引擎后端抽象基类。

定义 get_template/from_string 及安全的 template_dirs 迭代。
"""

from django.core.exceptions import ImproperlyConfiguredfrom django.core.exceptions import ImproperlyConfigured, SuspiciousFileOperation
from django.template.utils import get_app_template_dirs
from django.utils._os import safe_join
from django.utils.functional import cached_property


# 模板引擎基类：NAME/DIRS/APP_DIRS 配置与模板加载 API
class BaseEngine:
    # Core methods: engines have to provide their own implementation
    #               (except for from_string which is optional).

    def __init__(self, params):
        """
        Initialize the template engine.

        `params` is a dict of configuration settings.
        """
        params = params.copy()
        self.name = params.pop("NAME")
        self.dirs = list(params.pop("DIRS"))
        self.app_dirs = params.pop("APP_DIRS")
        if params:
            raise ImproperlyConfigured(
                "Unknown parameters: {}".format(", ".join(params))
            )

    # 系统检查钩子，子类可返回 Error/Warning 列表
    def check(self, **kwargs):
        return []

    @property
    def app_dirname(self):
        raise ImproperlyConfigured(
            "{} doesn't support loading templates from installed "
            "applications.".format(self.__class__.__name__)
        )

    # 可选：由源码字符串编译模板
    def from_string(self, template_code):
        """
        Create and return a template for the given source code.

        This method is optional.
        """
        raise NotImplementedError(
            "subclasses of BaseEngine should provide a from_string() method"
        )

    # 按名称加载模板，不存在时抛出 TemplateDoesNotExist
    def get_template(self, template_name):
        """
        Load and return a template for the given name.

        Raise TemplateDoesNotExist if no such template exists.
        """
        raise NotImplementedError(
            "subclasses of BaseEngine must provide a get_template() method"
        )

    # Utility methods: they are provided to minimize code duplication and
    #                  security issues in third-party backends.

    @cached_property
    # 合并 DIRS 与应用 templates 目录的只读元组
    def template_dirs(self):
        """
        Return a list of directories to search for templates.
        """
        # Immutable return value because it's cached and shared by callers.
        template_dirs = tuple(self.dirs)
        if self.app_dirs:
            template_dirs += get_app_template_dirs(self.app_dirname)
        return template_dirs

    # 安全拼接候选路径，跳过目录遍历攻击
    def iter_template_filenames(self, template_name):
        """
        Iterate over candidate files for template_name.

        Ignore files that don't lie inside configured template dirs to avoid
        directory traversal attacks.
        """
        for template_dir in self.template_dirs:
            try:
                yield safe_join(template_dir, template_name)
            except SuspiciousFileOperation:
                # The joined path was located outside of this template_dir
                # (it might be inside another one, so this isn't fatal).
                pass
