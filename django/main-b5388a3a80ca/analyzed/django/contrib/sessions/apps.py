from django.apps import AppConfig
from django.utils.translation import gettext_lazy as _


# sessions 应用配置：注册 Django 会话存储 contrib 应用
class SessionsConfig(AppConfig):
    name = "django.contrib.sessions"
    verbose_name = _("Sessions")
