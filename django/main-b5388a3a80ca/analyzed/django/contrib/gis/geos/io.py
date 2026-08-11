"""
GEOS 几何 I/O 模块 — WKB/WKT 读写器的 Python 封装。

Module that holds classes for performing I/O operations"""
Module that holds classes for performing I/O operations on GEOS geometry
objects. Specifically, this has Python implementations of WKB/WKT
reader and writer classes.
"""

from django.contrib.gis.geos.geometry import GEOSGeometry
from django.contrib.gis.geos.prototypes.io import (
    WKBWriter,
    WKTWriter,
    _WKBReader,
    _WKTReader,
)

__all__ = ["WKBWriter", "WKTWriter", "WKBReader", "WKTReader"]


# Public classes for (WKB|WKT)Reader, which return GEOSGeometry
# WKB 读取器：将二进制缓冲解析为 GEOSGeometry
class WKBReader(_WKBReader):
    # 读取 WKB 并返回 GEOSGeometry 实例
    def read(self, wkb):
        "Return a GEOSGeometry for the given WKB buffer."
        return GEOSGeometry(super().read(wkb))


# WKT 读取器：将文本字符串解析为 GEOSGeometry
class WKTReader(_WKTReader):
    # 读取 WKT 并返回 GEOSGeometry 实例
    def read(self, wkt):
        "Return a GEOSGeometry for the given WKT string."
        return GEOSGeometry(super().read(wkt))
