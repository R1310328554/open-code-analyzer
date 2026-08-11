# flatpages URL 配置：捕获任意路径并交给 flatpage 视图渲染
from django.contrib.flatpages import views
from django.urls import path

urlpatterns = [
    path("<path:url>", views.flatpage, name="django.contrib.flatpages.views.flatpage"),
]
