"""
django.contrib.flatpages.apps — Flat Pages 应用配置。

声明 default_auto_field 与应用 verbose_name。
"""
from django.apps import AppConfig
from django.utils.translation import gettext_lazy as _


# flatpages 内置 AppConfig
class FlatPagesConfig(AppConfig):
    default_auto_field = "django.db.models.AutoField"
    name = "django.contrib.flatpages"
    verbose_name = _("Flat Pages")
