"""
GeoDjango 实用工具包：图层映射、OGR 检查、SRS 注册等。

This module contains useful utilities for GeoDjango."""
This module contains useful utilities for GeoDjango.
"""

from django.contrib.gis.utils.layermapping import LayerMapError, LayerMapping
from django.contrib.gis.utils.ogrinfo import ogrinfo
from django.contrib.gis.utils.ogrinspect import mapping, ogrinspect
from django.contrib.gis.utils.srs import add_srs_entry

__all__ = [
    "add_srs_entry",
    "mapping",
    "ogrinfo",
    "ogrinspect",
    "LayerMapError",
    "LayerMapping",
]
