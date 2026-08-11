from django.apps import AppConfig
from django.core import serializers
from django.utils.translation import gettext_lazy as _


# GIS 应用配置：注册 geojson 内置序列化器
class GISConfig(AppConfig):
    default_auto_field = "django.db.models.AutoField"
    name = "django.contrib.gis"
    verbose_name = _("GIS")

    # 启动时将 geojson 序列化器路径写入 BUILTIN_SERIALIZERS
    def ready(self):
        serializers.BUILTIN_SERIALIZERS.setdefault(
            "geojson", "django.contrib.gis.serializers.geojson"
        )
