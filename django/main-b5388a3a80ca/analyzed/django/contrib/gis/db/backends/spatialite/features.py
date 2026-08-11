"""
SpatiaLite 数据库特性：继承空间与 SQLite 特性，声明几何字段与测试跳过规则。
"""
from django.contrib.gis.db.backends.base.features import BaseSpatialFeatures
from django.db.backends.sqlite3.features import (
    DatabaseFeatures as SQLiteDatabaseFeatures,
)
from django.utils.functional import cached_property


# SpatiaLite 特性：不支持 ALTER 几何列，支持 3D 存储
class DatabaseFeatures(BaseSpatialFeatures, SQLiteDatabaseFeatures):
    can_alter_geometry_field = False  # Not implemented
    supports_3d_storage = True

    @cached_property
    # 是否支持测地面积（取决于 geom_lib 版本）
    def supports_area_geodetic(self):
        return bool(self.connection.ops.geom_lib_version())

    @cached_property
    # 追加 SpatiaLite 不支持 Distance 对象距离查找的测试跳过项
    def django_test_skips(self):
        skips = super().django_test_skips
        skips.update(
            {
                "SpatiaLite doesn't support distance lookups with Distance objects.": {
                    "gis_tests.geogapp.tests.GeographyTest.test02_distance_lookup",
                },
            }
        )
        return skips
