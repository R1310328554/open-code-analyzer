"""
django.contrib.gis.db.backends.postgis.features — PostGIS 空间特性标志。

声明 PostGIS 对 geography、3D 存储/函数、栅格及空几何的支持情况。
"""
from django.contrib.gis.db.backends.base.features import BaseSpatialFeatures
from django.db.backends.postgresql.features import (
    DatabaseFeatures as PsycopgDatabaseFeatures,
)


# PostGIS 空间特性：全面支持 geography、3D、栅格与空几何
class DatabaseFeatures(BaseSpatialFeatures, PsycopgDatabaseFeatures):
    supports_geography = True
    supports_3d_storage = True
    supports_3d_functions = True
    supports_raster = True
    supports_empty_geometries = True
    empty_intersection_returns_none = False
