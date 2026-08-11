"""
django.contrib.gis.db.backends.oracle.introspection — Oracle 几何内省。

查询 USER_SDO_GEOM_METADATA 获取几何列 SRID 与维度信息。
"""
import oracledb

from django.db.backends.oracle.introspection import DatabaseIntrospection
from django.utils.functional import cached_property


# Oracle 内省：DB_TYPE_OBJECT 映射为 GeometryField
class OracleIntrospection(DatabaseIntrospection):
    # Associating any OBJECTVAR instances with GeometryField. This won't work
    # right on Oracle objects that aren't MDSYS.SDO_GEOMETRY, but it is the
    # only object type supported within Django anyways.
    @cached_property
    # 将 Oracle OBJECT 类型关联到 GeometryField
    def data_types_reverse(self):
        return {
            **super().data_types_reverse,
            oracledb.DB_TYPE_OBJECT: "GeometryField",
        }

    # 从 USER_SDO_GEOM_METADATA 读取 DIMINFO 与 SRID
    def get_geometry_type(self, table_name, description):
        with self.connection.cursor() as cursor:
            # Querying USER_SDO_GEOM_METADATA to get the SRID and dimension
            # information.
            try:
                cursor.execute(
                    'SELECT "DIMINFO", "SRID" FROM "USER_SDO_GEOM_METADATA" '
                    'WHERE "TABLE_NAME"=%s AND "COLUMN_NAME"=%s',
                    (table_name.upper(), description.name.upper()),
                )
                row = cursor.fetchone()
            except Exception as exc:
                raise Exception(
                    "Could not find entry in USER_SDO_GEOM_METADATA "
                    'corresponding to "%s"."%s"' % (table_name, description.name)
                ) from exc

            # TODO: Research way to find a more specific geometry field type
            # for the column's contents.
            field_type = "GeometryField"

            # Getting the field parameters.
            field_params = {}
            dim, srid = row
            if srid != 4326:
                field_params["srid"] = srid
            # Size of object array (SDO_DIM_ARRAY) is number of dimensions.
            dim = dim.size()
            if dim != 2:
                field_params["dim"] = dim
        return field_type, field_params
