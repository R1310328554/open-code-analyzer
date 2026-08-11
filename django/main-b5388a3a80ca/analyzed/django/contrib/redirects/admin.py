from django.contrib import admin
from django.contrib.redirects.models import Redirect


@admin.register(Redirect)
# Redirect 模型的后台管理：列表展示旧路径与新路径
class RedirectAdmin(admin.ModelAdmin):
    list_display = ("old_path", "new_path")
    list_filter = ("site",)
    search_fields = ("old_path", "new_path")
