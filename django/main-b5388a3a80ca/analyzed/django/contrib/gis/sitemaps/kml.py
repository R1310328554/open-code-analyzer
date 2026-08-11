from django.apps import apps
from django.contrib.gis.db.models import GeometryField
from django.contrib.sitemaps import Sitemap
from django.db import models
from django.urls import reverse


# KML 格式站点地图：为含 GeometryField 的模型生成 URL
class KMLSitemap(Sitemap):
    """
    A minimal hook to produce KML sitemaps.
    """

    geo_format = "kml"

    # 未指定 locations 时自动扫描已安装应用中的几何字段
    def __init__(self, locations=None):
        # If no locations specified, then we try to build for
        # every model in installed applications.
        self.locations = self._build_kml_sources(locations)

    # 收集 (app_label, model_name, field_name) 三元组列表
    def _build_kml_sources(self, sources):
        """
        Go through the given sources and return a 3-tuple of the application
        label, module name, and field name of every GeometryField encountered
        in the sources.

        If no sources are provided, then all models.
        """
        kml_sources = []
        if sources is None:
            sources = apps.get_models()
        for source in sources:
            if isinstance(source, models.base.ModelBase):
                for field in source._meta.fields:
                    if isinstance(field, GeometryField):
                        kml_sources.append(
                            (
                                source._meta.app_label,
                                source._meta.model_name,
                                field.name,
                            )
                        )
            elif isinstance(source, (list, tuple)):
                if len(source) != 3:
                    raise ValueError(
                        "Must specify a 3-tuple of (app_label, module_name, "
                        "field_name)."
                    )
                kml_sources.append(source)
            else:
                raise TypeError("KML Sources must be a model or a 3-tuple.")
        return kml_sources

    # 为每条 URL 附加 geo_format 属性
    def get_urls(self, page=1, site=None, protocol=None):
        """
        This method is overridden so the appropriate `geo_format` attribute
        is placed on each URL element.
        """
        urls = Sitemap.get_urls(self, page=page, site=site, protocol=protocol)
        for url in urls:
            url["geo_format"] = self.geo_format
        return urls

    # 返回 KML 数据源三元组列表
    def items(self):
        return self.locations

    # 反向解析 KML 视图 URL
    def location(self, obj):
        return reverse(
            "django.contrib.gis.sitemaps.views.%s" % self.geo_format,
            kwargs={
                "label": obj[0],
                "model": obj[1],
                "field_name": obj[2],
            },
        )


# KMZ（压缩 KML）格式站点地图
class KMZSitemap(KMLSitemap):
    geo_format = "kmz"
