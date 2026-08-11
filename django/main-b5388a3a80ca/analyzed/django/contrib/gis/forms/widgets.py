"""
django.contrib.gis.forms.widgets — 地图几何控件。

基于 OpenLayers 渲染可编辑几何，支持 WKT/GeoJSON 序列化与 SRID 转换。
"""
import logging

from django.contrib.gis import gdal
from django.contrib.gis.gdal import GDALException
from django.contrib.gis.geometry import json_regex
from django.contrib.gis.geos import GEOSException, GEOSGeometry
from django.contrib.gis.geos.prototypes.io import MAX_GEOM_COLLECTIONS
from django.forms.widgets import Widget

logger = logging.getLogger("django.contrib.gis")


# 几何地图控件基类：序列化/反序列化与模板上下文
class BaseGeometryWidget(Widget):
    """
    The base class for rich geometry widgets.
    Render a map using the WKT of the geometry.
    """

    base_layer = None
    geom_type = "GEOMETRY"
    map_srid = 4326
    display_raw = False
    max_geom_collections = MAX_GEOM_COLLECTIONS

    supports_3d = False
    template_name = ""  # set on subclasses

    # 注入默认经纬度与缩放属性
    def __init__(self, attrs=None):
        self.attrs = {
            key: getattr(self, key)
            for key in ("base_layer", "geom_type", "map_srid", "display_raw")
        }
        if attrs:
            self.attrs.update(attrs)

    # 将 GEOS 几何序列化为 WKT 字符串
    # 使用 GeoJSON 格式输出
    def serialize(self, value):
        return value.wkt if value else ""

    # 从字符串解析为 GEOSGeometry
    # 解析 GeoJSON 并按地图 SRID 修正坐标系
    def deserialize(self, value):
        try:
            return GEOSGeometry(value, max_geom_collections=self.max_geom_collections)
        except (GEOSException, GDALException, ValueError, TypeError) as err:
            logger.error("Error creating geometry from value '%s' (%s)", value, err)
        return None

    # 构建模板上下文，必要时将几何变换到地图 SRID
    def get_context(self, name, value, attrs):
        context = super().get_context(name, value, attrs)
        # If a string reaches here (via a validation error on another
        # field) then just reconstruct the Geometry.
        if value and isinstance(value, str):
            value = self.deserialize(value)

        if value:
            # Check that srid of value and map match
            if value.srid and value.srid != self.map_srid:
                try:
                    ogr = value.ogr
                    ogr.transform(self.map_srid)
                    value = ogr
                except gdal.GDALException as err:
                    logger.error(
                        "Error transforming geometry from srid '%s' to srid '%s' (%s)",
                        value.srid,
                        self.map_srid,
                        err,
                    )
        context["serialized"] = self.serialize(value)
        geom_type = gdal.OGRGeomType(self.attrs["geom_type"]).name
        context["widget"]["attrs"]["geom_name"] = (
            "Geometry" if geom_type == "Unknown" else geom_type
        )
        return context


# OpenLayers 地图控件：Web Mercator (3857) 与 GeoJSON 序列化
class OpenLayersWidget(BaseGeometryWidget):
    base_layer = "nasaWorldview"
    template_name = "gis/openlayers.html"
    map_srid = 3857

    class Media:
        css = {
            "all": (
                "https://cdn.jsdelivr.net/npm/ol@v10.9.0/ol.css",
                "gis/css/ol3.css",
            )
        }
        js = (
            "https://cdn.jsdelivr.net/npm/ol@v10.9.0/dist/ol.js",
            "gis/js/OLMapWidget.js",
        )

    def serialize(self, value):
        return value.json if value else ""

    def deserialize(self, value):
        geom = super().deserialize(value)
        # GeoJSON assumes WGS84 (4326). Use the map's SRID instead.
        if geom and json_regex.match(value) and self.map_srid != 4326:
            geom.srid = self.map_srid
        return geom


# OpenStreetMap 底图控件，带默认中心与缩放级别
class OSMWidget(OpenLayersWidget):
    """
    An OpenLayers/OpenStreetMap-based widget.
    """

    base_layer = "osm"
    default_lon = 5
    default_lat = 47
    default_zoom = 12

    def __init__(self, attrs=None):
        if attrs is None:
            attrs = {}
        attrs.setdefault("default_lon", self.default_lon)
        attrs.setdefault("default_lat", self.default_lat)
        attrs.setdefault("default_zoom", self.default_zoom)
        super().__init__(attrs=attrs)
