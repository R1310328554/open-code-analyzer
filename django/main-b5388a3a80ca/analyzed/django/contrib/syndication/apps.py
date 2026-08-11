from django.apps import AppConfig
from django.utils.translation import gettext_lazy as _


# syndication 应用配置
class SyndicationConfig(AppConfig):
    name = "django.contrib.syndication"
    verbose_name = _("Syndication")
