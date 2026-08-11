"""
django.contrib.gis.db.backends.mysql.base — MySQL GIS 数据库包装器。

扩展标准 MySQL DatabaseWrapper，注入 GIS 特性、内省、操作与 schema 编辑器。
"""
from django.db.backends.mysql.base import DatabaseWrapper as MySQLDatabaseWrapper

from .features import DatabaseFeatures
from .introspection import MySQLIntrospection
from .operations import MySQLOperations
from .schema import MySQLGISSchemaEditor


# GIS 数据库连接包装器：替换 features/introspection/ops/schema 类
class DatabaseWrapper(MySQLDatabaseWrapper):
    SchemaEditorClass = MySQLGISSchemaEditor
    # Classes instantiated in __init__().
    features_class = DatabaseFeatures
    introspection_class = MySQLIntrospection
    ops_class = MySQLOperations
