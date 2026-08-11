"""
django.contrib.gis.db.backends.mysql.introspection — MySQL 几何内省。

通过 DESCRIBE 解析几何列 OGC 类型，并检测存储引擎是否支持空间索引。
"""
from MySQLdb.constants import FIELD_TYPE

from django.contrib.gis.gdal import OGRGeomType
from django.db.backends.mysql.introspection import DatabaseIntrospection


# MySQL 内省：FIELD_TYPE.GEOMETRY 映射为 GeometryField
class MySQLIntrospection(DatabaseIntrospection):
    # Updating the data_types_reverse dictionary with the appropriate
    # type for Geometry fields.
    data_types_reverse = DatabaseIntrospection.data_types_reverse.copy()
    data_types_reverse[FIELD_TYPE.GEOMETRY] = "GeometryField"

    # DESCRIBE 表结构，用 OGRGeomType 将 OGC 类型转为 Django 字段类型
    def get_geometry_type(self, table_name, description):
        with self.connection.cursor() as cursor:
            # In order to get the specific geometry type of the field,
            # we introspect on the table definition using `DESCRIBE`.
            cursor.execute("DESCRIBE %s" % self.connection.ops.quote_name(table_name))
            # Increment over description info until we get to the geometry
            # column.
            for column, typ, null, key, default, extra in cursor.fetchall():
                if column == description.name:
                    # Using OGRGeomType to convert from OGC name to Django
                    # field. MySQL does not support 3D or SRIDs, so the field
                    # params are empty.
                    field_type = OGRGeomType(typ).django
                    field_params = {}
                    break
        return field_type, field_params

    # MyISAM、Aria 或 InnoDB 存储引擎支持空间索引
    def supports_spatial_index(self, cursor, table_name):
        # Supported with MyISAM, Aria, or InnoDB.
        storage_engine = self.get_storage_engine(cursor, table_name)
        return storage_engine in ("MyISAM", "Aria", "InnoDB")
