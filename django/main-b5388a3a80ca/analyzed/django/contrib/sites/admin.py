from django.contrib import admin
from django.contrib.sites.models import Site


@admin.register(Site)
# Site 模型的后台管理：列表展示域名与显示名称
class SiteAdmin(admin.ModelAdmin):
    list_display = ("domain", "name")
    search_fields = ("domain", "name")
