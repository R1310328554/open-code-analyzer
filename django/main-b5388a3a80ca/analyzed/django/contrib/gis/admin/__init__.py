# 重导出标准 Admin API，并附加 GISModelAdmin 供几何字段后台使用
from django.contrib.admin import (
    HORIZONTAL,
    VERTICAL,
    AdminSite,
    ModelAdmin,
    StackedInline,
    TabularInline,
    action,
    autodiscover,
    display,
    register,
    site,
)
from django.contrib.gis.admin.options import GISModelAdmin

__all__ = [
    "HORIZONTAL",
    "VERTICAL",
    "AdminSite",
    "ModelAdmin",
    "StackedInline",
    "TabularInline",
    "action",
    "autodiscover",
    "display",
    "register",
    "site",
    "GISModelAdmin",
]
