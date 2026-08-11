"""
GEOS 库 ctypes 初始化、错误/通知回调及 C 指针类型定义。

This module houses the ctypes initialization procedures"""
This module houses the ctypes initialization procedures, as well
as the notice and error handler function callbacks (get called
when an error occurs in GEOS).

This module also houses GEOS Pointer utilities, including
get_pointer_arr(), and GEOM_PTR.
"""

import logging
import os
from ctypes import CDLL, CFUNCTYPE, POINTER, Structure, c_char_p
from ctypes.util import find_library

from django.core.exceptions import ImproperlyConfigured
from django.utils.functional import SimpleLazyObject, cached_property
from django.utils.version import get_version_tuple

logger = logging.getLogger("django.contrib.gis")


# 加载 GEOS 共享库并配置 C API 函数原型
def load_geos():
    # Custom library path set?
    try:
        from django.conf import settings

        lib_path = settings.GEOS_LIBRARY_PATH
    except (AttributeError, ImportError, ImproperlyConfigured, OSError):
        lib_path = None

    # Setting the appropriate names for the GEOS-C library.
    if lib_path:
        lib_names = None
    elif os.name == "nt":
        # Windows NT libraries
        lib_names = ["geos_c", "libgeos_c-1"]
    elif os.name == "posix":
        # *NIX libraries
        lib_names = ["geos_c", "GEOS"]
    else:
        raise ImportError('Unsupported OS "%s"' % os.name)

    # Using the ctypes `find_library` utility to find the path to the GEOS
    # shared library. This is better than manually specifying each library name
    # and extension (e.g., libgeos_c.[so|so.1|dylib].).
    if lib_names:
        for lib_name in lib_names:
            lib_path = find_library(lib_name)
            if lib_path is not None:
                break

    # No GEOS library could be found.
    if lib_path is None:
        raise ImportError(
            'Could not find the GEOS library (tried "%s"). '
            "Try setting GEOS_LIBRARY_PATH in your settings." % '", "'.join(lib_names)
        )
    # Getting the GEOS C library. The C interface (CDLL) is used for
    # both *NIX and Windows.
    # See the GEOS C API source code for more details on the library function
    # calls: https://libgeos.org/doxygen/geos__c_8h_source.html
    _lgeos = CDLL(lib_path)
    # Here we set up the prototypes for the GEOS_init_r and GEOS_finish_r
    # routines, as well as the context handler setters.
    # These functions aren't actually called until they are
    # attached to a GEOS context handle -- this actually occurs in
    # geos/prototypes/threadsafe.py.
    _lgeos.GEOS_init_r.restype = CONTEXT_PTR
    _lgeos.GEOS_finish_r.argtypes = [CONTEXT_PTR]
    _lgeos.GEOSContext_setErrorHandler_r.argtypes = [CONTEXT_PTR, ERRORFUNC]
    _lgeos.GEOSContext_setNoticeHandler_r.argtypes = [CONTEXT_PTR, NOTICEFUNC]
    # Set restype for compatibility across 32 and 64-bit platforms.
    _lgeos.GEOSversion.restype = c_char_p
    return _lgeos


# The notice and error handler C function callback definitions.
# Supposed to mimic the GEOS message handler (C below):
#  typedef void (*GEOSMessageHandler)(const char *fmt, ...);
NOTICEFUNC = CFUNCTYPE(None, c_char_p, c_char_p)


# GEOS 通知消息回调，写入 django.contrib.gis 日志
def notice_h(fmt, lst):
    fmt, lst = fmt.decode(), lst.decode()
    try:
        warn_msg = fmt % lst
    except TypeError:
        warn_msg = fmt
    logger.warning("GEOS_NOTICE: %s\n", warn_msg)


notice_h = NOTICEFUNC(notice_h)

ERRORFUNC = CFUNCTYPE(None, c_char_p, c_char_p)


# GEOS 错误消息回调，写入 django.contrib.gis 日志
def error_h(fmt, lst):
    fmt, lst = fmt.decode(), lst.decode()
    try:
        err_msg = fmt % lst
    except TypeError:
        err_msg = fmt
    logger.error("GEOS_ERROR: %s\n", err_msg)


error_h = ERRORFUNC(error_h)

# #### GEOS Geometry C data structures, and utility functions. ####


# Opaque GEOS geometry structures, used for GEOM_PTR and CS_PTR
# 不透明 GEOS 几何 C 结构体
class GEOSGeom_t(Structure):
    pass


# 预计算几何 C 结构体
class GEOSPrepGeom_t(Structure):
    pass


# 坐标序列 C 结构体
class GEOSCoordSeq_t(Structure):
    pass


# GEOS 线程上下文句柄 C 结构体
class GEOSContextHandle_t(Structure):
    pass


# Pointers to opaque GEOS geometry structures.
GEOM_PTR = POINTER(GEOSGeom_t)
PREPGEOM_PTR = POINTER(GEOSPrepGeom_t)
CS_PTR = POINTER(GEOSCoordSeq_t)
CONTEXT_PTR = POINTER(GEOSContextHandle_t)


lgeos = SimpleLazyObject(load_geos)


# 延迟绑定 GEOS C 函数的工厂类
class GEOSFuncFactory:
    """
    Lazy loading of GEOS functions.
    """

    argtypes = None
    restype = None
    errcheck = None

    # 记录函数名与 ctypes 签名参数
    def __init__(self, func_name, *, restype=None, errcheck=None, argtypes=None):
        self.func_name = func_name
        if restype is not None:
            self.restype = restype
        if errcheck is not None:
            self.errcheck = errcheck
        if argtypes is not None:
            self.argtypes = argtypes

    # 调用已绑定的 GEOS C 函数
    def __call__(self, *args):
        return self.func(*args)

    @cached_property
    # 懒加载并缓存 GEOSFunc 实例
    def func(self):
        from django.contrib.gis.geos.prototypes.threadsafe import GEOSFunc

        func = GEOSFunc(self.func_name)
        func.argtypes = self.argtypes or []
        func.restype = self.restype
        if self.errcheck:
            func.errcheck = self.errcheck
        return func


# 返回 GEOS 库版本字符串
def geos_version():
    """Return the string version of the GEOS library."""
    return lgeos.GEOSversion()


# 返回 GEOS 版本 (major, minor, subminor) 元组
def geos_version_tuple():
    """Return the GEOS version as a tuple (major, minor, subminor)."""
    return get_version_tuple(geos_version().decode())
