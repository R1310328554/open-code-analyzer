"""
django.contrib.gis.gdal.base — GDAL 对象基类。
"""
from django.contrib.gis.gdal.error import GDALException
from django.contrib.gis.ptr import CPointerBase


# GDAL ctypes 指针基类，空指针时抛出 GDALException
class GDALBase(CPointerBase):
    null_ptr_exception_class = GDALException
