"""
django.contrib.admindocs.urls — admindocs 文档站点 URL 配置。

索引页、模板标签/过滤器、视图与模型文档及模板源码查看路由。
"""
from django.contrib.admindocs import views
from django.urls import path, re_path

# admindocs 各文档子页面的 path/re_path 列表
urlpatterns = [
    path(
        "",
        views.BaseAdminDocsView.as_view(template_name="admin_doc/index.html"),
        name="django-admindocs-docroot",
    ),
    path(
        "bookmarklets/",
        views.BookmarkletsView.as_view(),
        name="django-admindocs-bookmarklets",
    ),
    path(
        "tags/",
        views.TemplateTagIndexView.as_view(),
        name="django-admindocs-tags",
    ),
    path(
        "filters/",
        views.TemplateFilterIndexView.as_view(),
        name="django-admindocs-filters",
    ),
    path(
        "views/",
        views.ViewIndexView.as_view(),
        name="django-admindocs-views-index",
    ),
    path(
        "views/<view>/",
        views.ViewDetailView.as_view(),
        name="django-admindocs-views-detail",
    ),
    path(
        "models/",
        views.ModelIndexView.as_view(),
        name="django-admindocs-models-index",
    ),
    re_path(
        r"^models/(?P<app_label>[^.]+)\.(?P<model_name>[^/]+)/$",
        views.ModelDetailView.as_view(),
        name="django-admindocs-models-detail",
    ),
    path(
        "templates/<path:template>/",
        views.TemplateDetailView.as_view(),
        name="django-admindocs-templates",
    ),
]
