from django.apps import AppConfig
from django.utils.translation import gettext_lazy as _


# redirects 应用配置：注册 URL 重定向 contrib 应用
class RedirectsConfig(AppConfig):
    default_auto_field = "django.db.models.AutoField"
    name = "django.contrib.redirects"
    verbose_name = _("Redirects")
