"""
django.contrib.admin — 管理后台公共 API 导出与 autodiscover。

对外暴露 ModelAdmin、过滤器、装饰器与默认 site；启动时扫描各 app 的 admin 模块。
"""
from django.contrib.admin.decorators import action, display, register
from django.contrib.admin.filters import (
    AllValuesFieldListFilter,
    BooleanFieldListFilter,
    ChoicesFieldListFilter,
    DateFieldListFilter,
    EmptyFieldListFilter,
    FieldListFilter,
    ListFilter,
    RelatedFieldListFilter,
    RelatedOnlyFieldListFilter,
    SimpleListFilter,
)
from django.contrib.admin.options import (
    HORIZONTAL,
    VERTICAL,
    Action,
    ActionLocation,
    ModelAdmin,
    ShowFacets,
    StackedInline,
    TabularInline,
)
from django.contrib.admin.sites import AdminSite, site
from django.utils.module_loading import autodiscover_modules

__all__ = [
    "action",
    "Action",
    "ActionLocation",
    "display",
    "register",
    "ModelAdmin",
    "HORIZONTAL",
    "VERTICAL",
    "StackedInline",
    "TabularInline",
    "AdminSite",
    "site",
    "ListFilter",
    "SimpleListFilter",
    "FieldListFilter",
    "BooleanFieldListFilter",
    "RelatedFieldListFilter",
    "ChoicesFieldListFilter",
    "DateFieldListFilter",
    "AllValuesFieldListFilter",
    "EmptyFieldListFilter",
    "RelatedOnlyFieldListFilter",
    "ShowFacets",
    "autodiscover",
]


# 在各已安装应用中加载 admin.py 并注册 ModelAdmin
def autodiscover():
    autodiscover_modules("admin", register_to=site)
