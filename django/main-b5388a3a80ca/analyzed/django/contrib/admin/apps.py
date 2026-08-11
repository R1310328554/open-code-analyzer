"""
django.contrib.admin.apps — Admin 应用的 AppConfig 定义。

SimpleAdminConfig 仅注册系统检查；AdminConfig 额外执行 autodiscover。
"""
from django.apps import AppConfig
from django.contrib.admin.checks import check_admin_app, check_dependencies
from django.core import checks
from django.utils.translation import gettext_lazy as _


# 不自动发现 admin 模块的精简配置
class SimpleAdminConfig(AppConfig):
    """Simple AppConfig which does not do automatic discovery."""

    default_auto_field = "django.db.models.AutoField"
    default_site = "django.contrib.admin.sites.AdminSite"
    name = "django.contrib.admin"
    verbose_name = _("Administration")

    def ready(self):
        checks.register(check_dependencies, checks.Tags.admin)
        checks.register(check_admin_app, checks.Tags.admin)


# 默认 Admin 应用配置：ready() 中调用 autodiscover()
class AdminConfig(SimpleAdminConfig):
    """The default AppConfig for admin which does autodiscovery."""

    default = True

    def ready(self):
        super().ready()
        self.module.autodiscover()
