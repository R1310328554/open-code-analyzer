"""
django.contrib.gis.db.backends.oracle.features — Oracle 空间特性标志。

声明 Oracle Spatial 对测地周长、容差参数及约束中空间运算符的支持情况。
"""
from django.contrib.gis.db.backends.base.features import BaseSpatialFeatures
from django.db.backends.oracle.features import (
    DatabaseFeatures as OracleDatabaseFeatures,
)
from django.utils.functional import cached_property


# Oracle 空间特性：支持测地周长与容差参数，不支持约束内空间运算符
class DatabaseFeatures(BaseSpatialFeatures, OracleDatabaseFeatures):
    supports_add_srs_entry = False
    supports_geometry_field_introspection = False
    supports_geometry_field_unique_index = False
    supports_perimeter_geodetic = True
    supports_dwithin_distance_expr = False
    supports_tolerance_parameter = True
    unsupported_geojson_options = {"bbox", "crs", "precision"}

    @cached_property
    # 跳过 Oracle 不支持的空间约束与嵌套几何集合相关测试
    def django_test_skips(self):
        skips = super().django_test_skips
        skips.update(
            {
                "Oracle doesn't support spatial operators in constraints.": {
                    "gis_tests.gis_migrations.test_operations.OperationTests."
                    "test_add_check_constraint",
                },
                "Oracle doesn't support nested geometry collections.": {
                    "gis_tests.geoapp.tests.SaveLoadTests."
                    "test_geometrycollectionfield_default_max_ignored_on_read",
                },
            }
        )
        return skips
