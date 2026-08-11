"""
django.contrib.gis.db.backends.mysql.features — MySQL 空间特性标志。

声明 MySQL/MariaDB 对空间参考表、测地距离、Transform 等能力的支持情况。
"""
from django.contrib.gis.db.backends.base.features import BaseSpatialFeatures
from django.db.backends.mysql.features import DatabaseFeatures as MySQLDatabaseFeatures
from django.utils.functional import cached_property


# MySQL 空间特性：多数高级 GIS 功能不可用或依赖 MariaDB 版本
class DatabaseFeatures(BaseSpatialFeatures, MySQLDatabaseFeatures):
    empty_intersection_returns_none = False
    has_spatialrefsys_table = False
    supports_add_srs_entry = False
    supports_distance_geodetic = False
    supports_length_geodetic = False
    supports_area_geodetic = False
    supports_transform = False
    supports_null_geometries = False
    supports_num_points_poly = False
    unsupported_geojson_options = {"crs"}

    @cached_property
    # 仅 MariaDB 支持几何字段唯一索引（MySQL Worklog #11808）
    def supports_geometry_field_unique_index(self):
        # Not supported in MySQL since
        # https://dev.mysql.com/worklog/task/?id=11808
        return self.connection.mysql_is_mariadb

    @cached_property
    # MariaDB 不支持嵌套几何集合时跳过相关 GIS 测试
    def django_test_skips(self):
        skips = super().django_test_skips
        if self.connection.mysql_is_mariadb:
            skips.update(
                {
                    "MariaDB doesn't support nested geometry collections.": {
                        "gis_tests.geoapp.tests.SaveLoadTests."
                        "test_geometrycollectionfield_default_max_ignored_on_read",
                    },
                }
            )
        return skips
