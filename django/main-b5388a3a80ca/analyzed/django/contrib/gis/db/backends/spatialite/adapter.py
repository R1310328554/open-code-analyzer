"""
django.contrib.gis.db.backends.spatialite.adapter — SpatiaLite 几何适配器。

基于 WKT 适配器，实现 SQLite PrepareProtocol 协议。
"""
from django.contrib.gis.db.backends.base.adapter import WKTAdapter
from django.db.backends.sqlite3.base import Database


# SpatiaLite 几何适配器：以 WKT 字符串绑定 SQLite 参数
class SpatiaLiteAdapter(WKTAdapter):
    "SQLite adapter for geometry objects."

    # 实现 SQLite PrepareProtocol，返回 WKT 字符串
    def __conform__(self, protocol):
        if protocol is Database.PrepareProtocol:
            return str(self)
