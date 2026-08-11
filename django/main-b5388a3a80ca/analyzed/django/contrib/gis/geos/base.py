from django.contrib.gis.geos.error import GEOSException
from django.contrib.gis.ptr import CPointerBase


# GEOS 对象基类：空指针时抛出 GEOSException
class GEOSBase(CPointerBase):
    null_ptr_exception_class = GEOSException
