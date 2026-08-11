# 扩展 inspectdb：将通用 GeometryField 细化为具体空间字段类型
from django.core.management.commands.inspectdb import Command as InspectDBCommandfrom django.core.management.commands.inspectdb import Command as InspectDBCommand


# GIS 版 inspectdb：调用空间后端 introspection 获取精确几何类型
class Command(InspectDBCommand):
    db_module = "django.contrib.gis.db"

    # 若为 GeometryField 则通过 get_geometry_type 细化字段类型
    def get_field_type(self, connection, table_name, row):
        field_type, field_params, field_notes = super().get_field_type(
            connection, table_name, row
        )
        if field_type == "GeometryField":
            # Getting a more specific field type and any additional parameters
            # from the `get_geometry_type` routine for the spatial backend.
            field_type, geo_params = connection.introspection.get_geometry_type(
                table_name, row
            )
            field_params.update(geo_params)
        return field_type, field_params, field_notes
