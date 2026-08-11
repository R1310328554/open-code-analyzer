from django.apps import AppConfig
from django.utils.translation import gettext_lazy as _


# humanize 应用配置：注册模板过滤器（日期、数字等友好格式）
class HumanizeConfig(AppConfig):
    name = "django.contrib.humanize"
    verbose_name = _("Humanize")
