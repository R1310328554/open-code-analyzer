from django.apps import apps as django_apps
from django.contrib.sitemaps import Sitemap
from django.core.exceptions import ImproperlyConfigured


# 站点地图条目提供者：导出当前站点下无需登录即可访问的 FlatPage
class FlatPageSitemap(Sitemap):
    # 校验 sites 已安装，返回 registration_required=False 的页面 queryset
    def items(self):
        if not django_apps.is_installed("django.contrib.sites"):
            raise ImproperlyConfigured(
                "FlatPageSitemap requires django.contrib.sites, which isn't installed."
            )
        Site = django_apps.get_model("sites.Site")
        current_site = Site.objects.get_current()
        return current_site.flatpage_set.filter(registration_required=False)
