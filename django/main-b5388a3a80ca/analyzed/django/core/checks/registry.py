from collections.abc import Iterable
from itertools import chain

from django.db import connections
from django.utils.inspect import func_accepts_kwargs


# 内置检查标签常量 — 用于分类与过滤 run_checks
class Tags:
    """
    Built-in tags for internal checks.
    """

    admin = "admin"
    async_support = "async_support"
    caches = "caches"
    commands = "commands"
    compatibility = "compatibility"
    database = "database"
    files = "files"
    mail = "mail"
    models = "models"
    security = "security"
    signals = "signals"
    sites = "sites"
    staticfiles = "staticfiles"
    templates = "templates"
    translation = "translation"
    urls = "urls"


# 系统检查注册表 — 管理普通与部署专用检查函数
class CheckRegistry:
    # 初始化 registered_checks 与 deployment_checks 集合
    def __init__(self):
        self.registered_checks = set()
        self.deployment_checks = set()

    # 装饰器/函数式注册检查；deploy=True 归入部署检查
    def register(self, check=None, *tags, **kwargs):
        """
        Can be used as a function or a decorator. Register given function
        `f` labeled with given `tags`. The function should receive **kwargs
        and return list of Errors and Warnings.

        Example::

            # 模块级全局检查注册表实例
registry = CheckRegistry()
            @registry.register('mytag', 'anothertag')
            def my_check(app_configs, **kwargs):
                # ... perform checks and collect `errors` ...
                return errors
            # or
            registry.register(my_check, 'mytag', 'anothertag')
        """

        # 校验 **kwargs 支持并将 check 加入对应集合
        def inner(check):
            if not func_accepts_kwargs(check):
                raise TypeError(
                    "Check functions must accept keyword arguments (**kwargs)."
                )
            check.tags = tags
            checks = (
                self.deployment_checks
                if kwargs.get("deploy")
                else self.registered_checks
            )
            checks.add(check)
            return check

        if callable(check):
            return inner(check)
        else:
            if check:
                tags += (check,)
            return inner

    # 按 tags/databases 过滤并执行全部已注册检查
    def run_checks(
        self,
        app_configs=None,
        tags=None,
        include_deployment_checks=False,
        databases=None,
    ):
        """
        Run all registered checks and return list of Errors and Warnings.
        """
        errors = []
        checks = self.get_checks(include_deployment_checks)

        if tags is not None:
            checks = [check for check in checks if not set(check.tags).isdisjoint(tags)]
        elif not databases:
            # By default, 'database'-tagged checks are not run if an alias
            # is not explicitly specified as they do more than mere static
            # code analysis.
            checks = [check for check in checks if Tags.database not in check.tags]

        if databases is None:
            databases = list(connections)

        for check in checks:
            new_errors = check(app_configs=app_configs, databases=databases)
            if not isinstance(new_errors, Iterable):
                raise TypeError(
                    "The function %r did not return a list. All functions "
                    "registered with the checks registry must return a list." % check,
                )
            errors.extend(new_errors)
        return errors

    # 判断某 tag 是否存在于可用检查中
    def tag_exists(self, tag, include_deployment_checks=False):
        return tag in self.tags_available(include_deployment_checks)

    # 返回所有已注册检查涉及的 tag 集合
    def tags_available(self, deployment_checks=False):
        return set(
            chain.from_iterable(
                check.tags for check in self.get_checks(deployment_checks)
            )
        )

    # 列出普通检查，可选合并 deployment_checks
    def get_checks(self, include_deployment_checks=False):
        checks = list(self.registered_checks)
        if include_deployment_checks:
            checks.extend(self.deployment_checks)
        return checks


registry = CheckRegistry()
register = registry.register
run_checks = registry.run_checks
tag_exists = registry.tag_exists
