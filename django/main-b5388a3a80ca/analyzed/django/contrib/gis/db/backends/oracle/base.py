"""
django.contrib.gis.db.backends.oracle.base — Oracle GIS 数据库包装器。

扩展标准 Oracle DatabaseWrapper，注入 GIS 特性、内省、操作与 schema 编辑器。
"""
from django.db.backends.oracle.base import DatabaseWrapper as OracleDatabaseWrapper

from .features import DatabaseFeatures
from .introspection import OracleIntrospection
from .operations import OracleOperations
from .schema import OracleGISSchemaEditor


# GIS 数据库连接包装器：替换 features/introspection/ops/schema 类
class DatabaseWrapper(OracleDatabaseWrapper):
    SchemaEditorClass = OracleGISSchemaEditor
    # Classes instantiated in __init__().
    features_class = DatabaseFeatures
    introspection_class = OracleIntrospection
    ops_class = OracleOperations
