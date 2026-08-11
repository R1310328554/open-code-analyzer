"""django.contrib.admindocs.apps — admindocs 应用配置。"""
from django.apps import AppConfig
from django.utils.translation import gettext_lazy as _


# admindocs 的 AppConfig，注册应用名与 verbose_name
class AdminDocsConfig(AppConfig):
    name = "django.contrib.admindocs"
    verbose_name = _("Administrative Documentation")
