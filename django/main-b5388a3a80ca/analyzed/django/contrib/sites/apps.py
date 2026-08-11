from django.apps import AppConfig
from django.contrib.sites.checks import check_site_id
from django.core import checks
from django.db.models.signals import post_migrate
from django.utils.translation import gettext_lazy as _

from .management import create_default_site


# sites 应用配置：注册多站点框架 contrib 应用
class SitesConfig(AppConfig):
    default_auto_field = "django.db.models.AutoField"
    name = "django.contrib.sites"
    verbose_name = _("Sites")

    # 迁移后创建默认站点，并注册 SITE_ID 系统检查
    def ready(self):
        post_migrate.connect(create_default_site, sender=self)
        checks.register(check_site_id, checks.Tags.sites)
