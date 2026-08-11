"""
django.contrib.flatpages.admin — FlatPage 后台管理。

注册 FlatPage 模型，配置字段分组、列表与搜索。
"""
from django.contrib import admin
from django.contrib.flatpages.forms import FlatpageForm
from django.contrib.flatpages.models import FlatPage
from django.utils.translation import gettext_lazy as _


# 注册 FlatPageAdmin
@admin.register(FlatPage)
# 管理界面：url/title/content/sites 及高级选项折叠区
class FlatPageAdmin(admin.ModelAdmin):
    form = FlatpageForm
    fieldsets = (
        (None, {"fields": ("url", "title", "content", "sites")}),
        (
            _("Advanced options"),
            {
                "classes": ("collapse",),
                "fields": ("registration_required", "template_name"),
            },
        ),
    )
    list_display = ("url", "title")
    list_filter = ("sites", "registration_required")
    search_fields = ("url", "title")
