"""
django.contrib.gis.db.backends.spatialite.client — SpatiaLite 命令行客户端。
"""
from django.db.backends.sqlite3.client import DatabaseClient


# SpatiaLite 数据库客户端：使用 spatialite 可执行文件
class SpatiaLiteClient(DatabaseClient):
    executable_name = "spatialite"
